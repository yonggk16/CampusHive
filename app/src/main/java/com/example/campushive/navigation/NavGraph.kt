package com.example.campushive.navigation

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campushive.components.BottomBar
import com.example.campushive.components.ChatNotificationHelper
import com.example.campushive.screen.*
import com.example.campushive.viewmodel.ChatViewModel
import com.example.campushive.viewmodel.HelpdeskViewModel
import com.example.campushive.viewmodel.ListingViewModel
import com.example.campushive.viewmodel.ReviewViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// ==============================================================================
// 1. NAVIGATION SCREEN ROUTE CONTRACTS
// ==============================================================================
sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "Log In")
    data object SignUp : Screen("signup", "Sign Up")
    data object Home : Screen("home", "Home")
    data object Search : Screen("search", "Search")
    data object Create : Screen("create", "New listing")
    data object Chats : Screen("chats", "Chats")
    data object Profile : Screen("profile", "Profile")
}

// ==============================================================================
// 2. ROOT APPLICATION SCAFFOLD & NAV HOST
// ==============================================================================
@Composable
fun CampusHiveApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val listingViewModel: ListingViewModel = viewModel()
    val reviewViewModel: ReviewViewModel = viewModel()
    val helpdeskViewModel: HelpdeskViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    // Trigger Android system heads-up notification whenever a new incoming chat message arrives
    val incomingNotification by chatViewModel.incomingNotificationEvent.collectAsState()
    LaunchedEffect(incomingNotification) {
        val conv = incomingNotification
        if (conv != null) {
            ChatNotificationHelper.showChatNotification(context, conv)
            chatViewModel.consumeNotificationEvent()
        }
    }

    // Start destination is Home if user is signed in, else Login
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Show bottom bar on main tabs (Home, Search, Chats, Profile)
    val showBottomBar = currentRoute != null && (
        currentRoute == Screen.Home.route ||
        currentRoute.startsWith("search") ||
        currentRoute == Screen.Chats.route ||
        currentRoute == Screen.Profile.route
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { if (showBottomBar) BottomBar(navController, chatViewModel) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(180)) +
                    scaleIn(
                        initialScale = 0.985f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(140))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(180))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(140))
            }
        ) {
            // ==================================================================
            // 3. AUTHENTICATION DESTINATIONS
            // ==================================================================

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

            // ==================================================================
            // 4. MAIN NAVIGATION TABS
            // ==================================================================

            // Home Screen (New User Onboarding + Live Bounties)
            composable(Screen.Home.route) {
                HomeScreen(
                    listingViewModel = listingViewModel,
                    chatViewModel = chatViewModel,
                    onListingClick = { listing ->
                        navController.navigate("service_detail/${listing.id}")
                    },
                    onNavigateToSearch = { category ->
                        listingViewModel.setSelectedCategory(category)
                        val normalized = ListingViewModel.normalizeCategoryFilter(category)
                        val route = if (category.isNotBlank() && normalized != "All Categories") {
                            "search?category=${Uri.encode(normalized)}&focusSearch=false"
                        } else {
                            Screen.Search.route
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onSearchBarClick = {
                        listingViewModel.setSelectedCategory("All Categories")
                        listingViewModel.requestSearchFocus()
                        navController.navigate("search?category=&focusSearch=true") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onNavigateToCreateService = {
                        navController.navigate(Screen.Create.route)
                    },
                    onOpenChat = { chatId, studentName, serviceTitle ->
                        val encodedChatId = Uri.encode(chatId)
                        val encodedName = Uri.encode(studentName.ifBlank { "Student" })
                        val encodedTitle = Uri.encode(serviceTitle.ifBlank { "Campus Service" })
                        navController.navigate("chat_detail/$encodedChatId/$encodedName/$encodedTitle")
                    }
                )
            }

            // Search Screen with Optional Category & Auto-Focus Parameters
            composable(
                route = "search?category={category}&focusSearch={focusSearch}",
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("focusSearch") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val initialCategory = Uri.decode(backStackEntry.arguments?.getString("category") ?: "")
                val focusSearch = backStackEntry.arguments?.getBoolean("focusSearch") ?: false
                SearchScreen(
                    initialCategory = initialCategory,
                    autoFocusSearch = focusSearch,
                    listingViewModel = listingViewModel,
                    onListingClick = { listing ->
                        navController.navigate("service_detail/${listing.id}")
                    }
                )
            }

            // Chats Screen
            composable(Screen.Chats.route) {
                ChatsScreen(
                    chatViewModel = chatViewModel,
                    reviewViewModel = reviewViewModel,
                    onChatClick = { chat ->
                        val encodedChatId = Uri.encode(chat.id)
                        val encodedName = Uri.encode(chat.studentName.ifBlank { "Student" })
                        val encodedTitle = Uri.encode(chat.serviceTitle.ifBlank { "Campus Service" })
                        navController.navigate("chat_detail/$encodedChatId/$encodedName/$encodedTitle") {
                            launchSingleTop = true
                        }
                    },
                    onReviewClick = { chat ->
                        val extractedListingId = chat.listingId.ifBlank {
                            ChatViewModel.extractListingIdFromChatId(chat.id)
                        }
                        val encodedName = Uri.encode(chat.studentName.ifBlank { "Student Provider" })
                        val encodedTitle = Uri.encode(chat.serviceTitle.ifBlank { "Campus Service" })
                        val encodedListingId = Uri.encode(extractedListingId)
                        navController.navigate("post_review/$encodedName/$encodedTitle?listingId=$encodedListingId")
                    }
                )
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    listingViewModel = listingViewModel,
                    reviewViewModel = reviewViewModel,
                    onHelpdeskClick = {
                        navController.navigate("helpdesk")
                    },
                    onCreateServiceClick = {
                        navController.navigate(Screen.Create.route)
                    },
                    onSignOutClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ==================================================================
            // 5. MARKETPLACE & SERVICE CREATION
            // ==================================================================

            // Service Detail Destination
            composable("service_detail/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                ServiceDetailScreen(
                    listingId = listingId,
                    listingViewModel = listingViewModel,
                    reviewViewModel = reviewViewModel,
                    helpdeskViewModel = helpdeskViewModel,
                    onBackClick = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.Search.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onChatClick = { providerUid, providerName, serviceTitle ->
                        val activeUser = Firebase.auth.currentUser
                        val activeUid = activeUser?.uid.orEmpty()
                        val activeUserName = activeUser?.displayName?.trim()?.takeIf { it.isNotBlank() }
                            ?: activeUser?.email?.substringBefore("@")?.trim()
                            ?: ""
                        val targetListing = listingViewModel.listings.value.find { it.id == listingId }
                        val realProviderUid = providerUid.ifBlank { targetListing?.providerUid.orEmpty() }
                        val isOwnListing = (activeUid.isNotBlank() && realProviderUid.isNotBlank() && activeUid == realProviderUid) ||
                            (activeUserName.isNotBlank() && providerName.isNotBlank() && activeUserName.equals(providerName, ignoreCase = true))

                        if (isOwnListing) {
                            navController.navigate(Screen.Chats.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
                        } else {
                            val uniqueChatId = ChatViewModel.buildConversationId(listingId, activeUid)
                            chatViewModel.startConversation(
                                conversationId = uniqueChatId,
                                listingId = listingId,
                                providerUid = realProviderUid,
                                providerName = providerName,
                                serviceTitle = serviceTitle
                            )
                            val encodedChatId = Uri.encode(uniqueChatId)
                            val encodedProviderName = Uri.encode(providerName.ifBlank { "Provider" })
                            val encodedServiceTitle = Uri.encode(serviceTitle.ifBlank { "Campus Service" })
                            navController.navigate("chat_detail/$encodedChatId/$encodedProviderName/$encodedServiceTitle") {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // Create Service Screen (New Listing)
            composable(Screen.Create.route) {
                CreateServiceScreen(
                    listingViewModel = listingViewModel,
                    onClose = {
                        navController.popBackStack()
                    },
                    onServiceCreated = {
                        listingViewModel.setSelectedCategory("All Categories")
                        navController.navigate("search?category=All%20Categories&focusSearch=false") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }

            // ==================================================================
            // 6. REAL-TIME CHAT & FEEDBACK REVIEWS
            // ==================================================================

            // Chat Detail Destination
            composable("chat_detail/{chatId}/{studentName}/{serviceTitle}") { backStackEntry ->
                val chatId = Uri.decode(backStackEntry.arguments?.getString("chatId") ?: "")
                val studentName = Uri.decode(backStackEntry.arguments?.getString("studentName") ?: "Student")
                val serviceTitle = Uri.decode(backStackEntry.arguments?.getString("serviceTitle") ?: "Campus Service")

                ChatDetailScreen(
                    chatId = chatId,
                    studentName = studentName,
                    serviceTitle = serviceTitle,
                    chatViewModel = chatViewModel,
                    helpdeskViewModel = helpdeskViewModel,
                    onBackClick = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.Chats.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onViewServiceClick = { targetListingId ->
                        if (targetListingId.isNotBlank()) {
                            navController.navigate("service_detail/$targetListingId") {
                                popUpTo("service_detail/$targetListingId") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // Post Review Destination
            composable(
                route = "post_review/{providerName}/{serviceTitle}?listingId={listingId}",
                arguments = listOf(
                    navArgument("listingId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val providerName = Uri.decode(backStackEntry.arguments?.getString("providerName") ?: "Student Provider")
                val serviceTitle = Uri.decode(backStackEntry.arguments?.getString("serviceTitle") ?: "Campus Service")
                val listingId = Uri.decode(backStackEntry.arguments?.getString("listingId") ?: "")

                PostReviewScreen(
                    listingId = listingId,
                    providerName = providerName,
                    serviceTitle = serviceTitle,
                    reviewViewModel = reviewViewModel,
                    onBackClick = { navController.popBackStack() },
                    onReviewSubmitted = { navController.popBackStack() }
                )
            }

            // ==================================================================
            // 7. HELPDESK & MODERATION PORTAL DESTINATIONS
            // ==================================================================

            // Helpdesk Destination
            composable("helpdesk") {
                HelpdeskScreen(
                    helpdeskViewModel = helpdeskViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToMyReports = {
                        navController.navigate("my_reports")
                    },
                    onNavigateToModeratorQueue = {
                        navController.navigate("moderator_queue")
                    }
                )
            }

            // My Reports & Tickets Destination
            composable("my_reports") {
                MyReportsScreen(
                    helpdeskViewModel = helpdeskViewModel,
                    initialModeratorTab = false,
                    onBackClick = { navController.popBackStack() },
                    onReportClick = { reportId ->
                        navController.navigate("report_detail/$reportId")
                    }
                )
            }

            // Moderator Queue Destination
            composable("moderator_queue") {
                MyReportsScreen(
                    helpdeskViewModel = helpdeskViewModel,
                    initialModeratorTab = true,
                    onBackClick = { navController.popBackStack() },
                    onReportClick = { reportId ->
                        navController.navigate("report_detail/$reportId")
                    }
                )
            }

            // Report Detail Destination
            composable("report_detail/{reportId}") { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                ReportDetailScreen(
                    reportId = reportId,
                    helpdeskViewModel = helpdeskViewModel,
                    onBackClick = { navController.popBackStack() },
                    onDeleteOrUpdate = { navController.popBackStack() },
                    onViewListing = { listingId ->
                        if (listingId.isNotBlank()) {
                            navController.navigate("service_detail/$listingId")
                        }
                    }
                )
            }
        }
    }
}
