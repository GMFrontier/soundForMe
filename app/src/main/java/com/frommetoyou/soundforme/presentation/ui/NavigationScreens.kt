package com.frommetoyou.soundforme.presentation.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.frommetoyou.soundforme.presentation.navigation.HomeGraph
import com.frommetoyou.soundforme.presentation.navigation.HomeScreen
import com.frommetoyou.soundforme.presentation.navigation.SettingsScreen
import com.frommetoyou.soundforme.presentation.ui.screens.HomeScreen
import com.frommetoyou.soundforme.presentation.ui.screens.SettingScreen


fun NavGraphBuilder.homeNavGraph(navController: NavController) {
    navigation<HomeGraph>(startDestination = SettingsScreen) {
        composable<HomeScreen> { HomeScreen() }
        composable<SettingsScreen> { SettingScreen() }
    }
}