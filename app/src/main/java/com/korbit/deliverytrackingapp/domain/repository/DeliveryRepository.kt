package com.korbit.deliverytrackingapp.domain.repository

import androidx.paging.PagingData
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth: Room. UI and UseCases read/write only through this.
 * No direct network calls from UI.
 */
interface DeliveryRepository {

    fun observeAllDeliveries(): Flow<List<Delivery>>

    /** Paged task list for scale. statusFilter: ALL, ACTIVE, or DONE. */
    fun observeTasksPaged(pageSize: Int, statusFilter: String): Flow<PagingData<TaskWithDelivery>>

    fun observeDeliveryWithTasks(deliveryId: String): Flow<Delivery?>

    suspend fun updateTaskStatus(taskId: String, status: String, completedAt: Long?, updatedAt: Long = System.currentTimeMillis())

    suspend fun insertDeliveriesFromSync(deliveries: List<Delivery>)

    suspend fun updateDeliverySyncedAt(deliveryId: String, syncedAt: Long)

    /** Insert a new delivery with tasks (e.g. new pickup created offline). */
    suspend fun insertDeliveryWithTasks(delivery: Delivery)

    /** Get a single delivery with tasks by id (e.g. for syncing a created task to server). */
    suspend fun getDeliveryById(deliveryId: String): Delivery?

    suspend fun getDeliveryCount(): Int

    /** Task count by filter (ALL, ACTIVE, DONE) for list header. */
    suspend fun getTaskCount(statusFilter: String): Int
}
