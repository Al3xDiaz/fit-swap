package com.example.fitswap.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitswap.ui.theme.FitSwapTheme

/**
 * Modal unificado para terminar una rutina en curso: a diferencia de [ConfirmDialog] (binario),
 * ofrece explícitamente guardar o descartar lo registrado hoy — reemplaza el diálogo genérico
 * "¿Está seguro?" que antes usaban por separado el drawer de sesión y el back del sistema.
 */
@Composable
fun FinishRoutineDialog(onSave: () -> Unit, onDiscard: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("¿Terminar rutina?") },
        text = { Text("Podés guardar lo registrado hoy o descartarlo. Descartar no se puede deshacer.") },
        confirmButton = {
            TextButton(onClick = onSave, modifier = Modifier.testTag("saveRoutineButton")) {
                Text("Guardar y terminar")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDiscard, modifier = Modifier.testTag("discardRoutineButton")) {
                    Text("Descartar")
                }
                TextButton(onClick = onCancel, modifier = Modifier.testTag("cancelFinishRoutineButton")) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun FinishRoutineDialogPreview() {
    FitSwapTheme {
        FinishRoutineDialog(onSave = {}, onDiscard = {}, onCancel = {})
    }
}
