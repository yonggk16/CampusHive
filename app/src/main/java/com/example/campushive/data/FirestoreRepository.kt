package com.example.campushive.data

import android.util.Log
import com.example.campushive.model.Listing
import com.example.campushive.model.PricingType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * LISTING Entity (Collection: "listings")
 */
data class ListingEntity(
    var listingId: String = "",          // PK
    var providerUID: String = "",        // FK -> USER.uid
    var providerName: String = "Daniel Wong",
    var title: String = "",
    var category: String = "",
    var price: Double = 0.0,
    var pricingType: String = "PER_HOUR", // PER_HOUR | FIXED
    var rating: Double = 5.0,
    var reviewCount: Int = 0,
    var campusLocation: String = "",
    var description: String = "",
    var coverImageUrl: String = "",
    var status: String = "ACTIVE",       // ACTIVE | INACTIVE | DELETED
    @get:PropertyName("isVerifiedStudent")
    @set:PropertyName("isVerifiedStudent")
    var isVerifiedStudent: Boolean = true,
    var createdAt: Timestamp = Timestamp.now()
)

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val listingsRef = db.collection("listings")

    /**
     * Real-time Flow listener for Cloud Firestore Service Listings
     */
    fun getListingsFlow(): Flow<List<Listing>> = callbackFlow {
        val listener = listingsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepository", "Error listening to listings", error)
                return@addSnapshotListener
            }
            if (snapshot == null) {
                return@addSnapshotListener
            }

            val listings = snapshot.documents
                .sortedByDescending { doc ->
                    doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                }
                .mapNotNull { doc ->
                    doc.toListingOrNull()
                }
                .filter { it.title.isNotBlank() }

            trySend(listings)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Publish a new Service Listing to Cloud Firestore matching ERD schema
     */
    suspend fun createListing(
        listing: Listing,
        providerUID: String = Firebase.auth.currentUser?.uid ?: "usr_daniel"
    ): Boolean {
        return try {
            val docRef = listingsRef.document()
            val data = hashMapOf(
                "listingId" to docRef.id,
                "providerUID" to providerUID,
                "providerName" to listing.providerName.ifBlank { "Daniel Wong" },
                "title" to listing.title,
                "category" to listing.category,
                "price" to listing.price,
                "pricingType" to listing.pricingType.name,
                "rating" to listing.rating,
                "reviewCount" to listing.reviewCount,
                "campusLocation" to listing.campusLocation,
                "description" to listing.description,
                "coverImageUrl" to "",
                "status" to "ACTIVE",
                "isVerifiedStudent" to listing.isVerifiedStudent,
                "createdAt" to Timestamp.now()
            )

            docRef.set(data).await()
            Log.d("FirestoreRepository", "Successfully stored listing in Firestore: ${docRef.id}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to create listing in Firestore: ${e.message}", e)
            false
        }
    }
}

private fun DocumentSnapshot.toListingOrNull(): Listing? {
    return try {
        val title = getString("title") ?: return null
        val category = getString("category") ?: "Tutoring"
        val price = getDouble("price") ?: (getLong("price")?.toDouble() ?: 0.0)
        val pricingTypeStr = getString("pricingType") ?: "PER_HOUR"
        val providerName = getString("providerName")?.takeIf { it.isNotBlank() } ?: "Daniel Wong"
        val rating = getDouble("rating") ?: (getLong("rating")?.toDouble() ?: 5.0)
        val reviewCount = getLong("reviewCount")?.toInt() ?: 1
        val campusLocation = getString("campusLocation") ?: "Subang 2 Campus"
        val isVerified = getBoolean("isVerifiedStudent")
            ?: getBoolean("verifiedStudent")
            ?: true
        val description = getString("description") ?: ""

        val providerUID = getString("providerUID") ?: ""

        Listing(
            id = getString("listingId")?.takeIf { it.isNotBlank() } ?: id,
            providerUID = providerUID,
            title = title,
            category = category,
            price = price,
            pricingType = if (pricingTypeStr == "FIXED") PricingType.FIXED else PricingType.PER_HOUR,
            providerName = providerName,
            rating = rating,
            reviewCount = reviewCount,
            campusLocation = campusLocation,
            isVerifiedStudent = isVerified,
            description = description
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error mapping document $id to Listing", e)
        null
    }
}

// Mapper Extension
fun ListingEntity.toUIModel(): Listing {
    return Listing(
        id = listingId,
        providerUID = providerUID,
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
