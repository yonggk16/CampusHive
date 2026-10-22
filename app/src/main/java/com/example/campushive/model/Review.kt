package com.example.campushive.model

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val listingId: String = "",
    val providerUID: String = "",
    val providerName: String = "",
    val reviewerUID: String = "",
    val reviewerName: String = "",
    val serviceTitle: String = "",
    val rating: Double = 5.0,
    val comment: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
)
