package com.korbit.deliverytrackingapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.korbit.deliverytrackingapp.presentation.task.TaskDetailScreen
import com.korbit.deliverytrackingapp.presentation.tasks.TasksScreen

const val TASKS_LIST_ROUTE = "tasks"
const val TASK_DETAIL_ROUTE = "delivery/{deliveryId}"
const val DELIVERY_ID_ARG = "deliveryId"

fun taskDetailRoute(deliveryId: String) = "delivery/$deliveryId"

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TASKS_LIST_ROUTE
    ) {
        composable(TASKS_LIST_ROUTE) {
            TasksScreen(
                onTaskClick = { deliveryId, _ ->
                    navController.navigate(taskDetailRoute(deliveryId))
                }
            )
        }
        composable(
            route = TASK_DETAIL_ROUTE,
            arguments = listOf(navArgument(DELIVERY_ID_ARG) { defaultValue = "" })
        ) {
            TaskDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
