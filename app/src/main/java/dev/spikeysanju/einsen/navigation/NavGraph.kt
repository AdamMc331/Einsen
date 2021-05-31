package dev.spikeysanju.einsen.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import dev.spikeysanju.einsen.view.add.AddTaskScreen
import dev.spikeysanju.einsen.view.details.TaskDetailsScreen
import dev.spikeysanju.einsen.view.home.HomeScreen
import dev.spikeysanju.einsen.view.settings.SettingsScreen
import dev.spikeysanju.einsen.view.task.AllTaskScreen
import dev.spikeysanju.einsen.view.viewmodel.MainViewModel

object EndPoints {
    const val ID = "id"
}

@ExperimentalComposeUiApi
@Composable
fun NavGraph(toggleTheme: () -> Unit) {
    val navController = rememberNavController()
    val actions = remember(navController) { MainActions(navController) }

    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            val viewModel: MainViewModel = viewModel(
                factory = HiltViewModelFactory(LocalContext.current, it)
            )
            HomeScreen(navController, viewModel, actions)
        }

        composable(Screen.AddTask.route) {
            val viewModel = hiltNavGraphViewModel<MainViewModel>(backStackEntry = it)
            AddTaskScreen(viewModel, actions)
        }

        composable(Screen.AllTask.route) {
            val viewModel = hiltNavGraphViewModel<MainViewModel>(backStackEntry = it)
            AllTaskScreen(viewModel, actions)
        }

        composable(
            "${Screen.TaskDetails.route}/{id}",
            arguments = listOf(navArgument(EndPoints.ID) { type = NavType.LongType })
        ) {
            val viewModel = hiltNavGraphViewModel<MainViewModel>(backStackEntry = it)

            val taskID = it.arguments?.getLong(EndPoints.ID)
                ?: throw IllegalStateException("'task ID' shouldn't be null")

            viewModel.findTaskByID(taskID)

            TaskDetailsScreen(viewModel, actions)
        }

        composable(Screen.Settings.route) {
            val viewModel = hiltNavGraphViewModel<MainViewModel>(backStackEntry = it)
            SettingsScreen(viewModel, actions)
        }
    }
}

class MainActions(navController: NavController) {

    val upPress: () -> Unit = {
        navController.navigateUp()
    }

    val gotoAddTask: () -> Unit = {
        navController.navigate(Screen.AddTask.route)
    }

    val gotoAllTask: () -> Unit = {
        navController.navigate(Screen.AllTask.route)
    }

    val gotoTaskDetails: (id: Long) -> Unit = { id ->
        navController.navigate("${Screen.TaskDetails.route}/$id")
    }

    val gotoSettings: () -> Unit = {
        navController.navigate(Screen.Settings.route)
    }
}