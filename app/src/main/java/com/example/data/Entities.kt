package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val username: String,
    val passwordHash: String, // simple hashed/clear password for local verification
    val fullName: String,
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val quantity: Double,
    val unit: String = "片", // e.g. 片, 粒, 毫升, 包, 支, 瓶
    val efficacy: String = "", // efficacy
    val dosage: String = "", // 服用剂量, e.g. "1片" / "5ml"
    val frequency: String = "", // e.g. "一天3次" / "每4小时一次"
    val timing: String = "", // e.g. "饭前吃" / "饭后30分钟"
    val form: String = "内服", // form: 内服, 外用, 液体, 冲剂, 颗粒, 胶囊, 药膏, 喷雾
    val targetGroup: String = "通用", // 成人, 儿童, 通用
    val expiryDate: Long, // timestamp of expiration date
    val batchNumber: String = "",
    val location: String = "", // e.g., "客厅抽屉"
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val medicationId: Long,
    val medicationName: String,
    val username: String, // who executed the log
    val actionType: String, // "入库" (Added), "用药" (Used), "过期处理" (Disposed), "库存修正" (Modified)
    val quantityChange: Double, // can be negative for use/dispose
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
