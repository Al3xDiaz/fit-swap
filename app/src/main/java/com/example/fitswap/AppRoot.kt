package com.example.fitswap

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.navigation.AppNavHost
import com.example.fitswap.ui.theme.FitSwapTheme
import com.example.fitswap.ui.theme.ThemeViewModel

@Composable
fun AppRoot(themeViewModel: ThemeViewModel = hiltViewModel()) {
    val appTheme by themeViewModel.appTheme.collectAsStateWithLifecycle()
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> systemDarkTheme
    }

    FitSwapTheme(darkTheme = darkTheme) {
        AppNavHost()
    }
}
