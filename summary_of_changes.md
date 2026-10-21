# CampusHive — Development & Architecture Summary

## Executive Overview
This document summarizes all engineering accomplishments, backend integrations, architectural refactorings, and UI/UX design refinements completed for the CampusHive Android application.

---

## 1. Firebase Backend & Cloud Firestore Integration

### A. 1:1 ERD Data Repository ([`FirestoreRepository.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/data/FirestoreRepository.kt))
- **`ListingEntity` Schema**: Built to match the ERD `LISTING` table 1:1 (`listingId`, `providerUID`, `title`, `category`, `price`, `pricingType`, `campusLocation`, `description`, `coverImageUrl`, `status`, `isVerifiedStudent`, `createdAt`).
- **Real-Time Flow Listener (`getListingsFlow`)**: Streams live active listing documents directly from Cloud Firestore into a Kotlin Coroutines `Flow<List<Listing>>`.
- **Live Publishing (`createListing`)**: Asynchronously writes new service listings to the Cloud Firestore `"listings"` collection.

### B. Firebase Authentication Integration ([`AuthViewModel.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/viewmodel/AuthViewModel.kt))
- Connected [`LoginScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/LoginScreen.kt) and [`SignupScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/SignupScreen.kt) to `FirebaseAuth`.
- Handles student registration with university email/password, sets display names, and saves user profiles.
- Integrated sign-out and real authenticated student state on [`ProfileScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/ProfileScreen.kt).

---

## 2. Dynamic Real-Time Service Listing Flow

### A. Publishing Live Services ([`CreateServiceScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/CreateServiceScreen.kt))
- Captures title, category, price, pricing type (per hour / fixed), and description.
- Embeds Category Preview Badge ([`ServiceCategoryIconBox.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/components/ServiceCategoryIconBox.kt)) that auto-assigns icons/badges dynamically.
- Triggers instant 0ms local state update and background Cloud Firestore publishing via `listingViewModel.addListing(newListing)`.

### B. Real-Time Search & Explore Directory ([`SearchScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/SearchScreen.kt))
- **Hardcoded Data Removal**: Deleted hardcoded `sampleListings` dependency.
- Streams live listings from Cloud Firestore via `listingViewModel.listings.collectAsState()`.
- Keyword search filtering (title, category, provider, description) and price range filters (`Under RM20`, `RM20 - RM50`, `RM50+`).

### C. Dynamic Home Screen Category Counters ([`HomeScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/HomeScreen.kt))
- Replaced static listing counts in the Browse Services carousel with dynamic counting (`liveListings.count { ... }`).
- Category badges (`Tutoring`, `Design`, `Tech Help`, `Errands`) auto-increment in real time whenever a new service is published.

---

## 3. UI/UX Polish & Design System

### A. Synchronized Category Visual System ([`Listing.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/model/Listing.kt))
Centralized `getCategoryVisual(category)` with soft pastel container colors across all cards and screens:
- **Tutoring**: Soft Lavender (`#EFEBFF`) + `Σ`
- **Design**: Soft Peach (`#FFEBDD`) + `✎`
- **Coding / Tech Help**: Soft Sky Blue (`#E4F0FF`) + `</>`
- **Writing**: Soft Yellow (`#FFF9E6`) + `✎`
- **Campus Services / Errands**: Soft Mint Green (`#E3F7EC`) + `✓`

### B. Floating Island Navigation Bar ([`BottomBar.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/components/BottomBar.kt))
- Floating white pill (`CardWhite`) with active black tab highlight pill (`#14171E`) and white label/icon.
- Re-added floating black center **`+` (Create Service)** button with white plus icon.

### C. Spring Micro-Interactions ([`BounceModifier.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/ui/theme/BounceModifier.kt))
- Integrated `bounceClick` spring press feedback across interactive buttons, cards, pills, and navigation tabs.

---

## 4. Git & Branch Management

- **Branch Pushing**: Configured local branch `Bob` to track `origin/Bob` on GitHub.
- **PR Merging**: Merged PR #1 (`origin/cwbranch`) into `origin/main` and integrated into `Bob`.
- **Experimental Isolation**: Created branch `minimal` to isolate UI gradient & black/white theme experiments, keeping production branch `Bob` clean and untouched.

---

## 5. Summary Table of Key Files

| File | Primary Responsibility |
| :--- | :--- |
| [`FirestoreRepository.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/data/FirestoreRepository.kt) | Cloud Firestore CRUD & Real-Time Flow Listeners |
| [`ListingViewModel.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/viewmodel/ListingViewModel.kt) | Hybrid 0ms StateFlow & Async Firestore Sync |
| [`AuthViewModel.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/viewmodel/AuthViewModel.kt) | Firebase Authentication State & Operations |
| [`CreateServiceScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/CreateServiceScreen.kt) | Service Listing Creation Form & Publishing |
| [`SearchScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/SearchScreen.kt) | Real-Time Live Search & Filter Directory |
| [`HomeScreen.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/screen/HomeScreen.kt) | Dynamic Category Counters & Home Feed |
| [`BottomBar.kt`](file:///Users/kenyong/AndroidStudioProjects/CampusHiveMain/app/src/main/java/com/example/campushive/components/BottomBar.kt) | Floating Island Navigation & Center `+` Button |
