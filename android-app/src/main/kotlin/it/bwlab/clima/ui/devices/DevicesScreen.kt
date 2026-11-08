@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.bwlab.clima.ui.devices

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.bwlab.clima.AppViewModel
import it.bwlab.clima.R
import it.bwlab.clima.data.SavedDevice
import it.bwlab.clima.ui.components.ErrorToast
import it.bwlab.clima.ui.components.RenameDialog
import it.bwlab.clima.ui.control.SectionTitle
import it.bwlab.gree.core.DeviceInfo

private fun isValidIp(ip: String): Boolean {
    val parts = ip.trim().split(".")
    if (parts.size != 4) return false
    return parts.all { p ->
        val n = p.toIntOrNull() ?: return false
        n in 0..255 && p.length <= 3 && !(p.length > 1 && p.startsWith("0"))
    }
}

/** Schermata Climatizzatori (mockup 04). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DevicesScreen(
    app: AppViewModel,
    onBack: () -> Unit,
    onOpen: (String) -> Unit
) {
    val saved by app.savedDevices.collectAsState()
    val found by app.foundDevices.collectAsState()
    val unreachable by app.unreachable.collectAsState()
    val scanMessage by app.scanMessage.collectAsState()
    val deviceStates by app.deviceStates.collectAsState()
    val scanBusy by app.scanning.collectAsState()
    val usesF by app.prefs.usesFahrenheit.collectAsState(initial = false)

    var menuDevice by remember { mutableStateOf<SavedDevice?>(null) }
    var renameDevice by remember { mutableStateOf<SavedDevice?>(null) }
    var deleteDevice by remember { mutableStateOf<SavedDevice?>(null) }
    var manualIp by remember { mutableStateOf("") }
    var manualError by remember { mutableStateOf(false) }
    var manualPendingIp by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(saved.isEmpty()) {
        if (saved.isEmpty()) app.scan()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.screen_devices),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = app::scan, enabled = !scanBusy) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh_scan)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (saved.isNotEmpty()) {
                    item {
                        SectionTitle(stringResource(R.string.section_saved))
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(Modifier.padding(vertical = 4.dp)) {
                                saved.forEachIndexed { index, device ->
                                    SavedRow(
                                        device = device,
                                        state = deviceStates[device.mac],
                                        unreachable = unreachable.contains(device.mac),
                                        usesFahrenheit = usesF,
                                        onClick = { onOpen(device.id) },
                                        onLongClick = {
                                            menuDevice = device
                                        }
                                    )
                                    if (index < saved.size - 1) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp)
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.outline)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (found.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        SectionTitle(stringResource(R.string.section_found))
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column {
                                found.forEachIndexed { index, info ->
                                    FoundRow(
                                        info = info,
                                        onAdd = { app.addDevice(info) {} }
                                    )
                                    if (index < found.size - 1) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp)
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.outline)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (saved.isEmpty() && found.isEmpty() && !scanBusy) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(
                                    when (scanMessage) {
                                        "scan_empty" -> R.string.scan_empty
                                        else -> R.string.devices_empty
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Aggiungi manualmente (campo IP con validazione)
            SectionTitle(stringResource(R.string.action_add_manual))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = manualIp,
                onValueChange = {
                    manualIp = it.trim()
                    manualError = false
                },
                label = { Text(stringResource(R.string.field_ip)) },
                placeholder = { Text(stringResource(R.string.field_ip_hint)) },
                isError = manualError,
                supportingText = {
                    if (manualError) {
                        Text(stringResource(R.string.invalid_ip))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    if (isValidIp(manualIp)) {
                        manualPendingIp = manualIp
                        manualIp = ""
                    } else {
                        manualError = true
                    }
                },
                enabled = manualIp.isNotBlank(),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_add_manual))
            }

            Spacer(Modifier.height(10.dp))

            // Nota informativa sulla VPN
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(14.dp)) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.note_vpn),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Pulsante principale "Cerca in rete"
            Button(
                onClick = app::scan,
                enabled = !scanBusy,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (scanBusy) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.scan_running))
                } else {
                    Icon(
                        Icons.Filled.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_scan))
                }
            }

            if (scanMessage != null && scanMessage in listOf("scan_empty", "scan_failed", "bind_failed")) {
                val msg = stringResource(
                    when (scanMessage) {
                        "scan_failed" -> R.string.scan_failed
                        "bind_failed" -> R.string.bind_failed
                        else -> R.string.scan_empty
                    }
                )
                LaunchedEffect(scanMessage) {
                    kotlinx.coroutines.delay(2500)
                    app.scanMessage.value = null
                }
                ErrorToast(message = msg)
            }
        }
    }

    // Long-press: menu rinomina / elimina
    menuDevice?.let { dev ->
        AlertDialog(
            onDismissRequest = { menuDevice = null },
            title = { Text(dev.name) },
            text = {},
            confirmButton = {},
            dismissButton = {
                Column {
                    TextButton(
                        onClick = {
                            menuDevice = null
                            renameDevice = dev
                        }
                    ) { Text(stringResource(R.string.device_rename)) }
                    TextButton(
                        onClick = {
                            menuDevice = null
                            deleteDevice = dev
                        }
                    ) { Text(stringResource(R.string.device_delete)) }
                }
            }
        )
    }
    renameDevice?.let { dev ->
        RenameDialog(
            initial = dev.name,
            onDismiss = { renameDevice = null },
            onConfirm = { newName ->
                app.rename(dev, newName)
                renameDevice = null
            }
        )
    }
    deleteDevice?.let { dev ->
        AlertDialog(
            onDismissRequest = { deleteDevice = null },
            title = { Text(stringResource(R.string.device_delete)) },
            text = { Text(dev.name) },
            confirmButton = {
                TextButton(onClick = {
                    app.delete(dev)
                    deleteDevice = null
                }) { Text(stringResource(R.string.device_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteDevice = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Inserimento manuale: nome opzionale prima di bind + salvataggio
    manualPendingIp?.let { ip ->
        RenameDialog(
            initial = "",
            onDismiss = { manualPendingIp = null },
            onConfirm = { name ->
                app.addDeviceByIp(ip, name, onDone = { id ->
                    manualPendingIp = null
                    onOpen(id)
                })
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedRow(
    device: SavedDevice,
    state: it.bwlab.gree.core.ClimateState?,
    unreachable: Boolean,
    usesFahrenheit: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.SettingsRemote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when {
                    unreachable -> stringResource(R.string.device_unreachable)
                    state == null -> device.ip
                    state.pow -> stringResource(R.string.device_state_on, device.ip) +
                        " · " + it.bwlab.clima.util.Formatters.temperatureWithUnit(
                            state.setTemCelsius(), usesFahrenheit
                        )
                    else -> stringResource(R.string.device_state_off, device.ip) +
                        " · " + it.bwlab.clima.util.Formatters.temperatureWithUnit(
                            state.setTemCelsius(), usesFahrenheit
                        )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            Modifier
                .size(9.dp)
                .background(
                    when {
                        unreachable -> Color(0xFFE5484D)
                        state != null -> Color(0xFF2E9E5B)
                        else -> Color(0xFF9AA3B2)
                    },
                    CircleShape
                )
        )
    }
}

@Composable
private fun FoundRow(info: DeviceInfo, onAdd: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Wifi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "Gree " + info.mac.takeLast(8).lowercase(),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = info.ip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(
            onClick = onAdd,
            shape = RoundedCornerShape(50),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 4.dp
            ),
            modifier = Modifier.height(36.dp)
        ) {
            Text(stringResource(R.string.action_add))
        }
    }
}
