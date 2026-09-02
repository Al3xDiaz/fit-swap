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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.CloseNavigationIcon

@Composable
fun SwapExerciseScreen(
    onClose: () -> Unit,
    viewModel: SwapExerciseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Alternativas a \"${uiState.currentExerciseName}\"",
                navigationIcon = { CloseNavigationIcon(onClose = onClose) }
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

            uiState.substitutes.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay alternativas registradas todavía para este ejercicio.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("noSubstitutesLabel")
                )
            }

            else -> LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(uiState.substitutes, key = { it.id }) { exercise ->
                    SubstituteRow(exercise = exercise, onClick = {
                        viewModel.selectSubstitute(exercise)
                        onClose()
                    })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SubstituteRow(exercise: Exercise, onClick: () -> Unit) {
    Text(
        text = exercise.name,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("substituteItem_${exercise.id}")
            .padding(16.dp)
    )
}
