package com.korbit.deliverytrackingapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import com.korbit.deliverytrackingapp.domain.usecase.CreatePickupTaskUseCase
import com.korbit.deliverytrackingapp.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumented tests for delivery task actions: perform PICKED_UP, REACHED, DELIVERED, FAILED
 * and verify task status in DB and outbox event enqueued.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeliveryTaskActionsTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var deliveryRepository: DeliveryRepository

    @Inject
    lateinit var outboxRepository: OutboxRepository

    @Inject
    lateinit var updateTaskStatusUseCase: UpdateTaskStatusUseCase

    @Inject
    lateinit var createPickupTaskUseCase: CreatePickupTaskUseCase

    private val deliveryId = "action_test_del_1"
    private val taskId = "action_test_task_1"

    @Before
    fun setUp() = runBlocking {
        hiltRule.inject()
        deliveryRepository.clearAllForTesting()
        insertTestDeliveryWithPendingTask()
    }

    private suspend fun insertTestDeliveryWithPendingTask() {
        val now = System.currentTimeMillis()
        val delivery = Delivery(
            id = deliveryId,
            riderId = "rider_1",
            status = "ACTIVE",
            customerName = "Test Customer",
            customerAddress = "Test Address",
            customerPhone = "",
            warehouseName = "Test WH",
            warehouseAddress = "WH Address",
            lastUpdatedAt = now,
            syncedAt = null,
            tasks = listOf(
                DeliveryTask(
                    id = taskId,
                    deliveryId = deliveryId,
                    type = "PICKUP",
                    status = "PENDING",
                    sequence = 1,
                    completedAt = null,
                    createdAt = now,
                    lastModifiedAt = now
                )
            )
        )
        deliveryRepository.insertDeliveryWithTasks(delivery)
    }

    @Test
    fun performPickedUp_updatesTaskInDb_andEnqueuesOutboxEvent() = runBlocking {
        val pendingBefore = outboxRepository.getPendingCount()

        updateTaskStatusUseCase(
            taskId = taskId,
            status = "PICKED_UP",
            completedAt = System.currentTimeMillis(),
            action = "PICKED_UP",
            payload = null,
            actionTakenAt = System.currentTimeMillis()
        )

        val delivery = deliveryRepository.getDeliveryById(deliveryId)
        assertTrue(delivery != null)
        val task = delivery!!.tasks.first { it.id == taskId }
        assertEquals("PICKED_UP", task.status)
        assertTrue(task.wasEverPicked)
        assertEquals("DELIVER", task.type)

        assertEquals(pendingBefore + 1, outboxRepository.getPendingCount())
        assertTrue(outboxRepository.getPendingTaskIdsLimit(100).contains(taskId))
    }

    @Test
    fun performReached_afterPickedUp_updatesTaskAndEnqueuesOutbox() = runBlocking {
        updateTaskStatusUseCase(
            taskId = taskId,
            status = "PICKED_UP",
            completedAt = System.currentTimeMillis(),
            action = "PICKED_UP",
            actionTakenAt = System.currentTimeMillis()
        )
        val pendingAfterPickup = outboxRepository.getPendingCount()

        updateTaskStatusUseCase(
            taskId = taskId,
            status = "REACHED",
            completedAt = null,
            action = "REACHED",
            actionTakenAt = System.currentTimeMillis()
        )

        val delivery = deliveryRepository.getDeliveryById(deliveryId)
        val task = delivery!!.tasks.first { it.id == taskId }
        assertEquals("REACHED", task.status)

        assertEquals(pendingAfterPickup + 1, outboxRepository.getPendingCount())
    }

    @Test
    fun performDelivered_afterReached_updatesTaskAndEnqueuesOutbox() = runBlocking {
        updateTaskStatusUseCase(taskId, "PICKED_UP", System.currentTimeMillis(), "PICKED_UP", null, System.currentTimeMillis())
        updateTaskStatusUseCase(taskId, "REACHED", null, "REACHED", null, System.currentTimeMillis())
        val pendingBefore = outboxRepository.getPendingCount()

        val completedAt = System.currentTimeMillis()
        updateTaskStatusUseCase(taskId, "DELIVERED", completedAt, "DELIVERED", null, completedAt)

        val delivery = deliveryRepository.getDeliveryById(deliveryId)
        val task = delivery!!.tasks.first { it.id == taskId }
        assertEquals("DELIVERED", task.status)
        assertEquals(completedAt, task.completedAt)

        assertEquals(pendingBefore + 1, outboxRepository.getPendingCount())
    }

    @Test
    fun performFailed_fromPending_updatesTaskAndEnqueuesOutbox() = runBlocking {
        val completedAt = System.currentTimeMillis()
        updateTaskStatusUseCase(
            taskId = taskId,
            status = "FAILED",
            completedAt = completedAt,
            action = "FAILED",
            payload = "{\"reason\":\"unavailable\"}",
            actionTakenAt = completedAt
        )

        val delivery = deliveryRepository.getDeliveryById(deliveryId)
        val task = delivery!!.tasks.first { it.id == taskId }
        assertEquals("FAILED", task.status)
        assertEquals(completedAt, task.completedAt)

        assertTrue(outboxRepository.getPendingTaskIdsLimit(100).contains(taskId))
    }

    @Test
    fun performFailed_afterReached_updatesTaskAndEnqueuesOutbox() = runBlocking {
        updateTaskStatusUseCase(taskId, "PICKED_UP", System.currentTimeMillis(), "PICKED_UP", null, System.currentTimeMillis())
        updateTaskStatusUseCase(taskId, "REACHED", null, "REACHED", null, System.currentTimeMillis())

        val completedAt = System.currentTimeMillis()
        updateTaskStatusUseCase(taskId, "FAILED", completedAt, "FAILED", null, completedAt)

        val delivery = deliveryRepository.getDeliveryById(deliveryId)
        val task = delivery!!.tasks.first { it.id == taskId }
        assertEquals("FAILED", task.status)
        assertTrue(task.wasEverPicked)
    }

    @Test
    fun createPickupTask_insertsDeliveryAndTask_andEnqueuesCreatePickupOutboxEvent() = runBlocking {
        deliveryRepository.clearAllForTesting()
        assertEquals(0, outboxRepository.getPendingCount())

        val (delivery, task) = createPickupTaskUseCase(
            warehouseName = "New WH",
            warehouseAddress = "New WH Addr",
            customerName = "New Customer",
            customerAddress = "New Addr",
            customerPhone = "+999"
        )

        val loaded = deliveryRepository.getDeliveryById(delivery.id)
        assertTrue(loaded != null)
        assertEquals("New WH", loaded!!.warehouseName)
        assertEquals("New Customer", loaded.customerName)
        assertEquals(1, loaded.tasks.size)
        assertEquals("PICKUP", loaded.tasks.first().type)
        assertEquals("PENDING", loaded.tasks.first().status)

        assertEquals(1, outboxRepository.getPendingCount())
        assertTrue(outboxRepository.getPendingTaskIdsLimit(10).contains(task.id))
    }
}
