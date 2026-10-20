package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.model.Listing
import com.example.campushive.ui.theme.*

private data class ServiceCategory(
    val id: String,
    val name: String,
    val listingCount: String,
    val backgroundColor: Color,
    val icon: ImageVector,
    val symbolText: String? = null
)

private val homeCategories = listOf(
    ServiceCategory("tutoring", "Tutoring", "24 listings", CardWhite, Icons.Outlined.School, "Σ"),
    ServiceCategory("design", "Design", "18 listings", CardWhite, Icons.Outlined.Palette, "✎"),
    ServiceCategory("tech", "Tech help", "15 listings", CardWhite, Icons.Outlined.Code, "</>"),
    ServiceCategory("errands", "Errands", "9 listings", CardWhite, Icons.Filled.Check, "✓")
)

private data class QuickHelpItem(
    val iconBg: Color,
    val iconSymbol: String,
    val title: String,
    val categoryQuery: String
)

private val quickHelpItems = listOf(
    QuickHelpItem(Color(0xFFFFF1C9), "Σ", "Study for an exam", "Tutoring"),
    QuickHelpItem(Color(0xFFFFF1C9), "✎", "Design a poster", "Design"),
    QuickHelpItem(Color(0xFFFFF1C9), "</>", "Fix my code or laptop", "Tech help")
)

@Composable
fun HomeScreen(
    onListingClick: (Listing) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCreateService: () -> Unit = {},
    onOpenChat: (chatId: String, studentName: String, serviceTitle: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundGradient)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 120.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // 1. Top Greeting Row ("Hi, Daniel" + Notification Bell Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hi, Daniel",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                letterSpacing = (-0.8).sp
            )

            IconButton(
                onClick = {
                    Toast.makeText(context, "You're all caught up! No new notifications.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
                    .border(BorderStroke(1.dp, Hairline), CircleShape)
                    .bounceClick(scaleDown = 0.90f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Charcoal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 2. Search Services Bar
        Surface(
            shape = RoundedCornerShape(50),
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(horizontal = 20.dp)
                .bounceClick(scaleDown = 0.97f) { onNavigateToSearch() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MutedText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Search services",
                    fontSize = 15.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 3. Up Next Dark Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .bounceClick(scaleDown = 0.98f) {
                    onOpenChat("chat_1", "Alex", "Calculus Tutoring")
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Charcoal),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UP NEXT · SAT 10:00",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CardWhite,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Calculus with Alex",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CardWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = { onOpenChat("chat_1", "Alex", "Calculus Tutoring") },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = CardWhite, contentColor = Charcoal),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Open",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // 4. Section Header: "Browse services"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Browse services",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
                letterSpacing = (-0.2).sp
            )
            Text(
                text = "See all",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                modifier = Modifier.bounceClick { onNavigateToSearch() }
            )
        }

        Spacer(Modifier.height(10.dp))

        // 5. Browse Services Carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(homeCategories) { cat ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .bounceClick(scaleDown = 0.94f) { onNavigateToSearch() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Hairline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(HoneyTint),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cat.symbolText != null) {
                                Text(
                                    text = cat.symbolText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Charcoal
                                )
                            } else {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.name,
                                    tint = Charcoal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = cat.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = cat.listingCount,
                                fontSize = 12.sp,
                                color = Charcoal.copy(alpha = 0.75f),
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 6. Section Header: "What do you need help with?"
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "What do you need help with?",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
                letterSpacing = (-0.2).sp
            )
        }

        Spacer(Modifier.height(10.dp))

        // 7. Grouped Help Action List
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    quickHelpItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSearch() }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(item.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.iconSymbol,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Charcoal
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Charcoal,
                                modifier = Modifier.bounceClick(scaleDown = 0.92f) { onNavigateToSearch() }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Find",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CardWhite
                                    )
                                }
                            }
                        }

                        if (index < quickHelpItems.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 64.dp),
                                color = Hairline,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 8. "WHAT'S NEW" Callout Offer Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .bounceClick(scaleDown = 0.98f) { onNavigateToCreateService() },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, Hairline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "WHAT'S NEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.6.sp
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Got a skill? List it in 2 minutes.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Offer →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                }
            }
        }
    }
}
