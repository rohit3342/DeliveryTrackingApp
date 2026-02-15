package com.korbit.deliverytrackingapp.data.local.dao

import androidx.paging.PagingSource
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

    @Transaction
    @Query("SELECT * FROM deliveries WHERE id = :deliveryId")
    suspend fun getDeliveryWithTasksOnce(deliveryId: String): DeliveryWithTasks?

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

    /** Task count by filter for list header chips. statusFilter: ALL, ACTIVE, or DONE. */
    @Query("""
        SELECT COUNT(*) FROM delivery_tasks t
        WHERE (:statusFilter = 'ALL')
           OR (:statusFilter = 'ACTIVE' AND t.status IN ('PENDING','PICKED_UP','REACHED'))
           OR (:statusFilter = 'DONE' AND t.status IN ('DELIVERED','FAILED'))
    """)
    suspend fun getTaskCount(statusFilter: String): Int

    @Query("DELETE FROM deliveries")
    suspend fun deleteAll()

    /**
     * Task-level paged query for list screen. Filter: ALL, ACTIVE, or DONE.
     * Room adds LIMIT/OFFSET when loading pages.
     */
    @Query("""
        SELECT
            t.id AS t_id, t.deliveryId AS t_deliveryId, t.type AS t_type, t.status AS t_status,
            t.sequence AS t_sequence, t.completedAt AS t_completedAt, t.createdAt AS t_createdAt,
            t.updatedAt AS t_updatedAt, t.wasEverPicked AS t_wasEverPicked,
            d.id AS d_id, d.riderId AS d_riderId, d.status AS d_status, d.customerName AS d_customerName,
            d.customerAddress AS d_customerAddress, d.customerPhone AS d_customerPhone,
            d.warehouseName AS d_warehouseName, d.warehouseAddress AS d_warehouseAddress,
            d.lastUpdatedAt AS d_lastUpdatedAt, d.syncedAt AS d_syncedAt
        FROM delivery_tasks t
        INNER JOIN deliveries d ON t.deliveryId = d.id
        WHERE (:statusFilter = 'ALL')
           OR (:statusFilter = 'ACTIVE' AND t.status IN ('PENDING','PICKED_UP','REACHED'))
           OR (:statusFilter = 'DONE' AND t.status IN ('DELIVERED','FAILED'))
        ORDER BY t.createdAt DESC
    """)
    fun getTaskWithDeliveryPagingSource(statusFilter: String): PagingSource<Int, TaskWithDeliveryRow>

    data class TaskWithDeliveryRow(
        @Embedded(prefix = "t_") val task: DeliveryTaskEntity,
        @Embedded(prefix = "d_") val delivery: DeliveryEntity
    )

    data class DeliveryWithTasks(
        @Embedded val delivery: DeliveryEntity,
        @Relation(parentColumn = "id", entityColumn = "deliveryId", entity = DeliveryTaskEntity::class)
        val tasks: List<DeliveryTaskEntity>
    )
}
