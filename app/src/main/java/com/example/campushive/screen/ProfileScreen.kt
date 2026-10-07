package com.example.campushive.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.ui.theme.*

@Composable
fun ProfileScreen(
    onHelpdeskClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("My listings") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )

        Spacer(Modifier.height(16.dp))

        // Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, Hairline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar "DW"
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Honey),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DW",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        fontSize = 24.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Name & Verified Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Daniel Wong",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Campus Verified",
                        tint = VerifiedGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "@ Block B • Year 2",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Stats Row (Rating, Jobs Done, Reviews)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Honey, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("4.9", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Charcoal)
                        }
                        Text("RATING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                    }

                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = Hairline)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("23", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Charcoal)
                        Text("JOBS DONE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                    }

                    Divider(modifier = Modifier.height(28.dp).width(1.dp), color = Hairline)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("12", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Charcoal)
                        Text("REVIEWS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Segmented Tabs: My listings | Reviews
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                listOf("My listings", "Reviews").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Charcoal else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CardWhite else Charcoal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tab Content
        if (selectedTab == "My listings") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(color = HoneyTint, shape = RoundedCornerShape(6.dp)) {
                            Text("PHOTOGRAPHY", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Event Photography", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal)
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("• Active", fontSize = 11.sp, color = VerifiedGreen, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Honey, modifier = Modifier.size(12.dp))
                            Text(" 4.9 (12)", fontSize = 11.sp, color = Charcoal)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text("RM 50/session", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                    }

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Cream, contentColor = Charcoal),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Hairline),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("EDIT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        } else {
            // Reviews Tab
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(HoneyTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("PK", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Charcoal)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Priya K.", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Honey, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("5.0", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Charcoal)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "\"Shot our club's annual dinner - super patient and the photos came out amazing. Highly recommend!\"",
                        fontSize = 12.sp,
                        color = Charcoal,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Helpdesk & Support Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onHelpdeskClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, Hairline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Security, contentDescription = null, tint = Charcoal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Helpdesk & FAQ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal)
                        Text("Campus safety, reports & guidelines", fontSize = 11.sp, color = MutedText)
                    }
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MutedText)
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}
