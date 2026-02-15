package com.korbit.deliverytrackingapp.presentation.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.korbit.deliverytrackingapp.R
import com.korbit.deliverytrackingapp.domain.model.TaskWithDelivery
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TextGray = Color(0xFF8E8E93)
private val OrangeIcon = Color(0xFFFF9500)
private val BlueIcon = Color(0xFF2172FF)
private val PendingSyncOrange = Color(0xFFFF9500)
private val StatusDeliveredGreen = Color(0xFF34C759)
private val StatusFailedRed = Color(0xFFFF3B30)
private val StatusInTransitPurple = Color(0xFFAF52DE)
private val StatusPickedUpBlue = Color(0xFF5B9BFE)
private val StatusPendingGray = Color(0xFF8E8E93)

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

private fun formatLastUpdate(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(timeFormatter)
}

@Composable
fun HomeTaskCard(
    taskWithDelivery: TaskWithDelivery,
    pendingSync: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = taskWithDelivery.task
    val delivery = taskWithDelivery.delivery
    val displayId = "ORD-" + delivery.id.replace(Regex("[^A-Za-z0-9]"), "").uppercase().take(12).ifEmpty { task.id.takeLast(8).uppercase() }
    val (statusLabel, statusColor) = when (task.status) {
        "PENDING" -> "PENDING" to StatusPendingGray
        "PICKED_UP" -> "PICKED UP" to StatusPickedUpBlue
        "REACHED" -> "IN TRANSIT" to StatusInTransitPurple
        "DELIVERED" -> "DELIVERED" to StatusDeliveredGreen
        "FAILED" -> "FAILED" to StatusFailedRed
        else -> task.status to StatusPendingGray
    }
    val lastUpdateMillis = maxOf(delivery.lastUpdatedAt, task.lastModifiedAt).takeIf { it > 0 } ?: delivery.lastUpdatedAt
    val lastUpdateText = formatLastUpdate(lastUpdateMillis)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = displayId,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    if (pendingSync) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PendingSyncOrange)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pending_sync),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Filled.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = OrangeIcon
                    )
                    Text(
                        text = delivery.warehouseName.ifBlank { stringResource(R.string.pickup_origin) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextGray
                    )
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = BlueIcon
                    )
                    Text(
                        text = delivery.customerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.last_update, lastUpdateText),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = TextGray
            )
        }
    }
}
