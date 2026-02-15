package com.korbit.deliverytrackingapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TaskActionRequestDto(
    @SerializedName("task_id") val taskId: String,
    @SerializedName("action") val action: String,
    @SerializedName("payload") val payload: String?
)
