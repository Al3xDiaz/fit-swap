package com.example.fitswap.ui.screens.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.fitswap.domain.logic.HistorySession
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon

@Composable
fun ExerciseHistoryScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ExerciseHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Historial — ${uiState.exerciseName}",
                navigationIcon = { BackNavigationIcon(onBack = onBack) },
                actions = {
                    IconButton(onClick = onEdit, modifier = Modifier.testTag("editExerciseButton")) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar ejercicio")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.recentSessions.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin historial todavía",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("noHistoryLabel")
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Peso máximo: ${formatWeight(uiState.maxWeightKg ?: 0.0)} kg",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag("maxWeightLabel")
                )
                Text(
                    text = "Volumen total (series efectivas): ${formatWeight(uiState.totalVolumeKg)} kg",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag("totalVolumeLabel")
                )
                Text(
                    text = "Sesiones recientes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                LazyColumn {
                    items(uiState.recentSessions, key = { it.date }) { session ->
                        SessionRow(session, note = uiState.notesByDate[session.date])
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: HistorySession, note: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("historySessionRow_${session.date}")
            .padding(vertical = 8.dp)
    ) {
        Text(session.date.toString(), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "${session.setCount} ${seriesLabel(session.setCount)} · ${formatWeight(session.volumeKg)} kg",
            style = MaterialTheme.typography.bodyMedium
        )
        if (!note.isNullOrBlank()) {
            Text(
                text = "📝 $note",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("historySessionNote_${session.date}")
            )
        }
    }
}

private fun seriesLabel(setCount: Int): String = if (setCount == 1) "serie efectiva" else "series efectivas"

private fun formatWeight(kg: Double): String = "%.1f".format(kg)
