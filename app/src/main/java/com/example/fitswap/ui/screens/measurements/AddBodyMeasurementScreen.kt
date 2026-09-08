package com.example.fitswap.ui.screens.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Medición de hoy", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.weightKg,
                onValueChange = viewModel::onWeightChanged,
                label = { Text("Peso (kg)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("weightField")
            )
            OutlinedTextField(
                value = uiState.neckCm,
                onValueChange = viewModel::onNeckChanged,
                label = { Text("Cuello (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("neckField")
            )
            OutlinedTextField(
                value = uiState.waistCm,
                onValueChange = viewModel::onWaistChanged,
                label = { Text("Cintura (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("waistField")
            )
            OutlinedTextField(
                value = uiState.hipCm,
                onValueChange = viewModel::onHipChanged,
                label = { Text("Cadera (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("hipField")
            )
            OutlinedTextField(
                value = uiState.chestCm,
                onValueChange = viewModel::onChestChanged,
                label = { Text("Pecho (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("chestField")
            )
            OutlinedTextField(
                value = uiState.armCm,
                onValueChange = viewModel::onArmChanged,
                label = { Text("Brazo (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("armField")
            )
            OutlinedTextField(
                value = uiState.legCm,
                onValueChange = viewModel::onLegChanged,
                label = { Text("Pierna (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("legField")
            )
            OutlinedTextField(
                value = uiState.calfCm,
                onValueChange = viewModel::onCalfChanged,
                label = { Text("Pantorrilla (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("calfField")
            )
            OutlinedTextField(
                value = uiState.gluteCm,
                onValueChange = viewModel::onGluteChanged,
                label = { Text("Glúteo (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("gluteField")
            )
            OutlinedTextField(
                value = uiState.forearmCm,
                onValueChange = viewModel::onForearmChanged,
                label = { Text("Antebrazo (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("forearmField")
            )
            OutlinedTextField(
                value = uiState.shoulderCm,
                onValueChange = viewModel::onShoulderChanged,
                label = { Text("Hombro (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("shoulderField")
            )
            OutlinedTextField(
                value = uiState.wristCm,
                onValueChange = viewModel::onWristChanged,
                label = { Text("Muñeca (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.testTag("wristField")
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
