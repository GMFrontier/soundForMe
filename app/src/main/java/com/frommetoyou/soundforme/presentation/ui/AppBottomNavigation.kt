package com.frommetoyou.soundforme.presentation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.presentation.navigation.HomeGraph
import com.frommetoyou.soundforme.presentation.navigation.SettingsScreen
import kotlinx.serialization.Serializable


@Composable
fun CentralNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = BottomScreens.Home.route){
        homeNavGraph(navController)
    }

}

@Composable
fun AppBottomNavigation(navController: NavController) {
    val bottomScreens = remember {
        listOf(
            BottomScreens.Home,
            BottomScreens.Settings,
        )
    }

    NavigationBar {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination

        bottomScreens.forEach { screen ->
            val isSelected = currentDestination?.route == screen.route.toString()

            NavigationBarItem(
                icon = {
                    if (isSelected) Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = screen.selectedIcon),
                        contentDescription = screen.name
                    )
                    else Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = screen.unselectedIcon),
                        contentDescription = screen.name
                    )
                },
                label = { Text(screen.name) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Serializable
sealed class BottomScreens<T>(
    val name: String, // the name of the tab
    val selectedIcon: Int, // filled icon when selected
    val unselectedIcon: Int, // unfilled icon when not selected
    val route: T //graphs defined in feature module for each tab

) {

    @Serializable
    data object Home : BottomScreens<HomeGraph>(
        name = "Home",
        unselectedIcon = R.drawable.ic_launcher_background,
        selectedIcon = R.drawable.ic_launcher_background,
        route = HomeGraph
    )

    @Serializable
    data object Settings : BottomScreens<SettingsScreen>(
        name = "Search",
        unselectedIcon = R.drawable.ic_launcher_background,
        selectedIcon = R.drawable.ic_launcher_background,
        route = SettingsScreen
    )

}