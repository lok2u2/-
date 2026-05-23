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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextButton
import com.example.data.Medication
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.theme.StatusHealthy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allMeds by viewModel.allMedications.collectAsState()
    val expiredMeds by viewModel.expiredMedications.collectAsState()
    val expiringSoonMeds by viewModel.expiringSoonMedications.collectAsState()
    val homeQuery by viewModel.homeSearchQuery.collectAsState()
    val homeSearchResult by viewModel.homeSearchResult.collectAsState()
    val nowVal by viewModel.now.collectAsState()

    // Keep track of which statistic is "clicked" to view details right on Home
    // "ALL" (in-store), "EXPIRING" (expiring soon), "EXPIRED" (is expired), "NONE"
    var expandedStatSection by remember { mutableStateOf("NONE") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Welcome and Header Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(top = 16.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "你好，${currentUser?.fullName ?: "家庭成员"}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "守护一家的用药安全",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "家庭药箱",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Modern Search Field inside Header (Fuzzy Search)
                OutlinedTextField(
                    value = homeQuery,
                    onValueChange = { viewModel.setHomeSearchQuery(it) },
                    placeholder = {
                        Text(
                            "模糊搜索药品名称、功效 (如: 感冒)",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search icon",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    trailingIcon = {
                        if (homeQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setHomeSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.12f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_fuzzy_search"),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Main Scrolling Body
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fuzzy Search Results Panel (Visible only when search text is active)
            if (homeQuery.isNotEmpty()) {
                item {
                    Text(
                        text = "🔎 搜索结果 (${homeSearchResult.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (homeSearchResult.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "No drug match",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "没有找到匹配的药物，您可以换个名字或功效试试",
                                    fontSize = 13.sp,
                                    textAlign = java.nio.charset.StandardCharsets.UTF_8.hashCode().let { TextAlign.Center },
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    items(homeSearchResult) { med ->
                        MedicationHorizontalCard(
                            med = med,
                            currentTimestamp = nowVal,
                            onHeaderClick = {
                                viewModel.setScreen(Screen.MEDICINES)
                                viewModel.setMedsSearchQuery(med.name)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Quick Stats Dashboard Cards
            item {
                Text(
                    text = "📊 药箱概览",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // High-fidelity Stats Grid in single row/cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total in Stock Card
                    StatSquareCard(
                        modifier = Modifier.weight(1f),
                        title = "在库药品",
                        count = allMeds.count { it.quantity > 0 }.toString(),
                        icon = Icons.Filled.Inventory,
                        backgroundColor = StatusHealthy.copy(alpha = 0.12f),
                        iconTint = StatusHealthy,
                        isSelected = expandedStatSection == "ALL",
                        onClick = {
                            expandedStatSection = if (expandedStatSection == "ALL") "NONE" else "ALL"
                        }
                    )

                    // Soon to Expire Card (90 days)
                    StatSquareCard(
                        modifier = Modifier.weight(1f),
                        title = "即将过期",
                        count = expiringSoonMeds.size.toString(),
                        icon = Icons.Filled.Warning,
                        backgroundColor = StatusExpiringSoon.copy(alpha = 0.12f),
                        iconTint = StatusExpiringSoon,
                        isSelected = expandedStatSection == "EXPIRING",
                        onClick = {
                            expandedStatSection = if (expandedStatSection == "EXPIRING") "NONE" else "EXPIRING"
                        }
                    )

                    // Expired Card
                    StatSquareCard(
                        modifier = Modifier.weight(1f),
                        title = "已经过期",
                        count = expiredMeds.size.toString(),
                        icon = Icons.Filled.Dangerous,
                        backgroundColor = StatusExpired.copy(alpha = 0.12f),
                        iconTint = StatusExpired,
                        isSelected = expandedStatSection == "EXPIRED",
                        onClick = {
                            expandedStatSection = if (expandedStatSection == "EXPIRED") "NONE" else "EXPIRED"
                        }
                    )
                }
            }

            // "点进去即可查看" - Stat details container
            if (expandedStatSection != "NONE") {
                val segmentTitle = when (expandedStatSection) {
                    "ALL" -> "在库药品库里列表"
                    "EXPIRING" -> "即将过期药品 (90天内)"
                    "EXPIRED" -> "已过期药品 (请尽快处理)"
                    else -> ""
                }
                
                val targetList = when (expandedStatSection) {
                    "ALL" -> allMeds.filter { it.quantity > 0 }
                    "EXPIRING" -> expiringSoonMeds
                    "EXPIRED" -> expiredMeds
                    else -> emptyList()
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 $segmentTitle (${targetList.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "点击可前往详情编辑",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (targetList.isEmpty()) {
                            Text(
                                text = "暂无匹配药品记录",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            targetList.take(6).forEach { med ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setScreen(Screen.MEDICINES)
                                            viewModel.setMedsSearchQuery(med.name)
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = med.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "功效: ${med.efficacy.ifBlank { "未登记" }}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${med.quantity} ${med.unit}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = getExpiryStatusText(med.expiryDate, nowVal),
                                            fontSize = 10.sp,
                                            color = getExpiryColor(med.expiryDate, nowVal),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                )
                            }
                            if (targetList.size > 6) {
                                TextButton(
                                    onClick = { 
                                        viewModel.setScreen(Screen.MEDICINES)
                                        if (expandedStatSection == "EXPIRING" || expandedStatSection == "EXPIRED") {
                                            // clear query but can lead to meds page
                                            viewModel.setMedsSearchQuery("")
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("查看药箱全部 ${targetList.size} 个药品", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Medication Alert section: Show actual urgent warnings
            val urgentMedsCount = expiredMeds.size
            if (urgentMedsCount > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = StatusExpired.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationImportant,
                                contentDescription = "Alert",
                                tint = StatusExpired,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "家庭健康过期警告!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusExpired
                                )
                                Text(
                                    text = "您有 ${urgentMedsCount} 种药品已过期，过期药物可能失效或产生毒副作用。请点击底部“用药日志”或“药物管理”标记真实过期处理。",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Tips or Advice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocalHospital,
                                contentDescription = "medical guidance",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "👨‍⚕️ 药箱健康寄语",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. 服药遵循医嘱，不盲目加量或减量。\n" +
                                   "2. 定期清理家庭药箱：液体制剂开封后保质期会大幅缩短，儿童与成人用药切勿混淆。\n" +
                                   "3. 合理分类：冲剂和颗粒避免受潮解，外用软膏不宜与内服口服液贴标重合，安全有序。",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatSquareCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag("stat_${title}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) iconTint.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MedicationHorizontalCard(
    med: Medication,
    currentTimestamp: Long,
    onHeaderClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onHeaderClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Form Indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = getFormColor(med.form).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = med.form.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = getFormColor(med.form)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = med.name,
                        fontSize = 15.sp,
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

                Text(
                    text = "用法: 一天${med.frequency} · ${med.dosage} (${med.timing})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "功效: ${med.efficacy.ifBlank { "常规治疗" }}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${med.quantity} ${med.unit}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = getExpiryStatusText(med.expiryDate, currentTimestamp),
                    fontSize = 11.sp,
                    color = getExpiryColor(med.expiryDate, currentTimestamp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helpers
fun formatMillisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    return sdf.format(Date(millis))
}

fun getExpiryStatusText(expiryDate: Long, current: Long): String {
    val diff = expiryDate - current
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
    return if (days < 0) {
        "已过期 ${-days}天"
    } else if (days == 0) {
        "今天到期"
    } else {
        "剩 ${days}天"
    }
}

fun getExpiryColor(expiryDate: Long, current: Long): Color {
    val diff = expiryDate - current
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
    return if (days < 0) {
        StatusExpired
    } else if (days < 90) {
        StatusExpiringSoon
    } else {
        StatusHealthy
    }
}

fun getFormColor(form: String): Color {
    return when (form) {
        "液体" -> Color(0xFF33B5E5)
        "冲剂" -> Color(0xFFAA66CC)
        "颗粒" -> Color(0xFF99CC00)
        "胶囊" -> Color(0xFFFFBB33)
        "药膏" -> Color(0xFFFF4444)
        "外用" -> Color(0xFFCC0000)
        else -> Color(0xFF008080)
    }
}
