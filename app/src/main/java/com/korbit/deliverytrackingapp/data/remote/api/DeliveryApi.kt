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

    @POST("tasks/action")
    suspend fun submitTaskAction(@Body request: TaskActionRequestDto): Response<Unit>
}
