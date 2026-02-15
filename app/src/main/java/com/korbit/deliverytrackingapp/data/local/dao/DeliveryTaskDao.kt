package com.korbit.deliverytrackingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.korbit.deliverytrackingapp.data.local.entity.DeliveryTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryTaskDao {

    @Query("SELECT * FROM delivery_tasks WHERE deliveryId = :deliveryId ORDER BY sequence ASC")
    fun observeTasksByDeliveryId(deliveryId: String): Flow<List<DeliveryTaskEntity>>

    @Query("SELECT * FROM delivery_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): DeliveryTaskEntity?

    @Transaction
    @Query("SELECT * FROM delivery_tasks WHERE deliveryId = :deliveryId ORDER BY sequence ASC")
    fun getTasksByDeliveryIdSync(deliveryId: String): List<DeliveryTaskEntity>
}
