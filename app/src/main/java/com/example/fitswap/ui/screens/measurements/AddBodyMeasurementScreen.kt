package com.example.fitswap.ui.screens.measurements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.fitswap.domain.model.MeasurementField
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddBodyMeasurementScreen(
    onClose: () -> Unit,
    onOpenGuide: () -> Unit,
    viewModel: AddBodyMeasurementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Nueva medición",
                navigationIcon = { BackNavigationIcon(onBack = onClose) },
                actions = {
                    IconButton(onClick = onOpenGuide, modifier = Modifier.testTag("measurementGuideButton")) {
                        Icon(Icons.Filled.Info, contentDescription = "Cómo medir")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState.step) {
            AddMeasurementStep.SELECT_FIELDS -> FieldSelectionStep(
                modifier = Modifier.padding(innerPadding),
                selectedFields = uiState.selectedFields,
                onToggleField = viewModel::toggleField,
                onNext = viewModel::goToForm,
            )
            AddMeasurementStep.FORM -> MeasurementFormStep(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                viewModel = viewModel,
                onBack = viewModel::goBackToFieldSelection,
                onSave = {
                    scope.launch {
                        val saved = viewModel.save()
                        // `save()` termina tras varias escrituras a Room — forzar Main explícito
                        // para el callback de navegación en vez de asumir que la corrutina
                        // resume ahí (Room puede resumir en su propio executor de background).
                        if (saved) withContext(Dispatchers.Main) { onClose() }
                    }
                },
            )
        }
    }
}

/** Primer paso del stepper: elegir qué circunferencias se van a medir hoy, antes de mostrar sus
 * campos — el peso no aparece acá porque siempre se pide, en el paso siguiente. */
@Composable
private fun FieldSelectionStep(
    modifier: Modifier = Modifier,
    selectedFields: Set<MeasurementField>,
    onToggleField: (MeasurementField) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("¿Qué medidas vas a registrar hoy?", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "El peso siempre se pide. Elegí además qué circunferencias vas a medir esta vez.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        MeasurementField.entries.forEach { field ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleField(field) }
                    .testTag("fieldOption_${field.tag}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = field in selectedFields, onCheckedChange = { onToggleField(field) })
                Text(field.label)
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .testTag("nextStepButton")
        ) {
            Text("Siguiente")
        }
    }
}

/** Segundo paso: peso (siempre) + un campo por cada circunferencia elegida en el paso anterior. */
@Composable
private fun MeasurementFormStep(
    modifier: Modifier = Modifier,
    uiState: AddBodyMeasurementUiState,
    viewModel: AddBodyMeasurementViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    Column(
        modifier = modifier
            .fillMaxSize()
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

        uiState.selectedFields.forEach { field ->
            when (field) {
                MeasurementField.NECK -> OutlinedTextField(
                    value = uiState.neckCm,
                    onValueChange = viewModel::onNeckChanged,
                    label = { Text("Cuello (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("neckField")
                )
                MeasurementField.WAIST -> OutlinedTextField(
                    value = uiState.waistCm,
                    onValueChange = viewModel::onWaistChanged,
                    label = { Text("Cintura (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("waistField")
                )
                MeasurementField.HIP -> OutlinedTextField(
                    value = uiState.hipCm,
                    onValueChange = viewModel::onHipChanged,
                    label = { Text("Cadera (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("hipField")
                )
                MeasurementField.CHEST -> OutlinedTextField(
                    value = uiState.chestCm,
                    onValueChange = viewModel::onChestChanged,
                    label = { Text("Pecho (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("chestField")
                )
                MeasurementField.ARM -> OutlinedTextField(
                    value = uiState.armCm,
                    onValueChange = viewModel::onArmChanged,
                    label = { Text("Brazo (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("armField")
                )
                MeasurementField.LEG -> OutlinedTextField(
                    value = uiState.legCm,
                    onValueChange = viewModel::onLegChanged,
                    label = { Text("Pierna (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("legField")
                )
                MeasurementField.CALF -> OutlinedTextField(
                    value = uiState.calfCm,
                    onValueChange = viewModel::onCalfChanged,
                    label = { Text("Pantorrilla (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("calfField")
                )
                MeasurementField.GLUTE -> OutlinedTextField(
                    value = uiState.gluteCm,
                    onValueChange = viewModel::onGluteChanged,
                    label = { Text("Glúteo (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("gluteField")
                )
                MeasurementField.FOREARM -> OutlinedTextField(
                    value = uiState.forearmCm,
                    onValueChange = viewModel::onForearmChanged,
                    label = { Text("Antebrazo (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("forearmField")
                )
                MeasurementField.SHOULDER -> OutlinedTextField(
                    value = uiState.shoulderCm,
                    onValueChange = viewModel::onShoulderChanged,
                    label = { Text("Hombro (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("shoulderField")
                )
                MeasurementField.WRIST -> OutlinedTextField(
                    value = uiState.wristCm,
                    onValueChange = viewModel::onWristChanged,
                    label = { Text("Muñeca (cm)") },
                    keyboardOptions = decimalKeyboard,
                    modifier = Modifier.testTag("wristField")
                )
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backToFieldSelectionButton")
        ) {
            Text("Atrás")
        }

        Button(
            onClick = onSave,
            enabled = uiState.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("saveMeasurementButton")
        ) {
            Text("Guardar")
        }
    }
}
