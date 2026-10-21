package com.example.campushive.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.Listing
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ListingViewModel

private val popularSearchTags = listOf("Calculus Tutor", "Python", "Poster Design", "Dorm Move", "Proofreading", "Java", "Canva")
private val priceFilters = listOf("All Prices", "Under RM20", "RM20 - RM50", "RM50+")

@Composable
fun SearchScreen(
    onListingClick: (Listing) -> Unit = {},
    viewModel: ListingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriceFilter by remember { mutableStateOf("All Prices") }
    val listings by viewModel.listings.collectAsState()

    val searchResults = remember(listings, searchQuery, selectedPriceFilter) {
        listings.filter { listing ->
            val matchesQuery = searchQuery.isBlank() ||
                    listing.title.contains(searchQuery, ignoreCase = true) ||
                    listing.category.contains(searchQuery, ignoreCase = true) ||
                    listing.providerName.contains(searchQuery, ignoreCase = true) ||
                    listing.description.contains(searchQuery, ignoreCase = true)

            val matchesPrice = when (selectedPriceFilter) {
                "Under RM20" -> listing.price < 20.0
                "RM20 - RM50" -> listing.price in 20.0..50.0
                "RM50+" -> listing.price > 50.0
                else -> true
            }

            matchesQuery && matchesPrice
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header, Search Field, Tags & Filters
            item {
                Column {
                    Text(
                        text = "Explore Campus Services",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Charcoal
                    )

                    Spacer(Modifier.height(14.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by skill, major, or course name...", fontSize = 13.sp, color = MutedText) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Charcoal) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.bounceClick(scaleDown = 0.85f)
                                ) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = MutedText)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedBorderColor = Honey,
                            unfocusedBorderColor = Hairline
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // Popular Searches Tags
                    Text(
                        text = "Popular Searches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(popularSearchTags) { tag ->
                            Surface(
                                color = CardWhite,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Hairline),
                                modifier = Modifier.bounceClick(scaleDown = 0.93f) { searchQuery = tag }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.History, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(tag, fontSize = 12.sp, color = Charcoal)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Price Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(priceFilters) { filter ->
                            FilterChip(
                                selected = filter == selectedPriceFilter,
                                onClick = { selectedPriceFilter = filter },
                                label = { Text(filter, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Honey,
                                    selectedLabelColor = Charcoal,
                                    containerColor = CardWhite,
                                    labelColor = MutedText
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.bounceClick(scaleDown = 0.92f)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "All Available Gigs" else "Results for '$searchQuery'",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                        Text(
                            text = "${searchResults.size} found",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                }
            }

            // Results List (Horizontal Bar Cards)
            if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No campus services matched your search.",
                            color = MutedText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(searchResults) { listing ->
                    SearchHorizontalListingBar(
                        listing = listing,
                        onClick = { onListingClick(listing) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHorizontalListingBar(
    listing: Listing,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .bounceClick(scaleDown = 0.97f) { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, Hairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Visual Box matching Home Page Style
            ServiceCategoryIconBox(
                category = listing.category,
                size = 52.dp,
                shape = RoundedCornerShape(16.dp),
                symbolFontSize = 20.sp,
                iconSize = 24.dp
            )

            Spacer(Modifier.width(12.dp))

            // Main Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = HoneyTint,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = listing.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = listing.campusLocation,
                            fontSize = 11.sp,
                            color = MutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = listing.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = listing.providerName,
                        fontSize = 12.sp,
                        color = MutedText,
                        fontWeight = FontWeight.Medium
                    )
                    if (listing.isVerifiedStudent) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified Student",
                            tint = VerifiedGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // Right Price & Rating Column
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "RM${"%.0f".format(listing.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Text(
                        text = listing.pricingType.shortLabel,
                        fontSize = 10.sp,
                        color = MutedText
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${listing.rating}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Text(
                        text = " (${listing.reviewCount})",
                        fontSize = 10.sp,
                        color = MutedText
                    )
                }
            }
        }
    }
}