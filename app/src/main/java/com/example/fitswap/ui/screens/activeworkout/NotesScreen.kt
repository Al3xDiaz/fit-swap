package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.CloseNavigationIcon

@Composable
fun NotesScreen(
    onClose: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Nota de hoy — ${uiState.exerciseName}",
                navigationIcon = {
                    CloseNavigationIcon(onClose = {
                        viewModel.save()
                        onClose()
                    })
                }
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
            return@Scaffold
        }

        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Se guarda una sola nota por día para este ejercicio — se ve luego en el historial.")
            OutlinedTextField(
                value = uiState.noteText,
                onValueChange = viewModel::onNoteTextChanged,
                label = { Text("Nota de hoy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("noteInputField")
            )
            Button(
                onClick = {
                    viewModel.save()
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("saveNoteButton")
            ) {
                Text("Guardar")
            }
        }
    }
}
