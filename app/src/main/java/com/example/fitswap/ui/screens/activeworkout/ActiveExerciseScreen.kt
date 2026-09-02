package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.domain.model.PlannedSet
import com.example.fitswap.domain.model.SetType
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon

@Composable
fun ActiveExerciseScreen(
    onBack: () -> Unit,
    onSwapExercise: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenSessionMenu: () -> Unit,
    onAddMedia: () -> Unit,
    viewModel: ActiveExerciseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = uiState.exercise?.name.orEmpty(),
                navigationIcon = { BackNavigationIcon(onBack = onBack) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SecondaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Entrenamiento") },
                    modifier = Modifier.testTag("tab_entrenamiento")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Galería") },
                    modifier = Modifier.testTag("tab_galeria")
                )
            }

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                selectedTab == 0 -> TrainingTab(
                    uiState = uiState,
                    onSwapExercise = onSwapExercise,
                    onOpenNotes = onOpenNotes,
                    onOpenSessionMenu = onOpenSessionMenu,
                    onEffectiveWeightChange = viewModel::updateEffectiveWeight,
                    onStageWeightChange = viewModel::updateCurrentStageWeight,
                    onIncrementReps = viewModel::incrementReps,
                    onDecrementReps = viewModel::decrementReps,
                    onRegisterSet = viewModel::registerSet,
                )

                else -> GalleryTab(onAddMedia = onAddMedia)
            }
        }
    }
}

@Composable
private fun TrainingTab(
    uiState: ActiveExerciseUiState,
    onSwapExercise: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenSessionMenu: () -> Unit,
    onEffectiveWeightChange: (Double) -> Unit,
    onStageWeightChange: (Double) -> Unit,
    onIncrementReps: () -> Unit,
    onDecrementReps: () -> Unit,
    onRegisterSet: () -> Unit,
) {
    val exercise = uiState.exercise

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (exercise != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(exercise.muscleGroup) })
                AssistChip(onClick = {}, label = { Text(exercise.equipment) })
                exercise.tags.forEach { tag -> AssistChip(onClick = {}, label = { Text(tag) }) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("phaseIndicator"),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            uiState.phases.forEach { phase ->
                Text(text = phaseLabel(phase), modifier = Modifier.testTag("phase_${phase.type}"))
            }
        }

        OutlinedTextField(
            value = formatWeight(uiState.effectiveWeightKg),
            onValueChange = { text -> text.toDoubleOrNull()?.let(onEffectiveWeightChange) },
            label = { Text("Peso efectivo (kg)") },
            modifier = Modifier.testTag("effectiveWeightField")
        )
        uiState.lastLoggedWeightKg?.let { lastWeight ->
            Text(
                text = "Última vez: ${formatWeight(lastWeight)} kg",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("lastLoggedWeightLabel")
            )
        }

        OutlinedButton(onClick = onSwapExercise, modifier = Modifier.testTag("swapExerciseButton")) {
            Text("Cambiar ejercicio")
        }

        if (uiState.isComplete) {
            Text(
                text = "¡Ejercicio completado!",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("exerciseCompleteLabel")
            )
        } else {
            uiState.currentSet?.let { current ->
                Text(
                    text = stageTitle(current),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag("currentStageLabel")
                )

                if (!uiState.currentStageWeightIsEffective) {
                    OutlinedTextField(
                        value = formatWeight(uiState.currentStageWeightKg),
                        onValueChange = { text -> text.toDoubleOrNull()?.let(onStageWeightChange) },
                        label = { Text("Peso sugerido (kg)") },
                        modifier = Modifier.testTag("stageWeightField")
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Reps:")
                    IconButton(onClick = onDecrementReps, modifier = Modifier.testTag("repsMinusButton")) {
                        Text("-")
                    }
                    Text(
                        text = "${uiState.currentReps}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("repsValue")
                    )
                    IconButton(onClick = onIncrementReps, modifier = Modifier.testTag("repsPlusButton")) {
                        Text("+")
                    }
                }

                Button(
                    onClick = onRegisterSet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("registerSetButton")
                ) {
                    Text("Registrar serie")
                }
            }
        }

        uiState.restRemainingSeconds?.let { remaining ->
            Text(
                text = "⏱ Descanso: ${formatSeconds(remaining)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("restTimerLabel")
            )
        }

        if (uiState.effectiveSetsTotal > 0) {
            Row(modifier = Modifier.testTag("effectiveSetsDots")) {
                repeat(uiState.effectiveSetsTotal) { index ->
                    Text(if (index < uiState.effectiveSetsDone) "● " else "○ ")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onOpenNotes, modifier = Modifier.testTag("notesButton")) {
                Text("Notas")
            }
            OutlinedButton(onClick = onOpenSessionMenu, modifier = Modifier.testTag("sessionMenuButton")) {
                Text("Menú de sesión")
            }
        }
    }
}

@Composable
private fun GalleryTab(onAddMedia: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Sin fotos ni videos todavía", style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onAddMedia, modifier = Modifier.testTag("addMediaButton")) {
                Text("+ Agregar foto/video")
            }
        }
    }
}

private fun stageTitle(set: PlannedSet): String = when (set.type) {
    SetType.WARMUP -> "Calentamiento"
    SetType.APPROACH -> "Serie ${set.stageNumber} de ${set.totalInType} (aproximación)"
    SetType.EFFECTIVE -> "Serie ${set.stageNumber} de ${set.totalInType} (efectiva)"
}

private fun phaseLabel(phase: PhaseStatus): String {
    val name = when (phase.type) {
        SetType.WARMUP -> "Calentamiento"
        SetType.APPROACH -> "Aprox."
        SetType.EFFECTIVE -> "Efectiva"
    }
    val marker = when (phase.state) {
        PhaseState.DONE -> "✓"
        PhaseState.ACTIVE -> "▶"
        PhaseState.PENDING -> "○"
    }
    return "$name $marker"
}

private fun formatWeight(kg: Double): String = "%.1f".format(kg)

private fun formatSeconds(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
