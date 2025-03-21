package com.frommetoyou.soundforme.presentation.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import kotlinx.serialization.Serializable

@Composable
fun CentralNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomScreens.Home.route
    ) {
        homeNavGraph(navController)
        musicGraph(navController)
    }
}

@Composable
fun AppBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomScreens = getBottomScreens()

    val bottomBarState by remember(navBackStackEntry) {
        derivedStateOf {
            when (navBackStackEntry?.destination?.route) {
                null -> true
                HomeScreen::class.qualifiedName, SettingsScreen::class.qualifiedName -> true
                else -> false
            }
        }
    }

    if (bottomBarState)
        NavigationBar {

            bottomScreens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any {
                    it.route == screen.route::class.qualifiedName.toString()
                } == true

                NavigationBarItem(
                    icon = {
                        if (isSelected) Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = screen.selectedIcon),
                            contentDescription = screen.name.asString(
                                LocalContext.current
                            )
                        )
                        else Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = screen.unselectedIcon),
                            contentDescription = screen.name.asString(
                                LocalContext.current
                            )
                        )
                    },
                    label = { Text(screen.name.asString(LocalContext.current)) },
                    selected = isSelected,
                    onClick = {
                        if (isSelected.not())
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

@Composable
fun getBottomScreens(): List<BottomScreens<out Any>> {
    return listOf(
        BottomScreens.Home,
        BottomScreens.Settings,
    )
}

@Serializable
sealed class BottomScreens<T>(
    val name: UiText, // the name of the tab
    val selectedIcon: Int, // filled icon when selected
    val unselectedIcon: Int, // unfilled icon when not selected
    val route: T //graphs defined in feature module for each tab

) {

    @Serializable
    data object Home : BottomScreens<HomeScreen>(
        name = UiText.DynamicString("Home"),
        unselectedIcon = R.drawable.ic_outline_home_24,
        selectedIcon = R.drawable.ic_baseline_home_24,
        route = HomeScreen
    )

    @Serializable
    data object Settings : BottomScreens<SettingsScreen>(
        name = UiText.StringResource(R.string.settings),
        unselectedIcon = R.drawable.ic_outline_settings_24,
        selectedIcon = R.drawable.ic_baseline_settings_24,
        route = SettingsScreen
    )

}