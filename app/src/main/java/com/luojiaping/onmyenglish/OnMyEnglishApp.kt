package com.luojiaping.onmyenglish

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.luojiaping.onmyenglish.feature.settings.SettingsRoute
import com.luojiaping.onmyenglish.feature.stats.StatsScreen
import com.luojiaping.onmyenglish.feature.study.StudyScreen
import com.luojiaping.onmyenglish.feature.wordbook.WordbookRoute

private enum class MainDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    STUDY("study", "学习", Icons.Outlined.School),
    WORDBOOK("wordbook", "词库", Icons.Outlined.MenuBook),
    STATS("stats", "统计", Icons.Outlined.BarChart),
    SETTINGS("settings", "设置", Icons.Outlined.Settings),
}

@Composable
fun OnMyEnglishApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = MainDestination.STUDY.route,
            modifier = Modifier.padding(contentPadding),
        ) {
            composable(MainDestination.STUDY.route) { StudyScreen() }
            composable(MainDestination.WORDBOOK.route) { WordbookRoute() }
            composable(MainDestination.STATS.route) { StatsScreen() }
            composable(MainDestination.SETTINGS.route) { SettingsRoute() }
        }
    }
}
