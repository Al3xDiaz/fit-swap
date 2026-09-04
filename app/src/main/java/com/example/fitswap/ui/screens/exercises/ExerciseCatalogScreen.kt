package com.example.fitswap.ui.screens.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.ui.common.AppTopBar

@Composable
fun ExerciseCatalogScreen(
    navigationIcon: @Composable () -> Unit,
    onExerciseClick: (exerciseId: String) -> Unit,
    onAddExercise: (() -> Unit)? = null,
    viewModel: ExerciseCatalogViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(title = "Ejercicios", navigationIcon = navigationIcon)
        },
        floatingActionButton = {
            if (onAddExercise != null) {
                FloatingActionButton(onClick = onAddExercise, modifier = Modifier.testTag("addExerciseButton")) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar ejercicio")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::updateQuery,
                label = { Text("Buscar / filtrar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("exerciseSearchField")
            )

            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin resultados",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag("noExercisesLabel")
                    )
                }
            } else {
                LazyColumn {
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseRow(exercise = exercise, onClick = { onExerciseClick(exercise.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: Exercise, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("exerciseItem_${exercise.id}")
            .padding(16.dp)
    ) {
        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "${exercise.muscleGroup} · ${exercise.equipment}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
