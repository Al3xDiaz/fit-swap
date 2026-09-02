package com.example.fitswap.ui.screens.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.fitswap.data.repository.MoveDirection
import com.example.fitswap.domain.model.Routine
import com.example.fitswap.domain.model.RoutineDay
import com.example.fitswap.domain.model.RoutineExercise
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon
import com.example.fitswap.ui.common.ConfirmDialog

@Composable
fun EditRoutineScreen(
    onBack: () -> Unit,
    onRoutineDeleted: () -> Unit,
    onPickExercise: () -> Unit,
    pickedExerciseId: String?,
    onPickedExerciseConsumed: () -> Unit,
    viewModel: EditRoutineViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(pickedExerciseId) {
        val exerciseId = pickedExerciseId ?: return@LaunchedEffect
        viewModel.onExercisePicked(exerciseId)
        onPickedExerciseConsumed()
    }

    val currentRoutine = uiState.routine

    Scaffold(
        topBar = {
            AppTopBar(
                title = currentRoutine?.name ?: "Nueva rutina",
                navigationIcon = { BackNavigationIcon(onBack = onBack) }
            )
        }
    ) { innerPadding ->
        when {
            uiState.needsName -> NewRoutineNameForm(
                modifier = Modifier.padding(innerPadding),
                onCreate = viewModel::createRoutine,
            )

            uiState.isLoading || currentRoutine == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            else -> RoutineEditorBody(
                modifier = Modifier.padding(innerPadding),
                routine = currentRoutine,
                onRenameDay = viewModel::renameDay,
                onRemoveDay = viewModel::removeDay,
                onAddDay = viewModel::addDay,
                onAddExerciseClick = { dayId ->
                    viewModel.beginPickingExerciseFor(dayId)
                    onPickExercise()
                },
                onRemoveExercise = viewModel::removeExercise,
                onMoveExercise = viewModel::moveExercise,
                onDeleteRoutineClick = { showDeleteConfirm = true },
            )
        }
    }

    if (showDeleteConfirm && currentRoutine != null) {
        ConfirmDialog(
            title = "¿Está seguro?",
            message = "Se eliminará la rutina \"${currentRoutine.name}\" y no se puede deshacer.",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteRoutine()
                onRoutineDeleted()
            },
            onCancel = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun NewRoutineNameForm(modifier: Modifier = Modifier, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre de la rutina") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("newRoutineNameField")
        )
        Button(
            onClick = { onCreate(name) },
            modifier = Modifier.testTag("createRoutineButton")
        ) {
            Text("Crear rutina")
        }
    }
}

@Composable
private fun RoutineEditorBody(
    modifier: Modifier = Modifier,
    routine: Routine,
    onRenameDay: (dayId: String, newName: String) -> Unit,
    onRemoveDay: (dayId: String) -> Unit,
    onAddDay: (name: String) -> Unit,
    onAddExerciseClick: (dayId: String) -> Unit,
    onRemoveExercise: (dayId: String, routineExerciseId: String) -> Unit,
    onMoveExercise: (dayId: String, routineExerciseId: String, direction: MoveDirection) -> Unit,
    onDeleteRoutineClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        routine.days.forEach { day ->
            DayEditorSection(
                day = day,
                onRenameDay = { newName -> onRenameDay(day.id, newName) },
                onRemoveDay = { onRemoveDay(day.id) },
                onAddExerciseClick = { onAddExerciseClick(day.id) },
                onRemoveExercise = { routineExerciseId -> onRemoveExercise(day.id, routineExerciseId) },
                onMoveExercise = { routineExerciseId, direction -> onMoveExercise(day.id, routineExerciseId, direction) },
            )
            HorizontalDivider()
        }

        AddDaySection(onAddDay = onAddDay)

        if (!routine.isDefault) {
            OutlinedButton(
                onClick = onDeleteRoutineClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deleteRoutineButton")
            ) {
                Text("Eliminar rutina")
            }
        }
    }
}

@Composable
private fun DayEditorSection(
    day: RoutineDay,
    onRenameDay: (newName: String) -> Unit,
    onRemoveDay: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onRemoveExercise: (routineExerciseId: String) -> Unit,
    onMoveExercise: (routineExerciseId: String, direction: MoveDirection) -> Unit,
) {
    Column {
        DayHeaderRow(day = day, onRenameDay = onRenameDay, onRemoveDay = onRemoveDay)
        day.exercises.forEachIndexed { index, routineExercise ->
            ExerciseEditorRow(
                routineExercise = routineExercise,
                canMoveUp = index > 0,
                canMoveDown = index < day.exercises.lastIndex,
                onMoveUp = { onMoveExercise(routineExercise.id, MoveDirection.UP) },
                onMoveDown = { onMoveExercise(routineExercise.id, MoveDirection.DOWN) },
                onRemove = { onRemoveExercise(routineExercise.id) },
            )
        }
        OutlinedButton(
            onClick = onAddExerciseClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("addExerciseButton_${day.id}")
        ) {
            Text("+ Agregar ejercicio")
        }
    }
}

@Composable
private fun DayHeaderRow(day: RoutineDay, onRenameDay: (String) -> Unit, onRemoveDay: () -> Unit) {
    var isRenaming by remember(day.id) { mutableStateOf(false) }
    var draftName by remember(day.id, day.name) { mutableStateOf(day.name) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isRenaming) {
            OutlinedTextField(
                value = draftName,
                onValueChange = { draftName = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("dayNameField_${day.id}")
            )
            IconButton(
                onClick = { onRenameDay(draftName); isRenaming = false },
                modifier = Modifier.testTag("confirmRenameDayButton_${day.id}")
            ) {
                Icon(Icons.Default.Check, contentDescription = "Confirmar nombre")
            }
        } else {
            Text(
                text = day.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .testTag("dayName_${day.id}")
            )
            IconButton(
                onClick = { isRenaming = true },
                modifier = Modifier.testTag("renameDayButton_${day.id}")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Renombrar día")
            }
        }
        IconButton(onClick = onRemoveDay, modifier = Modifier.testTag("removeDayButton_${day.id}")) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar día")
        }
    }
}

@Composable
private fun ExerciseEditorRow(
    routineExercise: RoutineExercise,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editExerciseRow_${routineExercise.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = routineExercise.exercise.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onMoveUp,
            enabled = canMoveUp,
            modifier = Modifier.testTag("moveExerciseUpButton_${routineExercise.id}")
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir")
        }
        IconButton(
            onClick = onMoveDown,
            enabled = canMoveDown,
            modifier = Modifier.testTag("moveExerciseDownButton_${routineExercise.id}")
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar")
        }
        IconButton(onClick = onRemove, modifier = Modifier.testTag("removeExerciseButton_${routineExercise.id}")) {
            Icon(Icons.Default.Delete, contentDescription = "Quitar ejercicio")
        }
    }
}

@Composable
private fun AddDaySection(onAddDay: (String) -> Unit) {
    var draftName by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = draftName,
            onValueChange = { draftName = it },
            label = { Text("Nombre del día") },
            modifier = Modifier
                .weight(1f)
                .testTag("newDayNameField")
        )
        Button(
            onClick = { onAddDay(draftName); draftName = "" },
            modifier = Modifier.testTag("addDayButton")
        ) {
            Text("+ Agregar día")
        }
    }
}
