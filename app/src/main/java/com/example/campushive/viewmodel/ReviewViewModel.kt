package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.data.FirestoreRepository
import com.example.campushive.model.Review
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val repository: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    private val _listingReviews = MutableStateFlow<List<Review>>(emptyList())
    val listingReviews: StateFlow<List<Review>> = _listingReviews.asStateFlow()

    private val _providerReviews = MutableStateFlow<List<Review>>(emptyList())
    val providerReviews: StateFlow<List<Review>> = _providerReviews.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        // Stream all provider reviews by default for profile & global feedback
        loadReviewsForProvider()
    }

    /**
     * Connect real-time stream of reviews for a specific service listing
     */
    fun loadReviewsForListing(listingId: String) {
        if (listingId.isBlank()) return
        viewModelScope.launch {
            repository.getReviewsForListingFlow(listingId).collect { reviews ->
                _listingReviews.value = reviews
            }
        }
    }

    /**
     * Connect real-time stream of reviews for a specific provider
     */
    fun loadReviewsForProvider(providerUID: String = "") {
        viewModelScope.launch {
            repository.getReviewsForProviderFlow(providerUID).collect { reviews ->
                _providerReviews.value = reviews
            }
        }
    }

    /**
     * Submit a review to Cloud Firestore and update listing ratings
     */
    fun submitReview(
        listingId: String = "",
        providerUID: String = "",
        providerName: String = "Student Provider",
        serviceTitle: String = "Campus Service",
        rating: Int = 5,
        comment: String = "",
        tags: List<String> = emptyList(),
        onResult: (Boolean) -> Unit = {}
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true

        val currentUser = Firebase.auth.currentUser
        val reviewerUID = currentUser?.uid ?: "usr_reviewer"
        val reviewerName = currentUser?.displayName.orEmpty().ifBlank { "Student Reviewer" }

        val newReview = Review(
            listingId = listingId,
            providerUID = providerUID,
            providerName = providerName,
            reviewerUID = reviewerUID,
            reviewerName = reviewerName,
            serviceTitle = serviceTitle,
            rating = rating.toDouble(),
            comment = comment.trim(),
            tags = tags
        )

        // Optimistic local update for zero-latency UI reflection
        if (listingId.isNotBlank()) {
            _listingReviews.value = listOf(newReview) + _listingReviews.value
        }
        _providerReviews.value = listOf(newReview) + _providerReviews.value

        viewModelScope.launch {
            val success = repository.submitReview(newReview)
            _isSubmitting.value = false
            onResult(success)
        }
    }
}
