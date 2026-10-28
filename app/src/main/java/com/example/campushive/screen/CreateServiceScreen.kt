package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.Listing
import com.example.campushive.model.PricingType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ListingViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

private val categories = listOf("Tutoring", "Design", "Tech Help", "Errands", "Others")

@Composable
fun CreateServiceScreen(
    listingViewModel: ListingViewModel = viewModel(),
    onClose: () -> Unit = {},
    onServiceCreated: () -> Unit = {},
) {
    val context = LocalContext.current
    var isPublishing by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tutoring") }
    var priceText by remember { mutableStateOf("") }
    var selectedPricingType by remember { mutableStateOf(PricingType.PER_HOUR) }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New listing",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
                    .border(BorderStroke(1.dp, Hairline), CircleShape)
                    .bounceClick(scaleDown = 0.90f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = Charcoal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. CATEGORY SECTION
            Column {
                Text(
                    text = "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Charcoal else CardWhite,
                            contentColor = if (isSelected) CardWhite else Charcoal,
                            border = if (isSelected) null else BorderStroke(1.dp, Hairline),
                            modifier = Modifier.bounceClick(scaleDown = 0.94f) { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CardWhite else Charcoal
                            )
                        }
                    }
                }
            }

            // 2. SERVICE TITLE SECTION
            Column {
                Text(
                    text = "Service title",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ServiceCategoryIconBox(
                            category = selectedCategory,
                            size = 40.dp,
                            shape = RoundedCornerShape(12.dp),
                            symbolFontSize = 18.sp,
                            iconSize = 20.dp
                        )

                        Spacer(Modifier.width(12.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (title.isEmpty()) {
                                Text(
                                    text = "e.g. Weekend Calculus & Stats Tutoring",
                                    fontSize = 14.sp,
                                    color = MutedText.copy(alpha = 0.6f),
                                    maxLines = 1
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
                                cursorBrush = SolidColor(Honey),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "The icon is set automatically from your category.",
                    fontSize = 12.sp,
                    color = MutedText
                )
            }

            // 3. DESCRIPTION SECTION
            Column {
                Text(
                    text = "Description",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .padding(16.dp)
                    ) {
                        if (description.isEmpty()) {
                            Text(
                                text = "What you offer, when you're free, what's included",
                                fontSize = 14.sp,
                                color = MutedText.copy(alpha = 0.6f),
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
                            cursorBrush = SolidColor(Honey),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${description.length} / 300",
                    fontSize = 12.sp,
                    color = MutedText,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. PRICE SECTION (Redesigned matching reference image)
            Column {
                Text(
                    text = "Price",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left RM Amount Box
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, Hairline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RM ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                if (priceText.isEmpty()) {
                                    Text(
                                        text = "0",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedText.copy(alpha = 0.6f)
                                    )
                                }
                                BasicTextField(
                                    value = priceText,
                                    onValueChange = { priceText = it },
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    cursorBrush = SolidColor(Honey),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Right Pricing Type Segmented Toggle Pill (Per hour | Fixed)
                    Surface(
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(50),
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
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSelected) Charcoal else Color.Transparent)
                                        .clickable { selectedPricingType = pType }
                                        .padding(horizontal = 18.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pType.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CardWhite else Charcoal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Info Note
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = Charcoal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Your listing goes live as soon as you publish.",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // Sticky Bottom Publish Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        if (title.isBlank() || priceText.isBlank()) {
                            Toast.makeText(context, "Please enter a title and price", Toast.LENGTH_SHORT).show()
                        } else {
                            isPublishing = true
                            val parsedPrice = priceText.toDoubleOrNull() ?: 18.0
                            val currentUserName = Firebase.auth.currentUser?.displayName
                                ?.takeIf { it.isNotBlank() } ?: "Daniel Wong"
                            val currentUid = Firebase.auth.currentUser?.uid ?: "usr_daniel"
                            val newListing = Listing(
                                id = "srv_${System.currentTimeMillis()}",
                                providerUID = currentUid,
                                title = title.trim(),
                                category = selectedCategory,
                                price = parsedPrice,
                                pricingType = selectedPricingType,
                                providerName = currentUserName,
                                rating = 5.0,
                                reviewCount = 1,
                                campusLocation = "Subang 2 Campus",
                                isVerifiedStudent = true,
                                description = description.trim()
                            )

                            listingViewModel.addListing(newListing)
                            listingViewModel.setSelectedCategory("All Categories")
                            Toast.makeText(context, "Listing published live to Cloud Firestore!", Toast.LENGTH_SHORT).show()
                            onServiceCreated()
                        }
                    },
                    enabled = !isPublishing,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .bounceClick(scaleDown = 0.96f)
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CardWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Publish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CardWhite
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = CardWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
