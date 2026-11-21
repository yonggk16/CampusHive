package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ReviewViewModel

private val tagOptions = listOf("Clear explanations", "Punctual", "Fair price")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostReviewScreen(
    listingId: String = "",
    providerName: String,
    serviceTitle: String,
    reviewViewModel: ReviewViewModel = viewModel(),
    onBackClick: () -> Unit,
    onReviewSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf("Clear explanations") }
    val isSubmitting by reviewViewModel.isSubmitting.collectAsState()
    val maxReviewLength = 300

    val ratingLabel = when (rating) {
        1 -> "Needs work"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Great"
        else -> "Excellent"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
    ) {
        // =====================================================================
        // 1. SCROLLABLE CENTERED REVIEW CONTENT
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.gutter, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subtle Top-Right Close Button (matches form sheets & CreateServiceScreen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CreamTint)
                        .clickable(enabled = !isSubmitting, onClick = onBackClick),
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

            Spacer(Modifier.height(6.dp))

            // Checkmark Circle Icon Badge (refined with subtle border)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(HoneyTint)
                    .border(
                        BorderStroke(
                            1.5.dp,
                            if (CampusThemeState.isDark) BrandHoney else WarningBorder
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (CampusThemeState.isDark) BrandHoney else StarGoldDeep,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // SESSION COMPLETE label
            Text(
                text = "SESSION COMPLETE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (CampusThemeState.isDark) BrandHoney else StarGoldDeep,
                letterSpacing = 0.8.sp
            )

            Spacer(Modifier.height(6.dp))

            // Title Question
            Text(
                text = "How was ${providerName}'s\nsession?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                letterSpacing = (-0.4).sp
            )

            if (serviceTitle.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardWhite,
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Text(
                        text = serviceTitle,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Star Rating Card (integrates ReviewDialog's live rating label + CardWhite styling)
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
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star $star",
                                tint = if (star <= rating) StarGold else Hairline,
                                modifier = Modifier
                                    .size(38.dp)
                                    .bounceClick { rating = star }
                            )
                        }
                    }

                    Text(
                        text = "$rating.0 · $ratingLabel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick Tag Pills (centered FlowRow, theme-aware Honey/OnHoneyAccent)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tagOptions.forEach { tag ->
                    val isSelected = selectedTags.contains(tag)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Honey else CardWhite,
                        border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                        modifier = Modifier.bounceClick(scaleDown = 0.94f) {
                            if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                        }
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) OnHoneyAccent else Charcoal
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // YOUR REVIEW Card Box (CardWhite + Hairline + Cream inner input + live counter)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "YOUR REVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${reviewText.length} / $maxReviewLength",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Cream,
                        border = BorderStroke(1.dp, Hairline)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 92.dp)
                                .padding(14.dp)
                        ) {
                            if (reviewText.isEmpty()) {
                                Text(
                                    text = "Share details that help other students...",
                                    fontSize = 13.sp,
                                    color = MutedText.copy(alpha = 0.7f),
                                    lineHeight = 19.sp
                                )
                            }
                            BasicTextField(
                                value = reviewText,
                                onValueChange = { if (it.length <= maxReviewLength) reviewText = it },
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = Charcoal,
                                    lineHeight = 19.sp
                                ),
                                cursorBrush = SolidColor(Charcoal),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Reviews are public on the provider's profile and can be edited later.",
                fontSize = 11.sp,
                color = MutedText,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))
        }

        // =====================================================================
        // 2. ANCHORED BOTTOM ACTION BAR (matches form sheets & CreateServiceScreen)
        // =====================================================================
        Surface(
            color = CardWhite,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(thickness = 0.8.dp, color = Hairline)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            reviewViewModel.submitReview(
                                listingId = listingId,
                                providerName = providerName,
                                serviceTitle = serviceTitle,
                                rating = rating,
                                comment = reviewText.trim(),
                                tags = selectedTags.toList()
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Review published successfully!", Toast.LENGTH_SHORT).show()
                                    onReviewSubmitted()
                                } else {
                                    Toast.makeText(context, "Review saved locally!", Toast.LENGTH_SHORT).show()
                                    onReviewSubmitted()
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(16.dp),
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
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OnHoneyAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Submit review",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Text(
                        text = "Maybe later",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedText,
                        modifier = Modifier
                            .bounceClick { if (!isSubmitting) onBackClick() }
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
