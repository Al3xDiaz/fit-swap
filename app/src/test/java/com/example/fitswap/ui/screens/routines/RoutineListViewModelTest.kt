package com.example.fitswap.ui.screens.routines

import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.time.FixedCurrentDateProvider
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RoutineListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `expone las rutinas sembradas con la rutina por defecto marcada`() = runTest {
        val viewModel = RoutineListViewModel(FakeRoutineRepository(), FixedCurrentDateProvider(DayOfWeek.MONDAY))

        val routines = viewModel.routines.first { it.isNotEmpty() }

        assertTrue(routines.any { it.isDefault })
        assertTrue(routines.size >= 2)
    }
}
