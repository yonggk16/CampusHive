package com.example.campushive.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campushive.components.BottomBar
import com.example.campushive.screen.*

sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "Log In")
    data object SignUp : Screen("signup", "Sign Up")
    data object Home : Screen("home", "Home")
    data object Search : Screen("search", "Search")
    data object Create : Screen("create", "New listing")
    data object Chats : Screen("chats", "Chats")
    data object Profile : Screen("profile", "Profile")
}

@Composable
fun CampusHiveApp() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Show bottom bar only on main tabs (Home, Search, Chats, Profile)
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Chats.route,
        Screen.Profile.route
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // Login Screen
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToSignUp = {
                            navController.navigate(Screen.SignUp.route)
                        }
                    )
                }

                // Sign Up Screen
                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onSignUpSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // Home Screen (New User Onboarding + Live Bounties)
                composable(Screen.Home.route) {
                    HomeScreen(
                        onListingClick = { listing ->
                            navController.navigate("service_detail/${listing.id}")
                        },
                        onNavigateToSearch = {
                            navController.navigate(Screen.Search.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToCreateService = {
                            navController.navigate(Screen.Create.route)
                        },
                        onOpenChat = { chatId, studentName, serviceTitle ->
                            navController.navigate("chat_detail/$chatId/$studentName/$serviceTitle")
                        }
                    )
                }

                // Search Screen
                composable(Screen.Search.route) {
                    SearchScreen(
                        onListingClick = { listing ->
                            navController.navigate("service_detail/${listing.id}")
                        }
                    )
                }

                // Service Detail Destination
                composable("service_detail/{listingId}") { backStackEntry ->
                    val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                    ServiceDetailScreen(
                        listingId = listingId,
                        onBackClick = { navController.popBackStack() },
                        onChatClick = { providerName, serviceTitle ->
                            navController.navigate("chat_detail/chat_1/${providerName}/${serviceTitle}")
                        }
                    )
                }

                // Create Service Screen (New Listing)
                composable(Screen.Create.route) {
                    CreateServiceScreen(
                        onServiceCreated = {
                            navController.popBackStack()
                        }
                    )
                }

                // Chats Screen
                composable(Screen.Chats.route) {
                    ChatsScreen(
                        onChatClick = { chat ->
                            navController.navigate("chat_detail/${chat.id}/${chat.studentName}/${chat.serviceTitle}")
                        },
                        onReviewClick = { chat ->
                            navController.navigate("post_review/${chat.studentName}/${chat.serviceTitle}")
                        }
                    )
                }

                // Chat Detail Destination
                composable("chat_detail/{chatId}/{studentName}/{serviceTitle}") { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                    val studentName = backStackEntry.arguments?.getString("studentName") ?: "Student"
                    val serviceTitle = backStackEntry.arguments?.getString("serviceTitle") ?: "Campus Service"

                    ChatDetailScreen(
                        chatId = chatId,
                        studentName = studentName,
                        serviceTitle = serviceTitle,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Post Review Destination
                composable("post_review/{providerName}/{serviceTitle}") { backStackEntry ->
                    val providerName = backStackEntry.arguments?.getString("providerName") ?: "Student Provider"
                    val serviceTitle = backStackEntry.arguments?.getString("serviceTitle") ?: "Campus Service"

                    PostReviewScreen(
                        providerName = providerName,
                        serviceTitle = serviceTitle,
                        onBackClick = { navController.popBackStack() },
                        onReviewSubmitted = { navController.popBackStack() }
                    )
                }

                // Profile Screen
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onHelpdeskClick = {
                            navController.navigate("helpdesk")
                        }
                    )
                }

                // Helpdesk Destination
                composable("helpdesk") {
                    HelpdeskScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BottomBar(navController)
            }
        }
    }
}
