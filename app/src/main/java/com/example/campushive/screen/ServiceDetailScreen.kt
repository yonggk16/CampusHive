package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.sampleListings
import com.example.campushive.ui.theme.*

@Composable
fun ServiceDetailScreen(
    listingId: String,
    onBackClick: () -> Unit,
    onChatClick: (studentName: String, serviceTitle: String) -> Unit
) {
    val context = LocalContext.current
    val listing = sampleListings.find { it.id == listingId } ?: sampleListings.first()
    var isFavorite by remember { mutableStateOf(false) }

    val initials = listing.providerName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
    val firstName = listing.providerName.split(" ").firstOrNull() ?: listing.providerName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image/Banner Box (Top Category Icon Showcase)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(HoneyTint, Cream)
                        )
                    )
            ) {
                // Floating Top Navigation Row (Back & Heart Buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CardWhite)
                            .bounceClick(scaleDown = 0.90f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Charcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isFavorite = !isFavorite
                            Toast.makeText(context, if (isFavorite) "Saved to favorites!" else "Removed from favorites", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CardWhite)
                            .bounceClick(scaleDown = 0.88f)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Charcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Center Category Vector Icon Showcase matching Home Page Style
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ServiceCategoryIconBox(
                        category = listing.category,
                        size = 72.dp,
                        shape = RoundedCornerShape(24.dp),
                        symbolFontSize = 28.sp,
                        iconSize = 34.dp
                    )

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        color = CardWhite,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Hairline)
                    ) {
                        Text(
                            text = listing.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                    }
                }
            }

            // Detail Body Content
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category & Location Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFFFF7DB),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = listing.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8C6B10)
                        )
                    }

                    Surface(
                        color = Color(0xFFF6F3EE),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MutedText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = listing.campusLocation,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Charcoal
                            )
                        }
                    }
                }

                // Title
                Text(
                    text = listing.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    lineHeight = 28.sp
                )

                // Rating & Campus Verified Badge Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = listing.rating.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Charcoal
                        )
                        Text(
                            text = " (${listing.reviewCount} reviews)",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }

                    Surface(
                        color = Color(0xFFEBFBF3),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = VerifiedGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Campus Verified",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedGreen
                            )
                        }
                    }
                }

                // Dynamic Provider Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(scaleDown = 0.98f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Honey),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Charcoal
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "${listing.providerName} • ${listing.providerMajor}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Charcoal
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(VerifiedGreen)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Usually responds in ~10 min",
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = MutedText
                        )
                    }
                }

                // ABOUT THIS SERVICE Section
                Column {
                    Text(
                        text = "ABOUT THIS SERVICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = listing.description.ifBlank {
                            "1-on-1 tutoring and student service. High quality assistance and past year study notes included."
                        },
                        fontSize = 13.sp,
                        color = Charcoal,
                        lineHeight = 20.sp
                    )
                }

                // Specs Chips Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, Hairline)
                    ) {
                        Text(
                            text = "Weekdays & Weekends",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Charcoal
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, Hairline)
                    ) {
                        Text(
                            text = "On-campus or online",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Charcoal
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        // Sticky Bottom Booking Bar
        Surface(
            color = CardWhite,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "RM ${"%.0f".format(listing.price)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Text(
                        text = listing.pricingType.shortLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedText,
                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                    )
                }

                Button(
                    onClick = { onChatClick(listing.providerName, listing.title) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    modifier = Modifier.bounceClick(scaleDown = 0.95f)
                ) {
                    Text(
                        text = "Chat with $firstName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Honey,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
