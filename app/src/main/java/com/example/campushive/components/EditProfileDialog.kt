package com.example.campushive.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.model.UserProfile
import com.example.campushive.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    userProfile: UserProfile?,
    currentDisplayName: String,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, campusLocation: String, facultyOrMajor: String, phoneNumber: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var displayName by remember {
        mutableStateOf(userProfile?.displayName?.ifBlank { currentDisplayName } ?: currentDisplayName)
    }
    var bio by remember {
        mutableStateOf(
            userProfile?.bio?.takeUnless { it.equals("CampusHive Member", ignoreCase = true) } ?: ""
        )
    }
    var campusLocation by remember { mutableStateOf(userProfile?.campusLocation ?: "HELP University S2") }
    var facultyOrMajor by remember { mutableStateOf(userProfile?.facultyOrMajor ?: "School of Computing & IT") }
    var phoneNumber by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = if (CampusThemeState.isDark) BrandHoney else Charcoal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Edit Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Charcoal
                            )
                            Text(
                                text = "Customize how you appear on CampusHive.",
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

                Spacer(Modifier.height(2.dp))

                // Full Name
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Full name", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Bio / Tagline
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / Tagline", fontSize = 13.sp) },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campus Location
                OutlinedTextField(
                    value = campusLocation,
                    onValueChange = { campusLocation = it },
                    label = { Text("Campus / Location", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Faculty / Major
                OutlinedTextField(
                    value = facultyOrMajor,
                    onValueChange = { facultyOrMajor = it },
                    label = { Text("Faculty / Major", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Phone / WhatsApp Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Contact number (WhatsApp / Phone)", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))
            }

            // Anchored bottom save bar
            HorizontalDivider(thickness = 0.8.dp, color = Hairline)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        if (displayName.isNotBlank()) {
                            onSave(
                                displayName.trim(),
                                bio.trim(),
                                campusLocation.trim(),
                                facultyOrMajor.trim(),
                                phoneNumber.trim()
                            )
                        }
                    },
                    enabled = displayName.isNotBlank(),
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
                    Text(
                        text = "Save Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
