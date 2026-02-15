package com.korbit.deliverytrackingapp.presentation.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.korbit.deliverytrackingapp.domain.model.DeliveryTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Delivery Details") })
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
                Text(
                    text = delivery.customerName,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = delivery.customerAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Status: ${delivery.status}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                delivery.tasks.forEach { task ->
                    TaskItem(
                        task = task,
                        onComplete = { viewModel.handle(TaskDetailIntent.CompleteTask(task)) }
                    )
                }
            } ?: run {
                if (state.isLoading) {
                    Text("Loading...")
                } else {
                    Text(state.error ?: "No delivery")
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: DeliveryTask,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.type, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Status: ${task.status}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (task.status != "COMPLETED") {
                Button(onClick = onComplete) {
                    Text("Complete")
                }
            }
        }
    }
}
