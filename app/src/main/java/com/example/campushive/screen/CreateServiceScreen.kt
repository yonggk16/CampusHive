package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.LISTING_TIME_SLOTS
import com.example.campushive.model.LISTING_WEEK_DAYS
import com.example.campushive.model.Listing
import com.example.campushive.model.PricingType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.AuthViewModel
import com.example.campushive.viewmodel.ListingViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

private val categories = listOf("Tutoring", "Design", "Tech Help", "Errands", "Others")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateServiceScreen(
    listingViewModel: ListingViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onClose: () -> Unit = {},
    onServiceCreated: () -> Unit = {},
) {
    val context = LocalContext.current
    val userProfile by authViewModel.userProfile.collectAsState()
    var isPublishing by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tutoring") }
    var priceText by remember { mutableStateOf("") }
    var selectedPricingType by remember { mutableStateOf(PricingType.PER_HOUR) }
    var description by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf("Mon", "Tue", "Wed", "Thu", "Fri")) }
    var selectedTimeSlots by remember { mutableStateOf(setOf("Afternoon (12pm–5pm)")) }

    var hasAttemptedPublish by remember { mutableStateOf(false) }

    val currentUser = Firebase.auth.currentUser
    val isGoogleUser = currentUser?.providerData?.any { it.providerId == "google.com" } == true
    val isVerifiedStudent = isGoogleUser || currentUser?.isEmailVerified == true || userProfile?.isVerifiedStudent == true

    val maxAllowedPrice = if (selectedPricingType == PricingType.PER_HOUR) 50.0 else 1000.0
    val maxPriceHint = if (selectedPricingType == PricingType.PER_HOUR) "Max RM 50 / hr" else "Max RM 1,000"
    val parsedPriceInput = priceText.trim().toDoubleOrNull()

    val titleError = if (hasAttemptedPublish && title.isBlank()) {
        "Please enter a service title"
    } else null

    val priceError = when {
        priceText.isNotBlank() && parsedPriceInput == null -> "Enter a valid number"
        priceText.isNotBlank() && parsedPriceInput != null && parsedPriceInput <= 0.0 -> "Price must be greater than RM 0"
        priceText.isNotBlank() && parsedPriceInput != null && parsedPriceInput > maxAllowedPrice -> {
            if (selectedPricingType == PricingType.PER_HOUR) {
                "Exceeds max rate of RM 50 / hr"
            } else {
                "Exceeds max price of RM 1,000"
            }
        }
        hasAttemptedPublish && priceText.isBlank() -> "Please enter a price"
        else -> null
    }

    val availabilityError = when {
        hasAttemptedPublish && selectedDays.isEmpty() -> "Select at least one available day"
        hasAttemptedPublish && selectedTimeSlots.isEmpty() -> "Select at least one preferred time slot"
        else -> null
    }

    val availabilitySummary = remember(selectedDays, selectedTimeSlots) {
        val dayLabel = "${selectedDays.size} ${if (selectedDays.size == 1) "day" else "days"}"
        val slotLabel = "${selectedTimeSlots.size} ${if (selectedTimeSlots.size == 1) "slot" else "slots"}"
        "$dayLabel · $slotLabel"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
    ) {
        // =====================================================================
        // 1. CLEAN CURVED TOP HEADER
        // =====================================================================
        Surface(
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.gutter, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceCategoryIconBox(
                        category = selectedCategory,
                        size = 44.dp,
                        shape = RoundedCornerShape(14.dp),
                        symbolFontSize = 18.sp,
                        iconSize = 20.dp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "New listing",
                            fontSize = 31.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Offer a skill or service on campus",
                            fontSize = 12.sp,
                            color = MutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CreamTint)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Charcoal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // =====================================================================
        // 2. ORGANIZED CARD-BASED FORM BODY
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.gutter, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Unverified Student Warning Banner
            if (!isVerifiedStudent) {
                Surface(
                    color = WarningBg,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, WarningBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = WarningFg,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verification required to offer services",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = WarningFg
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Only verified students can publish listings. Please verify your student email in your profile first.",
                                fontSize = 11.sp,
                                color = WarningFg,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // CARD 1: SERVICE DETAILS (Category, Title, Description)
            // -----------------------------------------------------------------
            FormSectionCard(
                sectionTitle = "SERVICE DETAILS",
                trailingLabel = selectedCategory
            ) {
                // Category Chips (wrapped — no right-edge clipping)
                Text(
                    text = "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Honey else Cream,
                            contentColor = if (isSelected) OnHoneyAccent else Charcoal,
                            border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                            modifier = Modifier.bounceClick(scaleDown = 0.94f) { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) OnHoneyAccent else Charcoal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Service Title Input
                Text(
                    text = "Service title",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
                Spacer(Modifier.height(6.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (titleError != null) DangerBgSoft else Cream,
                    border = BorderStroke(
                        width = if (titleError != null) 1.5.dp else 1.dp,
                        color = if (titleError != null) DangerFgStrong else Hairline
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        if (title.isEmpty()) {
                            Text(
                                text = "e.g. Weekend Calculus & Stats Tutoring",
                                fontSize = 14.sp,
                                color = MutedText.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Charcoal
                            ),
                            cursorBrush = SolidColor(Charcoal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                InlineValidationError(message = titleError)

                Spacer(Modifier.height(16.dp))

                // Description Input with inline counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Description",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                    Text(
                        text = "${description.length} / 300",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
                Spacer(Modifier.height(6.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Cream,
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp)
                            .padding(14.dp)
                    ) {
                        if (description.isEmpty()) {
                            Text(
                                text = "What you offer, what's included, and any prerequisites...",
                                fontSize = 14.sp,
                                color = MutedText.copy(alpha = 0.7f),
                                lineHeight = 20.sp
                            )
                        }
                        BasicTextField(
                            value = description,
                            onValueChange = { if (it.length <= 300) description = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = Charcoal,
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(Charcoal),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // CARD 2: PRICING
            // -----------------------------------------------------------------
            FormSectionCard(
                sectionTitle = "PRICING",
                trailingLabel = maxPriceHint,
                isTrailingError = priceError != null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // RM Amount Box
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (priceError != null) DangerBgSoft else Cream,
                        border = BorderStroke(
                            width = if (priceError != null) 1.5.dp else 1.dp,
                            color = if (priceError != null) DangerFgStrong else Hairline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RM",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (priceError != null) DangerFgStrong else MutedText
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (priceText.isEmpty()) {
                                    Text(
                                        text = "0",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedText.copy(alpha = 0.55f)
                                    )
                                }
                                BasicTextField(
                                    value = priceText,
                                    onValueChange = { input ->
                                        val cleaned = input.filter { it.isDigit() || it == '.' }
                                        if (cleaned.count { it == '.' } <= 1) {
                                            priceText = cleaned
                                        }
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (priceError != null) DangerFgStrong else Charcoal
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    cursorBrush = SolidColor(if (priceError != null) DangerFgStrong else Charcoal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Pricing Type Segmented Toggle (Per hour | Fixed)
                    Surface(
                        modifier = Modifier.height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = CreamTint
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PricingType.entries.forEach { pType ->
                                val isSelected = selectedPricingType == pType
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(if (isSelected) Honey else Color.Transparent)
                                        .bounceClick { selectedPricingType = pType }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pType.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) OnHoneyAccent else Charcoal
                                    )
                                }
                            }
                        }
                    }
                }
                InlineValidationError(message = priceError)
            }

            // -----------------------------------------------------------------
            // CARD 3: AVAILABILITY & TIMING
            // -----------------------------------------------------------------
            FormSectionCard(
                sectionTitle = "AVAILABILITY",
                trailingLabel = availabilitySummary
            ) {
                Text(
                    text = "Days available",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
                Spacer(Modifier.height(8.dp))

                // Equal-width 7-day row (all 7 days visible, zero clipping)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    LISTING_WEEK_DAYS.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Honey else Cream,
                            border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick(scaleDown = 0.92f) {
                                    selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                }
                        ) {
                            Text(
                                text = day,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 9.dp),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) OnHoneyAccent else Charcoal,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Preferred time slots",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
                Spacer(Modifier.height(8.dp))

                // Wrapped time slots (all 4 slots visible, zero clipping)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LISTING_TIME_SLOTS.forEach { slot ->
                        val isSelected = selectedTimeSlots.contains(slot)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) HoneyTint else Cream,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) {
                                    if (CampusThemeState.isDark) BrandHoney else WarningBorder
                                } else {
                                    Hairline
                                }
                            ),
                            modifier = Modifier.bounceClick(scaleDown = 0.94f) {
                                selectedTimeSlots = if (isSelected) selectedTimeSlots - slot else selectedTimeSlots + slot
                            }
                        ) {
                            Text(
                                text = slot,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Charcoal
                            )
                        }
                    }
                }
                InlineValidationError(message = availabilityError)
            }

            Spacer(Modifier.height(4.dp))
        }

        // =====================================================================
        // 3. ANCHORED CURVED BOTTOM PUBLISH BAR
        // =====================================================================
        Surface(
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = Spacing.gutter, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Your listing goes live as soon as you publish.",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }

                    Button(
                        onClick = {
                            if (!isVerifiedStudent) {
                                Toast.makeText(
                                    context,
                                    "Only verified students can offer services. Please verify your email first!",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }
                            hasAttemptedPublish = true
                            val rawPrice = priceText.trim().toDoubleOrNull()
                            val hasErrors = title.isBlank() ||
                                rawPrice == null ||
                                rawPrice <= 0.0 ||
                                rawPrice > maxAllowedPrice ||
                                selectedDays.isEmpty() ||
                                selectedTimeSlots.isEmpty()
                            if (hasErrors) {
                                return@Button
                            } else {
                                val parsedPrice = rawPrice.coerceIn(0.0, maxAllowedPrice)
                                isPublishing = true
                                val user = Firebase.auth.currentUser
                                val currentUid = user?.uid.orEmpty()
                                val currentUserName = userProfile?.displayName
                                    ?.takeIf { it.isNotBlank() }
                                    ?: user?.displayName?.takeIf { it.isNotBlank() }
                                    ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                                    ?: "Student User"

                                val newListing = Listing(
                                    id = "srv_${System.currentTimeMillis()}",
                                    providerUid = currentUid,
                                    title = title.trim(),
                                    category = selectedCategory,
                                    price = parsedPrice,
                                    pricingType = selectedPricingType,
                                    providerName = currentUserName,
                                    rating = 0.0,
                                    reviewCount = 0,
                                    campusLocation = "HELP University S2",
                                    isVerifiedStudent = isVerifiedStudent,
                                    description = description.trim(),
                                    availableDays = LISTING_WEEK_DAYS.filter { selectedDays.contains(it) },
                                    availableTimeSlots = LISTING_TIME_SLOTS.filter { selectedTimeSlots.contains(it) }
                                )

                                listingViewModel.addListing(newListing) { success ->
                                    isPublishing = false
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Listing published live to Cloud Firestore!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Notice: Saved locally (Check network connection)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                listingViewModel.setSelectedCategory("All Categories")
                                onServiceCreated()
                            }
                        },
                        enabled = !isPublishing,
                        shape = Radius.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Honey,
                            contentColor = OnHoneyAccent,
                            disabledContainerColor = CreamTint,
                            disabledContentColor = MutedText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .bounceClick(scaleDown = 0.97f)
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OnHoneyAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Publish",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun InlineValidationError(message: String?) {
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, start = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = DangerFgStrong,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = message.orEmpty(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = DangerFgStrong
            )
        }
    }
}

@Composable
private fun FormSectionCard(
    sectionTitle: String,
    trailingLabel: String? = null,
    isTrailingError: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, Hairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sectionTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.8.sp
                )
                if (!trailingLabel.isNullOrBlank()) {
                    Text(
                        text = trailingLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isTrailingError) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTrailingError) DangerFgStrong else MutedText
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}
