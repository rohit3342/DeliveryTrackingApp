package com.korbit.deliverytrackingapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.korbit.deliverytrackingapp.domain.repository.DeliveryRepository
import com.korbit.deliverytrackingapp.domain.usecase.SeedBulkShipmentsUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Load test: inserts 8,000 shipments (each with one task), measures insert time,
 * and verifies task count. Run with: ./gradlew connectedDebugAndroidTest
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoadTest8000ShipmentsTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var deliveryRepository: DeliveryRepository

    @Inject
    lateinit var seedBulkShipmentsUseCase: SeedBulkShipmentsUseCase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun insert8000Shipments_andVerifyCount_measuresInsertTime() = runBlocking {
        val totalShipments = 8_000
        deliveryRepository.clearAllForTesting()
        assertEquals(0, deliveryRepository.getTaskCount("ALL"))

        val startMs = System.currentTimeMillis()
        seedBulkShipmentsUseCase(totalShipments)
        val insertDurationMs = System.currentTimeMillis() - startMs

        val taskCount = deliveryRepository.getTaskCount("ALL")
        val deliveryCount = deliveryRepository.getDeliveryCount()
        assertEquals("Task count must equal number of shipments", totalShipments, taskCount)
        assertEquals("Delivery count must equal number of shipments", totalShipments, deliveryCount)

        // Log results for docs/LOAD_TEST_8000_RESULTS.md (visible in test output)
        println("[LoadTest8000] INSERT_MS=$insertDurationMs TASKS=$taskCount DELIVERIES=$deliveryCount")
    }
}
