package com.example.campushive.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val displayName: String = "Student User",
    val email: String = "",
    val bio: String = "CampusHive Member",
    val campusLocation: String = "Subang 2 Campus",
    val facultyOrMajor: String = "General Student",
    val phoneNumber: String = "",
    val isVerifiedStudent: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
