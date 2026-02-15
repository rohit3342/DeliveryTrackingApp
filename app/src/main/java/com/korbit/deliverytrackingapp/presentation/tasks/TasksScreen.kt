package com.korbit.deliverytrackingapp.presentation.tasks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.korbit.deliverytrackingapp.presentation.tasks.components.TaskWithDeliveryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onTaskClick: (String, String) -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.syncMessage) {
        state.syncMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.handle(TasksIntent.ClearSyncMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assigned Tasks") },
                actions = {
                    IconButton(
                        onClick = { viewModel.handle(TasksIntent.ManualSync) },
                        enabled = !state.isSyncing
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = "Sync")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.handle(TasksIntent.ShowCreatePickup) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New pickup")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.selectedFilter == filter,
                        onClick = { viewModel.handle(TasksIntent.SetFilter(filter)) },
                        label = { Text(filter.label) }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.tasks.isEmpty() -> {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }

                    state.error != null -> {
                        Text(
                            text = state.error ?: "Error",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

                    state.filteredTasks.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(if (state.tasks.isEmpty()) "No tasks assigned" else "No tasks for this filter")
                            Button(onClick = { viewModel.handle(TasksIntent.ShowCreatePickup) }) {
                                Text("Create pickup")
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(
                                state.filteredTasks,
                                key = { "${it.delivery.id}_${it.task.id}" }
                            ) { twd ->
                                TaskWithDeliveryItem(
                                    taskWithDelivery = twd,
                                    onClick = { onTaskClick(twd.delivery.id, twd.task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showCreatePickupDialog) {
        CreatePickupDialog(
            onDismiss = { viewModel.handle(TasksIntent.DismissCreatePickup) },
            onCreate = { name, address, phone ->
                viewModel.handle(TasksIntent.CreatePickup(name, address, phone))
            }
        )
    }
}

@Composable
private fun CreatePickupDialog(
    onDismiss: () -> Unit,
    onCreate: (customerName: String, customerAddress: String, customerPhone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New pickup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Customer phone number *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && address.isNotBlank() && phone.isNotBlank()) {
                        onCreate(name.trim(), address.trim(), phone.trim())
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
