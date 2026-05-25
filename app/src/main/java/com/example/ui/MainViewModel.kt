package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Medication
import com.example.data.MedicationLog
import com.example.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    MEDICINES,
    LOGS,
    SYSTEM
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.userDao(), db.medicationDao(), db.medicationLogDao())

    // Currently logged-in User
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Screen navigation
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Fuzzy search fields
    private val _homeSearchQuery = MutableStateFlow("")
    val homeSearchQuery: StateFlow<String> = _homeSearchQuery.asStateFlow()

    private val _medsSearchQuery = MutableStateFlow("")
    val medsSearchQuery: StateFlow<String> = _medsSearchQuery.asStateFlow()

    // Filters for meds tab
    private val _filterCategory = MutableStateFlow("全部") // 全部, 内服, 外用, 液体, 冲剂, 胶囊, 药膏, 其他
    val filterCategory: StateFlow<String> = _filterCategory.asStateFlow()

    private val _filterAgeGroup = MutableStateFlow("全部") // 全部, 成人, 儿童, 通用
    val filterAgeGroup: StateFlow<String> = _filterAgeGroup.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultUser()
        }
    }

    // Login logic
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun login(username: String, passwordText: String): Boolean {
        if (username.isBlank() || passwordText.isBlank()) {
            _loginError.value = "用户名和密码不能为空"
            return false
        }
        viewModelScope.launch {
            val user = repository.getUserByUsername(username.trim())
            if (user != null && user.passwordHash == passwordText) {
                _currentUser.value = user
                _loginError.value = null
                // Go to home
                _currentScreen.value = Screen.HOME
            } else {
                _loginError.value = "用户名或密码错误"
            }
        }
        return true
    }

    fun register(username: String, passwordText: String, fullName: String): Boolean {
        if (username.isBlank() || passwordText.isBlank() || fullName.isBlank()) {
            _loginError.value = "所有字段都必须填写"
            return false
        }
        viewModelScope.launch {
            val existing = repository.getUserByUsername(username.trim())
            if (existing != null) {
                _loginError.value = "该用户名已被占用"
                return@launch
            }
            val newUser = User(
                username = username.trim(),
                passwordHash = passwordText,
                fullName = fullName.trim()
            )
            repository.registerUser(newUser)
            // Auto login after registration
            val registered = repository.getUserByUsername(newUser.username)
            if (registered != null) {
                _currentUser.value = registered
                _loginError.value = null
                _currentScreen.value = Screen.HOME
            }
        }
        return true
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
        _currentScreen.value = Screen.HOME
    }

    fun changePassword(newPasswordText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onError("当前未登录")
            return
        }
        if (newPasswordText.isBlank() || newPasswordText.length < 4) {
            onError("新密码长度不能少于4位")
            return
        }
        viewModelScope.launch {
            repository.updatePassword(user.id, newPasswordText)
            // update local current user copy
            _currentUser.value = user.copy(passwordHash = newPasswordText)
            onSuccess()
        }
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setHomeSearchQuery(query: String) {
        _homeSearchQuery.value = query
    }

    fun setMedsSearchQuery(query: String) {
        _medsSearchQuery.value = query
    }

    fun setFilterCategory(category: String) {
        _filterCategory.value = category
    }

    fun setFilterAgeGroup(age: String) {
        _filterAgeGroup.value = age
    }

    // Reactive streams
    val now = MutableStateFlow(System.currentTimeMillis())
    
    // We update 'now' when refresh triggered
    fun refreshTime() {
        now.value = System.currentTimeMillis()
    }

    // All medications reactive flow
    val allMedications: StateFlow<List<Medication>> = repository.getAllMedicationsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expired and Expiring soon reactive list
    // Expiring soon definition: expires within 90 days (90 * 24 * 3600 * 1000)
    private val limit90Days = 90L * 24 * 60 * 60 * 1000L
    
    val expiredMedications: StateFlow<List<Medication>> = now.flatMapLatest { current ->
        repository.getExpiredMedicationsFlow(current)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringSoonMedications: StateFlow<List<Medication>> = now.flatMapLatest { current ->
        repository.getExpiringSoonMedicationsFlow(current, current + limit90Days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtering & searching in Medication tab
    val filteredMedications: StateFlow<List<Medication>> = combine(
        allMedications, _medsSearchQuery, _filterCategory, _filterAgeGroup
    ) { meds, query, cat, age ->
        meds.filter { med ->
            val matchQuery = query.isBlank() || med.name.contains(query, ignoreCase = true) || med.efficacy.contains(query, ignoreCase = true)
            val matchCat = cat == "全部" || med.form == cat
            val matchAge = age == "全部" || med.targetGroup == age
            matchQuery && matchCat && matchAge
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Searching in Home Dashboard (fuzzy search over all medications)
    val homeSearchResult: StateFlow<List<Medication>> = combine(allMedications, _homeSearchQuery) { meds, query ->
        if (query.isBlank()) emptyList()
        else meds.filter { med ->
            med.name.contains(query, ignoreCase = true) || med.efficacy.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All database transaction logs
    val medicationLogs: StateFlow<List<MedicationLog>> = repository.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Core business actions
    fun addMedication(
        name: String,
        quantity: Double,
        unit: String,
        efficacy: String,
        dosage: String,
        frequency: String,
        timing: String,
        form: String,
        targetGroup: String,
        expiryDate: Long,
        batchNumber: String,
        location: String,
        instructionsImage: String? = null
    ) {
        val userName = _currentUser.value?.fullName ?: "匿名"
        viewModelScope.launch {
            val med = Medication(
                name = name,
                quantity = quantity,
                unit = unit,
                efficacy = efficacy,
                dosage = dosage,
                frequency = frequency,
                timing = timing,
                form = form,
                targetGroup = targetGroup,
                expiryDate = expiryDate,
                batchNumber = batchNumber,
                location = location,
                instructionsImage = instructionsImage
            )
            repository.addMedication(med, userName)
            refreshTime()
        }
    }

    fun updateMedication(
        id: Long,
        name: String,
        quantity: Double,
        unit: String,
        efficacy: String,
        dosage: String,
        frequency: String,
        timing: String,
        form: String,
        targetGroup: String,
        expiryDate: Long,
        batchNumber: String,
        location: String,
        previousQty: Double,
        instructionsImage: String? = null
    ) {
        val userName = _currentUser.value?.fullName ?: "匿名"
        viewModelScope.launch {
            val med = Medication(
                id = id,
                name = name,
                quantity = quantity,
                unit = unit,
                efficacy = efficacy,
                dosage = dosage,
                frequency = frequency,
                timing = timing,
                form = form,
                targetGroup = targetGroup,
                expiryDate = expiryDate,
                batchNumber = batchNumber,
                location = location,
                instructionsImage = instructionsImage
            )
            repository.updateMedicationDetails(med, userName, previousQty)
            refreshTime()
        }
    }

    fun useMedication(medId: Long, useQty: Double, note: String, onFinished: (Boolean) -> Unit) {
        val userName = _currentUser.value?.fullName ?: "未知"
        viewModelScope.launch {
            val success = repository.useMedication(medId, useQty, note, userName)
            onFinished(success)
            refreshTime()
        }
    }

    fun disposeMedication(medId: Long, disposeQty: Double, note: String, onFinished: (Boolean) -> Unit) {
        val userName = _currentUser.value?.fullName ?: "未知"
        viewModelScope.launch {
            val success = repository.disposeMedication(medId, disposeQty, note, userName)
            onFinished(success)
            refreshTime()
        }
    }

    fun deleteMedication(medId: Long) {
        val userName = _currentUser.value?.fullName ?: "未知"
        viewModelScope.launch {
            repository.deleteMedication(medId, userName)
            refreshTime()
        }
    }

    // --- Data Export & Import Operations ---
    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    fun clearBackupStatus() {
        _backupStatus.value = null
    }

    suspend fun getBackupJson(): String? {
        return try {
            repository.exportBackupData()
        } catch (e: Exception) {
            _backupStatus.value = "导出数据格式化发生错误: ${e.localizedMessage}"
            null
        }
    }

    fun restoreBackupJson(jsonString: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (jsonString.trim().isBlank()) {
                    onError("导入失败：备份文件为空")
                    return@launch
                }
                repository.importBackupData(jsonString)
                
                // Clear state, force logging back in as one of the restored target accounts for absolute safety
                _currentUser.value = null
                _currentScreen.value = Screen.HOME
                refreshTime()
                onSuccess()
            } catch (e: Exception) {
                onError("数据还原失败，无效的文件格式: ${e.localizedMessage}")
            }
        }
    }
}
