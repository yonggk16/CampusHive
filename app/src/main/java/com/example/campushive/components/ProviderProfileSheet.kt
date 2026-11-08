package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.model.Listing
import com.example.campushive.ui.theme.*
import com.example.campushive.ui.theme.AvatarOutline
import com.example.campushive.viewmodel.ProviderProfileViewModel

/**
 * Bottom-sheet preview of another student's profile.
 *
 * Opened from the provider card on a service listing, and from the avatar/name
 * in a chat header. Read-only by design: the viewer can see who they're dealing
 * with (verification, faculty, rating history, other services) and then take one
 * action — view a service or open the chat.
 *
 * Handles the common case where `users/{uid}` has no document: it falls back to
 * the display name passed in from the call site rather than showing a blank sheet.
 *
 * @param onDismiss close handler.
 * @param onListingClick optional; tapping one of the provider's other services.
 * @param onMessageClick optional; primary CTA. Hidden when null (e.g. own profile).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileSheet(
    uid: String,
    displayName: String,
    onDismiss: () -> Unit,
    isOnline: Boolean = false,
    serviceTitle: String? = null,
    viewModel: ProviderProfileViewModel = viewModel(),
    onListingClick: ((Listing) -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val listings by viewModel.listings.collectAsStateWithLifecycle()

    LaunchedEffect(uid) {
        viewModel.load(uid = uid, fallbackName = displayName)
    }

    val name = profile?.displayName?.takeIf { it.isNotBlank() } ?: displayName
    val initials = remember(name) {
        name.split(" ").filter { it.isNotBlank() }.take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("").ifBlank { "S" }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppMainBackground,
        shape = Radius.Card,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MutedText.copy(alpha = 0.4f)) }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.gutter, end = Spacing.gutter,
                top = 0.dp, bottom = Spacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { SheetHeader(name, initials, isOnline, profile?.isVerifiedStudent == true, onDismiss) }
            item { StatRow(reviews.size, viewModel.averageRating(), listings.size) }

            if (serviceTitle != null) {
                item { ContextLine(serviceTitle) }
            }

            profile?.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                item { AboutSection(bio) }
            }

            item {
                DetailsSection(
                    faculty = profile?.facultyOrMajor,
                    campus = profile?.campusLocation,
                    isVerified = profile?.isVerifiedStudent == true
                )
            }

            if (reviews.isNotEmpty()) {
                item {
                    SectionLabel("RECENT REVIEWS (${reviews.size})")
                }
                items(reviews.take(3)) { review ->
                    ReviewPreviewRow(
                        reviewer = review.reviewerName.ifBlank { "Student" },
                        comment = review.comment,
                        stars = review.rating
                    )
                }
            }

            if (listings.isNotEmpty() && onListingClick != null) {
                item {
                    SectionLabel("OTHER SERVICES (${listings.size})")
                }
                items(listings.take(3)) { listing ->
                    ProviderListingRow(listing) { onListingClick(listing) }
                }
            }

            if (onMessageClick != null) {
                item {
                    Spacer(Modifier.height(Spacing.xs))
                    PrimaryButton(text = "Message ${name.substringBefore(' ')}", onClick = onMessageClick)
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(
    name: String,
    initials: String,
    isOnline: Boolean,
    isVerified: Boolean,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(HoneyTint)
                    .border(1.5.dp, AvatarOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) OnlineGreen else OfflineGrey)
                    .border(2.dp, CardWhite, CircleShape)
            )
        }

        Spacer(Modifier.width(Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                letterSpacing = (-0.3).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isVerified) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = Charcoal,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Campus Verified",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) OnlineGreen else OfflineGrey)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isOnline) "Online now" else "Offline",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedText
                )
            }
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CardWhite)
                .border(1.dp, Hairline, CircleShape)
                .clip(CircleShape)
                .bounceClick(scaleDown = 0.92f, onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close",
                tint = Charcoal,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun StatRow(reviewCount: Int, avgRating: Double, listingCount: Int) {
    Surface(
        shape = Radius.Card,
        color = CardWhite,
        border = BorderStroke(1.dp, Hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatCell(
                value = if (reviewCount > 0) String.format("%.1f", avgRating) else "—",
                label = "RATING",
                modifier = Modifier.weight(1f),
                showStar = reviewCount > 0
            )
            StatDivider()
            StatCell(
                value = reviewCount.toString(),
                label = "REVIEWS",
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            StatCell(
                value = listingCount.toString(),
                label = "LISTINGS",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    showStar: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showStar) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(3.dp))
            }
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MutedText,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Hairline)
    )
}

@Composable
private fun ContextLine(serviceTitle: String) {
    Surface(
        shape = Radius.Tile,
        color = HoneyTint,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Viewing about",
                fontSize = 11.sp,
                color = Charcoal.copy(alpha = 0.65f)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = serviceTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MutedText,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = Spacing.xs)
    )
}

@Composable
private fun AboutSection(bio: String) {
    Column {
        SectionLabel("ABOUT")
        Spacer(Modifier.height(6.dp))
        Text(
            text = bio,
            fontSize = 13.sp,
            color = Charcoal.copy(alpha = 0.85f),
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun DetailsSection(faculty: String?, campus: String?, isVerified: Boolean) {
    val rows = buildList {
        faculty?.takeIf { it.isNotBlank() }?.let { add(Icons.Outlined.School to it) }
        campus?.takeIf { it.isNotBlank() }?.let { add(Icons.Outlined.LocationOn to it) }
    }
    if (rows.isEmpty()) return

    Column {
        SectionLabel("DETAILS")
        Spacer(Modifier.height(6.dp))
        Surface(
            shape = Radius.Card,
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
                rows.forEachIndexed { index, (icon, text) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Charcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (index < rows.lastIndex) {
                        HorizontalDivider(color = Hairline, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewPreviewRow(reviewer: String, comment: String, stars: Double) {
    Surface(
        shape = Radius.Inner,
        color = CardWhite,
        border = BorderStroke(1.dp, Hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reviewer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                repeat(5) { i ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (i < stars) StarGold else Hairline,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
            if (comment.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = comment,
                    fontSize = 12.sp,
                    color = Charcoal.copy(alpha = 0.8f),
                    lineHeight = 17.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Rating only",
                    fontSize = 11.sp,
                    color = MutedText,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun ProviderListingRow(listing: Listing, onClick: () -> Unit) {
    Surface(
        shape = Radius.Inner,
        color = CardWhite,
        border = BorderStroke(1.dp, Hairline),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.98f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServiceCategoryIconBox(
                category = listing.category,
                size = 36.dp,
                shape = Radius.Tile,
                symbolFontSize = 14.sp,
                iconSize = 17.dp
            )
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listing.category,
                    fontSize = 11.sp,
                    color = MutedText
                )
            }
            Text(
                text = if (listing.pricingType == com.example.campushive.model.PricingType.FIXED)
                    "RM ${listing.price.toInt()}"
                else
                    "RM ${listing.price.toInt()}/hr",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )
        }
    }
}
