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
import androidx.compose.material.icons.filled.WarningAmber
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
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.HelpdeskViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reportType = remember(initialType) { initialType }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val isSubmitting by helpdeskViewModel.isSubmitting.collectAsState()
    val maxDescriptionLength = 500

    val categories = when (reportType) {
        ReportType.PROBLEM -> listOf(
            "Technical issue",
            "App bug",
            "Payment / Pricing question",
            "Account issue",
            "Other"
        )
        ReportType.USER_SAFETY -> listOf(
            "Harassment / Spam",
            "Inappropriate behavior",
            "Fraud / Scam",
            "Unsafe campus meetup",
            "Other"
        )
        ReportType.LISTING_FLAG -> listOf(
            "Misleading information",
            "Inappropriate content",
            "Duplicate listing",
            "Incorrect pricing",
            "Other"
        )
    }

    val title = when (reportType) {
        ReportType.PROBLEM -> "Report a Problem"
        ReportType.USER_SAFETY -> "Report Safety Concern"
        ReportType.LISTING_FLAG -> "Flag Listing"
    }

    val subtitle = when (reportType) {
        ReportType.PROBLEM -> "Tell us what went wrong so we can fix it."
        ReportType.USER_SAFETY -> "Your report is confidential and reviewed promptly."
        ReportType.LISTING_FLAG -> "Help keep CampusHive accurate and safe."
    }

    LaunchedEffect(reportType) {
        if (selectedCategory.isBlank() || selectedCategory !in categories) {
            selectedCategory = categories.first()
        }
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
            // Scrollable form body
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
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = if (CampusThemeState.isDark) BrandHoney else Charcoal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Charcoal
                            )
                            Text(
                                text = subtitle,
                                fontSize = 13.sp,
                                color = MutedText,
                                lineHeight = 18.sp
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

                // Target context chip if reporting a specific listing or user
                if (targetName.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamTint)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Reporting:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedText
                        )
                        Text(
                            text = targetName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Category Selection
                Text(
                    text = "What's the issue?",
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
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Honey else CardWhite,
                            contentColor = if (isSelected) OnHoneyAccent else Charcoal,
                            border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                            modifier = Modifier.bounceClick { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) OnHoneyAccent else Charcoal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                // Description Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal
                    )
                    Text(
                        text = "${description.length}/$maxDescriptionLength",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= maxDescriptionLength) description = it },
                    placeholder = {
                        Text(
                            text = "Describe what happened so our team can investigate...",
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
                        .heightIn(min = 128.dp),
                    maxLines = 6,
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
                        helpdeskViewModel.submitReport(
                            type = reportType,
                            category = selectedCategory,
                            targetId = targetId,
                            targetName = targetName,
                            description = description.trim()
                        ) { success ->
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Report submitted! Our team will review it shortly.",
                                    Toast.LENGTH_LONG
                                ).show()
                                onSubmitSuccess()
                                onDismiss()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to submit report. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    enabled = !isSubmitting && description.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Honey,
                        contentColor = OnHoneyAccent,
                        disabledContainerColor = CreamTint,
                        disabledContentColor = MutedText
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnHoneyAccent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Submit Report",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
