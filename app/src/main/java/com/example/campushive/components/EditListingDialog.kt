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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.campushive.model.Listing
import com.example.campushive.model.PricingType
import com.example.campushive.ui.theme.*

private val availableCategories = listOf("Tutoring", "Design", "Tech Help", "Writing", "Errands")

@Composable
fun EditListingDialog(
    listing: Listing,
    onDismiss: () -> Unit,
    onSave: (Listing) -> Unit,
    onDelete: (String) -> Unit,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(listing.title) }
    var selectedCategory by remember { mutableStateOf(listing.category) }
    var priceText by remember { mutableStateOf("%.0f".format(listing.price)) }
    var selectedPricingType by remember { mutableStateOf(listing.pricingType) }
    var campusLocation by remember { mutableStateOf(listing.campusLocation) }
    var description by remember { mutableStateOf(listing.description) }
    var status by remember { mutableStateOf(listing.status) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Cream
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Service Listing",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardWhite)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = Charcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // TITLE Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("SERVICE TITLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                        Spacer(Modifier.height(4.dp))
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Charcoal),
                            cursorBrush = SolidColor(Honey),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // CATEGORY Selection
                Column {
                    Text("CATEGORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText, modifier = Modifier.padding(bottom = 6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableCategories) { cat ->
                            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Charcoal else CardWhite,
                                border = if (isSelected) null else BorderStroke(1.dp, Hairline),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) CardWhite else Charcoal
                                )
                            }
                        }
                    }
                }

                // PRICE & PRICING TYPE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("PRICE & TYPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("RM ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                                BasicTextField(
                                    value = priceText,
                                    onValueChange = { priceText = it },
                                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Charcoal),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    cursorBrush = SolidColor(Honey),
                                    singleLine = true,
                                    modifier = Modifier.width(60.dp)
                                )
                            }

                            Surface(shape = RoundedCornerShape(16.dp), color = CreamTint) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    PricingType.entries.forEach { pType ->
                                        val isSelected = selectedPricingType == pType
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) Charcoal else Color.Transparent)
                                                .clickable { selectedPricingType = pType }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = pType.label,
                                                fontSize = 11.sp,
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

                // CAMPUS LOCATION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("CAMPUS LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                        Spacer(Modifier.height(4.dp))
                        BasicTextField(
                            value = campusLocation,
                            onValueChange = { campusLocation = it },
                            textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Charcoal),
                            cursorBrush = SolidColor(Honey),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // DESCRIPTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("DESCRIPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.height(70.dp)) {
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                textStyle = TextStyle(fontSize = 13.sp, color = Charcoal, lineHeight = 18.sp),
                                cursorBrush = SolidColor(Honey),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // STATUS TOGGLE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("LISTING STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (status == "ACTIVE") "Active (Visible to students)" else "Inactive (Hidden)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (status == "ACTIVE") VerifiedGreen else Color(0xFFE65100)
                            )
                        }

                        Switch(
                            checked = status == "ACTIVE",
                            onCheckedChange = { isChecked ->
                                status = if (isChecked) "ACTIVE" else "INACTIVE"
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CardWhite,
                                checkedTrackColor = VerifiedGreen,
                                uncheckedThumbColor = CardWhite,
                                uncheckedTrackColor = MutedText
                            )
                        )
                    }
                }

                // ACTION BUTTONS (Save & Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete Button
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (title.isBlank() || priceText.isBlank()) {
                                Toast.makeText(context, "Title and Price cannot be empty", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val parsedPrice = priceText.toDoubleOrNull() ?: listing.price
                            val updatedListing = listing.copy(
                                title = title.trim(),
                                category = selectedCategory,
                                price = parsedPrice,
                                pricingType = selectedPricingType,
                                campusLocation = campusLocation.trim(),
                                description = description.trim(),
                                status = status
                            )
                            onSave(updatedListing)
                            Toast.makeText(context, "Listing updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(0.6f)
                    ) {
                        Text("Save Changes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Listing?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${listing.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(listing.id)
                        Toast.makeText(context, "Listing deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Charcoal)
                }
            },
            containerColor = CardWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
