package com.korbit.deliverytrackingapp.presentation.theme

import androidx.compose.ui.graphics.Color

/** Color grading for task status pills. */
object StatusColors {
    val Assigned = Color(0xFFF59E0B)   // amber
    val Picked = Color(0xFF3B82F6)     // blue
    val Reached = Color(0xFF8B5CF6)    // violet
    val Delivered = Color(0xFF22C55E)  // green
    val Failed = Color(0xFFEF4444)     // red

    fun forStatus(status: String): Color = when (status) {
        "PENDING" -> Assigned
        "PICKED_UP" -> Picked
        "REACHED" -> Reached
        "DELIVERED" -> Delivered
        "FAILED" -> Failed
        else -> Assigned
    }

    fun statusLabel(status: String): String = when (status) {
        "PENDING" -> "Assigned"
        "PICKED_UP" -> "Picked"
        "REACHED" -> "Reached"
        "DELIVERED" -> "Delivered"
        "FAILED" -> "Failed"
        else -> status
    }
}

/** Color grading for task type pills (different style from status). */
object TaskTypeColors {
    val Pickup = Color(0xFF0EA5E9)     // sky
    val Deliver = Color(0xFFF97316)    // orange

    fun forType(type: String): Color = when (type.uppercase()) {
        "PICKUP" -> Pickup
        "DELIVER" -> Deliver
        else -> Pickup
    }
}
