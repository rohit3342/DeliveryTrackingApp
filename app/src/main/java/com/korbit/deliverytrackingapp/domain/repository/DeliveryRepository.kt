package com.korbit.deliverytrackingapp.domain.repository

import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth: Room. UI and UseCases read/write only through this.
 * No direct network calls from UI.
 */
interface DeliveryRepository {

    fun observeAllDeliveries(): Flow<List<Delivery>>

    fun observeDeliveryWithTasks(deliveryId: String): Flow<Delivery?>

    suspend fun updateTaskStatus(taskId: String, status: String, completedAt: Long?)

    suspend fun insertDeliveriesFromSync(deliveries: List<Delivery>)

    suspend fun updateDeliverySyncedAt(deliveryId: String, syncedAt: Long)
}
