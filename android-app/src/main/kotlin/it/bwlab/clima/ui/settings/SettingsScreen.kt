@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.bwlab.clima.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.bwlab.clima.AppViewModel
import it.bwlab.clima.R
import it.bwlab.clima.ui.theme.ThemeMode
import java.util.Locale

/** Codice lingua -> endonimo (nome nativo, sempre uguale) */
private fun endonym(code: String): String {
    val loc = Locale.forLanguageTag(code)
    return loc.getDisplayLanguage(loc).lowercase(loc)
        .replaceFirstChar { it.uppercase(loc) }
}

/** Codice lingua -> nome tradotto nella lingua corrente dell'app */
private fun exonym(code: String): String {
    val current = Locale.getDefault()
    return Locale.forLanguageTag(code).getDisplayLanguage(current).lowercase(current)
        .replaceFirstChar { it.uppercase(current) }
}

/** Le lingue supportate dall'app (matcha locales_config.xml). */
val SUPPORTED_LANGUAGES: List<String> = listOf(
    "en", "it", "fr", "de", "es", "pt", "nl", "da", "sv", "fi", "nb", "is",
    "pl", "cs", "sk", "hu", "ro", "lt", "lv", "et", "ga", "mt", "hr",
    "sl", "sr", "bs", "sq", "mk", "bg", "el", "uk", "ca"
)

/** Schermata Impostazioni (mockup 05). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    app: AppViewModel,
    onBack: () -> Unit
) {
    val theme by app.prefs.theme.collectAsState(initial = ThemeMode.SYSTEM)
    val usesF by app.prefs.usesFahrenheit.collectAsState(initial = false)
    val autoRefresh by app.prefs.autoRefresh.collectAsState(initial = true)
    var showLanguageSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val version = try {
        context.packageManager
            .getPackageInfo(context.packageName, 0).versionName ?: ""
    } catch (e: Throwable) {
        ""
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
                        text = stringResource(R.string.screen_settings),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Lingua
            SettingRow(
                title = stringResource(R.string.settings_language),
                subtitle = stringResource(
                    R.string.settings_language_current,
                    currentLanguageEndonymSafe(),
                    currentLanguageExonymSafe()
                ),
                onClick = { showLanguageSheet = true }
            )

            // Unità °C / °F
            SettingCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_units),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    UnitsToggle(usesF = usesF, onChange = { app.setUnit(it) })
                }
            }

            // Aggiornamento automatico
            SettingCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_auto_refresh),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.settings_auto_refresh_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoRefresh,
                        onCheckedChange = { app.setAutoRefresh(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Tema
            ThemeSelector(
                current = theme,
                onSelect = { app.setTheme(it) }
            )

            // Versione
            Text(
                text = stringResource(R.string.about_version, version),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (showLanguageSheet) {
        LanguagePickerSheet(
            key = localeKeyCurrent(),
            onSelect = { code ->
                val locales = if (code == null) {
                    androidx.core.os.LocaleListCompat.getEmptyLocaleList()
                } else {
                    androidx.core.os.LocaleListCompat.forLanguageTags(code)
                }
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locales)
                showLanguageSheet = false
            },
            onDismiss = { showLanguageSheet = false }
        )
    }
}

/** Lingua scelta per-app: null = predefinita di sistema. */
private fun localeKeyCurrent(): String? {
    val locales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
    return if (locales.isEmpty) null else locales[0]?.language
}

@Composable
private fun currentLanguageEndonymSafe(): String =
    localeKeyCurrent()?.let { endonym(it) } ?: stringResource(R.string.language_system_default)

@Composable
private fun currentLanguageExonymSafe(): String =
    endonym(localeKeyCurrent() ?: Locale.getDefault().language)

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
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

@Composable
private fun UnitsToggle(usesF: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Segmentato a due opzioni: °C / °F
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                UnitPill(label = stringResource(R.string.unit_celsius), active = !usesF) { onChange(false) }
                UnitPill(label = stringResource(R.string.unit_fahrenheit), active = usesF) { onChange(true) }
            }
        }
    }
}

@Composable
private fun UnitPill(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ThemeSelector(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    SettingCard {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ThemeMode.SYSTEM to R.string.theme_system,
                    ThemeMode.LIGHT to R.string.theme_light,
                    ThemeMode.DARK to R.string.theme_dark
                ).forEach { (mode, label) ->
                    UnitPill(
                        label = stringResource(label),
                        active = current == mode
                    ) { onSelect(mode) }
                }
            }
        }
    }
}

/**
 * Foglio lingue: "Predefinita di sistema" in cima, poi le 31 lingue ordinate
 * alfabeticamente per endonimo, con nome tradotto a destra, più campo di ricerca.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerSheet(
    key: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val entries = remember(query) {
        SUPPORTED_LANGUAGES
            .map { it to (endonym(it) to exonym(it)) }
            .filter { (_, names) ->
                val q = query.trim().lowercase(Locale.ROOT)
                q.isBlank() || q in names.first.lowercase(Locale.ROOT) ||
                    q in names.second.lowercase(Locale.ROOT)
            }
            .sortedBy { (_, names) -> names.first.lowercase(Locale.ROOT) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.language_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                placeholder = { Text(stringResource(R.string.search_language)) },
                singleLine = true,
                shape = CircleShape,
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // voce "Predefinita di sistema"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(null) }
                    .padding(vertical = 12.dp)
            ) {
                RadioDot(selected = key == null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.language_system_default),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (key == null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(bottom = 24.dp)
            ) {
                items(entries) { (code, names) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioDot(selected = key == code)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = names.first,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (key == code) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = names.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    Box(
        Modifier
            .size(20.dp)
            .border(
                width = 2.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
