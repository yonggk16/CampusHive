package com.example.campushive.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.campushive.ui.theme.*

private val tagOptions = listOf("Clear explanations", "Punctual", "Fair price")

@Composable
fun ReviewDialog(
    providerName: String = "Amina",
    serviceTitle: String = "Calculus & Stats Tutoring",
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SESSION COMPLETE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerifiedGreen,
                    letterSpacing = 0.8.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "How was $providerName's tutoring session?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )

                Text(
                    text = "$serviceTitle • Sat 10:00 • 2 hrs",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Star Rating Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star $star",
                            tint = if (star <= rating) Color(0xFFFFB800) else Hairline,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { rating = star }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Quick Tag Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagOptions.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Honey else Cream,
                            border = BorderStroke(1.dp, Hairline),
                            modifier = Modifier.clickable {
                                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            }
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Review Comment Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Cream),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = "YOUR REVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.height(50.dp)) {
                            if (reviewText.isEmpty()) {
                                Text(
                                    text = "Share details that help other students...",
                                    fontSize = 12.sp,
                                    color = MutedText.copy(alpha = 0.7f)
                                )
                            }
                            BasicTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                textStyle = TextStyle(fontSize = 12.sp, color = Charcoal),
                                cursorBrush = SolidColor(Honey),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Reviews are public on the provider's profile and can be edited later.",
                    fontSize = 11.sp,
                    color = MutedText,
                    lineHeight = 15.sp
                )

                Spacer(Modifier.height(16.dp))

                // Buttons: Submit review | Maybe later
                Button(
                    onClick = {
                        Toast.makeText(context, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show()
                        onSubmit()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit review", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(4.dp))

                TextButton(onClick = onDismiss) {
                    Text("Maybe later", color = MutedText, fontSize = 13.sp)
                }
            }
        }
    }
}
