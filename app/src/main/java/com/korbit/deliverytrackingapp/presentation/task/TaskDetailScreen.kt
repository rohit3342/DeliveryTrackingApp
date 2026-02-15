package com.korbit.deliverytrackingapp.presentation.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.korbit.deliverytrackingapp.R
import com.korbit.deliverytrackingapp.domain.model.Delivery
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.model.TaskActionType
import com.korbit.deliverytrackingapp.presentation.theme.StatusColors
import com.korbit.deliverytrackingapp.presentation.util.TimeFormat

private val ScreenBg = Color(0xFFF5F5F5)
private val PickupCardBg = Color(0xFFFDF6EB)
private val PickupOrange = Color(0xFFE65100)
private val DeliveryCardBg = Color(0xFFE3F2FD)
private val DeliveryBlue = Color(0xFF1976D2)
private val ProgressGreen = Color(0xFF22C55E)
private val StatusPendingGray = Color(0xFF8E8E93)
private val StatusInTransitPurple = Color(0xFFAF52DE)
private val StatusPickedBlue = Color(0xFF5B9BFE)
private val StatusDeliveredGreen = Color(0xFF34C759)
private val StatusFailedRed = Color(0xFFFF3B30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onBack: () -> Unit = {},
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_content_description),
                            tint = Color.Black
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.order_details),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black
                        )
                        state.delivery?.let { d ->
                            Text(
                                text = "ORD-" + d.id.replace(Regex("[^A-Za-z0-9]"), "").uppercase().take(12).ifEmpty { d.id.takeLast(8).uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                state.delivery?.let { delivery ->
                    val task = delivery.tasks.firstOrNull()
                    if (task != null) {
                        OrderStatusRow(task = task)
                        Spacer(modifier = Modifier.height(16.dp))
                        OrderProgressCard(task = task)
                        Spacer(modifier = Modifier.height(16.dp))
                        PickupLocationCard(delivery = delivery)
                        Spacer(modifier = Modifier.height(16.dp))
                        DeliveryLocationCard(delivery = delivery)
                    }
                } ?: run {
                    if (state.isLoading) {
                        Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text(state.error ?: stringResource(R.string.no_delivery), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            state.delivery?.let { delivery ->
                val task = delivery.tasks.firstOrNull()
                if (task != null) {
                    val allowedActions = nextAllowedActions(task.status, task.wasEverPicked)
                    if (allowedActions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            allowedActions.forEach { action ->
                                ActionButton(
                                    action = action,
                                    taskType = task.type,
                                    onClick = { viewModel.handle(TaskDetailIntent.PerformAction(task, action)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusRow(task: DeliveryTask) {
    val statusLabel = when (task.status) {
        "PENDING" -> "PENDING"
        "PICKED_UP" -> "PICKED UP"
        "REACHED" -> stringResource(R.string.status_in_transit)
        "DELIVERED" -> "DELIVERED"
        "FAILED" -> "FAILED"
        else -> task.status
    }
    val (pillFill, pillStroke, pillText) = when (task.status) {
        "PENDING" -> Triple(Color(0xFFF0F0F0), StatusPendingGray, StatusPendingGray)
        "PICKED_UP" -> Triple(Color(0xFFE8F0FE), StatusPickedBlue, StatusPickedBlue)
        "REACHED" -> Triple(Color(0xFFF3E5F5), StatusInTransitPurple, StatusInTransitPurple)
        "DELIVERED" -> Triple(Color(0xFFE8F5E9), StatusDeliveredGreen, StatusDeliveredGreen)
        "FAILED" -> Triple(Color(0xFFFFEBEE), StatusFailedRed, StatusFailedRed)
        else -> Triple(Color(0xFFF0F0F0), StatusPendingGray, StatusPendingGray)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(pillFill)
                .border(1.dp, pillStroke, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelLarge,
                color = pillText
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.DarkGray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = TimeFormat.formatDetailPageTime(task.lastModifiedAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun OrderProgressCard(task: DeliveryTask) {
    val isPickupFailedEarly = task.type.equals("PICKUP", ignoreCase = true) && task.status == "FAILED" && !task.wasEverPicked
    val assignedDone = true
    val pickedDone = task.wasEverPicked || task.status in listOf("PICKED_UP", "REACHED", "DELIVERED") || (task.status == "FAILED" && task.wasEverPicked)
    val reachedDone = task.status in listOf("REACHED", "DELIVERED") || (task.status == "FAILED" && task.wasEverPicked)
    val outcomeDone = task.status in listOf("DELIVERED", "FAILED")
    val timeStr = { ts: Long -> TimeFormat.formatDetailPageTime(ts) }
    val pendingStr = stringResource(R.string.timeline_pending)

    val steps = if (isPickupFailedEarly) {
        listOf(
            ProgressStep(stringResource(R.string.step_assigned), timeStr(task.createdAt), true, false, false),
            ProgressStep(stringResource(R.string.step_pickup_failed), timeStr(task.lastModifiedAt), true, false, true)
        )
    } else {
        val outcomeLabel = when {
            task.status == "FAILED" && task.type.equals("PICKUP", ignoreCase = true) -> stringResource(R.string.step_pickup_failed)
            task.status == "FAILED" -> stringResource(R.string.step_failed_to_deliver)
            else -> stringResource(R.string.step_delivered)
        }
        listOf(
            ProgressStep(stringResource(R.string.step_assigned), timeStr(task.createdAt), assignedDone, task.status == "PENDING", false),
            ProgressStep(stringResource(R.string.step_picked_up), if (pickedDone) timeStr(task.lastModifiedAt) else pendingStr, pickedDone, task.status == "PICKED_UP", false),
            ProgressStep(stringResource(R.string.step_reached_customer), if (reachedDone) timeStr(task.lastModifiedAt) else pendingStr, reachedDone, task.status == "REACHED", false),
            ProgressStep(outcomeLabel, if (outcomeDone) timeStr(task.lastModifiedAt) else pendingStr, outcomeDone, false, task.status == "FAILED" && outcomeDone)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.order_progress),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            steps.forEachIndexed { index, step ->
                val isLast = index == steps.lastIndex
                ProgressStepRow(
                    label = step.label,
                    subtext = step.subtext,
                    done = step.done,
                    isCurrent = step.isCurrent,
                    isFailedOutcome = step.isFailedOutcome,
                    showConnector = !isLast
                )
            }
        }
    }
}

private data class ProgressStep(val label: String, val subtext: String, val done: Boolean, val isCurrent: Boolean, val isFailedOutcome: Boolean)

@Composable
private fun ProgressStepRow(
    label: String,
    subtext: String,
    done: Boolean,
    isCurrent: Boolean,
    isFailedOutcome: Boolean,
    showConnector: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .then(
                        if (done || isCurrent || isFailedOutcome)
                            Modifier.background(
                                when {
                                    isFailedOutcome -> StatusFailedRed
                                    else -> ProgressGreen
                                }
                            )
                        else
                            Modifier.border(2.dp, Color.LightGray, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isFailedOutcome -> Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    done -> Icon(
                        when (label) {
                            stringResource(R.string.step_assigned) -> Icons.Outlined.AccessTime
                            stringResource(R.string.step_picked_up) -> Icons.Filled.Inventory
                            stringResource(R.string.step_reached_customer) -> Icons.Filled.Send
                            else -> Icons.Filled.Check
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    isCurrent -> Icon(Icons.Outlined.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    else -> { /* empty circle, border only */ }
                }
            }
            if (showConnector) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(if (done) ProgressGreen else Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (done || isCurrent) Color.Black else Color.Gray
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun PickupLocationCard(delivery: Delivery) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PickupCardBg),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PickupOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.pickup_location),
                    style = MaterialTheme.typography.labelMedium,
                    color = PickupOrange
                )
                Text(
                    text = delivery.warehouseName.ifBlank { stringResource(R.string.pickup_origin) },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Text(
                    text = delivery.warehouseAddress.ifBlank { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun DeliveryLocationCard(delivery: Delivery) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeliveryCardBg),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DeliveryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.delivery_location),
                    style = MaterialTheme.typography.labelMedium,
                    color = DeliveryBlue
                )
                Text(
                    text = delivery.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Text(
                    text = delivery.customerAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                if (delivery.customerPhone.isNotBlank()) {
                    Text(
                        text = delivery.customerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = DeliveryBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(action: TaskActionType, taskType: String, onClick: () -> Unit) {
    val (textRes, icon, containerColor) = when (action) {
        TaskActionType.PICKED_UP -> Triple(R.string.action_order_picked_up, Icons.Filled.Inventory, StatusPickedBlue)
        TaskActionType.REACHED -> Triple(R.string.action_reached_customer, Icons.Filled.Send, StatusPickedBlue)
        TaskActionType.DELIVERED -> Triple(R.string.action_delivered, Icons.Filled.Check, StatusPickedBlue)
        TaskActionType.FAILED -> Triple(
            if (taskType.equals("PICKUP", ignoreCase = true)) R.string.action_mark_failed_pickup else R.string.action_failed,
            Icons.Filled.Cancel,
            Color.White
        )
    }
    val contentColor = if (action == TaskActionType.FAILED) StatusFailedRed else Color.White
    val borderModifier = if (action == TaskActionType.FAILED) Modifier.border(1.dp, StatusFailedRed, RoundedCornerShape(12.dp)) else Modifier
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(borderModifier),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = contentColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(textRes), color = contentColor)
    }
}

private fun nextAllowedActions(currentStatus: String, wasEverPicked: Boolean = false): List<TaskActionType> = when (currentStatus) {
    "PENDING" -> listOf(TaskActionType.PICKED_UP, TaskActionType.FAILED)
    "PICKED_UP" -> listOf(TaskActionType.REACHED)
    "REACHED" -> listOf(TaskActionType.DELIVERED, TaskActionType.FAILED)
    "DELIVERED" -> emptyList()
    "FAILED" -> if (wasEverPicked) listOf(TaskActionType.REACHED) else listOf(TaskActionType.PICKED_UP)
    else -> listOf(TaskActionType.PICKED_UP, TaskActionType.FAILED)
}
