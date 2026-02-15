package com.korbit.deliverytrackingapp.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.korbit.deliverytrackingapp.R
import com.korbit.deliverytrackingapp.presentation.tasks.components.HomeTaskCard

private val HeaderBlue = Color(0xFF2172FF)
private val ActiveGreen = Color(0xFF34C759)
private val BackgroundGray = Color(0xFFF5F5F5)
private val BorderGray = Color(0xFFD0D0D0)
private val EmptyTextGray = Color(0xFF8E8E93)
private val LinkBlue = Color(0xFF2172FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onTaskClick: (String, String) -> Unit,
    onOpenProblemStatement: () -> Unit = {},
    onNavigateToCreateTask: () -> Unit = {},
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderBlue)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(top = 32.dp, bottom = 24.dp, start = 20.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.rider_delivery_app),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.offline_first_subtitle),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                    IconButton(
                        onClick = { viewModel.handle(TasksIntent.ManualSync) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Sync,
                                contentDescription = stringResource(R.string.sync_content_description),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val homeFilters = listOf(TaskFilter.ALL, TaskFilter.ACTIVE, TaskFilter.DONE)
                val allCount = state.tasks.size
                val activeCount = state.tasks.count { TaskFilter.ACTIVE.matches(it) }
                val doneCount = state.tasks.count { TaskFilter.DONE.matches(it) }
                val counts = listOf(allCount, activeCount, doneCount)
                homeFilters.forEachIndexed { index, filter ->
                    val count = counts[index]
                    val isSelected = state.selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) when (filter) {
                                    TaskFilter.ALL -> HeaderBlue
                                    TaskFilter.ACTIVE -> ActiveGreen
                                    TaskFilter.DONE -> HeaderBlue
                                    else -> HeaderBlue
                                } else Color.White
                            )
                            .then(
                                if (!isSelected) Modifier.border(1.dp, BorderGray, RoundedCornerShape(20.dp))
                                else Modifier
                            )
                            .clickable { viewModel.handle(TasksIntent.SetFilter(filter)) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${stringResource(filter.labelResId)} ($count)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onNavigateToCreateTask,
                    modifier = Modifier
                        .size(44.dp)
                        .background(HeaderBlue, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_task),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.tasks.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = HeaderBlue
                        )
                    }
                    state.error != null -> {
                        Text(
                            text = state.error ?: stringResource(R.string.error_generic),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            color = EmptyTextGray
                        )
                    }
                    state.filteredTasks.isEmpty() -> {
                        Card(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.no_tasks_found),
                                    color = EmptyTextGray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.create_first_task),
                                    color = LinkBlue,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier.clickable(onClick = onNavigateToCreateTask)
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                state.filteredTasks,
                                key = { "${it.delivery.id}_${it.task.id}" }
                            ) { twd ->
                                HomeTaskCard(
                                    taskWithDelivery = twd,
                                    pendingSync = state.pendingSyncTaskIds.contains(twd.task.id),
                                    onClick = { onTaskClick(twd.delivery.id, twd.task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}
