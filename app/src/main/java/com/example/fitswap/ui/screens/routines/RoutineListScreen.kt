package com.example.fitswap.ui.screens.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.fitswap.domain.model.Routine
import com.example.fitswap.domain.model.WEEK_DAYS_ES
import com.example.fitswap.domain.model.toSpanishLabel
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.MenuNavigationIcon
import java.time.DayOfWeek

@Composable
fun RoutineListScreen(
    onMenuClick: () -> Unit,
    onRoutineClick: (String) -> Unit,
    onNewRoutineClick: () -> Unit,
    onStartWorkout: (routineId: String, dayId: String, exerciseId: String) -> Unit,
    viewModel: RoutineListViewModel = hiltViewModel(),
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val selectedWeekDay by viewModel.selectedWeekDay.collectAsStateWithLifecycle()
    var showDayPicker by remember { mutableStateOf(false) }
    val defaultRoutine = routines.find { it.isDefault }

    Scaffold(
        topBar = { AppTopBar(title = "Rutinas", navigationIcon = { MenuNavigationIcon(onMenuClick = onMenuClick) }) }
    ) { innerPadding ->
        if (defaultRoutine != null) {
            DefaultRoutineHome(
                modifier = Modifier.padding(innerPadding),
                routine = defaultRoutine,
                selectedWeekDay = selectedWeekDay,
                isToday = selectedWeekDay == viewModel.todayDayOfWeek,
                otherRoutines = routines.filterNot { it.id == defaultRoutine.id },
                onChangeDayClick = { showDayPicker = true },
                onOpenRoutine = { onRoutineClick(defaultRoutine.id) },
                onStartWorkout = { dayId, exerciseId -> onStartWorkout(defaultRoutine.id, dayId, exerciseId) },
                onClearDefault = viewModel::clearDefaultRoutine,
                onSetDefault = viewModel::setDefaultRoutine,
                onRoutineClick = onRoutineClick,
                onNewRoutineClick = onNewRoutineClick,
            )
            if (showDayPicker) {
                WeekDayPickerDialog(
                    onDaySelected = { day ->
                        viewModel.selectWeekDay(day)
                        showDayPicker = false
                    },
                    onDismiss = { showDayPicker = false },
                )
            }
        } else {
            RoutinePlainList(
                modifier = Modifier.padding(innerPadding),
                routines = routines,
                onRoutineClick = onRoutineClick,
                onNewRoutineClick = onNewRoutineClick,
                onSetDefault = viewModel::setDefaultRoutine,
            )
        }
    }
}

@Composable
private fun RoutinePlainList(
    modifier: Modifier,
    routines: List<Routine>,
    onRoutineClick: (String) -> Unit,
    onNewRoutineClick: () -> Unit,
    onSetDefault: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(routines, key = { it.id }) { routine ->
            RoutineListItem(
                routine = routine,
                onClick = { onRoutineClick(routine.id) },
                onToggleFavorite = { onSetDefault(routine.id) },
                modifier = Modifier.testTag("routineListItem_${routine.id}")
            )
        }
        item {
            OutlinedButton(
                onClick = onNewRoutineClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("newRoutineButton")
            ) {
                Text("+ Nueva rutina")
            }
        }
    }
}

@Composable
private fun RoutineListItem(
    routine: Routine,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(routine.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.testTag("setDefaultRoutineButton_${routine.id}")
            ) {
                Icon(
                    imageVector = if (routine.isDefault) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Marcar como rutina por defecto",
                )
            }
        }
    }
}

/** Pantalla principal cuando hay una rutina marcada como por defecto: la muestra directo, con
 * su día actual (semana completa si sus días tienen `dayOfWeek`) y acceso rápido a entrenar,
 * en vez de la lista plana de rutinas. */
@Composable
private fun DefaultRoutineHome(
    modifier: Modifier,
    routine: Routine,
    selectedWeekDay: DayOfWeek,
    isToday: Boolean,
    otherRoutines: List<Routine>,
    onChangeDayClick: () -> Unit,
    onOpenRoutine: () -> Unit,
    onStartWorkout: (dayId: String, exerciseId: String) -> Unit,
    onClearDefault: () -> Unit,
    onSetDefault: (String) -> Unit,
    onRoutineClick: (String) -> Unit,
    onNewRoutineClick: () -> Unit,
) {
    val hasWeekDays = routine.days.any { it.dayOfWeek != null }
    val day = if (hasWeekDays) routine.days.firstOrNull { it.dayOfWeek == selectedWeekDay } else routine.days.firstOrNull()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(onClick = onOpenRoutine, modifier = Modifier.fillMaxWidth().testTag("defaultRoutineCard")) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(routine.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = onClearDefault, modifier = Modifier.testTag("clearDefaultRoutineButton")) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Quitar como rutina por defecto")
                        }
                    }
                    if (hasWeekDays) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onChangeDayClick).testTag("changeDayButton")
                        ) {
                            Text(selectedWeekDay.toSpanishLabel(), style = MaterialTheme.typography.titleMedium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar día")
                        }
                    }
                    if (day == null || day.exercises.isEmpty()) {
                        Text(
                            text = if (isToday) "Hoy no te toca gym" else "Sin ejercicios este día",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.testTag("emptyDayMessage")
                        )
                    } else {
                        day.exercises.forEachIndexed { index, routineExercise ->
                            Text(
                                text = "${index + 1}. ${routineExercise.exercise.name}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Button(
                            onClick = { onStartWorkout(day.id, day.exercises.first().id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("startWorkoutButton_${day.id}")
                        ) {
                            Text("Empezar entrenamiento")
                        }
                    }
                }
            }
        }
        if (otherRoutines.isNotEmpty()) {
            item {
                Text("Otras rutinas", style = MaterialTheme.typography.titleMedium)
            }
            items(otherRoutines, key = { it.id }) { other ->
                RoutineListItem(
                    routine = other,
                    onClick = { onRoutineClick(other.id) },
                    onToggleFavorite = { onSetDefault(other.id) },
                    modifier = Modifier.testTag("routineListItem_${other.id}")
                )
            }
        }
        item {
            OutlinedButton(
                onClick = onNewRoutineClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("newRoutineButton")
            ) {
                Text("+ Nueva rutina")
            }
        }
    }
}

@Composable
private fun WeekDayPickerDialog(
    onDaySelected: (DayOfWeek) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elegir día") },
        text = {
            Column {
                WEEK_DAYS_ES.forEach { day ->
                    Text(
                        text = day.toSpanishLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDaySelected(day) }
                            .testTag("weekDayPickerItem_${day.name}")
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
