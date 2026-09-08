package com.example.fitswap.ui.screens.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.fitswap.domain.model.SetType
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistorySessionDetailScreen(
    onClose: () -> Unit,
    viewModel: HistorySessionDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "${uiState.exerciseName} — ${uiState.date}",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.rows.isEmpty()) {
                Text("Sin series para este día", style = MaterialTheme.typography.bodyLarge)
            } else {
                uiState.rows.forEach { row ->
                    HistoryRowEditor(
                        row = row,
                        onRepsChanged = { viewModel.onRepsChanged(row.id, it) },
                        onWeightChanged = { viewModel.onWeightChanged(row.id, it) },
                        onRemove = { viewModel.removeRow(row.id) },
                    )
                    HorizontalDivider()
                }
            }

            OutlinedTextField(
                value = uiState.noteText,
                onValueChange = viewModel::onNoteTextChanged,
                label = { Text("Nota del día") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("historyDetailNoteField")
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
                    .testTag("saveHistoryDetailButton")
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun HistoryRowEditor(
    row: EditableHistoryRow,
    onRepsChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("historyDetailRow_${row.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = setTypeLabel(row.type),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(96.dp)
        )
        OutlinedTextField(
            value = row.repsInput,
            onValueChange = onRepsChanged,
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .testTag("historyDetailRepsField_${row.id}")
        )
        OutlinedTextField(
            value = row.weightInput,
            onValueChange = onWeightChanged,
            label = { Text("Peso (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .weight(1f)
                .testTag("historyDetailWeightField_${row.id}")
        )
        IconButton(onClick = onRemove, modifier = Modifier.testTag("removeHistoryDetailRowButton_${row.id}")) {
            Icon(Icons.Default.Delete, contentDescription = "Quitar serie")
        }
    }
}

private fun setTypeLabel(type: SetType): String = when (type) {
    SetType.WARMUP -> "Calentamiento"
    SetType.APPROACH -> "Aproximación"
    SetType.EFFECTIVE -> "Efectiva"
}
