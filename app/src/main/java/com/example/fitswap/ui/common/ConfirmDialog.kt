package com.example.fitswap.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitswap.ui.theme.FitSwapTheme

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmLabel: String = "Confirmar",
    cancelLabel: String = "Cancelar",
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(cancelLabel) } }
    )
}

@Preview(showBackground = true)
@Composable
private fun ConfirmDialogPreview() {
    FitSwapTheme {
        ConfirmDialog(
            title = "¿Está seguro?",
            message = "Se terminará el entrenamiento en curso.",
            onConfirm = {},
            onCancel = {}
        )
    }
}
