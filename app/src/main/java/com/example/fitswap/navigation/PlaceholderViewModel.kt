package com.example.fitswap.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaceholderViewModel @Inject constructor() : ViewModel() {
    val message: String = "FitSwap"
}
