package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.model.TaskAction
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreatePickupTaskUseCaseTest {

    private lateinit var deliveryRepository: DeliveryRepository
    private lateinit var outboxRepository: OutboxRepository
    private lateinit var useCase: CreatePickupTaskUseCase

    @Before
    fun setUp() {
        deliveryRepository = mockk(relaxedUnitFun = true)
        outboxRepository = mockk(relaxedUnitFun = true)
        useCase = CreatePickupTaskUseCase(deliveryRepository, outboxRepository)
    }

    @Test
    fun invoke_insertsDeliveryWithTasksInRepository() = runTest {
        val (delivery, task) = useCase(
            warehouseName = "WH1",
            warehouseAddress = "Addr 1",
            customerName = "Customer",
            customerAddress = "Customer Addr",
            customerPhone = "+123"
        )

        coVerify {
            deliveryRepository.insertDeliveryWithTasks(
                match { d: Delivery ->
                    d.warehouseName == "WH1" &&
                        d.warehouseAddress == "Addr 1" &&
                        d.customerName == "Customer" &&
                        d.customerAddress == "Customer Addr" &&
                        d.customerPhone == "+123" &&
                        d.tasks.size == 1 &&
                        d.tasks.first().type == "PICKUP" &&
                        d.tasks.first().status == "PENDING"
                }
            )
        }
        assertEquals(delivery.tasks.first(), task)
    }

    @Test
    fun invoke_enqueuesCreatePickupActionInOutbox() = runTest {
        val (delivery, task) = useCase(
            warehouseName = "W",
            warehouseAddress = "A",
            customerName = "C",
            customerAddress = "CA",
            customerPhone = ""
        )

        coVerify {
            outboxRepository.enqueueTaskAction(
                match { action: TaskAction ->
                    action.taskId == task.id &&
                        action.action == "CREATE_PICKUP" &&
                        action.payload.contains("deliveryId") &&
                        action.payload.contains("warehouseName")
                }
            )
        }
    }

    @Test
    fun invoke_returnsDeliveryWithSinglePickupTask() = runTest {
        val (delivery, task) = useCase(
            warehouseName = "Warehouse",
            warehouseAddress = "123 St",
            customerName = "Name",
            customerAddress = "456 Rd",
            customerPhone = "555"
        )

        assertEquals("rider_1", delivery.riderId)
        assertEquals("ACTIVE", delivery.status)
        assertTrue(delivery.id.startsWith("d_"))
        assertTrue(task.id.startsWith("t_"))
        assertEquals(delivery.id, task.deliveryId)
        assertEquals("PICKUP", task.type)
        assertEquals("PENDING", task.status)
        assertEquals(1, task.sequence)
    }
}
