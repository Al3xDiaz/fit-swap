package com.example.fitswap.ui.screens.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.domain.model.Routine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])

    val routine: StateFlow<Routine?> = routineRepository.observeRoutine(routineId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun deleteRoutine() {
        viewModelScope.launch { routineRepository.deleteRoutine(routineId) }
    }
}
