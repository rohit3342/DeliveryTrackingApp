package com.korbit.deliverytrackingapp.presentation.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormat {
    private val dateTimeFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    fun formatCreationTime(timestampMs: Long): String =
        if (timestampMs > 0) dateTimeFormat.format(Date(timestampMs)) else "—"

    fun formatLastModified(timestampMs: Long): String =
        if (timestampMs > 0) dateTimeFormat.format(Date(timestampMs)) else "—"
}
