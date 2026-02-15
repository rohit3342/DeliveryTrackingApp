package com.korbit.deliverytrackingapp.presentation.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.korbit.deliverytrackingapp.R
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask
import com.korbit.deliverytrackingapp.domain.model.TaskActionType
import com.korbit.deliverytrackingapp.presentation.theme.StatusColors
import com.korbit.deliverytrackingapp.presentation.tasks.components.StatusPill
import com.korbit.deliverytrackingapp.presentation.tasks.components.TaskTypePill
import com.korbit.deliverytrackingapp.presentation.util.TimeFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onBack: () -> Unit = {},
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.task_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_content_description))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            state.delivery?.let { delivery ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = delivery.customerName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = delivery.customerAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (delivery.customerPhone.isNotBlank()) {
                            Text(
                                text = delivery.customerPhone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.tasks_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                delivery.tasks.forEach { task ->
                    TaskItem(
                        task = task,
                        onAction = { action -> viewModel.handle(TaskDetailIntent.PerformAction(task, action)) }
                    )
                }
            } ?: run {
                if (state.isLoading) {
                    Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(state.error ?: stringResource(R.string.no_delivery), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: DeliveryTask,
    onAction: (TaskActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val allowedActions = nextAllowedActions(task.status, task.wasEverPicked)
    val displayType = if (task.type.equals("PICKUP", ignoreCase = true) && (task.wasEverPicked || task.status != "PENDING")) "DELIVER" else task.type
    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskTypePill(type = displayType)
                    StatusPill(status = task.status)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TrackingTimeline(task = task)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.created, TimeFormat.formatCreationTime(task.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.last_modified, TimeFormat.formatLastModified(task.lastModifiedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (allowedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.actions_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allowedActions.forEach { action ->
                    ActionButton(action = action, taskType = task.type, onClick = { onAction(action) })
                }
            }
        }
    }
}

private enum class ConnectorStyle { SOLID, DOTTED, NONE }

@Composable
private fun TrackingTimeline(task: DeliveryTask) {
    val isPickupFailedEarly = task.type.equals("PICKUP", ignoreCase = true) && task.status == "FAILED" && !task.wasEverPicked
    val assignedDone = true
    val pickedDone = task.wasEverPicked || task.status in listOf("PICKED_UP", "REACHED", "DELIVERED") || (task.status == "FAILED" && task.wasEverPicked)
    val reachedDone = task.status in listOf("REACHED", "DELIVERED") || (task.status == "FAILED" && task.wasEverPicked)
    val outcomeDone = task.status in listOf("DELIVERED", "FAILED")
    val createdStr = TimeFormat.formatCreationTime(task.createdAt)
    val modifiedStr = TimeFormat.formatLastModified(task.lastModifiedAt)
    val pendingStr = stringResource(R.string.timeline_pending)

    val steps = if (isPickupFailedEarly) {
        listOf(
            StepRow(stringResource(R.string.step_assigned), createdStr, assignedDone, task.status == "PENDING", false),
            StepRow(stringResource(R.string.step_pickup_failed), modifiedStr, true, false, true)
        )
    } else {
        val outcomeLabel = when {
            task.status == "FAILED" && task.type.equals("PICKUP", ignoreCase = true) -> stringResource(R.string.step_pickup_failed)
            task.status == "FAILED" -> stringResource(R.string.step_failed)
            else -> stringResource(R.string.step_delivered)
        }
        listOf(
            StepRow(stringResource(R.string.step_assigned), createdStr, assignedDone, task.status == "PENDING", false),
            StepRow(stringResource(R.string.step_picked), if (pickedDone) modifiedStr else pendingStr, pickedDone, task.status == "PICKED_UP", false),
            StepRow(stringResource(R.string.step_reached), if (reachedDone) modifiedStr else pendingStr, reachedDone, task.status == "REACHED", false),
            StepRow(outcomeLabel, if (outcomeDone) modifiedStr else pendingStr, outcomeDone, false, task.status == "FAILED" && outcomeDone)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text = stringResource(R.string.tracking_timeline),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        steps.forEachIndexed { index, step ->
            val nextStep = if (index < steps.lastIndex) steps[index + 1] else null
            val lineStyle = if (nextStep != null) {
                when {
                    nextStep.isFailedOutcome -> ConnectorStyle.DOTTED
                    step.done && nextStep.done -> ConnectorStyle.SOLID
                    step.done && !nextStep.done -> ConnectorStyle.DOTTED
                    else -> ConnectorStyle.NONE
                }
            } else null
            TimelineNodeWithConnector(
                label = step.label,
                subtext = step.subtext,
                done = step.done,
                isCurrent = step.isCurrent,
                isFailedOutcome = step.isFailedOutcome,
                connectorStyle = lineStyle
            )
        }
    }
}

private data class StepRow(val label: String, val subtext: String, val done: Boolean, val isCurrent: Boolean, val isFailedOutcome: Boolean)

@Composable
private fun TimelineNodeWithConnector(
    label: String,
    subtext: String,
    done: Boolean,
    isCurrent: Boolean,
    isFailedOutcome: Boolean,
    connectorStyle: ConnectorStyle?
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Icon(
                imageVector = when {
                    isFailedOutcome -> Icons.Filled.Cancel
                    done -> Icons.Filled.CheckCircle
                    isCurrent -> Icons.Filled.CheckCircle
                    else -> Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = when {
                    isFailedOutcome -> StatusColors.Failed
                    done -> MaterialTheme.colorScheme.primary
                    isCurrent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }
            )
            if (connectorStyle != null) {
                TimelineConnector(style = connectorStyle)
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (done || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimelineConnector(style: ConnectorStyle) {
    val color = when (style) {
        ConnectorStyle.SOLID, ConnectorStyle.DOTTED -> MaterialTheme.colorScheme.primary
        ConnectorStyle.NONE -> Color.Transparent
    }
    Canvas(modifier = Modifier.size(width = 24.dp, height = 20.dp).padding(top = 2.dp)) {
        val strokeWidth = 2.dp.toPx()
        val centerX = size.width / 2
        val path = Path().apply { moveTo(centerX, 0f); lineTo(centerX, size.height) }
        when (style) {
            ConnectorStyle.SOLID -> drawPath(path, color, style = Stroke(width = strokeWidth))
            ConnectorStyle.DOTTED -> drawPath(path, color, style = Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)))
            ConnectorStyle.NONE -> { }
        }
    }
}

@Composable
private fun ActionButton(action: TaskActionType, taskType: String, onClick: () -> Unit) {
    val (textRes, icon, isDestructive) = when (action) {
        TaskActionType.PICKED_UP -> Triple(R.string.action_mark_picked, Icons.Filled.Inventory, false)
        TaskActionType.REACHED -> Triple(R.string.action_mark_reached, Icons.Filled.LocationOn, false)
        TaskActionType.DELIVERED -> Triple(R.string.action_mark_delivered, Icons.Filled.LocalShipping, false)
        TaskActionType.FAILED -> Triple(
            if (taskType.equals("PICKUP", ignoreCase = true)) R.string.action_mark_failed_pickup else R.string.action_mark_failed,
            Icons.Filled.Cancel,
            true
        )
    }
    val buttonColors = when (action) {
        TaskActionType.DELIVERED -> ButtonDefaults.buttonColors(containerColor = StatusColors.Delivered, contentColor = Color.White)
        TaskActionType.FAILED -> ButtonDefaults.buttonColors(containerColor = StatusColors.Failed, contentColor = Color.White)
        TaskActionType.PICKED_UP -> ButtonDefaults.buttonColors(containerColor = StatusColors.Picked, contentColor = Color.White)
        TaskActionType.REACHED -> ButtonDefaults.buttonColors(containerColor = StatusColors.Reached, contentColor = Color.White)
    }
    if (isDestructive) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusColors.Failed),
            border = BorderStroke(2.dp, StatusColors.Failed)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(textRes))
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = buttonColors
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(textRes))
        }
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
