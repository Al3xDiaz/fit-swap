package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.fitswap.domain.model.Note
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.CloseNavigationIcon

@Composable
fun NotesScreen(
    onClose: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var draftText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Notas — ${uiState.exerciseName}",
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
            return@Scaffold
        }

        Column(modifier = Modifier.padding(innerPadding)) {
            if (uiState.notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin notas todavía",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag("noNotesLabel")
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.notes, key = { it.id }) { note -> NoteRow(note) }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draftText,
                    onValueChange = { draftText = it },
                    label = { Text("Nueva nota") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("noteInputField")
                )
                Button(
                    onClick = {
                        viewModel.addNote(draftText)
                        draftText = ""
                    },
                    modifier = Modifier.testTag("addNoteButton")
                ) {
                    Text("Agregar")
                }
            }
        }
    }
}

@Composable
private fun NoteRow(note: Note) {
    Text(
        text = note.text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("noteItem_${note.id}")
            .padding(16.dp)
    )
}
