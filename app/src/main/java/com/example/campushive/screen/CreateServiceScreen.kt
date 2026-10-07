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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.model.PricingType
import com.example.campushive.ui.theme.*

private val categories = listOf("Tutoring", "Design", "Tech Help", "Errands")

@Composable
fun CreateServiceScreen(
    onServiceCreated: () -> Unit = {}
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("Weekend math tutoring") }
    var selectedCategory by remember { mutableStateOf("Tutoring") }
    var priceText by remember { mutableStateOf("18") }
    var selectedPricingType by remember { mutableStateOf(PricingType.PER_HOUR) }
    var description by remember { mutableStateOf("") }
    var hasPhoto by remember { mutableStateOf(true) }

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
                onClick = onServiceCreated,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
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
            // Photo Uploader Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dashed "ADD PHOTO" Box
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardWhite.copy(alpha = 0.5f))
                        .drawDashedBorder(color = MutedText.copy(alpha = 0.5f), cornerRadius = 20.dp)
                        .clickable {
                            hasPhoto = true
                            Toast.makeText(context, "Photo added", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Add Photo",
                            tint = MutedText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "ADD PHOTO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                    }
                }

                // Uploaded Photo Preview Box (Lavender background)
                if (hasPhoto) {
                    Box(modifier = Modifier.size(100.dp)) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFEFEBFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CropSquare,
                                contentDescription = "Photo Preview",
                                tint = Color(0xFF6C5CE7),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Badge count circle "1"
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Charcoal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                color = CardWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        ),
                        cursorBrush = SolidColor(Honey),
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            modifier = Modifier.clickable { selectedCategory = cat }
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

            // Bottom Publish Button (Honey pill with arrow ->)
            Button(
                onClick = {
                    if (title.isBlank() || priceText.isBlank()) {
                        Toast.makeText(context, "Please enter a title and price", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Listing published!", Toast.LENGTH_SHORT).show()
                        onServiceCreated()
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "Publish listing",
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

// Helper Modifier for dashed border
private fun Modifier.drawDashedBorder(color: Color, cornerRadius: Dp): Modifier = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}
