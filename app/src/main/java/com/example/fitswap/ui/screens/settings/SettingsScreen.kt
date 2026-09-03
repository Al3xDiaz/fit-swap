package com.example.fitswap.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.domain.model.UiDensity
import com.example.fitswap.domain.model.UnitSystem
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.MenuNavigationIcon

@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(title = "Configuraciones", navigationIcon = { MenuNavigationIcon(onMenuClick = onMenuClick) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection(title = "Tema") {
                AppTheme.entries.forEach { theme ->
                    SettingsOptionRow(
                        label = themeLabel(theme),
                        selected = settings.theme == theme,
                        onClick = { viewModel.onThemeSelected(theme) },
                        testTag = "themeOption_${theme.name}"
                    )
                }
            }
            SettingsSection(title = "Densidad de UI") {
                UiDensity.entries.forEach { density ->
                    SettingsOptionRow(
                        label = uiDensityLabel(density),
                        selected = settings.uiDensity == density,
                        onClick = { viewModel.onUiDensitySelected(density) },
                        testTag = "densityOption_${density.name}"
                    )
                }
            }
            SettingsSection(title = "Sistema de unidades") {
                UnitSystem.entries.forEach { unitSystem ->
                    SettingsOptionRow(
                        label = unitSystemLabel(unitSystem),
                        selected = settings.unitSystem == unitSystem,
                        onClick = { viewModel.onUnitSystemSelected(unitSystem) },
                        testTag = "unitSystemOption_${unitSystem.name}"
                    )
                }
            }
            SettingsSection(title = "Timer de descanso entre series") {
                restTimerOptionsSeconds.forEach { seconds ->
                    SettingsOptionRow(
                        label = restTimerLabel(seconds),
                        selected = settings.restTimerSeconds == seconds,
                        onClick = { viewModel.onRestTimerSecondsSelected(seconds) },
                        testTag = "restTimerOption_$seconds"
                    )
                }
            }
        }
    }
}

private val restTimerOptionsSeconds = listOf(30, 60, 90, 120, 180)

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun SettingsOptionRow(label: String, selected: Boolean, onClick: () -> Unit, testTag: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 4.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
    }
}

private fun themeLabel(theme: AppTheme): String = when (theme) {
    AppTheme.LIGHT -> "Claro"
    AppTheme.DARK -> "Oscuro"
    AppTheme.SYSTEM -> "Seguir sistema"
}

private fun uiDensityLabel(uiDensity: UiDensity): String = when (uiDensity) {
    UiDensity.COMPACT -> "Compacta"
    UiDensity.COMFORTABLE -> "Cómoda"
}

private fun unitSystemLabel(unitSystem: UnitSystem): String = when (unitSystem) {
    UnitSystem.METRIC -> "Métrico (kg, cm)"
    UnitSystem.IMPERIAL -> "Imperial (lb, in)"
}

private fun restTimerLabel(seconds: Int): String =
    if (seconds >= 60) "${seconds / 60} min" + if (seconds % 60 != 0) " ${seconds % 60} s" else ""
    else "$seconds s"
