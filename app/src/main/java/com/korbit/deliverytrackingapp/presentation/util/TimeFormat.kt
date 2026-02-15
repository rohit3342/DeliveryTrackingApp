package com.korbit.deliverytrackingapp.presentation.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormat {
    private val dateTimeFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    /** Order detail page style: 15/02/2026, 16:20:42 */
    private val detailPageFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault())

    fun formatCreationTime(timestampMs: Long): String =
        if (timestampMs > 0) dateTimeFormat.format(Date(timestampMs)) else "—"

    fun formatLastModified(timestampMs: Long): String =
        if (timestampMs > 0) dateTimeFormat.format(Date(timestampMs)) else "—"

    fun formatDetailPageTime(timestampMs: Long): String =
        if (timestampMs > 0) detailPageFormat.format(Date(timestampMs)) else "—"
}
