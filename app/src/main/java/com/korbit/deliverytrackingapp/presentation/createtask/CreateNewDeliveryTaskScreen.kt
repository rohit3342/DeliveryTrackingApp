package com.korbit.deliverytrackingapp.presentation.createtask

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.korbit.deliverytrackingapp.R

private val ScreenBackground = Color.White
private val PickupSectionBg = Color(0xFFFDF6EB)
private val DropSectionBg = Color(0xFFF2F8FD)
private val PrimaryBlue = Color(0xFF007AFF)
private val InputBorderGray = Color(0xFFD1D1D6)
private val InputBgGray = Color(0xFFF2F2F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewDeliveryTaskScreen(
    onBack: () -> Unit,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.createSuccess) {
        if (state.createSuccess) {
            viewModel.clearCreateSuccess()
            onBack()
        }
    }

    var warehouseName by remember { mutableStateOf("") }
    var warehouseAddress by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.size(48.dp))
                    Text(
                        text = stringResource(R.string.create_new_delivery_task),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = Color.Black
                        )
                    }
                }
                HorizontalDivider(color = InputBorderGray.copy(alpha = 0.5f))
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScreenBackground)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    ),
                    border = BorderStroke(1.dp, PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                androidx.compose.material3.Button(
                    onClick = {
                        if (customerName.isNotBlank() && customerAddress.isNotBlank() && customerPhone.isNotBlank()) {
                            viewModel.create(
                                warehouseName.trim(),
                                warehouseAddress.trim(),
                                customerName.trim(),
                                customerAddress.trim(),
                                customerPhone.trim()
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.create_task))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PickupSectionBg),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pickup_location_warehouse),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black
                    )
                    Text(
                        text = stringResource(R.string.warehouse_name),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = warehouseName,
                        onValueChange = { warehouseName = it },
                        placeholder = { Text(stringResource(R.string.warehouse_name_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = InputBorderGray,
                            focusedBorderColor = PrimaryBlue
                        )
                    )
                    Text(
                        text = stringResource(R.string.warehouse_address),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = warehouseAddress,
                        onValueChange = { warehouseAddress = it },
                        placeholder = { Text(stringResource(R.string.warehouse_address_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = InputBorderGray,
                            focusedBorderColor = PrimaryBlue
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DropSectionBg),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.drop_location_customer),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black
                    )
                    Text(
                        text = stringResource(R.string.customer_name),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        placeholder = { Text(stringResource(R.string.customer_name_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = InputBorderGray,
                            focusedBorderColor = PrimaryBlue
                        )
                    )
                    Text(
                        text = stringResource(R.string.customer_address),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        placeholder = { Text(stringResource(R.string.customer_address_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = InputBorderGray,
                            focusedBorderColor = PrimaryBlue
                        )
                    )
                    Text(
                        text = stringResource(R.string.customer_phone),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        placeholder = { Text(stringResource(R.string.customer_phone_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = InputBorderGray,
                            focusedBorderColor = PrimaryBlue
                        )
                    )
                }
            }
            if (state.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
