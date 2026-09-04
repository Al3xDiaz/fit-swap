package com.example.fitswap.ui.screens.activeworkout

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fitswap.domain.logic.HistorySession
import com.example.fitswap.domain.model.GalleryItem
import com.example.fitswap.domain.model.PlannedSet
import com.example.fitswap.domain.model.SetType
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.ConfirmDialog
import com.example.fitswap.ui.common.MenuNavigationIcon
import kotlinx.coroutines.launch

@Composable
fun ActiveExerciseScreen(
    onExitDiscarding: () -> Unit,
    onSwapExercise: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenDrawer: () -> Unit,
    onPreviousExercise: (exerciseId: String) -> Unit,
    onNextExercise: (exerciseId: String) -> Unit,
    viewModel: ActiveExerciseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val pagerScope = rememberCoroutineScope()
    var showExitConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (granted != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    BackHandler { showExitConfirm = true }

    Scaffold(
        topBar = {
            AppTopBar(
                title = uiState.exercise?.name.orEmpty(),
                navigationIcon = { MenuNavigationIcon(onMenuClick = onOpenDrawer) },
                actions = {
                    IconButton(onClick = onSwapExercise, modifier = Modifier.testTag("swapExerciseButton")) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = "Cambiar ejercicio")
                    }
                }
            )
        },
        bottomBar = {
            NextPreviousBar(
                previousExerciseId = uiState.previousExerciseId,
                nextExerciseId = uiState.nextExerciseId,
                onPreviousExercise = onPreviousExercise,
                onNextExercise = onNextExercise,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SecondaryTabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { pagerScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Entrenamiento") },
                    modifier = Modifier.testTag("tab_entrenamiento")
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { pagerScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Galería") },
                    modifier = Modifier.testTag("tab_galeria")
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { pagerScope.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text("Historial") },
                    modifier = Modifier.testTag("tab_historial")
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> TrainingTab(
                            uiState = uiState,
                            onOpenNotes = onOpenNotes,
                            onEffectiveWeightInputChange = viewModel::onEffectiveWeightInputChanged,
                            onEffectiveWeightFocusChange = viewModel::onEffectiveWeightFocusChanged,
                            onStageWeightInputChange = viewModel::onStageWeightInputChanged,
                            onStageWeightFocusChange = viewModel::onStageWeightFocusChanged,
                            onIncrementReps = viewModel::incrementReps,
                            onDecrementReps = viewModel::decrementReps,
                            onRepsInputChange = viewModel::onRepsInputChanged,
                            onRepsFocusChange = viewModel::onRepsFocusChanged,
                            onRegisterSet = viewModel::registerSet,
                            onCompleteExercise = viewModel::completeExercise,
                        )

                        1 -> GalleryTab(galleryItems = uiState.galleryItems)

                        else -> HistoryTab(summaries = uiState.historySummaries)
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.autoAdvanceToExerciseId) {
        val target = uiState.autoAdvanceToExerciseId ?: return@LaunchedEffect
        viewModel.consumeAutoAdvance()
        onNextExercise(target)
    }

    if (showExitConfirm) {
        ConfirmDialog(
            title = "¿Salir del entrenamiento?",
            message = "Se descartará el progreso de este ejercicio y se terminará la rutina en curso.",
            onConfirm = {
                showExitConfirm = false
                viewModel.endRoutine()
                onExitDiscarding()
            },
            onCancel = { showExitConfirm = false }
        )
    }
}

@Composable
private fun NextPreviousBar(
    previousExerciseId: String?,
    nextExerciseId: String?,
    onPreviousExercise: (String) -> Unit,
    onNextExercise: (String) -> Unit,
) {
    BottomAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { previousExerciseId?.let(onPreviousExercise) },
                enabled = previousExerciseId != null,
                modifier = Modifier.testTag("previousExerciseButton")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text(" Anterior")
            }
            OutlinedButton(
                onClick = { nextExerciseId?.let(onNextExercise) },
                enabled = nextExerciseId != null,
                modifier = Modifier.testTag("nextExerciseButton")
            ) {
                Text("Siguiente ")
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun TrainingTab(
    uiState: ActiveExerciseUiState,
    onOpenNotes: () -> Unit,
    onEffectiveWeightInputChange: (String) -> Unit,
    onEffectiveWeightFocusChange: (Boolean) -> Unit,
    onStageWeightInputChange: (String) -> Unit,
    onStageWeightFocusChange: (Boolean) -> Unit,
    onIncrementReps: () -> Unit,
    onDecrementReps: () -> Unit,
    onRepsInputChange: (String) -> Unit,
    onRepsFocusChange: (Boolean) -> Unit,
    onRegisterSet: () -> Unit,
    onCompleteExercise: () -> Unit,
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
            value = uiState.effectiveWeightInput,
            onValueChange = onEffectiveWeightInputChange,
            label = { Text("Peso efectivo (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .testTag("effectiveWeightField")
                .onFocusChanged { onEffectiveWeightFocusChange(it.isFocused) }
        )
        uiState.lastLoggedWeightKg?.let { lastWeight ->
            Text(
                text = "Última vez: ${formatWeight(lastWeight)} kg",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("lastLoggedWeightLabel")
            )
        }

        if (uiState.isComplete) {
            Text(
                text = "¡Ejercicio completado!",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("exerciseCompleteLabel")
            )
            uiState.completionCountdownSeconds?.let { remaining ->
                Text(
                    text = "Guardando y pasando al siguiente en ${remaining}s…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("completionCountdownLabel")
                )
                Button(
                    onClick = onCompleteExercise,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("completeExerciseButton")
                ) {
                    Text("Completar ejercicio")
                }
            }
        } else {
            uiState.currentSet?.let { current ->
                Text(
                    text = stageTitle(current),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag("currentStageLabel")
                )

                if (!uiState.currentStageWeightIsEffective) {
                    OutlinedTextField(
                        value = uiState.currentStageWeightInput,
                        onValueChange = onStageWeightInputChange,
                        label = { Text("Peso sugerido (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .testTag("stageWeightField")
                            .onFocusChanged { onStageWeightFocusChange(it.isFocused) }
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
                    OutlinedTextField(
                        value = uiState.currentRepsInput,
                        onValueChange = onRepsInputChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(72.dp)
                            .testTag("repsValue")
                            .onFocusChanged { onRepsFocusChange(it.isFocused) }
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

        OutlinedButton(onClick = onOpenNotes, modifier = Modifier.testTag("notesButton")) {
            Text("Notas")
        }
    }
}

/** Solo lectura: el alta/baja de fotos y videos se hace desde la pantalla de editar ejercicio
 * (`ExerciseFormScreen`) — acá solo se ven y se aplican al entrenamiento en curso. */
@Composable
private fun GalleryTab(galleryItems: List<GalleryItem>) {
    if (galleryItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin fotos ni videos todavía", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(96.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("galleryGrid"),
        ) {
            items(galleryItems, key = { it.id }) { item -> GalleryTile(item) }
        }
    }
}

@Composable
private fun GalleryTile(item: GalleryItem) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .testTag("galleryItem_${item.id}")
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (item.isVideo) {
            Text("▶ Video", style = MaterialTheme.typography.labelMedium)
        } else {
            AsyncImage(
                model = item.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun HistoryTab(summaries: List<ExerciseHistorySummary>) {
    if (summaries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin historial todavía", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("historyList"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(summaries, key = { it.exerciseId }) { summary ->
            HistorySummaryCard(summary)
            Row(modifier = Modifier.padding(vertical = 12.dp)) { HorizontalDivider() }
        }
    }
}

@Composable
private fun HistorySummaryCard(summary: ExerciseHistorySummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("historySummary_${summary.exerciseId}"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = summary.exerciseName, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Máximo: ${summary.maxWeightKg?.let { formatWeight(it) } ?: "-"} kg · Volumen total: ${formatWeight(summary.totalVolumeKg)} kg",
            style = MaterialTheme.typography.bodyMedium,
        )
        summary.recentSessions.forEach { session -> HistorySessionRow(session, note = summary.notesByDate[session.date]) }
        if (summary.recentSessions.isEmpty()) {
            Text("Sin series registradas", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HistorySessionRow(session: HistorySession, note: String?) {
    Column {
        Text(
            text = "${session.date} · ${session.setCount} series · ${formatWeight(session.volumeKg)} kg",
            style = MaterialTheme.typography.bodySmall,
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
