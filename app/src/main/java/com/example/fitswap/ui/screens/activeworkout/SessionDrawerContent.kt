package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.example.fitswap.ui.common.FinishRoutineDialog

/**
 * Contenido del drawer principal (☰, misma posición/click/gesto que en el resto de la app)
 * mientras hay una rutina activa — reemplaza el menú de navegación de la app (Rutinas,
 * Ejercicios, Configuración, etc.) para que no se pueda salir de la rutina en curso por swipe
 * o click sin querer; en su lugar muestra el estado de los ejercicios del día y la opción de
 * terminar la rutina.
 *
 * Reusa [SessionMenuViewModel] escaneado a la misma [NavBackStackEntry] de `ActiveExerciseScreen`
 * (mismo `ViewModelStore`, mismos argumentos de ruta) — así el drawer y la pantalla activa
 * comparten estado sin duplicar lógica de carga.
 */
@Composable
fun SessionDrawerContent(
    backStackEntry: NavBackStackEntry,
    onSelectExercise: (routineExerciseId: String) -> Unit,
    onFinishRoutine: () -> Unit,
    onClose: () -> Unit,
) {
    val viewModel: SessionMenuViewModel = hiltViewModel(viewModelStoreOwner = backStackEntry)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFinishConfirm by remember { mutableStateOf(false) }

    ModalDrawerSheet(modifier = Modifier.testTag("sessionDrawerSheet")) {
        Text(
            text = uiState.dayName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(uiState.items, key = { it.routineExerciseId }) { item ->
                    SessionDrawerRow(
                        item = item,
                        onClick = {
                            if (item.status == SessionExerciseStatus.ACTIVE) {
                                onClose()
                            } else {
                                onSelectExercise(item.routineExerciseId)
                            }
                        }
                    )
                }
                item {
                    HorizontalDivider()
                    Text(
                        text = "Terminar rutina",
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
        FinishRoutineDialog(
            onSave = {
                showFinishConfirm = false
                viewModel.saveRoutine()
                onFinishRoutine()
            },
            onDiscard = {
                showFinishConfirm = false
                viewModel.discardRoutine()
                onFinishRoutine()
            },
            onCancel = { showFinishConfirm = false }
        )
    }
}

@Composable
private fun SessionDrawerRow(item: SessionMenuItem, onClick: () -> Unit) {
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
