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
fun BottomBar(nav: NavController) {
    val current = nav.currentBackStackEntryAsState().value?.destination?.route

    fun go(s: Screen) = nav.navigate(s.route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    // Outer layout anchor (100% transparent, no box background or border)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)

    ) {
        // Floating White Pill Surface (Only this pill is drawn!)
        Surface(
            shape = RoundedCornerShape(50),
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabsLeft.forEach { TabItem(it, current == it.screen.route, Modifier.weight(1f)) { go(it.screen) } }
                Spacer(Modifier.weight(1f)) // space for floating center "+" button
                tabsRight.forEach { TabItem(it, current == it.screen.route, Modifier.weight(1f)) { go(it.screen) } }
            }
        }

        // Floating Center "+" Create Service Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Charcoal)
                .bounceClick(scaleDown = 0.92f) { go(Screen.Create) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New listing",
                tint = CardWhite,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TabItem(tab: Tab, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) Charcoal else CardWhite)
            .bounceClick(scaleDown = 0.95f, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = if (selected) CardWhite else MutedText,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tab.screen.title,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) CardWhite else MutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
