package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.ui.theme.*

private val tagOptions = listOf("Clear explanations", "Punctual", "Fair price")

@Composable
fun PostReviewScreen(
    providerName: String,
    serviceTitle: String,
    onBackClick: () -> Unit,
    onReviewSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf("Clear explanations") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        // Checkmark Circle Icon Badge (Light Yellow Circle)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF7DB)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFFD4A017),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // SESSION COMPLETE label
        Text(
            text = "SESSION COMPLETE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4A017),
            letterSpacing = 0.8.sp
        )

        Spacer(Modifier.height(6.dp))

        // Title Question
        Text(
            text = "How was ${providerName}'s\ntutoring session?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )

        Spacer(Modifier.height(4.dp))

        // Subtitle Context
        Text(
            text = "Calculus & Stats Tutoring • Sat 10:00 • 2 hrs",
            fontSize = 12.sp,
            color = MutedText,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Star Rating Row
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            (1..5).forEach { star ->
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Star $star",
                    tint = if (star <= rating) Color(0xFFFFB800) else Hairline,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { rating = star }
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Quick Tag Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tagOptions.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Charcoal else CardWhite,
                    border = if (isSelected) null else BorderStroke(1.dp, Hairline),
                    modifier = Modifier.clickable {
                        if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                    }
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) CardWhite else Charcoal
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // YOUR REVIEW Card Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "YOUR REVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.height(80.dp)) {
                    if (reviewText.isEmpty()) {
                        Text(
                            text = "Share details that help other students...",
                            fontSize = 13.sp,
                            color = MutedText.copy(alpha = 0.7f)
                        )
                    }
                    BasicTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        textStyle = TextStyle(fontSize = 13.sp, color = Charcoal, lineHeight = 18.sp),
                        cursorBrush = SolidColor(Honey),
                        modifier = Modifier.fillMaxSize()
                    )
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

        Spacer(Modifier.height(36.dp))

        // Submit Review Button (BLACK/CHARCOAL Background)
        Button(
            onClick = {
                Toast.makeText(context, "Review published successfully!", Toast.LENGTH_SHORT).show()
                onReviewSubmitted()
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Submit review",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Maybe Later Text Action
        Text(
            text = "Maybe later",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            modifier = Modifier.clickable { onBackClick() }
        )

        Spacer(Modifier.height(40.dp))
    }
}
