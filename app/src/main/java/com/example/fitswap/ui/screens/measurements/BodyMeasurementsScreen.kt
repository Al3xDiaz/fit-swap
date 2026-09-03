package com.example.fitswap.ui.screens.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.fitswap.domain.logic.BmiCategory
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.MenuNavigationIcon
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun BodyMeasurementsScreen(
    onMenuClick: () -> Unit,
    onAddMeasurement: () -> Unit,
    viewModel: BodyMeasurementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar(title = "Medidas", navigationIcon = { MenuNavigationIcon(onMenuClick = onMenuClick) }) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onAddMeasurement, modifier = Modifier.testTag("addMeasurementButton")) {
                Text("+ Nueva medición")
            }

            val profileIncomplete = uiState.profile.biologicalSex == null || uiState.profile.heightCm == null
            if (profileIncomplete) {
                Text(
                    text = "Completá tu perfil (sexo biológico y altura) al agregar una medición para calcular IMC y % de grasa.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("profileIncompleteHint")
                )
            }

            if (uiState.rows.isEmpty()) {
                Text("Sin mediciones todavía", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.rows, key = { it.entry.id }) { row -> MeasurementRow(row) }
                }
            }
        }
    }
}

@Composable
private fun MeasurementRow(row: BodyMeasurementRow) {
    Column(modifier = Modifier.testTag("measurementRow_${row.entry.id}")) {
        Text(
            text = "${row.entry.date.format(dateFormatter)} — ${formatNumber(row.entry.weightKg)} kg",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = row.bmi?.let { bmi -> "IMC: ${formatNumber(bmi)} (${bmiCategoryLabel(row.bmiCategory!!)})" }
                ?: "IMC: faltan datos (completá tu perfil)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("bmiLabel_${row.entry.id}")
        )
        Text(
            text = row.bodyFatPercent?.let { "% de grasa: ${formatNumber(it)}%" } ?: "% de grasa: faltan datos",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("bodyFatLabel_${row.entry.id}")
        )
    }
}

private fun bmiCategoryLabel(category: BmiCategory): String = when (category) {
    BmiCategory.BAJO_PESO -> "bajo peso"
    BmiCategory.NORMAL -> "normal"
    BmiCategory.SOBREPESO -> "sobrepeso"
    BmiCategory.OBESIDAD -> "obesidad"
}

private fun formatNumber(value: Double): String = "%.1f".format(value)
