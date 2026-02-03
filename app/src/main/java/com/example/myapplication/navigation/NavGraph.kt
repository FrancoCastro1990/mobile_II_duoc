package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.DetailScreen
import com.example.myapplication.ui.screens.FormScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.viewmodel.SolicitudViewModel

object Routes {
    const val HOME = "home"
    const val FORM = "form"
    const val DETAIL = "detail"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: SolicitudViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToForm = {
                    viewModel.clearForm()
                    navController.navigate(Routes.FORM)
                },
                onNavigateToDetail = { solicitudId ->
                    navController.navigate("${Routes.DETAIL}/$solicitudId")
                }
            )
        }

        composable(
            route = "${Routes.FORM}?solicitudId={solicitudId}",
            arguments = listOf(
                navArgument("solicitudId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getLong("solicitudId") ?: -1L
            if (solicitudId != -1L) {
                viewModel.loadSolicitudForEditing(solicitudId)
            }
            FormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FORM) {
            FormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.DETAIL}/{solicitudId}",
            arguments = listOf(
                navArgument("solicitudId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getLong("solicitudId") ?: return@composable
            DetailScreen(
                solicitudId = solicitudId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("${Routes.FORM}?solicitudId=$id")
                }
            )
        }
    }
}
