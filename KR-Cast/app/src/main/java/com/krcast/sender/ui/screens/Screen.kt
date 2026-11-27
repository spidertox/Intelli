package com.krcast.sender.ui.screens

sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    object Discovery : Screen()
    object ConnectionGuide : Screen()
    object Casting : Screen()
    object Settings : Screen()
    object Diagnostics : Screen()
}
