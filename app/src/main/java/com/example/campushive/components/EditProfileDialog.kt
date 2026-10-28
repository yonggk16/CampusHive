package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.campushive.model.UserProfile
import com.example.campushive.ui.theme.*

@Composable
fun EditProfileDialog(
    userProfile: UserProfile?,
    currentDisplayName: String,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, campusLocation: String, facultyOrMajor: String, phoneNumber: String) -> Unit
) {
    var displayName by remember { mutableStateOf(userProfile?.displayName?.ifBlank { currentDisplayName } ?: currentDisplayName) }
    var bio by remember { mutableStateOf(userProfile?.bio ?: "CampusHive Member") }
    var campusLocation by remember { mutableStateOf(userProfile?.campusLocation ?: "Subang 2 Campus") }
    var facultyOrMajor by remember { mutableStateOf(userProfile?.facultyOrMajor ?: "School of Computing & IT") }
    var phoneNumber by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Cream,
            border = BorderStroke(1.dp, Hairline)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit Student Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )

                Text(
                    text = "Customize how you appear to other students on CampusHive.",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // FULL NAME
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Full Name", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = Honey,
                        unfocusedBorderColor = Hairline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // BIO / TAGLINE
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / Tagline", fontSize = 12.sp) },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = Honey,
                        unfocusedBorderColor = Hairline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // CAMPUS LOCATION
                OutlinedTextField(
                    value = campusLocation,
                    onValueChange = { campusLocation = it },
                    label = { Text("Campus / Location", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = Honey,
                        unfocusedBorderColor = Hairline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // FACULTY / MAJOR
                OutlinedTextField(
                    value = facultyOrMajor,
                    onValueChange = { facultyOrMajor = it },
                    label = { Text("Faculty / Major", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = Honey,
                        unfocusedBorderColor = Hairline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // PHONE / WHATSAPP NUMBER
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Contact Number (WhatsApp/Phone)", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = Honey,
                        unfocusedBorderColor = Hairline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold, color = Charcoal)
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (displayName.isNotBlank()) {
                                onSave(displayName, bio, campusLocation, facultyOrMajor, phoneNumber)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold, color = CardWhite)
                    }
                }
            }
        }
    }
}
