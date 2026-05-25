package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
class AppRepository(
    private val userDao: UserDao,
    private val medicationDao: MedicationDao,
    private val medicationLogDao: MedicationLogDao
) {
    // Check if empty, prepopulate admin user if yes
    suspend fun ensureDefaultUser() {
        val existing = userDao.getAllUsers().firstOrNull()
        if (existing.isNullOrEmpty()) {
            userDao.insertUser(
                User(
                    username = "admin",
                    passwordHash = "123456", // standard default
                    fullName = "管理员 (Admin)"
                )
            )
            // also add some sample medications so the interface looks beautiful at first launch
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            
            val sample1 = Medication(
                name = "布洛芬缓释胶囊",
                quantity = 24.0,
                unit = "粒",
                efficacy = "缓解发热、头痛、关节痛",
                dosage = "每次1粒",
                frequency = "一天2次",
                timing = "饭后口服",
                form = "胶囊",
                targetGroup = "成人",
                expiryDate = now + 45 * dayMs, // expiring soon (in 45 days)
                location = "客厅智能药箱A座",
                batchNumber = "BF20260401"
            )
            val id1 = medicationDao.insertMedication(sample1)
            medicationLogDao.insertLog(
                MedicationLog(
                    medicationId = id1,
                    medicationName = sample1.name,
                    username = "系统",
                    actionType = "入库",
                    quantityChange = sample1.quantity,
                    unit = sample1.unit,
                    note = "初始化演示药品"
                )
            )

            val sample2 = Medication(
                name = "小儿氨酚黄那敏颗粒",
                quantity = 10.0,
                unit = "包",
                efficacy = "儿童感冒、暖热、流鼻涕",
                dosage = "每次1包",
                frequency = "一天3次",
                timing = "温开水冲服",
                form = "冲剂",
                targetGroup = "儿童",
                expiryDate = now - 5 * dayMs, // already expired (5 days ago)
                location = "厨房药箱",
                batchNumber = "XE20250512"
            )
            val id2 = medicationDao.insertMedication(sample2)
            medicationLogDao.insertLog(
                MedicationLog(
                    medicationId = id2,
                    medicationName = sample2.name,
                    username = "系统",
                    actionType = "入库",
                    quantityChange = sample2.quantity,
                    unit = sample2.unit,
                    note = "初始化演示过期药品"
                )
            )

            val sample3 = Medication(
                name = "阿莫西林胶囊",
                quantity = 12.0,
                unit = "粒",
                efficacy = "抗菌消炎",
                dosage = "每次2粒",
                frequency = "一天3次",
                timing = "饭后口服",
                form = "胶囊",
                targetGroup = "成人",
                expiryDate = now + 365 * dayMs, // normal
                location = "客厅智能药箱A座",
                batchNumber = "AMX20260101"
            )
            val id3 = medicationDao.insertMedication(sample3)
            medicationLogDao.insertLog(
                MedicationLog(
                    medicationId = id3,
                    medicationName = sample3.name,
                    username = "系统",
                    actionType = "入库",
                    quantityChange = sample3.quantity,
                    unit = sample3.unit,
                    note = "初始化演示普通库存"
                )
            )
        }
    }

    // User section
    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)
    fun getAllUsersFlow(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun registerUser(user: User): Long = userDao.insertUser(user)
    suspend fun updatePassword(userId: Long, passwordHash: String) = userDao.updatePassword(userId, passwordHash)

    // Medicine section
    fun getAllMedicationsFlow(): Flow<List<Medication>> = medicationDao.getAllMedicationsFlow()
    suspend fun getAllMedications(): List<Medication> = medicationDao.getAllMedications()
    suspend fun getMedicationById(id: Long): Medication? = medicationDao.getMedicationById(id)
    
    fun getExpiredMedicationsFlow(now: Long): Flow<List<Medication>> = medicationDao.getExpiredMedicationsFlow(now)
    fun getExpiringSoonMedicationsFlow(now: Long, expiringSoonLimit: Long): Flow<List<Medication>> = 
        medicationDao.getExpiringSoonMedicationsFlow(now, expiringSoonLimit)
        
    fun searchMedications(query: String): Flow<List<Medication>> = medicationDao.searchMedicationsFlow(query)

    suspend fun addMedication(medication: Medication, executingUser: String): Long {
        val id = medicationDao.insertMedication(medication)
        medicationLogDao.insertLog(
            MedicationLog(
                medicationId = id,
                medicationName = medication.name,
                username = executingUser,
                actionType = "入库",
                quantityChange = medication.quantity,
                unit = medication.unit,
                note = "新药品入库登记"
            )
        )
        return id
    }

    suspend fun updateMedicationDetails(medication: Medication, executingUser: String, previousQty: Double) {
        medicationDao.updateMedication(medication)
        val diff = medication.quantity - previousQty
        if (diff != 0.0) {
            medicationLogDao.insertLog(
                MedicationLog(
                    medicationId = medication.id,
                    medicationName = medication.name,
                    username = executingUser,
                    actionType = "库存修正",
                    quantityChange = diff,
                    unit = medication.unit,
                    note = "手动修改系统登记信息"
                )
            )
        }
    }

    suspend fun useMedication(medicationId: Long, useQty: Double, note: String, executingUser: String): Boolean {
        val med = medicationDao.getMedicationById(medicationId) ?: return false
        if (med.quantity < useQty) return false // stock insufficient but we can allow taking exactly remaining
        
        val newQty = (med.quantity - useQty).coerceAtLeast(0.0)
        val updatedMed = med.copy(quantity = newQty)
        medicationDao.updateMedication(updatedMed)
        
        medicationLogDao.insertLog(
            MedicationLog(
                medicationId = medicationId,
                medicationName = med.name,
                username = executingUser,
                actionType = "用药",
                quantityChange = -useQty,
                unit = med.unit,
                note = note.ifBlank { "常规服用" }
            )
        )
        return true
    }

    suspend fun disposeMedication(medicationId: Long, disposeQty: Double, note: String, executingUser: String): Boolean {
        val med = medicationDao.getMedicationById(medicationId) ?: return false
        val newQty = (med.quantity - disposeQty).coerceAtLeast(0.0)
        val updatedMed = med.copy(quantity = newQty)
        medicationDao.updateMedication(updatedMed)
        
        medicationLogDao.insertLog(
            MedicationLog(
                medicationId = medicationId,
                medicationName = med.name,
                username = executingUser,
                actionType = "过期处理",
                quantityChange = -disposeQty,
                unit = med.unit,
                note = note.ifBlank { "清理过期药品" }
            )
        )
        return true
    }

    suspend fun deleteMedication(medicationId: Long, executingUser: String) {
        val med = medicationDao.getMedicationById(medicationId) ?: return
        medicationDao.deleteMedication(med)
        
        // Also log this destruction
        medicationLogDao.insertLog(
            MedicationLog(
                medicationId = medicationId,
                medicationName = med.name,
                username = executingUser,
                actionType = "删除药品",
                quantityChange = -med.quantity,
                unit = med.unit,
                note = "将整条药品记录从系统中移除"
            )
        )
        // clean up logs or make logs orphaned? Usually we keep logs but with medicationId as orphaned
    }

    // Logs
    fun getAllLogsFlow(): Flow<List<MedicationLog>> = medicationLogDao.getAllLogsFlow()

    // Backup & Restore
    suspend fun exportBackupData(): String {
        val users = userDao.getAllUsersDirect()
        val medications = medicationDao.getAllMedications()
        val logs = medicationLogDao.getAllLogsDirect()
        return BackupHelper.serializeToJson(users, medications, logs)
    }

    suspend fun importBackupData(jsonString: String) {
        val parsed = BackupHelper.deserializeFromJson(jsonString)
        
        userDao.deleteAllUsers()
        medicationDao.deleteAllMedications()
        medicationLogDao.deleteAllLogs()

        if (parsed.users.isNotEmpty()) {
            userDao.insertUsers(parsed.users)
        }
        if (parsed.medications.isNotEmpty()) {
            medicationDao.insertMedications(parsed.medications)
        }
        if (parsed.logs.isNotEmpty()) {
            medicationLogDao.insertLogs(parsed.logs)
        }
    }
}
