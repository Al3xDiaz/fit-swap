package com.example.fitswap.ui.screens.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.domain.model.Routine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val currentDateProvider: CurrentDateProvider,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])

    val routine: StateFlow<Routine?> = routineRepository.observeRoutine(routineId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _selectedDayIndex = MutableStateFlow(0)
    val selectedDayIndex: StateFlow<Int> = _selectedDayIndex.asStateFlow()

    init {
        viewModelScope.launch {
            val loadedRoutine = routine.first { it != null } ?: return@launch
            _selectedDayIndex.value = defaultDayIndexFor(loadedRoutine)
        }
    }

    fun selectDay(index: Int) {
        val days = routine.value?.days ?: return
        if (index !in days.indices) return
        _selectedDayIndex.value = index
    }

    fun deleteRoutine() {
        viewModelScope.launch { routineRepository.deleteRoutine(routineId) }
    }

    /** El día de hoy si la rutina tiene uno para ese día de la semana; si no, el primero. */
    private fun defaultDayIndexFor(routine: Routine): Int {
        val today = currentDateProvider.today().dayOfWeek
        val todayIndex = routine.days.indexOfFirst { it.dayOfWeek == today }
        return if (todayIndex >= 0) todayIndex else 0
    }
}
