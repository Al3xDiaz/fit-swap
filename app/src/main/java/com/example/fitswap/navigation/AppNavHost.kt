package com.example.fitswap.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destination.Placeholder.route) {
        composable(Destination.Placeholder.route) {
            PlaceholderScreen()
        }
    }
}

@Composable
private fun PlaceholderScreen(viewModel: PlaceholderViewModel = hiltViewModel()) {
    Scaffold { innerPadding ->
        Text(text = viewModel.message, modifier = Modifier.padding(innerPadding))
    }
}
