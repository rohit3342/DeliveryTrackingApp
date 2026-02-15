package com.korbit.deliverytrackingapp.data.remote.api

import com.korbit.deliverytrackingapp.data.remote.dto.DeliveryDto
import com.korbit.deliverytrackingapp.data.remote.dto.TaskActionRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Mock API for delivery and task actions. Used only by SyncEngine – no direct UI → API.
 */
interface DeliveryApi {

    @GET("deliveries")
    suspend fun getDeliveries(): Response<List<DeliveryDto>>

    @GET("deliveries/{id}")
    suspend fun getDeliveryById(@Path("id") id: String): Response<DeliveryDto>

    /** Create multiple deliveries/tasks in one call (bulk). Empty list is a no-op. */
    @POST("tasks/create")
    suspend fun createTasks(@Body deliveries: List<DeliveryDto>): Response<Unit>

    /** Submit multiple task actions in one call (batch). Empty list is a no-op. */
    @POST("tasks/action")
    suspend fun submitTaskActions(@Body actions: List<TaskActionRequestDto>): Response<Unit>
}
