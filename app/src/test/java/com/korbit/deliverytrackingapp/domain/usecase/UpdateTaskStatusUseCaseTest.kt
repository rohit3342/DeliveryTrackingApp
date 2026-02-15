package com.korbit.deliverytrackingapp.domain.usecase

import com.korbit.deliverytrackingapp.domain.model.TaskAction
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import com.korbit.deliverytrackingapp.domain.repository.OutboxRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateTaskStatusUseCaseTest {

    private lateinit var deliveryRepository: DeliveryRepository
    private lateinit var outboxRepository: OutboxRepository
    private lateinit var useCase: UpdateTaskStatusUseCase

    @Before
    fun setUp() {
        deliveryRepository = mockk(relaxedUnitFun = true)
        outboxRepository = mockk(relaxedUnitFun = true)
        useCase = UpdateTaskStatusUseCase(deliveryRepository, outboxRepository)
    }

    @Test
    fun invoke_updatesTaskStatusInRepository() = runTest {
        val taskId = "task_1"
        val status = "PICKED_UP"
        val completedAt = 1000L
        val actionTakenAt = 1000L

        useCase(
            taskId = taskId,
            status = status,
            completedAt = completedAt,
            action = status,
            payload = null,
            actionTakenAt = actionTakenAt
        )

        coVerify {
            deliveryRepository.updateTaskStatus(
                taskId = taskId,
                status = status,
                completedAt = completedAt,
                updatedAt = actionTakenAt
            )
        }
    }

    @Test
    fun invoke_enqueuesTaskActionInOutbox() = runTest {
        val taskId = "task_2"
        val status = "DELIVERED"
        val completedAt = 2000L
        val actionTakenAt = 2000L

        useCase(
            taskId = taskId,
            status = status,
            completedAt = completedAt,
            action = status,
            payload = null,
            actionTakenAt = actionTakenAt
        )

        coVerify {
            outboxRepository.enqueueTaskAction(
                match { action: TaskAction ->
                    action.taskId == taskId &&
                        action.action == status &&
                        action.payload == null &&
                        action.actionTakenAt == actionTakenAt
                }
            )
        }
    }

    @Test
    fun invoke_withPayload_enqueuesOutboxWithPayload() = runTest {
        val taskId = "task_3"
        val status = "FAILED"
        val payload = "{\"reason\":\"customer_unavailable\"}"
        val actionTakenAt = 3000L

        useCase(
            taskId = taskId,
            status = status,
            completedAt = actionTakenAt,
            action = status,
            payload = payload,
            actionTakenAt = actionTakenAt
        )

        coVerify {
            outboxRepository.enqueueTaskAction(
                match { action: TaskAction ->
                    action.taskId == taskId &&
                        action.action == status &&
                        action.payload == payload &&
                        action.actionTakenAt == actionTakenAt
                }
            )
        }
    }

    @Test
    fun invoke_forReached_updatesStatusAndEnqueuesReached() = runTest {
        val taskId = "t_reached"
        val status = "REACHED"
        val actionTakenAt = 4000L

        useCase(
            taskId = taskId,
            status = status,
            completedAt = null,
            action = status,
            payload = null,
            actionTakenAt = actionTakenAt
        )

        coVerify {
            deliveryRepository.updateTaskStatus(
                taskId = taskId,
                status = status,
                completedAt = null,
                updatedAt = actionTakenAt
            )
        }
        coVerify {
            outboxRepository.enqueueTaskAction(
                match { it.taskId == taskId && it.action == "REACHED" }
            )
        }
    }
}
