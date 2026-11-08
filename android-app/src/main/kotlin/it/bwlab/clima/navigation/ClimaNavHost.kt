package it.bwlab.clima.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
 *
 * startDestination è sempre Devices: se c'è un'unità salvata si naviga subito
 * a Control. Evita uno startDestination dinamico `control/{id}` che, in caso di
 * errore nel ViewModel, lasciava solo lo Surface di sfondo (schermo bianco).
 */
@Composable
fun ClimaNavHost(viewModel: AppViewModel) {
    val nav = rememberNavController()
    val saved by viewModel.savedDevices.collectAsState()

    LaunchedEffect(saved) {
        if (nav.currentDestination?.route == DEVICES) {
            val id = viewModel.initialDeviceId
            if (id != null && saved.any { it.id == id }) {
                nav.navigate(controlRoute(id)) {
                    popUpTo(DEVICES) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = nav,
        startDestination = DEVICES,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(
            CONTROL_PATTERN,
            arguments = listOf(
                navArgument("deviceId") { type = NavType.StringType }
            )
        ) { entry ->
            val deviceId = entry.arguments?.getString("deviceId") ?: return@composable
            if (saved.none { it.id == deviceId }) {
                LaunchedEffect(deviceId) {
                    nav.navigate(DEVICES) {
                        popUpTo(nav.graph.id) { inclusive = true }
                    }
                }
                return@composable
            }
            ControlScreen(
                deviceId = deviceId,
                app = viewModel,
                onBack = {
                    nav.navigate(DEVICES) {
                        popUpTo(nav.graph.id) { inclusive = true }
                    }
                },
                onDevices = {
                    nav.navigate(DEVICES) {
                        launchSingleTop = true
                    }
                },
                onSettings = { nav.navigate(SETTINGS) }
            )
        }
        composable(DEVICES) {
            DevicesScreen(
                app = viewModel,
                onBack = { /* root */ },
                onOpen = { id ->
                    nav.navigate(controlRoute(id)) {
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
