package com.example.campushive.data

import android.util.Log
import com.example.campushive.model.HelpReport
import com.example.campushive.model.Listing
import com.example.campushive.model.PricingType
import com.example.campushive.model.ReportStatus
import com.example.campushive.model.ReportType
import com.example.campushive.model.Review
import com.example.campushive.model.UserProfile
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
import java.math.BigDecimal
import java.math.RoundingMode

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
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val listingsRef = db.collection("listings")
    private val reviewsRef = db.collection("reviews")
    private val usersRef = db.collection("users")
    private val reportsRef = db.collection("reports")

    /**
     * Real-time Flow listener for Cloud Firestore Service Listings (ACTIVE ones)
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
                .mapNotNull { doc ->
                    doc.toListingOrNull()
                }
                .filter { (it.title.isNotBlank()) && (it.status == "ACTIVE") }
                .sortedByDescending { it.createdAt }

            trySend(listings)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time Flow listener for user's managed listings (ACTIVE & INACTIVE, excludes DELETED)
     */
    fun getMyListingsFlow(providerUID: String = Firebase.auth.currentUser?.uid ?: ""): Flow<List<Listing>> = callbackFlow {
        val listener = listingsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepository", "Error listening to my listings", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val currentUid = providerUID.ifBlank { Firebase.auth.currentUser?.uid.orEmpty() }

            val myListings = snapshot.documents
                .mapNotNull { doc -> doc.toListingOrNull() }
                .filter { listing ->
                    if (currentUid.isNotBlank()) {
                        listing.providerUID == currentUid && listing.status != "DELETED" && listing.title.isNotBlank()
                    } else {
                        false
                    }
                }
                .sortedByDescending { it.createdAt }

            trySend(myListings)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Publish a new Service Listing to Cloud Firestore matching ERD schema
     */
    suspend fun createListing(
        listing: Listing,
        providerUID: String = Firebase.auth.currentUser?.uid ?: "usr_daniel",
    ): Boolean {
        return try {
            val docRef = if (listing.id.isNotBlank()) {
                listingsRef.document(listing.id)
            } else {
                listingsRef.document()
            }

            val effectiveUid = providerUID.ifBlank { Firebase.auth.currentUser?.uid ?: "usr_daniel" }

            val data = hashMapOf(
                "listingId" to docRef.id,
                "providerUID" to effectiveUid,
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
                "status" to listing.status.ifBlank { "ACTIVE" },
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

    /**
     * Update an existing Service Listing in Cloud Firestore
     */
    suspend fun updateListing(listing: Listing): Boolean {
        return try {
            val docRef = listingsRef.document(listing.id)
            val updates = mapOf(
                "title" to listing.title,
                "category" to listing.category,
                "price" to listing.price,
                "pricingType" to listing.pricingType.name,
                "campusLocation" to listing.campusLocation,
                "description" to listing.description,
                "status" to listing.status
            )
            docRef.update(updates).await()
            Log.d("FirestoreRepository", "Successfully updated listing: ${listing.id}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to update listing ${listing.id}: ${e.message}", e)
            false
        }
    }

    /**
     * Delete or soft-delete a Service Listing in Cloud Firestore
     */
    suspend fun deleteListing(listingId: String): Boolean {
        return try {
            val docRef = listingsRef.document(listingId)
            docRef.update("status", "DELETED").await()
            Log.d("FirestoreRepository", "Successfully marked listing as DELETED: $listingId")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to delete listing $listingId: ${e.message}", e)
            false
        }
    }

    /**
     * Toggle status (ACTIVE | INACTIVE) for a Service Listing
     */
    suspend fun updateListingStatus(listingId: String, newStatus: String): Boolean {
        return try {
            listingsRef.document(listingId).update("status", newStatus).await()
            Log.d("FirestoreRepository", "Updated status of $listingId to $newStatus")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to update status for $listingId: ${e.message}", e)
            false
        }
    }

    /**
     * Submit a new Review and recalculate listing rating & review count in Firestore
     */
    suspend fun submitReview(review: Review): Boolean {
        return try {
            val reviewDocRef = reviewsRef.document()
            val currentUserId = Firebase.auth.currentUser?.uid ?: "usr_reviewer"
            val currentUserName = Firebase.auth.currentUser?.displayName.orEmpty().ifBlank { review.reviewerName.ifBlank { "Student Reviewer" } }

            var resolvedProviderUID = review.providerUID
            var resolvedProviderName = review.providerName

            if (resolvedProviderUID.isBlank() && review.listingId.isNotBlank()) {
                try {
                    val listingSnap = listingsRef.document(review.listingId).get().await()
                    if (listingSnap.exists()) {
                        val pUid = listingSnap.getString("providerUID") ?: ""
                        val pName = listingSnap.getString("providerName") ?: ""
                        if (pUid.isNotBlank()) resolvedProviderUID = pUid
                        if (pName.isNotBlank()) resolvedProviderName = pName
                    }
                } catch (e: Exception) {
                    Log.e("FirestoreRepository", "Error resolving provider for review", e)
                }
            }

            val reviewData = hashMapOf(
                "reviewId" to reviewDocRef.id,
                "listingId" to review.listingId,
                "providerUID" to resolvedProviderUID,
                "providerName" to resolvedProviderName,
                "reviewerUID" to currentUserId,
                "reviewerName" to currentUserName,
                "serviceTitle" to review.serviceTitle,
                "rating" to review.rating,
                "comment" to review.comment,
                "tags" to review.tags,
                "createdAt" to Timestamp.now()
            )

            // Save the review
            reviewDocRef.set(reviewData).await()
            Log.d("FirestoreRepository", "Review document created: ${reviewDocRef.id}")

            // If a valid listingId exists, update listing aggregate rating and count
            if (review.listingId.isNotBlank()) {
                updateListingRatingStats(review.listingId)
            }

            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to submit review: ${e.message}", e)
            false
        }
    }

    /**
     * Recalculates average rating and reviewCount for a listing and updates Firestore
     */
    private suspend fun updateListingRatingStats(listingId: String) {
        try {
            val querySnapshot = reviewsRef.whereEqualTo("listingId", listingId).get().await()
            val reviews = querySnapshot.documents.mapNotNull { it.toReviewOrNull() }

            if (reviews.isNotEmpty()) {
                val totalCount = reviews.size
                val sumRating = reviews.sumOf { it.rating }
                val rawAvg = sumRating / totalCount
                val roundedRating = BigDecimal(rawAvg)
                    .setScale(1, RoundingMode.HALF_UP)
                    .toDouble()

                listingsRef.document(listingId).update(
                    mapOf(
                        "rating" to roundedRating,
                        "reviewCount" to totalCount
                    )
                ).await()

                Log.d("FirestoreRepository", "Updated listing $listingId rating to $roundedRating ($totalCount reviews)")
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to update listing rating stats: ${e.message}", e)
        }
    }

    /**
     * Real-time Flow listener for reviews matching a specific listingId
     */
    fun getReviewsForListingFlow(listingId: String): Flow<List<Review>> = callbackFlow {
        val query = if (listingId.isBlank()) {
            reviewsRef
        } else {
            reviewsRef.whereEqualTo("listingId", listingId)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepository", "Error listening to reviews for listing: $listingId", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val reviews = snapshot.documents
                .mapNotNull { it.toReviewOrNull() }
                .sortedByDescending { it.createdAt.toDate().time }

            trySend(reviews)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time Flow listener for reviews matching a provider or all reviews
     */
    fun getReviewsForProviderFlow(providerUID: String = ""): Flow<List<Review>> = callbackFlow {
        val listener = reviewsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepository", "Error listening to provider reviews", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val currentUid = providerUID.ifBlank { Firebase.auth.currentUser?.uid.orEmpty() }

            val allReviews = snapshot.documents
                .mapNotNull { it.toReviewOrNull() }
                .sortedByDescending { it.createdAt.toDate().time }

            val filtered = if (currentUid.isNotBlank()) {
                allReviews.filter { it.providerUID == currentUid || it.reviewerUID == currentUid }
            } else {
                emptyList()
            }

            trySend(filtered)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time Flow listener for a specific User Profile in Firestore
     */
    fun getUserProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listener = usersRef.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepository", "Error listening to user profile: $uid", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toUserProfileOrNull()
                trySend(profile)
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Save or update User Profile in Firestore
     */
    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        val uid = profile.uid.ifBlank { return false }
        return try {
            val docRef = usersRef.document(uid)
            val data = hashMapOf(
                "uid" to uid,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "bio" to profile.bio,
                "campusLocation" to profile.campusLocation,
                "facultyOrMajor" to profile.facultyOrMajor,
                "phoneNumber" to profile.phoneNumber,
                "isVerifiedStudent" to profile.isVerifiedStudent,
                "isModerator" to profile.isModerator,
                "createdAt" to profile.createdAt
            )
            docRef.set(data).await()
            Log.d("FirestoreRepository", "Successfully saved user profile: $uid")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to save user profile: ${e.message}", e)
            false
        }
    }

    /**
     * Fetch user profile once from Firestore
     */
    @Suppress("unused")
    suspend fun getUserProfile(uid: String): UserProfile? {
        if (uid.isBlank()) return null
        return try {
            val snapshot = usersRef.document(uid).get().await()
            if (snapshot.exists()) snapshot.toUserProfileOrNull() else null
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to fetch user profile: ${e.message}", e)
            null
        }
    }

    /**
     * Submit a Helpdesk / User Safety Report to Cloud Firestore
     */
    suspend fun submitReport(report: HelpReport): Boolean {
        return try {
            val docRef = if (report.reportId.isNotBlank()) {
                reportsRef.document(report.reportId)
            } else {
                reportsRef.document()
            }

            val effectiveUid = report.reporterUid.ifBlank { Firebase.auth.currentUser?.uid ?: "" }

            val data = hashMapOf(
                "reportId" to docRef.id,
                "reporterUid" to effectiveUid,
                "reporterName" to report.reporterName.ifBlank { "Student User" },
                "type" to report.type.name,
                "category" to report.category,
                "targetId" to report.targetId,
                "targetName" to report.targetName,
                "description" to report.description,
                "status" to report.status.name,
                "moderatorNote" to report.moderatorNote,
                "resolvedBy" to report.resolvedBy,
                "createdAt" to report.createdAt
            )

            docRef.set(data).await()
            Log.d("FirestoreRepository", "Successfully submitted report: ${docRef.id}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to submit report: ${e.message}", e)
            false
        }
    }

    /**
     * Real-time Flow listener for user's submitted reports/tickets
     */
    fun getUserReportsFlow(reporterUid: String = ""): Flow<List<HelpReport>> = callbackFlow {
        val listener = reportsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepository", "Error listening to user reports", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val currentUid = reporterUid.ifBlank { Firebase.auth.currentUser?.uid.orEmpty() }

            val reports = snapshot.documents
                .mapNotNull { it.toHelpReportOrNull() }
                .filter { report ->
                    if (currentUid.isNotBlank()) {
                        report.reporterUid == currentUid
                    } else {
                        false
                    }
                }
                .sortedByDescending { it.createdAt.toDate().time }

            trySend(reports)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time Flow listener for ALL campus reports (for Platform Moderators)
     */
    fun getAllReportsFlow(): Flow<List<HelpReport>> = callbackFlow {
        val listener = reportsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepository", "Error listening to all reports", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val reports = snapshot.documents
                .mapNotNull { it.toHelpReportOrNull() }
                .sortedByDescending { it.createdAt.toDate().time }

            trySend(reports)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Update report moderation status and resolution note (Moderator action)
     */
    suspend fun updateReportModeration(
        reportId: String,
        status: ReportStatus,
        moderatorNote: String,
        resolvedBy: String
    ): Boolean {
        return try {
            reportsRef.document(reportId).update(
                mapOf(
                    "status" to status.name,
                    "moderatorNote" to moderatorNote,
                    "resolvedBy" to resolvedBy
                )
            ).await()
            Log.d("FirestoreRepository", "Successfully moderated report: $reportId -> ${status.name}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to moderate report $reportId: ${e.message}", e)
            false
        }
    }

    /**
     * Update an existing Report in Firestore
     */
    suspend fun updateReport(reportId: String, category: String, description: String): Boolean {
        return try {
            reportsRef.document(reportId).update(
                mapOf(
                    "category" to category,
                    "description" to description
                )
            ).await()
            Log.d("FirestoreRepository", "Successfully updated report: $reportId")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to update report $reportId: ${e.message}", e)
            false
        }
    }

    /**
     * Delete a Report in Firestore
     */
    suspend fun deleteReport(reportId: String): Boolean {
        return try {
            reportsRef.document(reportId).delete().await()
            Log.d("FirestoreRepository", "Successfully deleted report: $reportId")
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to delete report $reportId: ${e.message}", e)
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
        val providerUID = getString("providerUID") ?: ""
        val status = getString("status") ?: "ACTIVE"
        val rating = getDouble("rating") ?: (getLong("rating")?.toDouble() ?: 5.0)
        val reviewCount = getLong("reviewCount")?.toInt() ?: 1
        val campusLocation = getString("campusLocation") ?: "Subang 2 Campus"
        val isVerified = getBoolean("isVerifiedStudent")
            ?: getBoolean("verifiedStudent")
            ?: true
        val description = getString("description") ?: ""
        val createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()

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
            description = description,
            status = status,
            createdAt = createdAt
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error mapping document $id to Listing", e)
        null
    }
}

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toReviewOrNull(): Review? {
    return try {
        val rating = getDouble("rating") ?: (getLong("rating")?.toDouble() ?: 5.0)
        val comment = getString("comment") ?: ""
        val reviewerName = getString("reviewerName")?.takeIf { it.isNotBlank() } ?: "Student"
        val serviceTitle = getString("serviceTitle") ?: "Campus Service"
        val tags = (get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

        Review(
            id = getString("reviewId") ?: id,
            listingId = getString("listingId") ?: "",
            providerUID = getString("providerUID") ?: "",
            providerName = getString("providerName") ?: "Student Provider",
            reviewerUID = getString("reviewerUID") ?: "",
            reviewerName = reviewerName,
            serviceTitle = serviceTitle,
            rating = rating,
            comment = comment,
            tags = tags,
            createdAt = getTimestamp("createdAt") ?: Timestamp.now()
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error mapping document $id to Review", e)
        null
    }
}

private fun DocumentSnapshot.toUserProfileOrNull(): UserProfile? {
    return try {
        val uid = getString("uid") ?: id
        val displayName = getString("displayName") ?: "Student User"
        val email = getString("email") ?: ""
        val bio = getString("bio") ?: "CampusHive Member"
        val campusLocation = getString("campusLocation") ?: "Subang 2 Campus"
        val facultyOrMajor = getString("facultyOrMajor") ?: "General Student"
        val phoneNumber = getString("phoneNumber") ?: ""
        val isVerified = getBoolean("isVerifiedStudent") ?: false
        val isModerator = getBoolean("isModerator") ?: false
        val createdAt = getTimestamp("createdAt") ?: Timestamp.now()

        UserProfile(
            uid = uid,
            displayName = displayName,
            email = email,
            bio = bio,
            campusLocation = campusLocation,
            facultyOrMajor = facultyOrMajor,
            phoneNumber = phoneNumber,
            isVerifiedStudent = isVerified,
            isModerator = isModerator,
            createdAt = createdAt
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error mapping document $id to UserProfile", e)
        null
    }
}

private fun DocumentSnapshot.toHelpReportOrNull(): HelpReport? {
    return try {
        val typeStr = getString("type") ?: "PROBLEM"
        val statusStr = getString("status") ?: "OPEN"

        val reportType = try { ReportType.valueOf(typeStr) } catch (e: Exception) { ReportType.PROBLEM }
        val reportStatus = try { ReportStatus.valueOf(statusStr) } catch (e: Exception) { ReportStatus.OPEN }

        HelpReport(
            reportId = getString("reportId") ?: id,
            reporterUid = getString("reporterUid") ?: "",
            reporterName = getString("reporterName") ?: "Student User",
            type = reportType,
            category = getString("category") ?: "General",
            targetId = getString("targetId") ?: "",
            targetName = getString("targetName") ?: "",
            description = getString("description") ?: "",
            status = reportStatus,
            moderatorNote = getString("moderatorNote") ?: "",
            resolvedBy = getString("resolvedBy") ?: "",
            createdAt = getTimestamp("createdAt") ?: Timestamp.now()
        )
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error mapping document $id to HelpReport", e)
        null
    }
}

// Mapper Extension
@Suppress("unused")
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
        description = description,
        status = status
    )
}
