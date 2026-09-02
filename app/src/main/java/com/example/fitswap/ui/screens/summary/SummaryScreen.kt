package com.example.fitswap.ui.screens.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.fitswap.domain.logic.MonthlyVolume
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.MenuNavigationIcon
import java.time.YearMonth

private val monthAbbreviations = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

@Composable
fun SummaryScreen(
    onMenuClick: () -> Unit,
    onViewExerciseHistory: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(title = "Resumen", navigationIcon = { MenuNavigationIcon(onMenuClick = onMenuClick) })
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

        val report = uiState.report

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Entrenamientos completados (este mes): ${report.completedWorkoutsThisMonth}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("completedWorkoutsLabel")
            )
            Text(
                text = "Racha actual: ${report.currentStreakWeeks} ${weekLabel(report.currentStreakWeeks)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("streakLabel")
            )
            Text(
                text = "Volumen total (series efectivas)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            report.monthlyVolumes.forEach { monthly -> MonthlyVolumeRow(monthly) }
            OutlinedButton(
                onClick = onViewExerciseHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .testTag("viewExerciseHistoryButton")
            ) {
                Text("Ver historial por ejercicio")
            }
        }
    }
}

@Composable
private fun MonthlyVolumeRow(monthly: MonthlyVolume) {
    Text(
        text = "${monthLabel(monthly.yearMonth)}: ${formatWeight(monthly.volumeKg)} kg",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.testTag("monthlyVolume_${monthly.yearMonth}")
    )
}

private fun weekLabel(weeks: Int): String = if (weeks == 1) "semana" else "semanas"

private fun monthLabel(yearMonth: YearMonth): String =
    "${monthAbbreviations[yearMonth.monthValue - 1]} ${yearMonth.year}"

private fun formatWeight(kg: Double): String = "%.1f".format(kg)
