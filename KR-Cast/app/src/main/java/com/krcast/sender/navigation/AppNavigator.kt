package com.krcast.sender.navigation

import androidx.compose.foundation.navigation.NavHost
import androidx.compose.foundation.navigation.composable
import androidx.compose.foundation.navigation.rememberNavController
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.krcast.sender.ui.theme.KRCastTheme
import com.krcast.sender.ui.screens.HomeScreen
import com.krcast.sender.ui.screens.DiscoveryScreen
import com.krcast.sender.ui.screens.ConnectionGuideScreen
import com.krcast.sender.ui.screens.CastingScreen
import com.krcast.sender.ui.screens.SettingsScreen
import com.krcast.sender.ui.screens.DiagnosticsScreen
import com.krcast.sender.ui.screens.SplashScreen
import com.krcast.sender.ui.screens.Screen

@Composable
fun KRNavigation() {
    val navController = rememberNavController()

    KRCastTheme {
        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.name
            ) {
                composable(Screen.Splash.name) { SplashScreen(onFinished = { navController.navigate(Screen.Home.name) }) }
                composable(Screen.Home.name) { HomeScreen(onFindTv = { navController.navigate(Screen.Discovery.name) }, onCompatibility = { navController.navigate(Screen.Diagnostics.name) }) }
                composable(Screen.Discovery.name) { DiscoveryScreen(onBack = { navController.popBackStack() }, onConnect = { navController.navigate(Screen.ConnectionGuide.name) }) }
                composable(Screen.ConnectionGuide.name) { ConnectionGuideScreen(onBack = { navController.popBackStack() }, onCast = { navController.navigate(Screen.Casting.name) }) }
                composable(Screen.Casting.name) { CastingScreen(onStop = { navController.popBackStack() }) }
                composable(Screen.Settings.name) { SettingsScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Diagnostics.name) { DiagnosticsScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}
