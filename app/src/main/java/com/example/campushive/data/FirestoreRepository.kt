package com.example.campushive.data

import com.example.campushive.model.Listing
import com.example.campushive.model.PricingType
import com.example.campushive.model.sampleListings
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * LISTING Entity (Collection: "listings")
 */
data class ListingEntity(
    val listingId: String = "",          // PK
    val providerUID: String = "",        // FK -> USER.uid
    val title: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val pricingType: String = "PER_HOUR", // PER_HOUR | FIXED
    val campusLocation: String = "",
    val description: String = "",
    val coverImageUrl: String = "",
    val status: String = "ACTIVE",       // ACTIVE | INACTIVE | DELETED
    val isVerifiedStudent: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
)

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val listingsRef = db.collection("listings")

    /**
     * Real-time Flow listener for Cloud Firestore Service Listings
     */
    fun getListingsFlow(): Flow<List<Listing>> = callbackFlow {
        val listener = listingsRef
            .whereEqualTo("status", "ACTIVE")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(sampleListings)
                    return@addSnapshotListener
                }

                val listings = snapshot.documents.mapNotNull { doc ->
                    try {
                        val entity = doc.toObject(ListingEntity::class.java)?.copy(listingId = doc.id)
                        entity?.toUIModel()
                    } catch (e: Exception) {
                        null
                    }
                }

                trySend(if (listings.isEmpty()) sampleListings else listings)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Publish a new Service Listing to Cloud Firestore matching ERD schema
     */
    suspend fun createListing(listing: Listing, providerUID: String = "usr_daniel"): Boolean {
        return try {
            val docRef = listingsRef.document()
            val entityToSave = ListingEntity(
                listingId = docRef.id,
                providerUID = providerUID,
                title = listing.title,
                category = listing.category,
                price = listing.price,
                pricingType = listing.pricingType.name,
                campusLocation = listing.campusLocation,
                description = listing.description,
                coverImageUrl = "",
                status = "ACTIVE",
                isVerifiedStudent = listing.isVerifiedStudent,
                createdAt = Timestamp.now()
            )
            docRef.set(entityToSave).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

// Mapper Extension
fun ListingEntity.toUIModel(
    providerName: String = "Daniel Wong",
    rating: Double = 5.0,
    reviewCount: Int = 12
): Listing {
    return Listing(
        id = listingId,
        title = title,
        category = category,
        price = price,
        pricingType = if (pricingType == "FIXED") PricingType.FIXED else PricingType.PER_HOUR,
        providerName = providerName,
        rating = rating,
        reviewCount = reviewCount,
        campusLocation = campusLocation,
        isVerifiedStudent = isVerifiedStudent,
        description = description
    )
}
