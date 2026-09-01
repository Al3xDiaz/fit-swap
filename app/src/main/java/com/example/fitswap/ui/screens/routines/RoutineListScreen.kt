package com.example.fitswap.ui.screens.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import com.example.fitswap.domain.model.Routine
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.MenuNavigationIcon

@Composable
fun RoutineListScreen(
    onMenuClick: () -> Unit,
    onRoutineClick: (String) -> Unit,
    onNewRoutineClick: () -> Unit,
    viewModel: RoutineListViewModel = hiltViewModel(),
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar(title = "Rutinas", navigationIcon = { MenuNavigationIcon(onMenuClick = onMenuClick) }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(routines, key = { it.id }) { routine ->
                RoutineListItem(
                    routine = routine,
                    onClick = { onRoutineClick(routine.id) },
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
}

@Composable
private fun RoutineListItem(routine: Routine, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (routine.isDefault) {
                Icon(Icons.Default.Star, contentDescription = "Rutina por defecto")
            }
            Text(routine.name, style = MaterialTheme.typography.titleMedium)
        }
    }
}
