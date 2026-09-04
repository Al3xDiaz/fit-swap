package com.example.fitswap.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.domain.model.Routine
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RoutineListViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    currentDateProvider: CurrentDateProvider,
) : ViewModel() {

    val routines: StateFlow<List<Routine>> = routineRepository.observeRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Fijo por instancia — usado para resaltar "hoy" y como día inicial de [selectedWeekDay]
     * cuando se muestra la rutina por defecto directo en la pantalla principal. */
    val todayDayOfWeek: DayOfWeek = currentDateProvider.today().dayOfWeek

    private val _selectedWeekDay = MutableStateFlow(todayDayOfWeek)
    val selectedWeekDay: StateFlow<DayOfWeek> = _selectedWeekDay.asStateFlow()

    fun selectWeekDay(day: DayOfWeek) {
        _selectedWeekDay.value = day
    }

    fun setDefaultRoutine(routineId: String) {
        viewModelScope.launch { routineRepository.setDefaultRoutine(routineId) }
    }

    fun clearDefaultRoutine() {
        viewModelScope.launch { routineRepository.clearDefaultRoutine() }
    }
}
