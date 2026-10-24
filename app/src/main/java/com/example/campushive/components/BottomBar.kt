package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.campushive.navigation.Screen
import com.example.campushive.ui.theme.*

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.viewmodel.ChatViewModel

private data class Tab(val screen: Screen, val icon: ImageVector)

private val tabsLeft = listOf(
    Tab(Screen.Home, Icons.Outlined.Home),
    Tab(Screen.Search, Icons.Outlined.Search),
)
private val tabsRight = listOf(
    Tab(Screen.Chats, Icons.Outlined.ChatBubbleOutline),
    Tab(Screen.Profile, Icons.Outlined.PersonOutline),
)

@Composable
fun BottomBar(
    nav: NavController,
    chatViewModel: ChatViewModel = viewModel()
) {
    val current = nav.currentBackStackEntryAsState().value?.destination?.route
    val conversations by chatViewModel.conversations.collectAsState()
    val totalUnread = remember(conversations) { conversations.sumOf { it.unreadCount } }

    fun go(s: Screen) = nav.navigate(s.route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    fun isTabSelected(tab: Tab, currentRoute: String?): Boolean {
        if (currentRoute == null) return false
        return when (tab.screen) {
            Screen.Home -> currentRoute == Screen.Home.route
            Screen.Search -> currentRoute.startsWith("search") || currentRoute == Screen.Search.route
            Screen.Chats -> currentRoute.startsWith("chats") || currentRoute == Screen.Chats.route
            Screen.Profile -> currentRoute.startsWith("profile") || currentRoute == Screen.Profile.route
            else -> currentRoute == tab.screen.route
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabsLeft.forEach { TabItem(it, isTabSelected(it, current), 0, Modifier.weight(1f)) { go(it.screen) } }
                Spacer(Modifier.weight(1f)) // space for floating center "+" button
                tabsRight.forEach { TabItem(it, isTabSelected(it, current), if (it.screen == Screen.Chats) totalUnread else 0, Modifier.weight(1f)) { go(it.screen) } }
            }
        }
        // Floating Honey "+" Button with instant spring press feedback
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Honey)
                .bounceClick(scaleDown = 0.92f) { go(Screen.Create) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New listing", tint = Charcoal)
        }
    }
}

@Composable
private fun TabItem(
    tab: Tab,
    selected: Boolean,
    unreadCount: Int = 0,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) HoneyTint else CardWhite)
            .bounceClick(scaleDown = 0.95f, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(tab.icon, null, tint = if (selected) Charcoal else MutedText, modifier = Modifier.size(22.dp))

            if (tab.screen == Screen.Chats && unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .offset(x = 3.dp, y = (-3).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD32F2F)) // Red Unread Message Dot Indicator
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        Text(
            tab.screen.title,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Charcoal else MutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
