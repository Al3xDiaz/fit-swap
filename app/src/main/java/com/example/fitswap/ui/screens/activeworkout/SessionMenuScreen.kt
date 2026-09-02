package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.CloseNavigationIcon
import com.example.fitswap.ui.common.ConfirmDialog

@Composable
fun SessionMenuScreen(
    onClose: () -> Unit,
    onSelectExercise: (routineExerciseId: String) -> Unit,
    onFinishRoutine: () -> Unit,
    viewModel: SessionMenuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFinishConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = uiState.dayName,
                navigationIcon = { CloseNavigationIcon(onClose = onClose) }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(uiState.items, key = { it.routineExerciseId }) { item ->
                    SessionMenuRow(
                        item = item,
                        onClick = {
                            if (item.status == SessionExerciseStatus.ACTIVE) {
                                onClose()
                            } else {
                                onSelectExercise(item.routineExerciseId)
                            }
                        }
                    )
                    HorizontalDivider()
                }
                item {
                    Text(
                        text = "Terminar rutina?",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFinishConfirm = true }
                            .testTag("finishRoutineItem")
                            .padding(16.dp)
                    )
                }
            }
        }
    }

    if (showFinishConfirm) {
        ConfirmDialog(
            title = "¿Está seguro?",
            message = "Se terminará el entrenamiento en curso y volverás a Rutinas.",
            onConfirm = {
                showFinishConfirm = false
                viewModel.endRoutine()
                onFinishRoutine()
            },
            onCancel = { showFinishConfirm = false }
        )
    }
}

@Composable
private fun SessionMenuRow(item: SessionMenuItem, onClick: () -> Unit) {
    val marker = when (item.status) {
        SessionExerciseStatus.DONE -> "✓"
        SessionExerciseStatus.ACTIVE -> "▶"
        SessionExerciseStatus.PENDING -> "○"
    }
    Text(
        text = "$marker  ${item.exerciseName}",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("sessionMenuItem_${item.routineExerciseId}")
            .padding(16.dp)
    )
}
