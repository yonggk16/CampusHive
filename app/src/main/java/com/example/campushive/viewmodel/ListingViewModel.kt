package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import com.example.campushive.model.Listing
import com.example.campushive.model.sampleListings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ListingViewModel : ViewModel() {
    private val _listings = MutableStateFlow<List<Listing>>(sampleListings)
    val listings: StateFlow<List<Listing>> = _listings.asStateFlow()

    fun addListing(listing: Listing) {
        _listings.value = _listings.value + listing
    }

    fun removeListing(listingId: String) {
        _listings.value = _listings.value.filterNot { it.id == listingId }
    }
}
