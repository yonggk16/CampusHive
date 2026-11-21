package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val priceFilters = listOf("All Prices", "Under RM20", "RM20 - RM50", "RM50+")
private val categoryFilters = ListingViewModel.CATEGORY_FILTERS
private val sortOptions = listOf("Newest", "Top Rated", "Lowest Price", "Highest Price")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialCategory: String = "",
    autoFocusSearch: Boolean = false,
    listingViewModel: ListingViewModel = viewModel(),
    onListingClick: (Listing) -> Unit = {}
) {
    val context = LocalContext.current
    val liveListings by listingViewModel.listings.collectAsState()
    val selectedCategoryFilter by listingViewModel.selectedCategory.collectAsState()
    val shouldFocusSearch by listingViewModel.shouldFocusSearch.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPriceFilter by remember { mutableStateOf("All Prices") }
    var selectedSortOption by remember { mutableStateOf("Newest") }
    var filtersExpanded by remember { mutableStateOf(false) }

    val hasCategoryFilter = selectedCategoryFilter.isNotBlank() &&
        !selectedCategoryFilter.equals("All Categories", ignoreCase = true)
    val hasPriceFilter = selectedPriceFilter != "All Prices"
    val hasSortFilter = selectedSortOption != "Newest"
    val activeFilterCount = (if (hasCategoryFilter) 1 else 0) +
        (if (hasPriceFilter) 1 else 0) +
        (if (hasSortFilter) 1 else 0)
    val hasActiveFilters = searchQuery.isNotBlank() || activeFilterCount > 0

    // Tracks whether user has scrolled down into the listings (with hysteresis to avoid mid-scroll jitter)
    var isScrolled by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (!isScrolled && (index > 0 || offset > 72)) {
                    isScrolled = true
                } else if (isScrolled && index == 0 && offset < 12) {
                    isScrolled = false
                }
            }
    }

    // Auto-collapse filters when the user starts scrolling down through listings
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && filtersExpanded) {
            filtersExpanded = false
        }
    }

    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    val searchCornerRadius by animateDpAsState(
        targetValue = if (isSearchFocused) 16.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "searchCornerRadius"
    )
    val searchShadowElevation by animateDpAsState(
        targetValue = if (isSearchFocused) 4.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "searchShadowElevation"
    )
    val searchScale by animateFloatAsState(
        targetValue = if (isSearchFocused) 1.0f else 0.995f,
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

    LaunchedEffect(autoFocusSearch, shouldFocusSearch) {
        if (autoFocusSearch || shouldFocusSearch) {
            listingViewModel.consumeSearchFocus()
            runCatching {
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            }
            delay(60.milliseconds)
            runCatching {
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    val searchResults = remember(searchQuery, selectedPriceFilter, selectedCategoryFilter, selectedSortOption, liveListings) {
        val filtered = liveListings.filter { listing ->
            val matchesCategory = when {
                selectedCategoryFilter.isBlank() || selectedCategoryFilter.equals("All Categories", ignoreCase = true) -> true
                selectedCategoryFilter.equals("Tech Help", ignoreCase = true) ->
                    listing.category.equals("Tech Help", ignoreCase = true) ||
                        listing.category.equals("Coding", ignoreCase = true) ||
                        listing.category.equals("Tech", ignoreCase = true)
                selectedCategoryFilter.equals("Errands", ignoreCase = true) ->
                    listing.category.equals("Errands", ignoreCase = true) ||
                        listing.category.equals("Campus Services", ignoreCase = true)
                selectedCategoryFilter.equals("Others", ignoreCase = true) ->
                    listing.category.equals("Others", ignoreCase = true) ||
                        listing.category.equals("Other", ignoreCase = true)
                else -> listing.category.equals(selectedCategoryFilter, ignoreCase = true)
            }

            val matchesQuery = searchQuery.isBlank() ||
                listing.title.contains(searchQuery, ignoreCase = true) ||
                listing.category.contains(searchQuery, ignoreCase = true) ||
                listing.providerName.contains(searchQuery, ignoreCase = true) ||
                listing.description.contains(searchQuery, ignoreCase = true) ||
                listing.campusLocation.contains(searchQuery, ignoreCase = true)

            val matchesPrice = when (selectedPriceFilter) {
                "Under RM20" -> listing.price < 20.0
                "RM20 - RM50" -> listing.price in 20.0..50.0
                "RM50+" -> listing.price > 50.0
                else -> true
            }

            matchesCategory && matchesQuery && matchesPrice
        }

        when (selectedSortOption) {
            "Top Rated" -> filtered.sortedWith(
                compareByDescending<Listing> { it.reviewCount > 0 }
                    .thenByDescending { it.rating }
                    .thenByDescending { it.reviewCount }
            )
            "Lowest Price" -> filtered.sortedBy { it.price }
            "Highest Price" -> filtered.sortedByDescending { it.price }
            else -> filtered.sortedByDescending { it.createdAt }
        }
    }

    fun resetAllFilters() {
        searchQuery = ""
        selectedPriceFilter = "All Prices"
        selectedSortOption = "Newest"
        listingViewModel.setSelectedCategory("All Categories")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
    ) {
        // =====================================================================
        // 1. STICKY HEADER: COLLAPSIBLE TITLE + ALWAYS-PINNED SEARCH + FILTERS
        // =====================================================================
        Surface(
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            shadowElevation = if (isScrolled || filtersExpanded) 4.dp else 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.gutter, vertical = 12.dp)
            ) {
                // Title row collapses smoothly once the user scrolls down
                AnimatedVisibility(
                    visible = !isScrolled,
                    enter = expandVertically(tween(180, easing = FastOutSlowInEasing)) + fadeIn(tween(150)),
                    exit = shrinkVertically(tween(160, easing = FastOutSlowInEasing)) + fadeOut(tween(120))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explore Services",
                            fontSize = 31.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            letterSpacing = (-0.5).sp
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = HoneyTint,
                            border = BorderStroke(
                                1.dp,
                                if (CampusThemeState.isDark) Hairline else WarningBorderSoft
                            )
                        ) {
                            Text(
                                text = "${searchResults.size} ${if (searchResults.size == 1) "gig" else "gigs"}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }
                    }
                }

                // Always-Pinned Search Input + Collapsible Filters Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
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
                        textStyle = LocalTextStyle.current.copy(color = Charcoal, fontSize = 14.sp),
                        placeholder = {
                            Text(
                                text = "Search services, skills, campus...",
                                fontSize = 13.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = if (isSearchFocused) Charcoal else MutedText,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.bounceClick(scaleDown = 0.85f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        tint = MutedText,
                                        modifier = Modifier.size(18.dp)
                                    )
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
                            focusedContainerColor = Cream,
                            unfocusedContainerColor = Cream,
                            focusedBorderColor = Charcoal,
                            unfocusedBorderColor = Hairline,
                            focusedTextColor = Charcoal,
                            unfocusedTextColor = Charcoal,
                            cursorColor = Charcoal
                        )
                    )

                    // Collapsible Filter Toggle Button with Active Count Badge
                    Box(contentAlignment = Alignment.TopEnd) {
                        val buttonBg = when {
                            filtersExpanded -> Honey
                            activeFilterCount > 0 -> HoneyTint
                            else -> Cream
                        }
                        val buttonBorder = when {
                            filtersExpanded -> Honey
                            activeFilterCount > 0 -> if (CampusThemeState.isDark) BrandHoney else WarningBorder
                            else -> Hairline
                        }
                        val iconTint = if (filtersExpanded) OnHoneyAccent else Charcoal

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = buttonBg,
                            border = BorderStroke(1.dp, buttonBorder),
                            modifier = Modifier
                                .size(50.dp)
                                .bounceClick(scaleDown = 0.92f) {
                                    filtersExpanded = !filtersExpanded
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = if (filtersExpanded) "Hide filters" else "Show filters",
                                    tint = iconTint,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        if (activeFilterCount > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 3.dp, y = (-3).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (filtersExpanded) BrandHoney else Honey)
                                    .border(1.5.dp, CardWhite, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeFilterCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (filtersExpanded) Color(0xFF09090B) else OnHoneyAccent
                                )
                            }
                        }
                    }
                }

                // Active Filter Pills Strip (shown when collapsed so active filters remain visible & removable)
                AnimatedVisibility(
                    visible = !filtersExpanded && activeFilterCount > 0,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasCategoryFilter) {
                            item {
                                ActiveFilterTag(
                                    label = selectedCategoryFilter,
                                    onClear = { listingViewModel.setSelectedCategory("All Categories") }
                                )
                            }
                        }
                        if (hasPriceFilter) {
                            item {
                                ActiveFilterTag(
                                    label = selectedPriceFilter,
                                    onClear = { selectedPriceFilter = "All Prices" }
                                )
                            }
                        }
                        if (hasSortFilter) {
                            item {
                                ActiveFilterTag(
                                    label = "Sort: $selectedSortOption",
                                    onClear = { selectedSortOption = "Newest" }
                                )
                            }
                        }
                        item {
                            Text(
                                text = "Reset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedText,
                                modifier = Modifier
                                    .bounceClick { resetAllFilters() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Collapsible Filters Panel (Category, Price, Sort)
                AnimatedVisibility(
                    visible = filtersExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(thickness = 0.8.dp, color = Hairline)

                        // Category Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CATEGORY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedText,
                                letterSpacing = 0.7.sp
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(categoryFilters) { catFilter ->
                                    val isSelected = catFilter.equals(selectedCategoryFilter, ignoreCase = true) ||
                                        (catFilter == "All Categories" && !hasCategoryFilter)
                                    CompactFilterChip(
                                        label = catFilter,
                                        selected = isSelected,
                                        onClick = {
                                            val nextCategory = if (isSelected && catFilter != "All Categories") {
                                                "All Categories"
                                            } else {
                                                catFilter
                                            }
                                            listingViewModel.setSelectedCategory(nextCategory)
                                        }
                                    )
                                }
                            }
                        }

                        // Price Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "PRICE RANGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedText,
                                letterSpacing = 0.7.sp
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(priceFilters) { filter ->
                                    CompactFilterChip(
                                        label = filter,
                                        selected = filter == selectedPriceFilter,
                                        onClick = { selectedPriceFilter = filter }
                                    )
                                }
                            }
                        }

                        // Sort Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SORT BY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedText,
                                letterSpacing = 0.7.sp
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(sortOptions) { sortOption ->
                                    CompactFilterChip(
                                        label = sortOption,
                                        selected = sortOption == selectedSortOption,
                                        isHoneyStyle = true,
                                        onClick = { selectedSortOption = sortOption }
                                    )
                                }
                            }
                        }

                        // Panel Footer: Reset + Done
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasActiveFilters) {
                                Text(
                                    text = "Reset filters",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedText,
                                    modifier = Modifier
                                        .bounceClick { resetAllFilters() }
                                        .padding(vertical = 4.dp)
                                )
                            } else {
                                Spacer(Modifier.width(1.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Honey,
                                modifier = Modifier.bounceClick(scaleDown = 0.94f) {
                                    filtersExpanded = false
                                }
                            ) {
                                Text(
                                    text = "Show ${searchResults.size} ${if (searchResults.size == 1) "result" else "results"}",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnHoneyAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 2. SCROLLABLE LISTINGS FEED
        // =====================================================================
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    listingViewModel.refreshUserListings()
                    delay(800.milliseconds)
                    isRefreshing = false
                    Toast.makeText(context, "Search results updated", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = Spacing.gutter,
                    end = Spacing.gutter,
                    top = 14.dp,
                    bottom = rememberBottomBarClearance()
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Results summary bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val headerLabel = when {
                            hasCategoryFilter -> "$selectedCategoryFilter Services"
                            searchQuery.isNotBlank() -> "Results for \"$searchQuery\""
                            else -> "All Available Gigs"
                        }
                        Text(
                            text = headerLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${searchResults.size} found",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                }

                // Results List or Empty State
                if (searchResults.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = Radius.Card,
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = if (liveListings.isEmpty()) "No campus services yet" else "No matching services found",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (liveListings.isEmpty())
                                        "Be the first student to publish a skill on CampusHive!"
                                    else
                                        "Try clearing your search or switching category and price filters.",
                                    color = MutedText,
                                    fontSize = 12.sp
                                )
                                if (hasActiveFilters && liveListings.isNotEmpty()) {
                                    Spacer(Modifier.height(14.dp))
                                    Button(
                                        onClick = { resetAllFilters() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Honey,
                                            contentColor = OnHoneyAccent
                                        ),
                                        shape = Radius.Pill
                                    ) {
                                        Text("Reset Filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(searchResults, key = { it.id }) { listing ->
                        SearchHorizontalListingBar(
                            listing = listing,
                            onClick = { onListingClick(listing) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactFilterChip(
    label: String,
    selected: Boolean,
    isHoneyStyle: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        selected && isHoneyStyle -> HoneyTint
        selected -> Honey
        else -> Cream
    }
    val textColor = when {
        selected && isHoneyStyle -> Charcoal
        selected -> OnHoneyAccent
        else -> Charcoal
    }
    val borderColor = when {
        selected && isHoneyStyle -> if (CampusThemeState.isDark) BrandHoney else WarningBorder
        selected -> Honey
        else -> Hairline
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.bounceClick(scaleDown = 0.93f, onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun ActiveFilterTag(
    label: String,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = HoneyTint,
        border = BorderStroke(1.dp, if (CampusThemeState.isDark) BrandHoney else WarningBorder),
        modifier = Modifier.bounceClick(scaleDown = 0.93f, onClick = onClear)
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 7.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal
            )
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Remove $label",
                tint = Charcoal,
                modifier = Modifier.size(13.dp)
            )
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
            .clip(Radius.Card)
            .bounceClick(scaleDown = 0.97f) { onClick() },
        shape = Radius.Card,
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
                shape = Radius.Inner,
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
                        shape = Radius.Tag
                    ) {
                        Text(
                            text = listing.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
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
                            tint = StarGold,
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
                        shape = Radius.Tag
                    ) {
                        Text(
                            text = "NEW",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                    }
                }
            }
        }
    }
}
