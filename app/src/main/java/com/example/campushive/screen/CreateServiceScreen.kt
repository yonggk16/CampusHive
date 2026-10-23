package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

private val categories = listOf("Tutoring", "Design", "Tech Help", "Errands")

@Composable
fun CreateServiceScreen(
    listingViewModel: ListingViewModel = viewModel(),
    onClose: () -> Unit = {},
    onServiceCreated: () -> Unit = {}
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
            .background(Cream)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Category Icon Preview Card (Auto-assigned based on chosen category)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ServiceCategoryIconBox(
                        category = selectedCategory,
                        size = 68.dp,
                        shape = RoundedCornerShape(20.dp),
                        symbolFontSize = 26.sp,
                        iconSize = 30.dp
                    )

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "CATEGORY PREVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText,
                            letterSpacing = 0.7.sp
                        )

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = selectedCategory,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = "Icon auto-assigned based on selection",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }
            }

            // SERVICE TITLE Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "SERVICE TITLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) {
                            Text(
                                text = "e.g., Weekend Calculus & Stats Tutoring",
                                fontSize = 15.sp,
                                color = MutedText.copy(alpha = 0.6f)
                            )
                        }
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            ),
                            cursorBrush = SolidColor(Honey),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // CATEGORY Selection
            Column {
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Charcoal else CardWhite,
                            border = if (isSelected) null else BorderStroke(1.dp, Hairline),
                            modifier = Modifier.bounceClick(scaleDown = 0.94f) { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) CardWhite else Charcoal
                            )
                        }
                    }
                }
            }

            // DESCRIPTION Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "DESCRIPTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(modifier = Modifier.height(80.dp)) {
                        if (description.isEmpty()) {
                            Text(
                                text = "What do you offer? Availability, what's included, experience...",
                                fontSize = 14.sp,
                                color = MutedText.copy(alpha = 0.7f),
                                lineHeight = 20.sp
                            )
                        }
                        BasicTextField(
                            value = description,
                            onValueChange = { description = it },
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
            }

            // PRICE Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "PRICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // RM + Number Input with Honey underline
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RM ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                            Column {
                                BasicTextField(
                                    value = priceText,
                                    onValueChange = { priceText = it },
                                    textStyle = TextStyle(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    cursorBrush = SolidColor(Honey),
                                    singleLine = true,
                                    modifier = Modifier.width(60.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(3.dp)
                                        .background(Honey, shape = RoundedCornerShape(2.dp))
                                )
                            }
                        }

                        // Segmented Toggle Pill (Per hour | Fixed)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CreamTint,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                PricingType.entries.forEach { pType ->
                                    val isSelected = selectedPricingType == pType
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) Charcoal else Color.Transparent)
                                            .clickable { selectedPricingType = pType }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pType.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) CardWhite else Charcoal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Info Notice Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF9E6),
                border = BorderStroke(1.dp, Color(0xFFF3E2C4))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Listing goes live immediately after publishing",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8C6B10)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF8C6B10),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Bottom Publish Button
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
                        Toast.makeText(context, "Listing published live to Cloud Firestore!", Toast.LENGTH_SHORT).show()
                        onServiceCreated()
                    }
                },
                enabled = !isPublishing,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .bounceClick(scaleDown = 0.95f)
            ) {
                Text(
                    text = if (isPublishing) "Publishing..." else "Publish listing",
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

            Spacer(Modifier.height(80.dp))
        }
    }
}
