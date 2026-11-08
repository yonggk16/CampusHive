package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.data.FirestoreRepository
import com.example.campushive.model.Listing
import com.example.campushive.model.Review
import com.example.campushive.model.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Read-only preview of another student's profile, opened from a service listing
 * or a chat header.
 *
 * Deliberately separate from AuthViewModel: that one owns *the signed-in user*
 * and its state is bound to the session. Previewing someone else must not
 * overwrite it, so this holds an isolated, re-targetable stream instead.
 */
class ProviderProfileViewModel(
    private val repository: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var profileJob: Job? = null
    private var reviewsJob: Job? = null

    /** The uid currently being previewed, so we can cancel stale streams on re-target. */
    private var loadedUid: String = ""

    /**
     * Starts streaming the given user's profile, reviews and active listings.
     * Safe to call repeatedly; a new uid cancels the previous streams.
     *
     * @param uid Firestore uid of the user being previewed.
     * @param fallbackName display name known at the call site. Used when the
     *   user has no `users/{uid}` document — common for accounts that signed up
     *   before profiles were written, so the sheet still shows a sensible name.
     */
    fun load(uid: String, fallbackName: String = "") {
        if (uid.isBlank()) return
        if (uid == loadedUid && _profile.value != null) return
        loadedUid = uid

        profileJob?.cancel()
        reviewsJob?.cancel()

        _isLoading.value = true
        _profile.value = null
        _reviews.value = emptyList()

        profileJob = viewModelScope.launch {
            repository.getUserProfileFlow(uid).collect { fetched ->
                _profile.value = fetched ?: fallbackName.takeIf { it.isNotBlank() }?.let {
                    UserProfile(uid = uid, displayName = it)
                }
                _isLoading.value = false
            }
        }

        reviewsJob = viewModelScope.launch {
            repository.getReviewsForProviderFlow(uid).collect { fetched ->
                _reviews.value = fetched.sortedByDescending { it.createdAt.seconds }
            }
        }

        viewModelScope.launch {
            repository.getListingsFlow().collect { all ->
                _listings.value = all.filter {
                    it.providerUid == uid && it.status.equals("ACTIVE", ignoreCase = true)
                }
            }
        }
    }

    /** Average star rating across all reviews received. */
    fun averageRating(): Double {
        val list = _reviews.value
        if (list.isEmpty()) return 0.0
        return list.map { it.rating }.average()
    }
}
