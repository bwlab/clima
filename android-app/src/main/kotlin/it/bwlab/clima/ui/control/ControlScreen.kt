package it.bwlab.clima.ui.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import it.bwlab.clima.AppViewModel
import it.bwlab.clima.R
import it.bwlab.clima.data.DeviceRepository
import it.bwlab.clima.ui.components.ErrorToast
import it.bwlab.clima.ui.components.RenameDialog

/**
 * Schermata Controllo (mockup 01, 01b / 02 heat / 03 de).
 * Scorrimento: la card grande si riduce a una barra compatta fissa in alto (01b).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    deviceId: String,
    app: AppViewModel,
    onBack: () -> Unit,
    onDevices: () -> Unit,
    onSettings: () -> Unit
) {
    val vm = viewModel<ControlViewModel>(
        key = "control_$deviceId",
        initializer = { ControlViewModel(repository = app.repository, deviceId = deviceId) }
    )
    val ui by vm.ui.collectAsState()
    val autoRefresh by app.prefs.autoRefresh.collectAsState(initial = true)
    val usesF by app.prefs.usesFahrenheit.collectAsState(initial = false)
    val timers by app.timers.collectAsState(emptyList())
    val context = LocalContext.current

    // Polling dello stato ogni 5 s solo con l'app in primo piano
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, autoRefresh) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                vm.refresh()
                vm.setVpn(DeviceRepository.isVpnActive(context))
                if (autoRefresh) vm.startPolling(true)
            } else if (event == Lifecycle.Event.ON_STOP) {
                vm.stopPolling()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            vm.stopPolling()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val listState = rememberLazyListState()
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex >= 1 } }

    var showRename by remember { mutableStateOf(false) }
    // Se il nome è ancora quello tecnico (MAC), invita subito a scegliere un nome umano.
    LaunchedEffect(ui.device.id, ui.device.name) {
        val n = ui.device.name.trim()
        val technical = n.isEmpty() ||
            n.matches(Regex("(?i)^gree\\s*[0-9a-f:-]{4,}$")) ||
            n.matches(Regex("(?i)^[0-9a-f]{6,}$"))
        if (technical) showRename = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (collapsed) {
                CompactTopBar(
                    state = ui.state,
                    usesFahrenheit = usesF,
                    onPower = { vm.power(it) },
                    onBack = onBack,
                    onDevices = onDevices,
                    onSettings = onSettings
                )
            } else {
                ControlTopBar(
                    title = ui.device.name,
                    onTitleClick = { showRename = true },
                    onBack = onBack,
                    onDevices = onDevices,
                    onSettings = onSettings
                )
            }
        }
    ) { pad ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = ui.connecting),
            onRefresh = { vm.refresh() },
            modifier = Modifier.padding(pad)
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { StatusChips(connected = ui.reachable, connecting = ui.connecting, vpn = ui.vpn) }
                item { MainCard(ui = ui, vm = vm, usesFahrenheit = usesF) }
                item {
                    ModeGrid(
                        current = ui.state?.mode,
                        dimmed = ui.state?.pow == false,
                        onSelect = vm::setMode
                    )
                }
                item {
                    FanSelector(
                        current = ui.state?.wdSpd,
                        dimmed = ui.state?.pow == false,
                        onSelect = vm::setFan
                    )
                }
                item { FeatureChips(ui = ui, vm = vm) }
                item { AirflowSection(ui = ui, vm = vm) }
                item { TimerSection(device = ui.device, timers = timers, app = app) }
                item {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text(
                            text = it.bwlab.clima.util.Formatters.updatedAgo(ui.lastUpdated),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (ui.errorKey != null) {
                val msg = androidx.compose.ui.res.stringResource(R.string.command_failed)
                LaunchedEffect(ui.errorKey) {
                    kotlinx.coroutines.delay(2500)
                    vm.clearError()
                }
                ErrorToast(message = msg)
            }
        }
    }

    if (showRename) {
        RenameDialog(
            initial = ui.device.name,
            onDismiss = { showRename = false },
            onConfirm = { newName ->
                app.rename(ui.device, newName)
                showRename = false
            }
        )
    }
}
