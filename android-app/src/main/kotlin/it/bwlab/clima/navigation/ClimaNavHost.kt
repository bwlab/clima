package it.bwlab.clima.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import it.bwlab.clima.AppViewModel
import it.bwlab.clima.ui.control.ControlScreen
import it.bwlab.clima.ui.devices.DevicesScreen
import it.bwlab.clima.ui.settings.SettingsScreen

private const val CONTROL_PATTERN = "control/{deviceId}"
private const val DEVICES = "devices"
private const val SETTINGS = "settings"

private fun controlRoute(deviceId: String) = "control/$deviceId"

/**
 * Navigazione delle 3 schermate.
 * Al primo avvio senza dispositivi salvati si apre direttamente Climatizzatori.
 */
@Composable
fun ClimaNavHost(viewModel: AppViewModel) {
    val nav = rememberNavController()
    val savedNow = viewModel.savedDevices.value
    val start = if (savedNow.isNotEmpty()) controlRoute(savedNow.first().id) else DEVICES

    NavHost(
        navController = nav,
        startDestination = start
    ) {
        composable(
            CONTROL_PATTERN,
            arguments = listOf(
                navArgument("deviceId") { type = NavType.StringType }
            )
        ) { entry ->
            val deviceId = entry.arguments?.getString("deviceId") ?: return@composable
            ControlScreen(
                deviceId = deviceId,
                app = viewModel,
                onBack = { nav.popBackStack() },
                onDevices = {
                    nav.navigate(DEVICES) {
                        popUpTo(CONTROL_PATTERN)
                    }
                },
                onSettings = { nav.navigate(SETTINGS) }
            )
        }
        composable(DEVICES) {
            DevicesScreen(
                app = viewModel,
                onBack = { nav.popBackStack() },
                onOpen = { id ->
                    nav.navigate(controlRoute(id)) {
                        popUpTo(DEVICES)
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(SETTINGS) {
            SettingsScreen(
                app = viewModel,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
