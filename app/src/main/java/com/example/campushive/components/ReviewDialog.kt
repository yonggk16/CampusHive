package com.example.campushive.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ReviewViewModel

private val tagOptions = listOf("Clear explanations", "Punctual", "Fair price")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    listingId: String = "",
    providerName: String = "Amina",
    serviceTitle: String = "Calculus & Stats Tutoring",
    reviewViewModel: ReviewViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }
    val isSubmitting by reviewViewModel.isSubmitting.collectAsState()
    val maxReviewLength = 300

    val ratingLabel = when (rating) {
        1 -> "Needs work"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Great"
        else -> "Excellent"
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = CardWhite,
        contentColor = Charcoal,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                width = 40.dp,
                height = 4.dp,
                color = Hairline
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(HoneyTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = BrandHoney,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Rate $providerName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Charcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = serviceTitle,
                                fontSize = 13.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CreamTint)
                            .clickable(enabled = !isSubmitting, onClick = onDismiss),
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

                Spacer(Modifier.height(22.dp))

                // Star Rating Block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Cream)
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                Spacer(Modifier.height(20.dp))

                // Quick Tag Pills
                Text(
                    text = "What stood out?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
                Spacer(Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagOptions.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Honey else CardWhite,
                            contentColor = if (isSelected) OnHoneyAccent else Charcoal,
                            border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                            modifier = Modifier.bounceClick {
                                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            }
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) OnHoneyAccent else Charcoal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                // Review Comment Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your review",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                    Text(
                        text = "${reviewText.length}/$maxReviewLength",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { if (it.length <= maxReviewLength) reviewText = it },
                    placeholder = {
                        Text(
                            text = "Share details that help other students...",
                            fontSize = 13.sp,
                            color = MutedText
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Charcoal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 112.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal,
                        unfocusedBorderColor = Hairline,
                        focusedContainerColor = Cream,
                        unfocusedContainerColor = Cream,
                        focusedTextColor = Charcoal,
                        unfocusedTextColor = Charcoal,
                        cursorColor = Charcoal
                    )
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Reviews appear publicly on the provider's listing.",
                    fontSize = 12.sp,
                    color = MutedText
                )

                Spacer(Modifier.height(20.dp))
            }

            // Anchored bottom submit bar
            HorizontalDivider(thickness = 0.8.dp, color = Hairline)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
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
                        ) {
                            Toast.makeText(context, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show()
                            onSubmit()
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Honey,
                        contentColor = OnHoneyAccent,
                        disabledContainerColor = CreamTint,
                        disabledContentColor = MutedText
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnHoneyAccent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Submit Review",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
