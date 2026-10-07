package com.example.campushive.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.components.ListingCard
import com.example.campushive.model.Listing
import com.example.campushive.model.sampleListings
import com.example.campushive.ui.theme.*

private val popularSearchTags = listOf("Calculus Tutor", "Python", "Poster Design", "Dorm Move", "Proofreading", "Java", "Canva")
private val priceFilters = listOf("All Prices", "Under RM20", "RM20 - RM50", "RM50+")

@Composable
fun SearchScreen(
    onListingClick: (Listing) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriceFilter by remember { mutableStateOf("All Prices") }

    val searchResults = remember(searchQuery, selectedPriceFilter) {
        sampleListings.filter { listing ->
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header, Search Field, Tags & Filters Span Across 2 Columns
            item(span = { GridItemSpan(2) }) {
                Column {
                    Spacer(Modifier.height(8.dp))

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
                                IconButton(onClick = { searchQuery = "" }) {
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
                                border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
                                modifier = Modifier.clickable { searchQuery = tag }
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
                                shape = RoundedCornerShape(20.dp)
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

                    Spacer(Modifier.height(4.dp))
                }
            }

            // Results Grid Items
            if (searchResults.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
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
                    ListingCard(listing,
                        onClick = { onListingClick(listing)})
                }
            }
        }
    }
}