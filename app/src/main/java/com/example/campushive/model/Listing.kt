package com.example.campushive.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class PricingType(val label: String, val shortLabel: String) {
    PER_HOUR("Per hour", "/ hr"),
    FIXED("Fixed", "/ gig")
}

data class CategoryVisual(
    val icon: ImageVector,
    val bgColor: Color,
    val symbol: String? = null
)

fun getCategoryVisual(category: String): CategoryVisual {
    return when (category.lowercase()) {
        "tutoring" -> CategoryVisual(
            icon = Icons.Outlined.School,
            bgColor = Color(0xFFEFEBFF),
            symbol = "Σ"
        )
        "design" -> CategoryVisual(
            icon = Icons.Outlined.Palette,
            bgColor = Color(0xFFFFEBDD),
            symbol = "✎"
        )
        "coding", "tech help", "tech" -> CategoryVisual(
            icon = Icons.Outlined.Code,
            bgColor = Color(0xFFE4F0FF),
            symbol = "</>"
        )
        "writing" -> CategoryVisual(
            icon = Icons.Outlined.EditNote,
            bgColor = Color(0xFFFFF9E6),
            symbol = "✎"
        )
        "campus services", "errands" -> CategoryVisual(
            icon = Icons.Outlined.Handyman,
            bgColor = Color(0xFFE3F7EC),
            symbol = "✓"
        )
        else -> CategoryVisual(
            icon = Icons.Outlined.Category,
            bgColor = Color(0xFFFFF9E6),
            symbol = null
        )
    }
}

data class Listing(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val pricingType: PricingType = PricingType.PER_HOUR,
    val providerName: String,
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val campusLocation: String,
    val isVerifiedStudent: Boolean = true,
    val description: String = ""
)

// Sample Campus Service Gigs
val sampleListings = listOf(
    Listing(
        id = "srv_1",
        title = "I will tutor you in Calculus I & Discrete Math with custom study notes",
        category = "Tutoring",
        price = 20.00,
        pricingType = PricingType.PER_HOUR,
        providerName = "Alex Miller",
        rating = 4.9,
        reviewCount = 34,
        campusLocation = "Main Library / Online",
        description = "A+ student in Calc 1 & 2. I provide step-by-step problem sets and exam prep sheets."
    ),
    Listing(
        id = "srv_2",
        title = "I will design eye-catching posters and Canva slides for your student club",
        category = "Design",
        price = 15.00,
        pricingType = PricingType.FIXED,
        providerName = "Sarah Kim",
        rating = 5.0,
        reviewCount = 19,
        campusLocation = "Online",
        description = "High resolution club flyers, Instagram story templates, and presentation pitch decks."
    ),
    Listing(
        id = "srv_3",
        title = "I will debug your Java, Kotlin, or C++ programming assignments",
        category = "Coding",
        price = 25.00,
        pricingType = PricingType.PER_HOUR,
        providerName = "Marcus Chen",
        rating = 5.0,
        reviewCount = 42,
        campusLocation = "Engineering Building / Zoom",
        description = "Help with memory leaks, OOP concepts, algorithms, and git troubleshooting."
    ),
    Listing(
        id = "srv_4",
        title = "I will proofread and edit your research papers or lab reports",
        category = "Writing",
        price = 10.00,
        pricingType = PricingType.FIXED,
        providerName = "Jordan Taylor",
        rating = 4.8,
        reviewCount = 27,
        campusLocation = "Online",
        description = "APA/MLA citation formatting, grammar fixes, flow improvements, and thesis structure polish."
    ),
    Listing(
        id = "srv_5",
        title = "I will help you move dorm furniture, heavy boxes, or fridge during move-in",
        category = "Campus Services",
        price = 30.00,
        pricingType = PricingType.FIXED,
        providerName = "Dave Rodriguez",
        rating = 4.9,
        reviewCount = 15,
        campusLocation = "On Campus / All Dorms",
        description = "Hassle-free dorm moving assistance. Has dolly cart and truck available."
    )
)
