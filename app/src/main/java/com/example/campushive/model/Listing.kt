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
import com.example.campushive.ui.theme.HoneyTint

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
            bgColor = HoneyTint,
            symbol = "Σ"
        )
        "design" -> CategoryVisual(
            icon = Icons.Outlined.Palette,
            bgColor = HoneyTint,
            symbol = "✎"
        )
        "coding", "tech help", "tech" -> CategoryVisual(
            icon = Icons.Outlined.Code,
            bgColor = HoneyTint,
            symbol = "</>"
        )
        "writing" -> CategoryVisual(
            icon = Icons.Outlined.EditNote,
            bgColor = HoneyTint,
            symbol = "✎"
        )
        "campus services", "errands" -> CategoryVisual(
            icon = Icons.Outlined.Handyman,
            bgColor = HoneyTint,
            symbol = "✓"
        )
        "others", "other" -> CategoryVisual(
            icon = Icons.Outlined.Category,
            bgColor = HoneyTint,
            symbol = "✦"
        )
        else -> CategoryVisual(
            icon = Icons.Outlined.Category,
            bgColor = HoneyTint,
            symbol = "✦"
        )
    }
}

data class Listing(
    val id: String,
    val providerUID: String = "",
    val title: String,
    val category: String,
    val price: Double,
    val pricingType: PricingType = PricingType.PER_HOUR,
    val providerName: String,
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val campusLocation: String,
    val isVerifiedStudent: Boolean = true,
    val description: String = "",
    val status: String = "ACTIVE", // ACTIVE, INACTIVE, DELETED
    val createdAt: Long = System.currentTimeMillis()
)
