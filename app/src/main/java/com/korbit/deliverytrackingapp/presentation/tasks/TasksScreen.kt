package com.korbit.deliverytrackingapp.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.korbit.deliverytrackingapp.R
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
                title = { Text(stringResource(R.string.assigned_tasks)) },
                actions = {
                    IconButton(
                        onClick = { viewModel.handle(TasksIntent.ManualSync) },
                        enabled = !state.isSyncing
                    ) {
                        Icon(
                            imageVector = if (state.isAllSynced) Icons.Filled.CheckCircle else Icons.Filled.Sync,
                            contentDescription = if (state.isAllSynced) stringResource(R.string.sync_done_content_description) else stringResource(R.string.sync_content_description)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.handle(TasksIntent.ShowCreatePickup) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_pickup_content_description))
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
                        label = { Text(stringResource(filter.labelResId)) }
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
                            text = state.error ?: stringResource(R.string.error_generic),
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
                            Text(if (state.tasks.isEmpty()) stringResource(R.string.no_tasks_assigned) else stringResource(R.string.no_tasks_for_filter))
                            Button(onClick = { viewModel.handle(TasksIntent.ShowCreatePickup) }) {
                                Text(stringResource(R.string.create_pickup))
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
        CreateNewDeliveryTaskDialog(
            onDismiss = { viewModel.handle(TasksIntent.DismissCreatePickup) },
            onCreate = { orderId, whName, whAddress, custName, custAddress, custPhone ->
                viewModel.handle(TasksIntent.CreatePickup(orderId, whName, whAddress, custName, custAddress, custPhone))
            }
        )
    }
}

@Composable
private fun CreateNewDeliveryTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (orderId: String, warehouseName: String, warehouseAddress: String, customerName: String, customerAddress: String, customerPhone: String) -> Unit
) {
    var orderId by remember { mutableStateOf("") }
    var warehouseName by remember { mutableStateOf("") }
    var warehouseAddress by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    val orangeTint = MaterialTheme.colorScheme.tertiaryContainer
    val blueTint = MaterialTheme.colorScheme.secondaryContainer

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.create_new_delivery_task),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = orderId,
                    onValueChange = { orderId = it },
                    label = { Text(stringResource(R.string.order_id)) },
                    placeholder = { Text(stringResource(R.string.order_id_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = orangeTint),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.pickup_location_warehouse),
                            style = MaterialTheme.typography.titleSmall
                        )
                        OutlinedTextField(
                            value = warehouseName,
                            onValueChange = { warehouseName = it },
                            label = { Text(stringResource(R.string.warehouse_name)) },
                            placeholder = { Text(stringResource(R.string.warehouse_name_hint)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = warehouseAddress,
                            onValueChange = { warehouseAddress = it },
                            label = { Text(stringResource(R.string.warehouse_address)) },
                            placeholder = { Text(stringResource(R.string.warehouse_address_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = blueTint),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.drop_location_customer),
                            style = MaterialTheme.typography.titleSmall
                        )
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text(stringResource(R.string.customer_name)) },
                            placeholder = { Text(stringResource(R.string.customer_name_hint)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customerAddress,
                            onValueChange = { customerAddress = it },
                            label = { Text(stringResource(R.string.customer_address)) },
                            placeholder = { Text(stringResource(R.string.customer_address_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text(stringResource(R.string.customer_phone)) },
                            placeholder = { Text(stringResource(R.string.customer_phone_hint)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && customerAddress.isNotBlank() && customerPhone.isNotBlank()) {
                                onCreate(
                                    orderId.trim(),
                                    warehouseName.trim(),
                                    warehouseAddress.trim(),
                                    customerName.trim(),
                                    customerAddress.trim(),
                                    customerPhone.trim()
                                )
                            }
                        }
                    ) {
                        Text(stringResource(R.string.create_task))
                    }
                }
            }
        }
    }
}
