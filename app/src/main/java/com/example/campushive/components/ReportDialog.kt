package com.example.campushive.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.HelpdeskViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportDialog(
    initialType: ReportType = ReportType.PROBLEM,
    targetId: String = "",
    targetName: String = "",
    helpdeskViewModel: HelpdeskViewModel,
    onDismiss: () -> Unit,
    onSubmitSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var reportType by remember { mutableStateOf(initialType) }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val isSubmitting by helpdeskViewModel.isSubmitting.collectAsState()

    val categories = when (reportType) {
        ReportType.PROBLEM -> listOf("Technical issue", "App bug", "Payment / Pricing question", "Account issue", "Other")
        ReportType.USER_SAFETY -> listOf("Harassment / Spam", "Inappropriate behavior", "Fraud / Scam", "Unsafe campus meetup", "Other")
        ReportType.LISTING_FLAG -> listOf("Misleading information", "Inappropriate content", "Duplicate listing", "Incorrect pricing", "Other")
    }

    LaunchedEffect(reportType) {
        if (selectedCategory.isBlank() || selectedCategory !in categories) {
            selectedCategory = categories.first()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = HoneyTint
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = Charcoal,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Text(
                            text = when (reportType) {
                                ReportType.PROBLEM -> "Report a Problem"
                                ReportType.USER_SAFETY -> "Report Safety Concern"
                                ReportType.LISTING_FLAG -> "Flag Listing"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Charcoal
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = MutedText)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Target Context if available
                if (targetName.isNotBlank()) {
                    Surface(
                        color = Cream,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Hairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Reporting: $targetName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Charcoal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Category Selection Label
                Text(
                    text = "SELECT CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(8.dp))

                // Chips for Category
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Charcoal else CardWhite,
                            contentColor = if (isSelected) CardWhite else Charcoal,
                            border = BorderStroke(1.dp, if (isSelected) Charcoal else Hairline),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CardWhite else Charcoal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Description Input
                Text(
                    text = "DETAILS & DESCRIPTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text(
                            text = "Provide additional details to help our support team investigate...",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Honey,
                        unfocusedBorderColor = Hairline,
                        focusedContainerColor = Cream,
                        unfocusedContainerColor = Cream
                    )
                )

                Spacer(Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (description.isBlank()) {
                            Toast.makeText(context, "Please enter details for your report", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        helpdeskViewModel.submitReport(
                            type = reportType,
                            category = selectedCategory,
                            targetId = targetId,
                            targetName = targetName,
                            description = description
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "Report submitted! Our team will review it shortly.", Toast.LENGTH_LONG).show()
                                onSubmitSuccess()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Failed to submit report. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CardWhite, strokeWidth = 2.dp)
                    } else {
                        Text("Submit Report", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
