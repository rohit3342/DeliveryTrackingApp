package com.korbit.deliverytrackingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.korbit.deliverytrackingapp.data.local.entity.OutboxEntity

@Dao
interface OutboxDao {

    @Query("SELECT * FROM outbox WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getPendingEvents(status: String = OutboxEntity.OutboxStatus.PENDING): List<OutboxEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: OutboxEntity): Long

    @Update
    suspend fun update(event: OutboxEntity)

    @Query("UPDATE outbox SET status = :status, syncedAt = :syncedAt, failureReason = :failureReason WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, syncedAt: Long?, failureReason: String?)

    @Query("DELETE FROM outbox WHERE status = :status AND syncedAt IS NOT NULL")
    suspend fun deleteSynced(status: String = OutboxEntity.OutboxStatus.SYNCED)
}
