package com.frommetoyou.soundforme.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.frommetoyou.soundforme.presentation.ui.screens.HomeScreen
import com.frommetoyou.soundforme.presentation.ui.screens.MusicSelectionScreen
import com.frommetoyou.soundforme.presentation.ui.screens.SettingScreen

fun NavGraphBuilder.homeNavGraph(navController: NavController) {
        composable<HomeScreen> { HomeScreen() }
        composable<SettingsScreen> { SettingScreen(navController = navController) }
}
fun NavGraphBuilder.musicGraph(navController: NavController) {
        composable<MusicSelectionScreen> { MusicSelectionScreen(navController = navController) }
}