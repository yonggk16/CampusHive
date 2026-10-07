package com.example.campushive.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campushive.components.BottomBar
import com.example.campushive.screen.PlaceholderScreen

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object Search : Screen("search", "Search")
    data object Create : Screen("create", "New listing")
    data object Chats : Screen("chats", "Chats")
    data object Profile : Screen("profile", "Profile")
}

@Composable
fun CampusHiveApp() {
    val navController = rememberNavController()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            listOf(Screen.Home, Screen.Search, Screen.Create, Screen.Chats, Screen.Profile).forEach { s ->
                composable(s.route) { PlaceholderScreen(s.title) }
            }
        }
    }
}