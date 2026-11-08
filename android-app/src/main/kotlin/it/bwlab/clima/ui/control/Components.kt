@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.bwlab.clima.ui.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Toys
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import it.bwlab.clima.AppViewModel
import it.bwlab.clima.R
import it.bwlab.clima.data.AppTimer
import it.bwlab.clima.data.SavedDevice
import it.bwlab.clima.ui.theme.CoolGradEnd
import it.bwlab.clima.ui.theme.CoolGradStart
import it.bwlab.clima.ui.theme.HeatGradEnd
import it.bwlab.clima.ui.theme.HeatGradStart
import it.bwlab.clima.ui.theme.OffGradEnd
import it.bwlab.clima.ui.theme.OffGradStart
import it.bwlab.clima.util.Formatters
import it.bwlab.gree.core.ClimateState
import it.bwlab.gree.core.FanSpeed
import it.bwlab.gree.core.Mode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// Elementi schermata Controllo (mockup 01/01b/02/03)
// ---------------------------------------------------------------------------

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

// ------------------------------- Top bar -----------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlTopBar(
    title: String,
    onTitleClick: () -> Unit,
    onBack: () -> Unit,
    onDevices: () -> Unit,
    onSettings: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
        },
        title = {
            // Tap sul nome = rinomina
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTitleClick)
            )
        },
        actions = {
            IconButton(onClick = onDevices) {
                Icon(
                    Icons.Filled.SettingsRemote,
                    contentDescription = stringResource(R.string.cd_devices)
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_settings)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTopBar(
    state: ClimateState?,
    usesFahrenheit: Boolean,
    onPower: (Boolean) -> Unit,
    onBack: () -> Unit,
    onDevices: () -> Unit,
    onSettings: () -> Unit
) {
    val gradient = modeGradient(state?.mode, state?.pow ?: true)
    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onDevices, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.SettingsRemote,
                    contentDescription = stringResource(R.string.cd_devices),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(brush = gradient, shape = RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = compactTemp(state?.setTemCelsius(), usesFahrenheit),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = compactMode(state?.mode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                PowerPillButton(
                    on = state?.pow == true,
                    onPower = onPower,
                    contentColor = gradient
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun compactTemp(tempC: Int?, usesFahrenheit: Boolean): String =
    tempC?.let { Formatters.temperatureWithUnit(it, usesFahrenheit) } ?: "—"

@Composable
private fun compactMode(mode: Mode?): String = when (mode) {
    Mode.COOL -> stringResource(R.string.mode_cool)
    Mode.DRY -> stringResource(R.string.mode_dry)
    Mode.FAN -> stringResource(R.string.mode_fan)
    Mode.HEAT -> stringResource(R.string.mode_heat)
    else -> stringResource(R.string.mode_auto)
}

// ------------------------------- Card --------------------------------------

@Composable
fun modeGradient(mode: Mode?, powered: Boolean): Brush = when {
    !powered -> Brush.verticalGradient(listOf(OffGradStart, OffGradEnd))
    mode == Mode.HEAT -> Brush.verticalGradient(listOf(HeatGradStart, HeatGradEnd))
    else -> Brush.verticalGradient(listOf(CoolGradStart, CoolGradEnd))
}

@Composable
fun MainCard(
    ui: ControlViewModel.UiState,
    vm: ControlViewModel,
    usesFahrenheit: Boolean
) {
    val state = ui.state
    val powered = state?.pow ?: false
    val gradient = modeGradient(state?.mode, powered)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(brush = gradient)
                .padding(20.dp)
        ) {
            // Quadrante circolare trascinabile
            TemperatureDial(
                tempC = ui.displayedTemp,
                roomC = state?.temSenCorrected(),
                usesFahrenheit = usesFahrenheit,
                powered = powered,
                connecting = ui.connecting,
                onDial = { vm.dialTemperature(it) },
                onDecrease = { vm.changeTemperature(-1) },
                onIncrease = { vm.changeTemperature(1) }
            )

            Spacer(Modifier.height(16.dp))

            // Riepilogo "Modalità · Ventola"
            Text(
                text = summaryLine(state),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            // Pulsante Accendi/Spegni unico
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                PowerPillButton(
                    on = powered,
                    onPower = vm::power,
                    contentColor = if (powered) gradient else null,
                    accent = powered
                )
            }
        }
    }
}

@Composable
private fun summaryLine(state: ClimateState?): String {
    val mode = when (state?.mode) {
        Mode.AUTO -> stringResource(R.string.mode_auto)
        Mode.COOL -> stringResource(R.string.mode_cool)
        Mode.DRY -> stringResource(R.string.mode_dry)
        Mode.FAN -> stringResource(R.string.mode_fan)
        Mode.HEAT -> stringResource(R.string.mode_heat)
        null -> "—"
    }
    val fan = if (state?.wdSpd == FanSpeed.AUTO) {
        stringResource(R.string.fan_auto)
    } else {
        stringResource(
            when (state?.wdSpd) {
                FanSpeed.LOW -> R.string.fan_level_1
                FanSpeed.MED_LOW -> R.string.fan_level_2
                FanSpeed.MED -> R.string.fan_level_3
                FanSpeed.MED_HIGH -> R.string.fan_level_4
                FanSpeed.HIGH -> R.string.fan_level_5
                else -> R.string.fan_auto
            }
        )
    }
    return "$mode · ${stringResource(R.string.section_fan)} $fan"
}

/** Pulsante bianco a pillola "Acceso/Spento" con icona power. */
@Composable
fun PowerPillButton(
    on: Boolean,
    onPower: (Boolean) -> Unit,
    contentColor: Brush? = null,
    accent: Boolean = false
) {
    val target = !on
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White,
        contentColor = Color(0xFF1F4FA0),
        modifier = Modifier.clickable(onClick = { onPower(target) })
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Icon(
                Icons.Filled.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (on) stringResource(R.string.action_power_on)
                else stringResource(R.string.action_power_off),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ------------------------------- Quadrante ----------------------------------

private const val DIAL_START = 225f          // gradi dell'architettura: 270° arc 16..30
private const val DIAL_SWEEP = 270f
private const val TEMP_MIN = 16
private const val TEMP_MAX = 30

/**
 * Quadrante 270°, 16–30 °C, tacche ogni grado, trascinabile.
 * Supporto TalkBack: description "Temperatura impostata X gradi".
 */
@Composable
fun TemperatureDial(
    tempC: Int?,
    roomC: Int?,
    usesFahrenheit: Boolean,
    powered: Boolean,
    connecting: Boolean,
    onDial: (Int) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val displayedValue = tempC?.let { Formatters.temperature(it, usesFahrenheit) } ?: "—"
    val contentTemp = stringResource(R.string.a11y_temperature, displayedValue)
    val decreaseLabel = stringResource(R.string.cd_temperature_decrease)
    val increaseLabel = stringResource(R.string.cd_temperature_increase)
    val controlsEnabled = powered && !connecting && tempC != null
    val dim = (if (powered) 1f else 0.35f) * (if (connecting) 0.6f else 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(dim)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            // Pulsanti − / + ai lati del quadrante (mockup 01)
            IconButton(
                onClick = onDecrease,
                enabled = controlsEnabled,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = decreaseLabel,
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onIncrease,
                enabled = controlsEnabled,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = increaseLabel,
                    tint = Color.White
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 52.dp)
                    .pointerInput(controlsEnabled) {
                        if (controlsEnabled) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val pos = change.position
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val angle = angleFrom(center, pos)
                                val t = TEMP_MIN + ((angle - DIAL_START + 360f) % 360f) /
                                    DIAL_SWEEP * (TEMP_MAX - TEMP_MIN)
                                onDial(t.toInt().coerceIn(TEMP_MIN, TEMP_MAX))
                            }
                        }
                    }
                    .semantics {
                        contentDescription = contentTemp
                        customActions = listOf(
                            CustomAccessibilityAction(decreaseLabel) {
                                if (controlsEnabled) onDecrease()
                                controlsEnabled
                            },
                            CustomAccessibilityAction(increaseLabel) {
                                if (controlsEnabled) onIncrease()
                                controlsEnabled
                            }
                        )
                    }
            ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 26.dp.toPx()
                val padding = stroke / 2 + 4.dp.toPx()
                val radius = (size.minDimension - padding * 2) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // arco di fondo
                drawArc(
                    color = Color.White.copy(alpha = 0.24f),
                    startAngle = DIAL_START,
                    sweepAngle = DIAL_SWEEP,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // arco riempito fino alla temperatura
                val temp = tempC ?: TEMP_MIN
                val sweep =
                    (temp - TEMP_MIN) * 1f / (TEMP_MAX - TEMP_MIN) * DIAL_SWEEP
                drawArc(
                    color = Color.White,
                    startAngle = DIAL_START,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // tacche ogni grado
                for (deg in TEMP_MIN..TEMP_MAX) {
                    val t = deg - TEMP_MIN
                    val a = Math.toRadians(
                        (DIAL_START + t * (DIAL_SWEEP / (TEMP_MAX - TEMP_MIN))).toDouble()
                    )
                    val tickInner = radius - padding - 8.dp.toPx()
                    val tickOuter = radius - padding - 2.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(
                            center.x + tickInner * cos(a).toFloat(),
                            center.y + tickInner * sin(a).toFloat()
                        ),
                        end = Offset(
                            center.x + tickOuter * cos(a).toFloat(),
                            center.y + tickOuter * sin(a).toFloat()
                        ),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                // pomello
                val a = Math.toRadians(
                    (DIAL_START + sweep).toDouble()
                )
                drawCircle(
                    color = Color.White,
                    radius = 10.dp.toPx(),
                    center = Offset(
                        center.x + (radius - padding) * cos(a).toFloat(),
                        center.y + (radius - padding) * sin(a).toFloat()
                    )
                )
            }

            // testo centrale
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.label_target),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = displayedValue,
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(
                            if (usesFahrenheit) R.string.unit_fahrenheit else R.string.unit_celsius
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 18.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Pill "Ambiente xx °C" (dal sensore, TemSen)
                if (roomC != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.WbSunny,
                                contentDescription = stringResource(
                                    R.string.a11y_room_temperature,
                                    Formatters.temperature(roomC, usesFahrenheit)
                                ),
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.label_room) + " " +
                                    Formatters.temperatureWithUnit(roomC, usesFahrenheit),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            } // Box dial canvas+testo
        } // Box dial + −/+

        // scala min/max
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = Formatters.temperature(TEMP_MIN, usesFahrenheit),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = Formatters.temperature(TEMP_MAX, usesFahrenheit),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/** Angolo (in gradi, sistema drawArc) dal centro verso il punto. */
private fun angleFrom(center: Offset, point: Offset): Float {
    val dx = point.x - center.x
    val dy = point.y - center.y
    val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
    return ((deg + 90f + 360f) % 360f).toFloat()
}

// ------------------------------- Chips di stato ------------------------------

@Composable
fun StatusChips(connected: Boolean, connecting: Boolean, vpn: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ChipDot(
            when {
                connecting -> stringResource(R.string.status_connecting)
                connected -> stringResource(R.string.status_connected)
                else -> stringResource(R.string.status_unreachable)
            },
            color = when {
                connecting -> Color(0xFF9AA3B2)
                connected -> Color(0xFF2E9E5B)
                else -> Color(0xFFE5484D)
            }
        )
        ChipDot(
            if (vpn) stringResource(R.string.network_vpn)
            else stringResource(R.string.network_home_wifi),
            color = Color(0xFF9AA3B2)
        )
    }
}

@Composable
private fun ChipDot(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ------------------------------- Modalità -----------------------------------

private val modeIcons: Map<Mode, ImageVector> = mapOf(
    Mode.AUTO to Icons.Outlined.Tune,
    Mode.COOL to Icons.Outlined.AcUnit,
    Mode.DRY to Icons.Outlined.Water,
    Mode.FAN to Icons.Outlined.Toys,
    Mode.HEAT to Icons.Outlined.WbSunny
)

@Composable
fun ModeGrid(current: Mode?, dimmed: Boolean, onSelect: (Mode) -> Unit) {
    val grid = listOf(
        Mode.AUTO to R.string.mode_auto,
        Mode.COOL to R.string.mode_cool,
        Mode.DRY to R.string.mode_dry,
        Mode.FAN to R.string.mode_fan,
        Mode.HEAT to R.string.mode_heat
    )
    val accentOwner = current ?: Mode.AUTO
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.72f else 1f)
    ) {
        grid.forEachIndexed { index, (mode, labelRes) ->
            val selected = mode == accentOwner
            val tileColor = if (selected) {
                if (mode == Mode.HEAT) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val tileContent = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = tileColor,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(mode) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .semantics {
                            contentDescription = "" // volutamente: a11y dal testo del riquadro
                        }
                ) {
                    Icon(
                        imageVector = modeIcons.getValue(mode),
                        contentDescription = null,
                        tint = tileContent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = tileContent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ------------------------------- Ventola ------------------------------------

@Composable
fun FanSelector(current: FanSpeed?, dimmed: Boolean, onSelect: (FanSpeed) -> Unit) {
    val cur = current ?: FanSpeed.AUTO
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.72f else 1f)
    ) {
        // tile "Auto"
        SegmentTile(
            label = stringResource(R.string.fan_auto),
            selected = cur == FanSpeed.AUTO,
            onClick = { onSelect(FanSpeed.AUTO) },
            modifier = Modifier.weight(1f)
        )
        val speeds = listOf(
            FanSpeed.LOW to 1, FanSpeed.MED_LOW to 2, FanSpeed.MED to 3,
            FanSpeed.MED_HIGH to 4, FanSpeed.HIGH to 5
        )
        speeds.forEach { (speed, bars) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (cur == speed) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clickable { onSelect(speed) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        repeat(bars) { level ->
                            val w = 4.dp * (level + 1)
                            Box(
                                Modifier
                                    .padding(top = 3.dp)
                                    .size(width = w, height = 4.dp)
                                    .background(
                                        if (cur == speed) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bars: Int = 0
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.aspectRatio(1f).clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------------- Funzioni -----------------------------------

private data class FeatureSpec(
    val key: String,
    val labelRes: Int,
    val icon: ImageVector?
) {
    fun isOn(state: ClimateState?): Boolean = when (key) {
        "Tur" -> state?.tur == 1
        "Quiet" -> state?.quiet == 1
        "SwhSlp" -> state?.swhSlp == 1
        "Lig" -> state?.lig == 1
        "Health" -> state?.health == 1
        "Air" -> state?.air == 1
        "Blo" -> state?.blo == 1
        else -> false
    }

    fun isSupported(state: ClimateState?): Boolean {
        if (state == null) return false
        return when (key) {
            "Blo" -> "Blo" in state.supported && (state.mode == Mode.COOL || state.mode == Mode.DRY)
            else -> key in state.supported
        }
    }
}

@Composable
fun FeatureChips(ui: ControlViewModel.UiState, vm: ControlViewModel) {
    val features = listOf(
        FeatureSpec("Tur", R.string.feature_turbo, Icons.Outlined.Bolt),
        FeatureSpec("Quiet", R.string.feature_quiet, Icons.Outlined.VolumeOff),
        FeatureSpec("SwhSlp", R.string.feature_sleep, Icons.Outlined.Bedtime),
        FeatureSpec("Lig", R.string.feature_light, Icons.Outlined.Lightbulb),
        FeatureSpec("Health", R.string.feature_health, Icons.Outlined.Spa),
        FeatureSpec("Air", R.string.feature_fresh_air, Icons.Outlined.Cottage),
        FeatureSpec("Blo", R.string.feature_x_fan, null)
    )
    val visible = features.filter { it.isSupported(ui.state) }
    if (visible.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (chunk in visible.chunked(3)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (ui.state?.pow == false) 0.72f else 1f)
            ) {
                chunk.forEach { f ->
                    val accent = if (ui.state?.mode == Mode.HEAT) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                    FilterChip(
                        selected = f.isOn(ui.state),
                        onClick = { vm.toggleFeature(f.key, !f.isOn(ui.state)) },
                        leadingIcon = f.icon?.let {
                            { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        },
                        label = {
                            Text(
                                text = stringResource(f.labelRes),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedContainerColor = accent,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            iconColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ------------------------------- Flusso d'aria -------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirflowSection(ui: ControlViewModel.UiState, vm: ControlViewModel) {
    var sheetVertical by remember { mutableStateOf(false) }
    var sheetHorizontal by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.section_airflow))
        AirflowRow(
            icon = Icons.Outlined.SwapVert,
            title = stringResource(R.string.airflow_vertical),
            subtitle = louvreLabel(horizontal = false, ui.state),
            onClick = { sheetVertical = true }
        )
        AirflowRow(
            icon = Icons.Outlined.SwapHoriz,
            title = stringResource(R.string.airflow_horizontal),
            subtitle = louvreLabel(horizontal = true, ui.state),
            onClick = { sheetHorizontal = true }
        )
    }

    if (sheetVertical) {
        LouvreSheet(
            horizontal = false,
            current = ui.state?.swUpDn ?: 0,
            onPick = {
                vm.setLouvre(false, it)
                sheetVertical = false
            },
            onDismiss = { sheetVertical = false }
        )
    }
    if (sheetHorizontal) {
        LouvreSheet(
            horizontal = true,
            current = ui.state?.swingLfRig ?: 0,
            onPick = {
                vm.setLouvre(true, it)
                sheetHorizontal = false
            },
            onDismiss = { sheetHorizontal = false }
        )
    }
}

@Composable
private fun louvreLabel(horizontal: Boolean, state: ClimateState?): String {
    val code = if (horizontal) state?.swingLfRig ?: 0 else state?.swUpDn ?: 0
    return when {
        code == 1 -> stringResource(R.string.louvre_swing_full)
        code in 2..6 -> stringResource(R.string.louvre_position, code - 1)
        !horizontal && code in 7..11 -> stringResource(R.string.louvre_swing_partial, code - 6)
        else -> stringResource(R.string.louvre_fixed)
    }
}

@Composable
private fun AirflowRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LouvreSheet(
    horizontal: Boolean,
    current: Int,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = if (horizontal) stringResource(R.string.airflow_horizontal)
                else stringResource(R.string.airflow_vertical),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(10.dp))
            // Fisse (posizione attuale)
            OptionRow(
                label = stringResource(R.string.louvre_fixed),
                selected = current == 0,
                onClick = { onPick(0) }
            )
            // Oscillazione completa
            OptionRow(
                label = stringResource(R.string.louvre_swing_full),
                selected = current == 1,
                onClick = { onPick(1) }
            )
            // Posizioni fisse 1–5
            for (pos in 1..5) {
                val code = pos + 1
                OptionRow(
                    label = stringResource(R.string.louvre_position, pos),
                    selected = current == code,
                    onClick = { onPick(code) }
                )
            }
            // oscillazioni parziali (solo alette verticali)
            if (!horizontal) {
                for (i in 1..5) {
                    val code = i + 6
                    OptionRow(
                        label = stringResource(R.string.louvre_swing_partial, i),
                        selected = current == code,
                        onClick = { onPick(code) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Box(
            Modifier
                .size(20.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                )
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}


// ------------------------------- Timer ---------------------------------------

@Composable
private fun localFormatTime(hour: Int, minute: Int): String {
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).run {
        java.util.Calendar.getInstance().let { cal ->
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
            cal.set(java.util.Calendar.MINUTE, minute)
            format(cal.time)
        }
    }
}

@Composable
fun TimerSection(
    device: SavedDevice,
    timers: List<AppTimer>,
    app: AppViewModel
) {
    var creating by remember { mutableStateOf<String?>(null) } // "on" / "off"

    val deviceTimers = timers.filter { it.deviceId == device.id }
    val onTimer = deviceTimers.firstOrNull { it.turnOn }
    val offTimer = deviceTimers.firstOrNull { !it.turnOn }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.section_timer))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                TimerRow(
                    title = stringResource(R.string.timer_on),
                    timer = onTimer,
                    onDelete = { onTimer?.let { app.deleteTimer(it) } },
                    onEditRequest = { creating = "on" }
                )
                TimerRowDivider()
                TimerRow(
                    title = stringResource(R.string.timer_off),
                    timer = offTimer,
                    onDelete = { offTimer?.let { app.deleteTimer(it) } },
                    onEditRequest = { creating = "off" }
                )
            }
        }

        TextButton(onClick = { creating = "on" }) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.timer_add))
        }

        Text(
            text = stringResource(R.string.timer_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (creating != null) {
        val turnOn = creating == "on"
        val existing = if (turnOn) onTimer else offTimer
        TimerEditorDialog(
            isOnEditor = turnOn,
            initialHour = existing?.hour ?: java.util.Calendar.getInstance()
                .get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = existing?.minute ?: java.util.Calendar.getInstance()
                .get(java.util.Calendar.MINUTE),
            initialOnce = existing?.once ?: true,
            onDismiss = { creating = null },
            onSave = { hour, minute, once ->
                existing?.let { app.deleteTimer(it) }
                app.saveTimer(
                    AppTimer(
                        id = java.util.UUID.randomUUID().toString(),
                        deviceId = device.id,
                        turnOn = turnOn,
                        hour = hour,
                        minute = minute,
                        once = once
                    )
                )
                creating = null
            }
        )
    }
}

@Composable
private fun TimerRowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun TimerRow(
    title: String,
    timer: AppTimer?,
    onEditRequest: () -> Unit,
    onDelete: () -> Unit
) {
    var checked by remember(timer) { mutableStateOf(timer != null) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    timer == null -> stringResource(R.string.timer_not_set)
                    timer.once -> stringResource(
                        R.string.timer_once_at,
                        localFormatTime(timer.hour, timer.minute)
                    )
                    else -> stringResource(
                        R.string.timer_daily_at,
                        localFormatTime(timer.hour, timer.minute)
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (timer != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.timer_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { on ->
                checked = on
                when {
                    on && timer == null -> onEditRequest()
                    on && timer != null -> { /* già attivo */ }
                    else -> onDelete()
                }
            },
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

/** Finestra di modifica orario/ripetizione del timer. */
@Composable
fun TimerEditorDialog(
    initialHour: Int,
    initialMinute: Int,
    initialOnce: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Boolean) -> Unit,
    isOnEditor: Boolean = false
) {
    val timeState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    var once by remember { mutableStateOf(initialOnce) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (isOnEditor) stringResource(R.string.timer_on)
                    else stringResource(R.string.timer_off),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(12.dp))
                TimePicker(state = timeState)

                // Ripetizione: una volta / ogni giorno
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = once,
                        onClick = { once = true },
                        label = { Text(stringResource(R.string.timer_once)) }
                    )
                    FilterChip(
                        selected = !once,
                        onClick = { once = false },
                        label = { Text(stringResource(R.string.timer_daily)) }
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.action_cancel)) }
                    Button(
                        onClick = { onSave(timeState.hour, timeState.minute, once) },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.timer_save)) }
                }
            }
        }
    }
}
