package com.example.fitswap.ui.screens.measurements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddBodyMeasurementScreen(
    onClose: () -> Unit,
    viewModel: AddBodyMeasurementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppTopBar(title = "Nueva medición", navigationIcon = { BackNavigationIcon(onBack = onClose) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Perfil", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                SexOption(
                    label = "Masculino",
                    selected = uiState.biologicalSex == BiologicalSex.MALE,
                    onClick = { viewModel.onSexSelected(BiologicalSex.MALE) },
                    testTag = "sexOption_MALE",
                )
                SexOption(
                    label = "Femenino",
                    selected = uiState.biologicalSex == BiologicalSex.FEMALE,
                    onClick = { viewModel.onSexSelected(BiologicalSex.FEMALE) },
                    testTag = "sexOption_FEMALE",
                )
            }
            OutlinedTextField(
                value = uiState.heightCm,
                onValueChange = viewModel::onHeightChanged,
                label = { Text("Altura (cm)") },
                modifier = Modifier.testTag("heightField")
            )

            Text("Medición de hoy", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.weightKg,
                onValueChange = viewModel::onWeightChanged,
                label = { Text("Peso (kg)") },
                modifier = Modifier.testTag("weightField")
            )
            OutlinedTextField(
                value = uiState.neckCm,
                onValueChange = viewModel::onNeckChanged,
                label = { Text("Cuello (cm, opcional)") },
                modifier = Modifier.testTag("neckField")
            )
            OutlinedTextField(
                value = uiState.waistCm,
                onValueChange = viewModel::onWaistChanged,
                label = { Text("Cintura (cm, opcional)") },
                modifier = Modifier.testTag("waistField")
            )
            OutlinedTextField(
                value = uiState.hipCm,
                onValueChange = viewModel::onHipChanged,
                label = { Text("Cadera (cm, opcional)") },
                modifier = Modifier.testTag("hipField")
            )
            OutlinedTextField(
                value = uiState.chestCm,
                onValueChange = viewModel::onChestChanged,
                label = { Text("Pecho (cm, opcional)") },
                modifier = Modifier.testTag("chestField")
            )
            OutlinedTextField(
                value = uiState.armCm,
                onValueChange = viewModel::onArmChanged,
                label = { Text("Brazo (cm, opcional)") },
                modifier = Modifier.testTag("armField")
            )
            OutlinedTextField(
                value = uiState.legCm,
                onValueChange = viewModel::onLegChanged,
                label = { Text("Pierna (cm, opcional)") },
                modifier = Modifier.testTag("legField")
            )
            OutlinedTextField(
                value = uiState.calfCm,
                onValueChange = viewModel::onCalfChanged,
                label = { Text("Pantorrilla (cm, opcional)") },
                modifier = Modifier.testTag("calfField")
            )

            Button(
                onClick = {
                    scope.launch {
                        val saved = viewModel.save()
                        // `save()` termina tras varias escrituras a Room — forzar Main explícito
                        // para el callback de navegación en vez de asumir que la corrutina
                        // resume ahí (Room puede resumir en su propio executor de background).
                        if (saved) withContext(Dispatchers.Main) { onClose() }
                    }
                },
                enabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveMeasurementButton")
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun SexOption(label: String, selected: Boolean, onClick: () -> Unit, testTag: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.padding(start = 4.dp))
    }
}
