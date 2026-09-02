package com.example.fitswap.ui.screens.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.fitswap.domain.model.RoutineDay
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon
import com.example.fitswap.ui.common.ConfirmDialog

@Composable
fun RoutineDetailScreen(
    onBack: () -> Unit,
    onStartWorkout: (dayId: String, exerciseId: String) -> Unit,
    onEditRoutine: () -> Unit,
    viewModel: RoutineDetailViewModel = hiltViewModel(),
) {
    val routine by viewModel.routine.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val currentRoutine = routine

    Scaffold(
        topBar = {
            AppTopBar(
                title = currentRoutine?.name.orEmpty(),
                navigationIcon = { BackNavigationIcon(onBack = onBack) }
            )
        }
    ) { innerPadding ->
        if (currentRoutine == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(currentRoutine.days, key = { it.id }) { day ->
                RoutineDaySection(day = day, onEditRoutine = onEditRoutine, onStartWorkout = onStartWorkout)
            }
            if (!currentRoutine.isDefault) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deleteRoutineButton")
                    ) {
                        Text("Eliminar rutina")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && currentRoutine != null) {
        ConfirmDialog(
            title = "¿Está seguro?",
            message = "Se eliminará la rutina \"${currentRoutine.name}\" y no se puede deshacer.",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteRoutine()
                onBack()
            },
            onCancel = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun RoutineDaySection(
    day: RoutineDay,
    onEditRoutine: () -> Unit,
    onStartWorkout: (dayId: String, exerciseId: String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(day.name, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onEditRoutine) {
                Icon(Icons.Default.Edit, contentDescription = "Editar rutina")
            }
        }
        day.exercises.forEachIndexed { index, routineExercise ->
            Text(
                text = "${index + 1}. ${routineExercise.exercise.name}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        if (day.exercises.isNotEmpty()) {
            Button(
                onClick = { onStartWorkout(day.id, day.exercises.first().id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("startWorkoutButton_${day.id}")
            ) {
                Text("Empezar entrenamiento")
            }
        }
    }
}
