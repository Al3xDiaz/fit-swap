package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.domain.logic.ExerciseCatalogSearch
import com.example.fitswap.domain.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val exercises: StateFlow<List<Exercise>> =
        combine(exerciseRepository.observeExercises(), _query) { allExercises, query ->
            ExerciseCatalogSearch.filter(allExercises, query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }
}
