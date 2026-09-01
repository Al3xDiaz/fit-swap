package com.example.fitswap.navigation

sealed class Destination(val route: String) {
    data object Placeholder : Destination("placeholder")
}
