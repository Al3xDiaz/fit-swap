package com.example.fitswap.ui.screens.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.domain.logic.BmiCategory
import com.example.fitswap.domain.logic.MuscleFocusRatio
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.ConfirmDialog
import com.example.fitswap.ui.common.MenuNavigationIcon
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun BodyMeasurementsScreen(
    onMenuClick: () -> Unit,
    onAddMeasurement: () -> Unit,
    onEditMeasurement: (measurementId: String) -> Unit,
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
                    text = "Completá tu perfil (sexo biológico y altura) en Configuración para calcular IMC y % de grasa.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("profileIncompleteHint")
                )
            }

            if (uiState.rows.isEmpty()) {
                Text("Sin mediciones todavía", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.rows, key = { it.entry.id }) { row ->
                        MeasurementRow(
                            row,
                            onEdit = { onEditMeasurement(row.entry.id) },
                            onDelete = { viewModel.deleteMeasurement(row.entry.id) },
                        )
                    }
                    if (uiState.muscleFocusRatios.isNotEmpty()) {
                        item { MuscleFocusSection(uiState.muscleFocusRatios) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementRow(row: BodyMeasurementRow, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("measurementRow_${row.entry.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        IconButton(onClick = onEdit, modifier = Modifier.testTag("editMeasurementButton_${row.entry.id}")) {
            Icon(Icons.Default.Edit, contentDescription = "Editar medición")
        }
        IconButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.testTag("deleteMeasurementButton_${row.entry.id}")
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar medición")
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "¿Eliminar medición?",
            message = "Se eliminará esta medición del ${row.entry.date.format(dateFormatter)}. No se puede deshacer.",
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onCancel = { showDeleteConfirm = false }
        )
    }
}

/** Sección sin `Card` a propósito, como el resto de la pantalla: texto plano para no ocultar datos
 * faltantes detrás de un número (ver comentario de `BodyMetrics.kt`) — un `HorizontalDivider` +
 * título alcanza para separar la sección de la lista de mediciones. */
@Composable
private fun MuscleFocusSection(ratios: List<MuscleFocusRatio>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Enfoque muscular", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Estas relaciones son una referencia general basada en proporciones corporales, " +
                "no un diagnóstico médico ni una evaluación de precisión clínica — usalas solo como guía orientativa.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag("muscleFocusDisclaimer")
        )
        ratios.forEach { ratio ->
            Text(
                text = "${ratio.label}: ${ratio.value?.let { formatNumber(it) } ?: "—"} — ${ratio.interpretation}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("muscleFocusRow_${ratio.id.name}")
            )
        }
    }
}

private fun bmiCategoryLabel(category: BmiCategory): String = when (category) {
    BmiCategory.BAJO_PESO -> "bajo peso"
    BmiCategory.NORMAL -> "normal"
    BmiCategory.SOBREPESO -> "sobrepeso"
    BmiCategory.OBESIDAD -> "obesidad"
}

private fun formatNumber(value: Double): String = "%.1f".format(value)
