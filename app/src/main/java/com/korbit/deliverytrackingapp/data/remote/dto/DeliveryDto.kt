package com.korbit.deliverytrackingapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeliveryDto(
    @SerializedName("id") val id: String,
    @SerializedName("rider_id") val riderId: String,
    @SerializedName("status") val status: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("customer_address") val customerAddress: String,
    @SerializedName("customer_phone") val customerPhone: String? = null,
    @SerializedName("last_updated_at") val lastUpdatedAt: Long,
    @SerializedName("tasks") val tasks: List<TaskDto>?
)

data class TaskDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String,
    @SerializedName("sequence") val sequence: Int,
    @SerializedName("completed_at") val completedAt: Long?
)
