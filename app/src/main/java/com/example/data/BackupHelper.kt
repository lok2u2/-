package com.example.data

import org.json.JSONArray
import org.json.JSONObject

object BackupHelper {
    
    fun serializeToJson(
        users: List<User>,
        medications: List<Medication>,
        logs: List<MedicationLog>
    ): String {
        val root = JSONObject()
        root.put("backup_version", 1)
        root.put("backup_time", System.currentTimeMillis())

        val usersArray = JSONArray()
        for (u in users) {
            val jo = JSONObject()
            jo.put("id", u.id)
            jo.put("username", u.username)
            jo.put("passwordHash", u.passwordHash)
            jo.put("fullName", u.fullName)
            jo.put("createdTime", u.createdTime)
            usersArray.put(jo)
        }
        root.put("users", usersArray)

        val medsArray = JSONArray()
        for (m in medications) {
            val jo = JSONObject()
            jo.put("id", m.id)
            jo.put("name", m.name)
            jo.put("quantity", m.quantity)
            jo.put("unit", m.unit)
            jo.put("efficacy", m.efficacy)
            jo.put("dosage", m.dosage)
            jo.put("frequency", m.frequency)
            jo.put("timing", m.timing)
            jo.put("form", m.form)
            jo.put("targetGroup", m.targetGroup)
            jo.put("expiryDate", m.expiryDate)
            jo.put("batchNumber", m.batchNumber)
            jo.put("location", m.location)
            jo.put("instructionsImage", m.instructionsImage)
            jo.put("createdTime", m.createdTime)
            medsArray.put(jo)
        }
        root.put("medications", medsArray)

        val logsArray = JSONArray()
        for (l in logs) {
            val jo = JSONObject()
            jo.put("id", l.id)
            jo.put("medicationId", l.medicationId)
            jo.put("medicationName", l.medicationName)
            jo.put("username", l.username)
            jo.put("actionType", l.actionType)
            jo.put("quantityChange", l.quantityChange)
            jo.put("unit", l.unit)
            jo.put("timestamp", l.timestamp)
            jo.put("note", l.note)
            logsArray.put(jo)
        }
        root.put("medication_logs", logsArray)

        return root.toString(2) // 2 space indented readability
    }

    fun deserializeFromJson(jsonStr: String): ParsedBackup {
        val root = JSONObject(jsonStr)
        
        val usersList = mutableListOf<User>()
        val usersArray = root.optJSONArray("users")
        if (usersArray != null) {
            for (i in 0 until usersArray.length()) {
                val jo = usersArray.getJSONObject(i)
                usersList.add(
                    User(
                        id = jo.optLong("id", 0L),
                        username = jo.getString("username"),
                        passwordHash = jo.getString("passwordHash"),
                        fullName = jo.getString("fullName"),
                        createdTime = jo.optLong("createdTime", System.currentTimeMillis())
                    )
                )
            }
        }

        val medsList = mutableListOf<Medication>()
        val medsArray = root.optJSONArray("medications")
        if (medsArray != null) {
            for (i in 0 until medsArray.length()) {
                val jo = medsArray.getJSONObject(i)
                medsList.add(
                    Medication(
                        id = jo.optLong("id", 0L),
                        name = jo.getString("name"),
                        quantity = jo.getDouble("quantity"),
                        unit = jo.optString("unit", "片"),
                        efficacy = jo.optString("efficacy", ""),
                        dosage = jo.optString("dosage", ""),
                        frequency = jo.optString("frequency", ""),
                        timing = jo.optString("timing", ""),
                        form = jo.optString("form", "内服"),
                        targetGroup = jo.optString("targetGroup", "通用"),
                        expiryDate = jo.getLong("expiryDate"),
                        batchNumber = jo.optString("batchNumber", ""),
                        location = jo.optString("location", ""),
                        instructionsImage = if (jo.has("instructionsImage") && !jo.isNull("instructionsImage")) jo.getString("instructionsImage") else null,
                        createdTime = jo.optLong("createdTime", System.currentTimeMillis())
                    )
                )
            }
        }

        val logsList = mutableListOf<MedicationLog>()
        val logsArray = root.optJSONArray("medication_logs") ?: root.optJSONArray("logs")
        if (logsArray != null) {
            for (i in 0 until logsArray.length()) {
                val jo = logsArray.getJSONObject(i)
                logsList.add(
                    MedicationLog(
                        id = jo.optLong("id", 0L),
                        medicationId = jo.optLong("medicationId", 0L),
                        medicationName = jo.getString("medicationName"),
                        username = jo.getString("username"),
                        actionType = jo.getString("actionType"),
                        quantityChange = jo.getDouble("quantityChange"),
                        unit = jo.getString("unit"),
                        timestamp = jo.optLong("timestamp", System.currentTimeMillis()),
                        note = jo.optString("note", "")
                    )
                )
            }
        }

        return ParsedBackup(usersList, medsList, logsList)
    }
}

data class ParsedBackup(
    val users: List<User>,
    val medications: List<Medication>,
    val logs: List<MedicationLog>
)
