package com.example.campushive.util

import com.example.campushive.model.ChatConversation
import com.example.campushive.model.Listing
import com.example.campushive.model.UserProfile

data class RecommendedListing(
    val listing: Listing,
    val reason: String,
    val score: Double
)

object ListingRecommender {

    private const val SEVEN_DAYS_MILLIS = 7L * 24L * 60L * 60L * 1000L

    fun recommend(
        allListings: List<Listing>,
        currentUid: String,
        currentUserName: String,
        userProfile: UserProfile?,
        conversations: List<ChatConversation>,
        savedIds: Set<String>,
        limit: Int = 5
    ): List<RecommendedListing> {
        val candidates = allListings.filter { listing ->
            val isOwnByUid = currentUid.isNotBlank() && listing.providerUid == currentUid
            val isOwnByName = currentUserName.isNotBlank() &&
                listing.providerName.equals(currentUserName, ignoreCase = true)
            listing.status == "ACTIVE" && !isOwnByUid && !isOwnByName
        }
        if (candidates.isEmpty()) return emptyList()

        val major = userProfile?.facultyOrMajor.orEmpty().trim().lowercase()
        val campus = userProfile?.campusLocation.orEmpty().trim().lowercase()
        val majorPreferredCategories = inferCategoriesForMajor(major)
        val majorTokens = major
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 3 && it !in setOf("general", "student", "diploma", "bachelor", "degree", "year") }

        val listingById = allListings.associateBy { it.id }
        val interactedCategories = buildSet {
            conversations.forEach { conv ->
                listingById[conv.listingId]?.category?.lowercase()?.let { add(it) }
            }
            savedIds.forEach { id ->
                listingById[id]?.category?.lowercase()?.let { add(it) }
            }
        }

        val now = System.currentTimeMillis()

        return candidates.map { listing ->
            var score = 0.0
            var topReason = ""
            var topReasonWeight = 0.0

            val catLower = listing.category.lowercase()
            val textBlob = "${listing.title} ${listing.description}".lowercase()

            // 1. Major / course match (+35)
            val matchesMajorCategory = majorPreferredCategories.any { pref ->
                catLower.contains(pref) || pref.contains(catLower)
            }
            val matchesMajorKeyword = majorTokens.any { token -> textBlob.contains(token) }
            if (matchesMajorCategory || matchesMajorKeyword) {
                val pts = if (matchesMajorKeyword) 38.0 else 32.0
                score += pts
                if (pts > topReasonWeight) {
                    topReasonWeight = pts
                    topReason = "For your major"
                }
            }

            // 2. Past chat / saved category affinity (+25)
            if (interactedCategories.contains(catLower)) {
                val pts = 25.0
                score += pts
                if (pts > topReasonWeight) {
                    topReasonWeight = pts
                    topReason = "Based on your activity"
                }
            }

            // 3. Rating & social proof (up to +20)
            if (listing.reviewCount > 0) {
                val ratingPts = (listing.rating.coerceIn(0.0, 5.0) * 2.5) +
                    (listing.reviewCount.coerceAtMost(10) * 0.75)
                score += ratingPts
                if (listing.rating >= 4.5 && ratingPts > topReasonWeight) {
                    topReasonWeight = ratingPts
                    topReason = "Top rated"
                }
            }

            // 4. Same campus (+10)
            if (campus.isNotBlank() && listing.campusLocation.lowercase().contains(campus)) {
                score += 10.0
                if (10.0 > topReasonWeight) {
                    topReasonWeight = 10.0
                    topReason = "Same campus"
                }
            }

            // 5. Verified student (+5)
            if (listing.isVerifiedStudent) {
                score += 5.0
            }

            // 6. Freshness (+5 within 7 days)
            if (now - listing.createdAt <= SEVEN_DAYS_MILLIS) {
                score += 5.0
                if (topReason.isBlank()) {
                    topReason = "New on campus"
                }
            }

            if (topReason.isBlank()) {
                topReason = if (listing.reviewCount > 0) "Popular on campus" else "Campus pick"
            }

            RecommendedListing(
                listing = listing,
                reason = topReason,
                score = score
            )
        }
            .sortedWith(
                compareByDescending<RecommendedListing> { it.score }
                    .thenByDescending { it.listing.rating }
                    .thenByDescending { it.listing.createdAt }
            )
            .take(limit)
    }

    private fun inferCategoriesForMajor(major: String): Set<String> {
        if (major.isBlank() || major == "general student") return emptySet()
        return buildSet {
            if (listOf("computer", "it", "software", "tech", "data", "cyber", "info", "coding", "engineer").any { major.contains(it) }) {
                add("tech help")
                add("coding")
                add("tutoring")
            }
            if (listOf("design", "art", "media", "comm", "creative", "film", "market").any { major.contains(it) }) {
                add("design")
                add("writing")
            }
            if (listOf("business", "finance", "account", "econ", "psych", "law", "math", "science", "bio", "chem").any { major.contains(it) }) {
                add("tutoring")
                add("writing")
            }
        }
    }
}
