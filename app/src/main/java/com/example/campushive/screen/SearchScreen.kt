package com.example.campushive.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.Listing
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ListingViewModel
import kotlinx.coroutines.delay

private val popularSearchTags = listOf("Calculus Tutor", "Python", "Poster Design", "Dorm Move", "Proofreading", "Java", "Canva")
private val priceFilters = listOf("All Prices", "Under RM20", "RM20 - RM50", "RM50+")
private val categoryFilters = ListingViewModel.CATEGORY_FILTERS

@Composable
fun SearchScreen(
    initialCategory: String = "",
    autoFocusSearch: Boolean = false,
    listingViewModel: ListingViewModel = viewModel(),
    onListingClick: (Listing) -> Unit = {}
) {
    val liveListings by listingViewModel.listings.collectAsState()
    val selectedCategoryFilter by listingViewModel.selectedCategory.collectAsState()
    val shouldFocusSearch by listingViewModel.shouldFocusSearch.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPriceFilter by remember { mutableStateOf("All Prices") }

    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    // Critically-damped spring morph from Home Screen pill (24.dp corner radius) to active Search input (18.dp corner radius + subtle lift)
    val searchCornerRadius by animateDpAsState(
        targetValue = if (isSearchFocused) 18.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "searchCornerRadius"
    )
    val searchShadowElevation by animateDpAsState(
        targetValue = if (isSearchFocused) 6.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "searchShadowElevation"
    )
    val searchScale by animateFloatAsState(
        targetValue = if (isSearchFocused) 1.0f else 0.99f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "searchScale"
    )

    LaunchedEffect(initialCategory) {
        if (initialCategory.isNotBlank()) {
            listingViewModel.setSelectedCategory(initialCategory)
        }
        searchQuery = ""
        selectedPriceFilter = "All Prices"
    }

    // Automatically focus the search input and show the soft keyboard when transitioning from the Home Screen search bar
    LaunchedEffect(autoFocusSearch, shouldFocusSearch) {
        if (autoFocusSearch || shouldFocusSearch) {
            listingViewModel.consumeSearchFocus()
            // Request focus immediately on composition and show keyboard so user can type right away
            runCatching {
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            }
            delay(60)
            runCatching {
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    val searchResults = remember(searchQuery, selectedPriceFilter, selectedCategoryFilter, liveListings) {
        liveListings.filter { listing ->
            val matchesCategory = when {
                selectedCategoryFilter.isBlank() || selectedCategoryFilter.equals("All Categories", ignoreCase = true) -> true
                selectedCategoryFilter.equals("Tech Help", ignoreCase = true) ->
                    listing.category.equals("Tech Help", ignoreCase = true) ||
                        listing.category.equals("Coding", ignoreCase = true) ||
                        listing.category.equals("Tech", ignoreCase = true)
                selectedCategoryFilter.equals("Errands", ignoreCase = true) ->
                    listing.category.equals("Errands", ignoreCase = true) ||
                        listing.category.equals("Campus Services", ignoreCase = true)
                else -> listing.category.equals(selectedCategoryFilter, ignoreCase = true)
            }

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

            matchesCategory && matchesQuery && matchesPrice
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header, Search Field, Category Chips, Price Chips, Tags
            item {
                Column {
                    Text(
                        text = "Explore Campus Services",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Charcoal
                    )

                    Spacer(Modifier.height(14.dp))

                    // Seamless Search Field with Auto-Focus & Critically-Damped Spring Morph
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = searchScale
                                scaleY = searchScale
                            }
                            .shadow(
                                elevation = searchShadowElevation,
                                shape = RoundedCornerShape(searchCornerRadius),
                                ambientColor = Charcoal.copy(alpha = 0.08f),
                                spotColor = Charcoal.copy(alpha = 0.12f)
                            )
                            .focusRequester(searchFocusRequester)
                            .onFocusChanged { focusState ->
                                isSearchFocused = focusState.isFocused
                            },
                        placeholder = { Text("Search services, skills, or courses...", fontSize = 14.sp, color = MutedText) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = if (isSearchFocused) Charcoal else MutedText
                            )
                        },
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        shape = RoundedCornerShape(searchCornerRadius),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedBorderColor = Honey,
                            unfocusedBorderColor = Hairline
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Category Filter Chips
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categoryFilters) { catFilter ->
                            val isSelected = catFilter.equals(selectedCategoryFilter, ignoreCase = true) ||
                                    (catFilter == "All Categories" && (selectedCategoryFilter.isBlank() || selectedCategoryFilter.equals("All Categories", ignoreCase = true)))
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val nextCategory = if (isSelected && catFilter != "All Categories") "All Categories" else catFilter
                                    listingViewModel.setSelectedCategory(nextCategory)
                                },
                                label = { Text(catFilter, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Charcoal,
                                    selectedLabelColor = CardWhite,
                                    containerColor = CardWhite,
                                    labelColor = MutedText
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.bounceClick(scaleDown = 0.92f)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

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

                    Spacer(Modifier.height(14.dp))

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
                        val headerLabel = when {
                            selectedCategoryFilter.isNotBlank() && selectedCategoryFilter != "All Categories" -> "$selectedCategoryFilter Services"
                            searchQuery.isNotBlank() -> "Results for '$searchQuery'"
                            else -> "All Available Gigs"
                        }
                        Text(
                            text = headerLabel,
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
                            text = if (liveListings.isEmpty()) "No campus services published yet. Be the first to list a skill!" else "No services matched your category or search.",
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
            // Category Visual Box
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

                if (listing.reviewCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "%.1f".format(listing.rating),
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
                } else {
                    Surface(
                        color = HoneyTint,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "NEW",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                    }
                }
            }
        }
    }
}
