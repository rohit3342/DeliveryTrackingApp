package com.korbit.deliverytrackingapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.korbit.deliverytrackingapp.presentation.delivery.DeliveryScreen
import com.korbit.deliverytrackingapp.presentation.task.TaskDetailScreen

const val DELIVERY_LIST_ROUTE = "deliveries"
const val TASK_DETAIL_ROUTE = "delivery/{deliveryId}"
const val DELIVERY_ID_ARG = "deliveryId"

fun taskDetailRoute(deliveryId: String) = "delivery/$deliveryId"

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = DELIVERY_LIST_ROUTE
    ) {
        composable(DELIVERY_LIST_ROUTE) {
            DeliveryScreen(
                onDeliveryClick = { id ->
                    navController.navigate(taskDetailRoute(id))
                }
            )
        }
        composable(
            route = TASK_DETAIL_ROUTE,
            arguments = listOf(navArgument(DELIVERY_ID_ARG) { defaultValue = "" })
        ) {
            TaskDetailScreen()
        }
    }
}
