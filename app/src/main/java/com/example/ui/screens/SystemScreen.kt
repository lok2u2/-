package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun SystemScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allMeds by viewModel.allMedications.collectAsState()
    val logs by viewModel.medicationLogs.collectAsState()

    var isChangePasswordDialogOpen by remember { mutableStateOf(false) }
    var changePasswordNewText by remember { mutableStateOf("") }
    var changePasswordErrorMsg by remember { mutableStateOf<String?>(null) }
    var changePasswordSuccessMsg by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var backupJsonString by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isImportConfirmDialogOpen by remember { mutableStateOf(false) }
    var pendingImportText by remember { mutableStateOf("") }

    var isPasteImportDialogOpen by remember { mutableStateOf(false) }
    var clipboardInputText by remember { mutableStateOf("") }

    // Storage Access Framework file launchers
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val json = backupJsonString
            if (json != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                            writer.write(json)
                        }
                    }
                    successMessage = "数据已成功导出为本地 JSON 备份文件！"
                    errorMessage = null
                } catch (e: Exception) {
                    errorMessage = "写入系统文件失败: ${e.localizedMessage}"
                    successMessage = null
                }
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                        val stringBuilder = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line).append("\n")
                        }
                        pendingImportText = stringBuilder.toString()
                        isImportConfirmDialogOpen = true
                    }
                }
            } catch (e: Exception) {
                errorMessage = "读取备份文件发生错误: ${e.localizedMessage}"
                successMessage = null
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App header
        Surface(
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "User Avatar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.fullName ?: "普通家庭成员",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "家庭角色账户: @${currentUser?.username ?: "admin"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Log out action button
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.testTag("logout_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Configuration items list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 数据统计概况",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DataStatItem(
                            modifier = Modifier.weight(1f),
                            label = "药品库存规格",
                            value = "${allMeds.size} 种"
                        )
                        DataStatItem(
                            modifier = Modifier.weight(1f),
                            label = "履历日志数",
                            value = "${logs.size} 条"
                        )
                    }
                }
            }

            // General Settings Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚙️ 安全与设置中心",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Change password action row
                    SettingActionRow(
                        title = "修改本人账户密码",
                        subtitle = "设置自定义密码保护，拒绝硬编码",
                        icon = Icons.Filled.LockOpen,
                        onClick = {
                            changePasswordNewText = ""
                            changePasswordErrorMsg = null
                            changePasswordSuccessMsg = null
                            isChangePasswordDialogOpen = true
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            .padding(vertical = 4.dp)
                    )

                    // 2. Clear caching warning information callback
                    SettingActionRow(
                        title = "关于家庭药箱系统",
                        subtitle = "系统版本 v1.0.2 · Android 本地高性能安全架构",
                        icon = Icons.Filled.Info,
                        onClick = { /* informative */ }
                    )
                }
            }

            // --- Success Alert ---
            AnimatedVisibility(visible = successMessage != null) {
                successMessage?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF2E8B57),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { successMessage = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("清空", fontSize = 12.sp, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- Error Alert ---
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { errorMessage = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("清空", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Backup and Import Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💾 数据备份与跨机迁移",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "支持本地备份与快速文本分享，以便您同步更换手机继续使用，保障全家用药及安全日志历史永不丢失。",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Export standard file launcher row
                    SettingActionRow(
                        title = "导出备份数据至本地文件",
                        subtitle = "保存为安全可移植的 .json 备份文档",
                        icon = Icons.Filled.FileUpload,
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.getBackupJson()
                                if (json != null) {
                                    backupJsonString = json
                                    exportFileLauncher.launch("family_meds_backup_${System.currentTimeMillis() / 1000}.json")
                                } else {
                                    errorMessage = "准备备份数据错误"
                                }
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            .padding(vertical = 4.dp)
                    )

                    // 2. Import standard file picker row
                    SettingActionRow(
                        title = "从备份文件恢复数据",
                        subtitle = "读取本地 .json 文件，覆盖置换还原所有历史",
                        icon = Icons.Filled.FileDownload,
                        onClick = {
                            try {
                                importFileLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            } catch (e: Exception) {
                                errorMessage = "无法启动系统文件选择器: ${e.localizedMessage}"
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            .padding(vertical = 4.dp)
                    )

                    // 3. Copy text to clipboard
                    SettingActionRow(
                        title = "复制全量数据代码",
                        subtitle = "一键复制全量加密文本，以便通过微信/QQ发送传输",
                        icon = Icons.Filled.ContentCopy,
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.getBackupJson()
                                if (json != null) {
                                    clipboardManager.setText(AnnotatedString(json))
                                    successMessage = "全量数据备份代码已成功复制到剪贴板！可以直接发送给新手机系统复原。"
                                    errorMessage = null
                                } else {
                                    errorMessage = "生成备份文本失败"
                                }
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            .padding(vertical = 4.dp)
                    )

                    // 4. Paste text and restore
                    SettingActionRow(
                        title = "粘帖代码还原系统数据",
                        subtitle = "直接粘帖其它设备分享的代码，瞬间恢复药箱",
                        icon = Icons.Filled.ContentPaste,
                        onClick = {
                            clipboardInputText = ""
                            isPasteImportDialogOpen = true
                        }
                    )
                }
            }

            // Suggestion details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Tips",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "💡 建议与家庭共建：",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. 为确保用药安全，建议每位家庭成员使用各自的名字（如“爸爸”、“妈妈”、“姐姐”）分别注册并持有子账号。\n" +
                               "2. 记录吃药或处理过期药品时，操作记录会被永久铭刻保留在【服药与处理日志】中，不可人为篡改，确保家庭用药历史真实性。\n" +
                               "3. 保质期预警会默认在保质期临近90天时亮起黄色注意，超过保质期亮起红灯并启动拦截逻辑。药品库按期优先重排，守护家中儿童与老人的身心安泰。",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Change password dialog
        if (isChangePasswordDialogOpen) {
            AlertDialog(
                onDismissRequest = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isChangePasswordDialogOpen = false
                },
                title = { Text("修改个人登录密码", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "当前操作人: ${currentUser?.fullName}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = changePasswordNewText,
                            onValueChange = { changePasswordNewText = it },
                            label = { Text("输入新登录密码 (最少4位)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("new_password_input")
                        )

                        AnimatedVisibility(visible = changePasswordErrorMsg != null) {
                            changePasswordErrorMsg?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = changePasswordSuccessMsg != null) {
                            changePasswordSuccessMsg?.let {
                                Text(
                                    text = it,
                                    color = Color(0xFF2E8B57), // green
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.changePassword(
                                changePasswordNewText,
                                onSuccess = {
                                    changePasswordErrorMsg = null
                                    changePasswordSuccessMsg = "密码更新成功！"
                                    // auto-close soon
                                },
                                onError = { error ->
                                    changePasswordSuccessMsg = null
                                    changePasswordErrorMsg = error
                                }
                            )
                        }
                    ) {
                        Text("点击确定修改")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        isChangePasswordDialogOpen = false
                    }) {
                        Text("关闭")
                    }
                }
            )
        }

        // --- 1. Confirm File Import Disk Overwrite Dialog ---
        if (isImportConfirmDialogOpen) {
            AlertDialog(
                onDismissRequest = { isImportConfirmDialogOpen = false },
                title = { Text("⚠️ 恢复数据安全风险提示", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Text(
                        text = "您正在请求还原导入家庭药箱历史数据。\n\n" +
                               "注意：该操作属于高风险更改！它将完全清除本机当前的：\n" +
                               "1. 所有注册的家庭人员账号；\n" +
                               "2. 所有常备及临期药品存储数据；\n" +
                               "3. 所有相关的服药、清理、库存修正日志记录。\n\n" +
                               "此行为覆盖后将无法撤销，您确认继续进行替换吗？",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            isImportConfirmDialogOpen = false
                            viewModel.restoreBackupJson(
                                pendingImportText,
                                onSuccess = {
                                    successMessage = "家庭药箱数据成功复原！请使用备份中的账号登录系统。"
                                    errorMessage = null
                                },
                                onError = { error ->
                                    errorMessage = error
                                    successMessage = null
                                }
                            )
                        }
                    ) {
                        Text("确 认 置 换", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isImportConfirmDialogOpen = false }) {
                        Text("取 消")
                    }
                }
            )
        }

        // --- 2. Paste Text Backup Dialog ---
        if (isPasteImportDialogOpen) {
            var pasteErrorMsg by remember { mutableStateOf<String?>(null) }
            
            AlertDialog(
                onDismissRequest = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isPasteImportDialogOpen = false
                },
                title = { Text("粘帖文本还原备份", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "请在下方长按粘帖另一台手机导出的备份 JSON 格式文本。确认后本机数据将全量置换覆盖。",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = clipboardInputText,
                            onValueChange = { 
                                clipboardInputText = it
                                pasteErrorMsg = null
                            },
                            label = { Text("在此输入或粘帖备份数据文本") },
                            placeholder = { Text("{\n  \"backup_version\": 1... \n}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("paste_backup_input"),
                            maxLines = 10
                        )

                        AnimatedVisibility(visible = pasteErrorMsg != null) {
                            pasteErrorMsg?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            if (clipboardInputText.trim().isBlank()) {
                                pasteErrorMsg = "粘帖内容不能为空！"
                                return@Button
                            }
                            isPasteImportDialogOpen = false
                            viewModel.restoreBackupJson(
                                clipboardInputText,
                                onSuccess = {
                                    successMessage = "备份文本复原成功！请使用还原后的任意关联账号登录系统。"
                                    errorMessage = null
                                },
                                onError = { error ->
                                    errorMessage = error
                                    successMessage = null
                                }
                            )
                        }
                    ) {
                        Text("确认还原并覆盖")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        isPasteImportDialogOpen = false
                    }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
fun DataStatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
