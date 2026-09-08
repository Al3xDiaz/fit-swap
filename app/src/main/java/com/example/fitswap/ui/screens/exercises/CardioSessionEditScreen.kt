package com.example.fitswap.ui.screens.exercises

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
fun CardioSessionEditScreen(
    onClose: () -> Unit,
    viewModel: CardioSessionEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Editar sesión — ${uiState.exerciseName}",
                navigationIcon = { BackNavigationIcon(onBack = onClose) }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)

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
                value = uiState.durationMinutes,
                onValueChange = viewModel::onDurationChanged,
                label = { Text("Duración (min)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editCardioDurationField")
            )
            OutlinedTextField(
                value = uiState.distanceKm,
                onValueChange = viewModel::onDistanceChanged,
                label = { Text("Distancia (km, opcional)") },
                keyboardOptions = decimalKeyboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editCardioDistanceField")
            )
            OutlinedTextField(
                value = uiState.avgHeartRate,
                onValueChange = viewModel::onHeartRateChanged,
                label = { Text("Ritmo cardíaco (bpm, opcional)") },
                keyboardOptions = numberKeyboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editCardioHeartRateField")
            )
            OutlinedTextField(
                value = uiState.calories,
                onValueChange = viewModel::onCaloriesChanged,
                label = { Text("Calorías (opcional)") },
                keyboardOptions = numberKeyboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editCardioCaloriesField")
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
                    .testTag("saveCardioSessionButton")
            ) {
                Text("Guardar")
            }
        }
    }
}
