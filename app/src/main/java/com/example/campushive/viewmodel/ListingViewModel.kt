package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.data.FirestoreRepository
import com.example.campushive.model.Listing
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListingViewModel(
    private val repository: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings.asStateFlow()

    private val _myListings = MutableStateFlow<List<Listing>>(emptyList())
    val myListings: StateFlow<List<Listing>> = _myListings.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All Categories")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _shouldFocusSearch = MutableStateFlow(false)
    val shouldFocusSearch: StateFlow<Boolean> = _shouldFocusSearch.asStateFlow()

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = normalizeCategoryFilter(category)
    }

    fun requestSearchFocus() {
        _shouldFocusSearch.value = true
    }

    fun consumeSearchFocus() {
        _shouldFocusSearch.value = false
    }

    init {
        // Stream live active public listings from Cloud Firestore
        viewModelScope.launch {
            repository.getListingsFlow().collect { firestoreListings ->
                val currentListings = _listings.value
                val localOnly = currentListings.filter { localItem ->
                    firestoreListings.none { it.id == localItem.id }
                }
                _listings.value = (localOnly + firestoreListings).sortedByDescending { it.createdAt }
            }
        }

        // Stream live managed listings for the currently logged in user
        refreshUserListings()
    }

    fun refreshUserListings() {
        val currentUid = Firebase.auth.currentUser?.uid.orEmpty()
        viewModelScope.launch {
            repository.getMyListingsFlow(currentUid).collect { userListings ->
                val currentMyListings = _myListings.value
                val localOnly = currentMyListings.filter { localItem ->
                    userListings.none { it.id == localItem.id }
                }
                _myListings.value = (localOnly + userListings).sortedByDescending { it.createdAt }
            }
        }
    }

    fun addListing(listing: Listing, onResult: (Boolean) -> Unit = {}) {
        val currentUserId = Firebase.auth.currentUser?.uid.orEmpty()
        val listingWithUid = if (listing.providerUID.isBlank()) listing.copy(providerUID = currentUserId) else listing

        // 1. Instant local optimistic update for 0ms lag
        _listings.value = listOf(listingWithUid) + _listings.value.filterNot { it.id == listingWithUid.id }
        _myListings.value = listOf(listingWithUid) + _myListings.value.filterNot { it.id == listingWithUid.id }

        // Reset category filter to All Categories so the newly added listing is immediately visible
        _selectedCategory.value = "All Categories"

        // 2. Async Cloud Firestore sync
        viewModelScope.launch {
            val success = repository.createListing(listingWithUid, currentUserId)
            onResult(success)
        }
    }

    fun updateListing(updatedListing: Listing, onResult: (Boolean) -> Unit = {}) {
        // Optimistic update
        _listings.value = _listings.value.map { if (it.id == updatedListing.id) updatedListing else it }
        _myListings.value = _myListings.value.map { if (it.id == updatedListing.id) updatedListing else it }

        viewModelScope.launch {
            val success = repository.updateListing(updatedListing)
            onResult(success)
        }
    }

    fun deleteListing(listingId: String, onResult: (Boolean) -> Unit = {}) {
        // Optimistic delete
        _listings.value = _listings.value.filterNot { it.id == listingId }
        _myListings.value = _myListings.value.filterNot { it.id == listingId }

        viewModelScope.launch {
            val success = repository.deleteListing(listingId)
            onResult(success)
        }
    }

    fun toggleListingStatus(listingId: String, onResult: (Boolean) -> Unit = {}) {
        val targetListing = _myListings.value.find { it.id == listingId } ?: return
        val newStatus = if (targetListing.status == "ACTIVE") "INACTIVE" else "ACTIVE"
        val updated = targetListing.copy(status = newStatus)

        // Optimistic status toggle
        _myListings.value = _myListings.value.map { if (it.id == listingId) updated else it }
        if (newStatus == "ACTIVE") {
            if (_listings.value.none { it.id == listingId }) {
                _listings.value = listOf(updated) + _listings.value
            } else {
                _listings.value = _listings.value.map { if (it.id == listingId) updated else it }
            }
        } else {
            _listings.value = _listings.value.filterNot { it.id == listingId }
        }

        viewModelScope.launch {
            val success = repository.updateListingStatus(listingId, newStatus)
            onResult(success)
        }
    }

    fun removeListing(listingId: String) {
        deleteListing(listingId)
    }

    companion object {
        val CATEGORY_FILTERS = listOf("All Categories", "Tutoring", "Design", "Tech Help", "Errands")

        fun normalizeCategoryFilter(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) return "All Categories"
            return CATEGORY_FILTERS.firstOrNull { it.equals(trimmed, ignoreCase = true) }
                ?: when (trimmed.lowercase()) {
                    "coding", "tech", "tech help" -> "Tech Help"
                    "campus services", "errand" -> "Errands"
                    else -> trimmed
                }
        }
    }
}
