package com.example.fitswap.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.domain.model.Routine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class RoutineListViewModel @Inject constructor(
    routineRepository: RoutineRepository,
) : ViewModel() {

    val routines: StateFlow<List<Routine>> = routineRepository.observeRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
