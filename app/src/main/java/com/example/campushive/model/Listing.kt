package com.example.campushive.model

enum class PricingType(val label: String, val shortLabel: String) {
    PER_HOUR("Per hour", "/ hr"),
    FIXED("Fixed", "/ gig")
}

data class Listing(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val pricingType: PricingType = PricingType.PER_HOUR,
    val providerName: String,
    val providerMajor: String,
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
        providerMajor = "Math & CS Major • Sr.",
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
        providerMajor = "Graphic Design • Jr.",
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
        providerMajor = "Computer Science • Grad",
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
        providerMajor = "English Lit • Sr.",
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
        providerMajor = "Kinesiology • Soph.",
        rating = 4.9,
        reviewCount = 15,
        campusLocation = "On Campus / All Dorms",
        description = "Hassle-free dorm moving assistance. Has dolly cart and truck available."
    )
)
