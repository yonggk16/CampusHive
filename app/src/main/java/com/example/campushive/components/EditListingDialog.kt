package com.example.campushive.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.model.LISTING_TIME_SLOTS
import com.example.campushive.model.LISTING_WEEK_DAYS
import com.example.campushive.model.Listing
import com.example.campushive.model.PricingType
import com.example.campushive.ui.theme.*

private val availableCategories = listOf("Tutoring", "Design", "Tech Help", "Writing", "Errands")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingDialog(
    listing: Listing,
    onDismiss: () -> Unit,
    onSave: (Listing) -> Unit,
    onDelete: (String) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(listing.title) }
    var selectedCategory by remember { mutableStateOf(listing.category) }
    var priceText by remember { mutableStateOf("%.0f".format(listing.price)) }
    var selectedPricingType by remember { mutableStateOf(listing.pricingType) }
    var campusLocation by remember { mutableStateOf(listing.campusLocation) }
    var description by remember { mutableStateOf(listing.description) }
    var status by remember { mutableStateOf(listing.status) }
    var selectedDays by remember { mutableStateOf(listing.availableDays.toSet()) }
    var selectedTimeSlots by remember { mutableStateOf(listing.availableTimeSlots.toSet()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Cream,
        unfocusedContainerColor = Cream,
        focusedBorderColor = Charcoal,
        unfocusedBorderColor = Hairline,
        focusedTextColor = Charcoal,
        unfocusedTextColor = Charcoal,
        focusedLabelColor = Charcoal,
        unfocusedLabelColor = MutedText,
        cursorColor = Charcoal
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = if (CampusThemeState.isDark) BrandHoney else Charcoal,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Edit Listing",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                            Text(
                                text = "Update pricing, schedule, or visibility.",
                                fontSize = 13.sp,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CreamTint)
                            .clickable(onClick = onDismiss),
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

                // Service Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Service title", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableCategories) { cat ->
                            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) Honey else CardWhite,
                                border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                                modifier = Modifier.bounceClick { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) OnHoneyAccent else Charcoal
                                )
                            }
                        }
                    }
                }

                // Price & Billing Type
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Price & billing",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) priceText = input
                            },
                            prefix = {
                                Text(
                                    text = "RM ",
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CreamTint
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                PricingType.entries.forEach { pType ->
                                    val isSelected = selectedPricingType == pType
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(if (isSelected) Honey else Color.Transparent)
                                            .bounceClick { selectedPricingType = pType }
                                            .padding(horizontal = 12.dp, vertical = 11.dp),
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
                }

                // Campus Location
                OutlinedTextField(
                    value = campusLocation,
                    onValueChange = { campusLocation = it },
                    label = { Text("Campus location", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontSize = 13.sp) },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Availability & Timing
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Availability & timing",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(LISTING_WEEK_DAYS) { day ->
                            val isSelected = selectedDays.contains(day)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) Honey else CardWhite,
                                border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                                modifier = Modifier.bounceClick {
                                    selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                }
                            ) {
                                Text(
                                    text = day,
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) OnHoneyAccent else Charcoal
                                )
                            }
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(LISTING_TIME_SLOTS) { slot ->
                            val isSelected = selectedTimeSlots.contains(slot)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) HoneyTint else CardWhite,
                                border = BorderStroke(1.dp, if (isSelected) Charcoal else Hairline),
                                modifier = Modifier.bounceClick {
                                    selectedTimeSlots = if (isSelected) selectedTimeSlots - slot else selectedTimeSlots + slot
                                }
                            ) {
                                Text(
                                    text = slot,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = Charcoal
                                )
                            }
                        }
                    }
                }

                // Listing Visibility Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Cream)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Listing visibility",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal
                        )
                        Text(
                            text = if (status == "ACTIVE") "Active · visible to students" else "Inactive · hidden from search",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (status == "ACTIVE") MutedText else DangerFg
                        )
                    }

                    Switch(
                        checked = status == "ACTIVE",
                        onCheckedChange = { isChecked ->
                            status = if (isChecked) "ACTIVE" else "INACTIVE"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnHoneyAccent,
                            checkedTrackColor = Honey,
                            uncheckedThumbColor = CardWhite,
                            uncheckedTrackColor = Hairline,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                Spacer(Modifier.height(4.dp))
            }

            // Anchored bottom action bar (Delete + Save)
            HorizontalDivider(thickness = 0.8.dp, color = Hairline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerFgStrong),
                    border = BorderStroke(1.dp, DangerBorderSoft),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(0.38f)
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (title.isBlank() || priceText.isBlank()) {
                            Toast.makeText(context, "Title and Price cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val rawPrice = priceText.toDoubleOrNull() ?: listing.price
                        val maxAllowedPrice = if (selectedPricingType == PricingType.PER_HOUR) 50.0 else 1000.0
                        if (rawPrice > maxAllowedPrice) {
                            val limitMsg = if (selectedPricingType == PricingType.PER_HOUR)
                                "Maximum rate for hourly services is RM 50 / hr"
                            else
                                "Maximum price for fixed gig services is RM 1,000"
                            Toast.makeText(context, limitMsg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val parsedPrice = rawPrice.coerceIn(0.0, maxAllowedPrice)
                        val updatedListing = listing.copy(
                            title = title.trim(),
                            category = selectedCategory,
                            price = parsedPrice,
                            pricingType = selectedPricingType,
                            campusLocation = campusLocation.trim(),
                            description = description.trim(),
                            status = status,
                            availableDays = LISTING_WEEK_DAYS.filter { selectedDays.contains(it) },
                            availableTimeSlots = LISTING_TIME_SLOTS.filter { selectedTimeSlots.contains(it) }
                        )
                        onSave(updatedListing)
                        Toast.makeText(context, "Listing updated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Honey,
                        contentColor = OnHoneyAccent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(0.62f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Save Changes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteListingConfirmDialog(
            listingTitle = listing.title,
            onDismiss = { showDeleteConfirm = false },
            onConfirmDelete = {
                showDeleteConfirm = false
                onDelete(listing.id)
                Toast.makeText(context, "Listing deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
