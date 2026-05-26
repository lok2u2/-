package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.example.data.Medication
import com.example.ui.MainViewModel
import java.util.Calendar
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.theme.StatusHealthy
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ZoomIn
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.File
import android.widget.Toast
import android.content.ActivityNotFoundException
import com.example.ui.ImageUtils
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@Composable
fun MedicinesScreen(viewModel: MainViewModel) {
    val meds by viewModel.filteredMedications.collectAsState()
    val searchQuery by viewModel.medsSearchQuery.collectAsState()
    
    val catFilter by viewModel.filterCategory.collectAsState()
    val ageFilter by viewModel.filterAgeGroup.collectAsState()
    
    val nowVal by viewModel.now.collectAsState()

    // Dialog state controllers
    var isAddDialogVisible by remember { mutableStateOf(false) }
    var detailMedicationToShow by remember { mutableStateOf<Medication?>(null) }
    var medicationToEdit by remember { mutableStateOf<Medication?>(null) }
    var medicationToDelete by remember { mutableStateOf<Medication?>(null) }
    
    // Quick action states (consumption/disposal)
    var activeActionType by remember { mutableStateOf("") } // "USE", "DISPOSE", "NONE"
    var actionQtyInput by remember { mutableStateOf("") }
    var actionNoteInput by remember { mutableStateOf("") }
    var actionFeedbackMsg by remember { mutableStateOf<String?>(null) }

    val categories = listOf("全部", "胶囊", "冲剂", "颗粒", "液体", "药膏", "外用", "其他")
    val ageGroups = listOf("全部", "成人", "儿童", "通用")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header Search & Filters Combo
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "💊 药物管理",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Tab Search Textfield
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setMedsSearchQuery(it) },
                        placeholder = { Text("搜索药品名称、功效或性状...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search icon") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setMedsSearchQuery("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_search_bar"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Filter Chips: Drug Form
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "剂型：",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(end = 12.dp)
                        ) {
                            items(categories) { cat ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (cat == catFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { viewModel.setFilterCategory(cat) }
                                        .padding(horizontal = 11.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (cat == catFilter) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Filter Chips: Age Group
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MedicalServices,
                            contentDescription = "Target age",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "群体：",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(ageGroups) { age ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (age == ageFilter) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { viewModel.setFilterAgeGroup(age) }
                                        .padding(horizontal = 11.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = age,
                                        color = if (age == ageFilter) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real Interactive Medicine Stock List
            if (meds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Healing,
                                contentDescription = "Empty stock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "未找到符合过滤条件的药品",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "您可以点击底部的 + 按钮登记家庭新药物入库",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(meds) { med ->
                        MedicationListCard(
                            med = med,
                            currentTimestamp = nowVal,
                            onCardClick = {
                                activeActionType = "NONE" // reset action
                                detailMedicationToShow = med
                            },
                            onFastUseClick = {
                                activeActionType = "USE"
                                actionQtyInput = "1"
                                actionNoteInput = ""
                                actionFeedbackMsg = null
                                detailMedicationToShow = med
                            }
                        )
                    }
                }
            }
        }

        // Floating button to Add Medicine
        FloatingActionButton(
            onClick = { isAddDialogVisible = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp)
                .testTag("add_medication_fab"),
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "新增药品登记")
        }

        // 1. ADD MEDICATION DIALOG
        if (isAddDialogVisible) {
            MedicationFormDialog(
                title = "录入家庭新药",
                onDismiss = { isAddDialogVisible = false },
                onSubmit = { name, qty, un, eff, dos, freq, tim, form, age, exp, bNum, loc, img ->
                    viewModel.addMedication(name, qty, un, eff, dos, freq, tim, form, age, exp, bNum, loc, img)
                    isAddDialogVisible = false
                }
            )
        }

        // 2. DETAILED MEDICATION VIEW & TRANSACTION LOG OPERATIONS
        detailMedicationToShow?.let { med ->
            // Re-fetch current state of drug in case modified
            val currentMed = meds.find { it.id == med.id } ?: med
            
            AlertDialog(
                onDismissRequest = { detailMedicationToShow = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("med_detail_dialog"),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = currentMed.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "存量: ${currentMed.quantity} ${currentMed.unit}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Edit & Delete button
                        Row {
                            IconButton(onClick = {
                                medicationToEdit = currentMed
                                detailMedicationToShow = null
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                medicationToDelete = currentMed
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Expiration banner
                        ExpiryWarningBanner(currentMed.expiryDate, nowVal)
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Details grid card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                DetailRow(label = "主要功效", value = currentMed.efficacy.ifBlank { "未说明" })
                                DetailRow(label = "每次剂量", value = currentMed.dosage.ifBlank { "按照医嘱" })
                                DetailRow(label = "服药频次", value = currentMed.frequency.ifBlank { "一天多次" })
                                DetailRow(label = "服用时间", value = currentMed.timing.ifBlank { "饭前后通用" })
                                DetailRow(label = "适宜群体", value = currentMed.targetGroup)
                                DetailRow(label = "剂型形式", value = currentMed.form)
                                DetailRow(label = "保质期至", value = formatMillisToDate(currentMed.expiryDate))
                                DetailRow(label = "生产批号", value = currentMed.batchNumber.ifBlank { "无批号" })
                                DetailRow(label = "存放位置", value = currentMed.location.ifBlank { "常温抽屉" })
                            }
                        }

                        // If there is an instruction photo, show it with a nice expandable feature or native view!
                        currentMed.instructionsImage?.let { base64 ->
                            val bitmap = remember(base64) { ImageUtils.base64ToBitmap(base64) }
                            if (bitmap != null) {
                                var isExpandedPhotoOpen by remember { mutableStateOf(false) }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("📄 服用说明书：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clickable { isExpandedPhotoOpen = true },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "说明书照片",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.2f))
                                        )
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.ZoomIn,
                                                contentDescription = "查看大图",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("点击查看大图", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }

                                if (isExpandedPhotoOpen) {
                                    AlertDialog(
                                        onDismissRequest = { isExpandedPhotoOpen = false },
                                        title = { Text("查看服用说明书大图", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(360.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    bitmap = bitmap.asImageBitmap(),
                                                    contentDescription = "说明书大图",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { isExpandedPhotoOpen = false }) {
                                                Text("关闭")
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // TRANSACTION ACTION: USE OR DISCARD (Authentic updates)
                        Text(
                            text = "✍️ 真实记录修改（服药或药物过期清理）",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    activeActionType = "USE"
                                    actionQtyInput = if(currentMed.dosage.contains(Regex("\\d+"))) {
                                        Regex("\\d+").find(currentMed.dosage)?.value ?: "1"
                                    } else "1"
                                    actionNoteInput = ""
                                    actionFeedbackMsg = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeActionType == "USE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    contentColor = if (activeActionType == "USE") Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("记录服用", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    activeActionType = "DISPOSE"
                                    actionQtyInput = currentMed.quantity.toString()
                                    actionNoteInput = "清理过期变质药品"
                                    actionFeedbackMsg = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeActionType == "DISPOSE") StatusExpired else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    contentColor = if (activeActionType == "DISPOSE") Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("过期报废", fontSize = 12.sp)
                            }
                        }

                        // Input fields for USE or DISPOSE
                        if (activeActionType != "NONE") {
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = actionQtyInput,
                                    onValueChange = { actionQtyInput = it },
                                    label = { Text("真实变动数量 (${currentMed.unit})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = actionNoteInput,
                                    onValueChange = { actionNoteInput = it },
                                    label = { Text("用药人或报废说明备注") },
                                    placeholder = { Text("如: 宝宝感冒吃1粒") },
                                    modifier = Modifier.weight(1.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            actionFeedbackMsg?.let { msg ->
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    val actQty = actionQtyInput.toDoubleOrNull()
                                    if (actQty == null || actQty <= 0.0) {
                                        actionFeedbackMsg = "请输入合法的有效正数"
                                        return@Button
                                    }
                                    if (activeActionType == "USE" && actQty > currentMed.quantity) {
                                        actionFeedbackMsg = "剩余库存不足，当前仅剩: ${currentMed.quantity} ${currentMed.unit}"
                                        return@Button
                                    }

                                    if (activeActionType == "USE") {
                                        viewModel.useMedication(currentMed.id, actQty, actionNoteInput) { ok ->
                                            if (ok) {
                                                activeActionType = "NONE"
                                                actionFeedbackMsg = null
                                            } else {
                                                actionFeedbackMsg = "用药记录失败"
                                            }
                                        }
                                    } else {
                                        viewModel.disposeMedication(currentMed.id, actQty, actionNoteInput) { ok ->
                                            if (ok) {
                                                activeActionType = "NONE"
                                                actionFeedbackMsg = null
                                            } else {
                                                actionFeedbackMsg = "报废处理失败"
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("确定真实递减库存并发射日志", fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { detailMedicationToShow = null }) {
                        Text("关闭")
                    }
                }
            )
        }

        // 3. EDIT MEDICATION DIALOG
        medicationToEdit?.let { currentMed ->
            MedicationFormDialog(
                title = "修改药品信息",
                medication = currentMed,
                onDismiss = { medicationToEdit = null },
                onSubmit = { name, qty, un, eff, dos, freq, tim, form, age, exp, bNum, loc, img ->
                    viewModel.updateMedication(
                        currentMed.id, name, qty, un, eff, dos, freq, tim, form, age, exp, bNum, loc, currentMed.quantity, img
                    )
                    medicationToEdit = null
                }
            )
        }

        // 4. CONFIRM DELETE DIALOG
        medicationToDelete?.let { med ->
            AlertDialog(
                onDismissRequest = { medicationToDelete = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "确认删除该药品吗？",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "正在删除药品：${med.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "注意：删除后该药品的【所有库存数据】、【绑定的说明书图片】以及相关的【服药日志历史记录】都将被永久清除，且该操作不可撤销！",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.deleteMedication(med.id)
                            medicationToDelete = null
                            detailMedicationToShow = null
                        }
                    ) {
                        Text("确定删除", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { medicationToDelete = null }) {
                        Text("考虑一下 (取消)")
                    }
                }
            )
        }
    }
}

@Composable
fun MedicationListCard(
    med: Medication,
    currentTimestamp: Long,
    onCardClick: () -> Unit,
    onFastUseClick: () -> Unit
) {
    val expiryStatusText = getExpiryStatusText(med.expiryDate, currentTimestamp)
    val expiryColor = getExpiryColor(med.expiryDate, currentTimestamp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("med_card_${med.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // First Row: Name and Expiration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = med.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (med.targetGroup == "儿童") Color(0xFFFFECEC) else Color(0xFFECEFFF),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = med.targetGroup,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (med.targetGroup == "儿童") Color(0xFFD32F2F) else Color(0xFF1976D2)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(expiryColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = expiryStatusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = expiryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Efficacy
            Text(
                text = "功效: ${med.efficacy.ifBlank { "日常疾病克星" }}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Dynamic specifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "用法：一天${med.frequency} · ${med.dosage} (${med.timing})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Stock size vs log trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = "location",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = med.location.ifBlank { "药箱常温区" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "存量: ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${med.quantity} ${med.unit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (med.quantity <= 0) StatusExpired else MaterialTheme.colorScheme.primary
                    )
                    
                    if (med.quantity > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onFastUseClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "快速吃药",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .background(StatusExpired.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "已空仓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusExpired
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpiryWarningBanner(expiryDate: Long, currentTimestamp: Long) {
    val diff = expiryDate - currentTimestamp
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()

    val bannerColor = when {
        days < 0 -> StatusExpired.copy(alpha = 0.12f)
        days == 0 -> StatusExpiringSoon.copy(alpha = 0.12f)
        days < 90 -> StatusExpiringSoon.copy(alpha = 0.12f)
        else -> StatusHealthy.copy(alpha = 0.12f)
    }

    val text = when {
        days < 0 -> "⚠️ 已过期 ${-days}天，不宜使用!"
        days == 0 -> "⚡ 今天已到保质期限，请慎重用药!"
        days < 90 -> "⚠️ 即将过期（剩 ${days}天），请妥善处理。"
        else -> "✅ 状态良好（剩 ${days}天），可安心储存使用。"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bannerColor, shape = RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (days < 0) StatusExpired else if (days < 90) StatusExpiringSoon else StatusHealthy,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label：",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

// 4. MULTIPURPOSE CLEAN FORM DIALOG FOR ADD / EDIT MEDICATION
@Composable
fun MedicationFormDialog(
    title: String,
    medication: Medication? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String, quantity: Double, unit: String, efficacy: String, dosage: String,
        frequency: String, timing: String, form: String, targetGroup: String, expiryDate: Long,
        batchNumber: String, location: String, instructionsImage: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var instructionsImageBase64 by remember { mutableStateOf(medication?.instructionsImage) }
    var isPhotoActionSheetVisible by remember { mutableStateOf(false) }

    fun createTempPictureUri(): Uri? {
        return try {
            val directory = File(context.cacheDir, "camera_photos")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File.createTempFile("temp_take_photo_", ".jpg", directory)
            val authority = "${context.packageName}.fileprovider"
            androidx.core.content.FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var isProcessingImage by remember { mutableStateOf(false) }

    // Camera launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                isProcessingImage = true
                coroutineScope.launch {
                    val base64 = withContext(Dispatchers.IO) {
                        ImageUtils.uriToBase64(context, uri)
                    }
                    if (base64 != null) {
                        instructionsImageBase64 = base64
                    }
                    isProcessingImage = false
                }
            }
        }
    }

    // Gallery launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingImage = true
            coroutineScope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    ImageUtils.uriToBase64(context, uri)
                }
                if (base64 != null) {
                    instructionsImageBase64 = base64
                }
                isProcessingImage = false
            }
        }
    }

    var name by remember { mutableStateOf(medication?.name ?: "") }
    var quantityStr by remember { mutableStateOf(medication?.quantity?.toString() ?: "10") }
    var unit by remember { mutableStateOf(medication?.unit ?: "片") }
    var efficacy by remember { mutableStateOf(medication?.efficacy ?: "") }
    var dosage by remember { mutableStateOf(medication?.dosage ?: "每次1片") }
    var frequency by remember { mutableStateOf(medication?.frequency ?: "3次") }
    var timing by remember { mutableStateOf(medication?.timing ?: "饭后半小时") }
    
    var formSelected by remember { mutableStateOf(medication?.form ?: "胶囊") }
    var targetGroupSelected by remember { mutableStateOf(medication?.targetGroup ?: "通用") }
    
    var batchNumber by remember { mutableStateOf(medication?.batchNumber ?: "") }
    var location by remember { mutableStateOf(medication?.location ?: "客厅药箱") }

    // Dynamic Safe Year-Month-Day Selection preloaded:
    val initialDate = Calendar.getInstance()
    if (medication != null) {
        initialDate.timeInMillis = medication.expiryDate
    } else {
        // default 1 year after
        initialDate.add(Calendar.YEAR, 1)
    }

    var yearStr by remember { mutableStateOf(initialDate.get(Calendar.YEAR).toString()) }
    var monthStr by remember { mutableStateOf((initialDate.get(Calendar.MONTH) + 1).toString()) }
    var dayStr by remember { mutableStateOf(initialDate.get(Calendar.DAY_OF_MONTH).toString()) }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }

    val forms = listOf("胶囊", "冲剂", "颗粒", "液体", "药膏", "外用", "其他")
    val units = listOf("片", "粒", "包", "克", "毫升", "支", "瓶", "盒")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (feedbackMsg != null) {
                        Text(
                            text = feedbackMsg ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                                .padding(8.dp)
                        )
                    }

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("药品名称 (必填)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_name")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Quantity
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("存量数量") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("form_quantity")
                        )

                        // Unit selector text field
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("单位 (片/毫升..)") },
                            modifier = Modifier.weight(1f).testTag("form_unit")
                        )
                    }

                    // Efficacy/Function
                    OutlinedTextField(
                        value = efficacy,
                        onValueChange = { efficacy = it },
                        label = { Text("主要功效/治疗疾病 (如: 止咳、退烧缓头痛)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_efficacy")
                    )

                    // Usage Details
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dosage,
                            onValueChange = { dosage = it },
                            label = { Text("服药剂量 (如每次1片)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = frequency,
                            onValueChange = { frequency = it },
                            label = { Text("一天几次") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Timing
                    OutlinedTextField(
                        value = timing,
                        onValueChange = { timing = it },
                        label = { Text("服用时间安排") },
                        placeholder = { Text("如: 饭后半小时、空腹、睡前等") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Instruction Photo Section
                    Text("服用说明书图片 (拍照或相册)：", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    if (instructionsImageBase64 != null) {
                        val bitmap = remember(instructionsImageBase64) { ImageUtils.base64ToBitmap(instructionsImageBase64) }
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "说明书照片",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Remove button overlay top-right
                                IconButton(
                                    onClick = { instructionsImageBase64 = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        .size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "清除图片",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                // Replace button overlay bottom-right
                                Button(
                                    onClick = { isPhotoActionSheetVisible = true },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                                ) {
                                    Text("重新拍照/选取", fontSize = 10.sp)
                                }
                            }
                        } else {
                            instructionsImageBase64 = null
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPhotoActionSheetVisible = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCamera,
                                    contentDescription = "添加照片",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("添加说明书照片 (拍照/相册)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("可更换手机保存及恢复，字迹清晰优先", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                    }

                    if (isPhotoActionSheetVisible) {
                        AlertDialog(
                            onDismissRequest = { isPhotoActionSheetVisible = false },
                            title = { Text("选择照片途径", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                            text = { Text("您可以直接使用手机拍照说明书，或从本地相册中选用已有的说明书照片。", fontSize = 13.sp) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        isPhotoActionSheetVisible = false
                                        val uri = createTempPictureUri()
                                        if (uri != null) {
                                            tempPhotoUri = uri
                                            try {
                                                takePictureLauncher.launch(uri)
                                            } catch (e: ActivityNotFoundException) {
                                                Toast.makeText(context, "未找到相机应用，请通过相册选取图片！", Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "启动相机失败: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("手机拍照")
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        isPhotoActionSheetVisible = false
                                        try {
                                            pickImageLauncher.launch("image/*")
                                        } catch (e: ActivityNotFoundException) {
                                            Toast.makeText(context, "未找到图片浏览器或相册应用！", Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "启动相册失败: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("相册选取")
                                    }
                                }
                            }
                        )
                    }

                    // Drug Dosage Form Dropdown Selection (We make it visually selection or flow)
                    Text("分类剂型：", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(forms) { fm ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (formSelected == fm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { formSelected = fm }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = fm,
                                    fontSize = 11.sp,
                                    color = if (formSelected == fm) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Suitable Age Group Radio
                    Text("适宜人群：", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val ageOptions = listOf("通用", "成人", "儿童")
                        ageOptions.forEach { age ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = targetGroupSelected == age,
                                    onClick = { targetGroupSelected = age }
                                )
                                Text(text = age, fontSize = 12.sp)
                            }
                        }
                    }

                    // Expiration Date Inputs (Year / Month / Day) - 100% Native Crash-Free Date editing!
                    Text("有效期截至：", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = yearStr,
                            onValueChange = { yearStr = it },
                            label = { Text("年 (YYYY)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = monthStr,
                            onValueChange = { monthStr = it },
                            label = { Text("月 (MM)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.9f)
                        )
                        OutlinedTextField(
                            value = dayStr,
                            onValueChange = { dayStr = it },
                            label = { Text("日 (DD)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    // Storage location or batch no
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("存放抽屉/位置") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = batchNumber,
                            onValueChange = { batchNumber = it },
                            label = { Text("生产批号") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isProcessingImage) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
                            .clickable(enabled = true, onClick = {}),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "正在高效加工及压缩说明书图片...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        feedbackMsg = "请输入有效的药品名字"
                        return@Button
                    }
                    val qtyVal = quantityStr.toDoubleOrNull()
                    if (qtyVal == null || qtyVal < 0.0) {
                        feedbackMsg = "数量必须为合法的正数"
                        return@Button
                    }

                    // Parse Custom Date values securely
                    val yr = yearStr.toIntOrNull()
                    val mn = monthStr.toIntOrNull()
                    val dy = dayStr.toIntOrNull()

                    if (yr == null || mn == null || dy == null || mn < 1 || mn > 12 || dy < 1 || dy > 31) {
                        feedbackMsg = "必须输入合法的保质期年月日日期 (e.g. 2027 年 5 月 23 日)"
                        return@Button
                    }

                    // Calculate expiry date miliseconds
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, yr)
                    cal.set(Calendar.MONTH, mn - 1)
                    cal.set(Calendar.DAY_OF_MONTH, dy)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)

                    val finalExpiryDateTimestamp = cal.timeInMillis

                    focusManager.clearFocus()
                    keyboardController?.hide()

                    onSubmit(
                        name.trim(),
                        qtyVal,
                        unit.trim(),
                        efficacy.trim(),
                        dosage.trim(),
                        frequency.trim(),
                        timing.trim(),
                        formSelected,
                        targetGroupSelected,
                        finalExpiryDateTimestamp,
                        batchNumber.trim(),
                        location.trim(),
                        instructionsImageBase64
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("登记确认")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onDismiss()
            }) {
                Text("取消")
            }
        }
    )
}
