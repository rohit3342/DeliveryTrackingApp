package com.korbit.deliverytrackingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryEntity
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {

    @Query("SELECT * FROM deliveries ORDER BY lastUpdatedAt DESC")
    fun observeAllDeliveries(): Flow<List<DeliveryEntity>>

    @Transaction
    @Query("SELECT * FROM deliveries ORDER BY lastUpdatedAt DESC")
    fun observeAllDeliveriesWithTasks(): Flow<List<DeliveryWithTasks>>

    @Query("SELECT * FROM deliveries WHERE id = :deliveryId")
    fun observeDeliveryById(deliveryId: String): Flow<DeliveryEntity?>

    @Transaction
    @Query("SELECT * FROM deliveries WHERE id = :deliveryId")
    fun observeDeliveryWithTasks(deliveryId: String): Flow<DeliveryWithTasks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(deliveries: List<DeliveryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(delivery: DeliveryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DeliveryTaskEntity>)

    @Query("UPDATE deliveries SET status = :status, lastUpdatedAt = :updatedAt, syncedAt = :syncedAt WHERE id = :deliveryId")
    suspend fun updateDeliveryStatus(deliveryId: String, status: String, updatedAt: Long, syncedAt: Long?)

    @Query("UPDATE deliveries SET syncedAt = :syncedAt WHERE id = :deliveryId")
    suspend fun updateSyncedAt(deliveryId: String, syncedAt: Long)

    @Query("SELECT COUNT(*) FROM deliveries")
    suspend fun getDeliveryCount(): Int

    @Query("DELETE FROM deliveries")
    suspend fun deleteAll()

    data class DeliveryWithTasks(
        @Embedded val delivery: DeliveryEntity,
        @Relation(parentColumn = "id", entityColumn = "deliveryId", entity = DeliveryTaskEntity::class)
        val tasks: List<DeliveryTaskEntity>
    )
}
