package com.example.fitswap.ui.screens.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
fun MeasurementEditScreen(
    onClose: () -> Unit,
    viewModel: MeasurementEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppTopBar(title = "Editar medición", navigationIcon = { BackNavigationIcon(onBack = onClose) }) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

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
            OutlinedTextField(
                value = uiState.weightKg,
                onValueChange = viewModel::onWeightChanged,
                label = { Text("Peso (kg)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editWeightField")
            )
            OutlinedTextField(
                value = uiState.neckCm,
                onValueChange = viewModel::onNeckChanged,
                label = { Text("Cuello (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editNeckField")
            )
            OutlinedTextField(
                value = uiState.waistCm,
                onValueChange = viewModel::onWaistChanged,
                label = { Text("Cintura (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editWaistField")
            )
            OutlinedTextField(
                value = uiState.hipCm,
                onValueChange = viewModel::onHipChanged,
                label = { Text("Cadera (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editHipField")
            )
            OutlinedTextField(
                value = uiState.chestCm,
                onValueChange = viewModel::onChestChanged,
                label = { Text("Pecho (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editChestField")
            )
            OutlinedTextField(
                value = uiState.armCm,
                onValueChange = viewModel::onArmChanged,
                label = { Text("Brazo (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editArmField")
            )
            OutlinedTextField(
                value = uiState.legCm,
                onValueChange = viewModel::onLegChanged,
                label = { Text("Pierna (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editLegField")
            )
            OutlinedTextField(
                value = uiState.calfCm,
                onValueChange = viewModel::onCalfChanged,
                label = { Text("Pantorrilla (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editCalfField")
            )
            OutlinedTextField(
                value = uiState.gluteCm,
                onValueChange = viewModel::onGluteChanged,
                label = { Text("Glúteo (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editGluteField")
            )
            OutlinedTextField(
                value = uiState.forearmCm,
                onValueChange = viewModel::onForearmChanged,
                label = { Text("Antebrazo (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editForearmField")
            )
            OutlinedTextField(
                value = uiState.shoulderCm,
                onValueChange = viewModel::onShoulderChanged,
                label = { Text("Hombro (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editShoulderField")
            )
            OutlinedTextField(
                value = uiState.wristCm,
                onValueChange = viewModel::onWristChanged,
                label = { Text("Muñeca (cm, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier.fillMaxWidth().testTag("editWristField")
            )

            Button(
                onClick = {
                    scope.launch {
                        val saved = viewModel.save()
                        if (saved) withContext(Dispatchers.Main) { onClose() }
                    }
                },
                enabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveMeasurementEditButton")
            ) {
                Text("Guardar")
            }
        }
    }
}
