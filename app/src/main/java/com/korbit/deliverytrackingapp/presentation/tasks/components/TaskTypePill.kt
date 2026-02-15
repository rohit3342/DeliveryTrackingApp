package com.korbit.deliverytrackingapp.presentation.tasks.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.korbit.deliverytrackingapp.R
import com.korbit.deliverytrackingapp.presentation.theme.TaskTypeColors

@Composable
fun TaskTypePill(
    type: String,
    modifier: Modifier = Modifier
) {
    val borderColor = TaskTypeColors.forType(type)
    val label = when (type.uppercase()) {
        "PICKUP" -> stringResource(R.string.task_type_pickup)
        "DELIVER" -> stringResource(R.string.task_type_deliver)
        else -> type.replace("_", " ")
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = borderColor
        )
    }
}
