package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersDirect(): List<User>

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPasswordHash: String)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY expiryDate ASC")
    fun getAllMedicationsFlow(): Flow<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY expiryDate ASC")
    suspend fun getAllMedications(): List<Medication>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Long): Medication?

    @Query("SELECT * FROM medications WHERE expiryDate <= :currentTimestamp AND quantity > 0")
    fun getExpiredMedicationsFlow(currentTimestamp: Long): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE expiryDate > :currentTimestamp AND expiryDate <= :expiringSoonTimestamp AND quantity > 0")
    fun getExpiringSoonMedicationsFlow(currentTimestamp: Long, expiringSoonTimestamp: Long): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE name LIKE '%' || :query || '%' OR efficacy LIKE '%' || :query || '%' ORDER BY expiryDate ASC")
    fun searchMedicationsFlow(query: String): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Query("DELETE FROM medications")
    suspend fun deleteAllMedications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<Medication>)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)
}

@Dao
interface MedicationLogDao {
    @Query("SELECT * FROM medication_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_logs")
    suspend fun getAllLogsDirect(): List<MedicationLog>

    @Query("DELETE FROM medication_logs")
    suspend fun deleteAllLogs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<MedicationLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLog): Long

    @Query("DELETE FROM medication_logs WHERE medicationId = :medicationId")
    suspend fun deleteLogsForMedication(medicationId: Long)
}
