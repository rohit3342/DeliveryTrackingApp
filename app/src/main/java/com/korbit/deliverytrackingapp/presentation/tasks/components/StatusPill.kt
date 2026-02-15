package com.korbit.deliverytrackingapp.presentation.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.korbit.deliverytrackingapp.R
import com.korbit.deliverytrackingapp.presentation.theme.StatusColors

@Composable
fun StatusPill(
    status: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = StatusColors.forStatus(status)
    val label = when (status) {
        "PENDING" -> stringResource(R.string.status_assigned)
        "PICKED_UP" -> stringResource(R.string.status_picked)
        "REACHED" -> stringResource(R.string.status_reached)
        "DELIVERED" -> stringResource(R.string.status_delivered)
        "FAILED" -> stringResource(R.string.status_failed)
        else -> status
    }
    val onColor = Color.White
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = onColor
        )
    }
}
