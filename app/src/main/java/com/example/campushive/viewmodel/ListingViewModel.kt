package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.data.FirestoreRepository
import com.example.campushive.model.Listing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListingViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings.asStateFlow()

    init {
        // Stream live updates from Cloud Firestore
        viewModelScope.launch {
            repository.getListingsFlow().collect { firestoreListings ->
                val currentList = _listings.value
                val newLocals = currentList.filter { local ->
                    firestoreListings.none { it.id == local.id || it.title == local.title }
                }
                _listings.value = newLocals + firestoreListings
            }
        }
    }

    fun addListing(listing: Listing, onResult: (Boolean) -> Unit = {}) {
        // 1. Instant local state update at the top of the list for 0ms lag
        _listings.value = listOf(listing) + _listings.value.filterNot { it.id == listing.id }

        // 2. Async Cloud Firestore sync in shared ViewModel scope
        viewModelScope.launch {
            val success = repository.createListing(listing)
            onResult(success)
        }
    }

    fun removeListing(listingId: String) {
        _listings.value = _listings.value.filterNot { it.id == listingId }
    }
}
