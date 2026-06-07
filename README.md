# 🚗 ApniVehicle — Pakistan's Vehicle Marketplace

<div align="center">

**A full-featured Android vehicle buying & selling app built for the Pakistani market**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Material Design 3](https://img.shields.io/badge/UI-Material%20Design%203-6750A4)](https://m3.material.io/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207)-orange)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-red)](https://developer.android.com/about/versions/15)

</div>

---

## 📖 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Firebase Setup](#-firebase-setup)
- [ImgBB Image Hosting](#-imgbb-image-hosting)
- [Installation](#-installation)
- [Sample Data](#-sample-data)
- [Screen Walkthrough](#-screen-walkthrough)
- [Permissions](#-permissions)
- [Troubleshooting](#-troubleshooting)
- [Future Roadmap](#-future-roadmap)

---

## 📱 About

ApniVehicle is a comprehensive Android marketplace for buying and selling vehicles in Pakistan. Users can post ads with multiple images, search and filter by brand/model/city/price, chat with sellers, track price drops, compare vehicles side by side, and view analytics on their listings.

The app uses **Firebase Firestore** as the backend database, **Firebase Authentication** for login/signup, and **ImgBB** (free image hosting) to store vehicle photos — so images are accessible on any device through permanent `https://i.ibb.co/...` links.

---

## ✨ Features

### Authentication
- Email/Password sign up and login via **Firebase Auth**
- Remember Me with encrypted SharedPreferences
- Session persistence across app restarts
- Proper error messages for all Firebase error codes (wrong password, user not found, too many requests, etc.)
- Entry animations on login and signup screens

### Home & Browse
- Vehicle cards with 16:9 image, price in PKR, city, year
- Category filter chips: All / Cars / Bikes / Trucks / Buses / Vans / Jeeps / Rickshaws / Tractors
- Grid / List layout toggle
- Sort by: Latest, Price Low→High, Price High→Low, Alphabetical, Oldest
- Filter dialog: city, brand, price range, year
- SwipeRefreshLayout pull-to-refresh
- Empty state with icon and message

### Post Ad (Add Vehicle)
- 5-section card layout: Photos, Basic Info, Location & Year, Vehicle Specs, Description
- Pick **1–8 images** from gallery
- Horizontal thumbnail strip — tap any thumbnail to preview, X button to remove individual image
- Cover image badge on first photo
- Upload progress overlay with percentage and image counter
- Images uploaded to **ImgBB** → permanent URL stored in Firestore
- Local file fallback when offline
- All dropdowns populated from `Constants` (Pakistan-specific makes, models, cities)
- Brand/model/city/fuel/transmission/condition dropdowns
- Real-time field validation with inline error messages
- Tips card at the bottom for best ad practices

### Vehicle Detail
- Swipeable image gallery (ViewPager2 + TabLayout dots indicator)
- Full specs: year, fuel, transmission, mileage, color, condition, engine
- Seller rating and review count
- **Chat with Seller** button (opens in-app chat)
- **Call Seller** button (dials phone)
- View count tracked per vehicle
- Offline stale-data indicator
- Post-contact review prompt

### In-App Chat
- Real-time messaging via **Firebase Firestore**
- Chat list with unread badge on bottom nav
- Offline banner when not connected
- Message timestamps with relative time (Just now / 5m ago / 2h ago)
- Rounded message bubbles (sent right, received left)

### My Ads
- Shows only **your own listings** (filtered strictly by `sellerId == currentUserId`)
- Live count label: "3 active listings"
- Edit and delete each ad
- Empty state with "Post Your First Ad" button
- Price drop broadcast when you lower a vehicle price

### Favorites
- Tap heart icon on any card to save
- Favorites tab shows saved vehicles
- Persisted locally and synced to Firestore
- Price drop notification when a favorited vehicle price drops

### Advanced Search
- Search by keyword, brand, model, city
- Chip filters: condition (New / Used / Certified), fuel type, transmission
- Price slider, mileage range
- Save search as preference → reusable from Saved Searches
- Search history (last 20 queries)

### Analytics Dashboard
- Total views, favorites, contacts across all listings
- Performance score (0–100)
- Best performing category
- Market position: above/at/below average
- Per-vehicle breakdown
- Market trend table (category, avg price, trend arrow)

### Notifications
- In-app notification panel (popup from toolbar bell icon)
- Shows title, message, relative timestamp per notification
- Per-item dismiss (X button)
- Clear All button
- Badge count on bell icon
- System notifications for: vehicle added, vehicle deleted, favorite added, battery low, price drops
- Notification list persisted in SharedPreferences (up to 50 items)

### Vehicle Comparison
- Select 2 vehicles to compare side by side
- Spec matrix: price, year, mileage, fuel, transmission, condition

### User Profile
- Avatar upload
- Edit phone, location, bio
- Email verification
- Rating and review count
- Total listings and sold count

### Settings
- Dark / Light theme toggle
- Language preference
- Notifications toggle
- Account info display
- About, Privacy Policy, Terms & Conditions
- Sign Out button

### Onboarding
- 3-screen swipeable introduction for first-time users
- Skip button
- Remembered via SharedPreferences (shows once)

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Material Design 3, ViewBinding, ConstraintLayout |
| Architecture | MVVM + Repository Pattern |
| Backend DB | Firebase Firestore |
| Authentication | Firebase Auth (Email/Password) |
| Image Hosting | ImgBB API (free, no billing) |
| Image Loading | Glide (disk cache, URL + local file) |
| Networking | Retrofit 2 + OkHttp + Moshi |
| Local DB | Room (vehicle make/model cache, 24h TTL) |
| Local Storage | JSON files via Gson (vehicles, users, favorites) |
| Encrypted Prefs | EncryptedSharedPreferences (AndroidX Security) |
| Real-time Chat | Firestore snapshot listeners |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Image Compression | Custom `ImageSaver` + `ImgBBUploader` |
| EXIF Handling | AndroidX ExifInterface |
| Coroutines | kotlinx-coroutines-android + coroutines-play-services |
| Vehicle Data API | CarQuery API (fallback to Constants) |
| Animations | ObjectAnimator, AnimatorSet, DecelerateInterpolator |
| Min SDK | 24 (Android 7.0) |
| Target / Compile SDK | 36 |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer                          │
│  Activities / Fragments / Adapters / ViewBinding    │
└────────────────────┬────────────────────────────────┘
                     │ observes / calls
┌────────────────────▼────────────────────────────────┐
│                Repository Layer                     │
│  AuthRepository · VehicleRepository                 │
│  ChatRepository · ReviewRepository                  │
│  VehicleDataRepository                              │
└──────────┬────────────────┬───────────────┬─────────┘
           │                │               │
    ┌──────▼──────┐  ┌──────▼──────┐ ┌─────▼────────┐
    │  Firestore  │  │ Local JSON  │ │  ImgBB API   │
    │  (cloud)    │  │ (offline)   │ │  (images)    │
    └─────────────┘  └─────────────┘ └──────────────┘
```

**Data flow for posting an ad:**
```
User fills form
    → Validate inputs
    → Compress images (1024px JPEG ~150KB each)
    → Upload to ImgBB → get https:// URLs
    → Create Vehicle object with URLs
    → addVehicleAsync() → local JSON + Firestore
    → Notification shown
```

**Data flow for loading home:**
```
VehicleRepository.init()
    → Load local JSON cache
    → Start Firestore snapshot listener
    → On snapshot: merge Firestore + local-only vehicles
    → HomeFragment observes → submitList() → DiffUtil
```

---

## 📂 Project Structure

```
app/src/main/java/com/example/apnivehicle/
│
├── activities/
│   ├── ChatActivity.kt           Real-time chat screen
│   ├── DetailActivity.kt         Vehicle detail with image gallery
│   ├── LoginActivity.kt          Firebase Auth login + animations
│   ├── MainActivity.kt           Host: bottom nav, toolbar, fragments
│   ├── OnboardingActivity.kt     First-launch intro slides
│   ├── SignUpActivity.kt         Firebase Auth signup + animations
│   └── SplashActivity.kt         Animated splash + init
│
├── adapters/
│   ├── ImagePagerAdapter.kt      Vehicle detail image swiper
│   ├── ImageThumbnailAdapter.kt  Horizontal thumbnail strip in Post Ad
│   ├── MarketTrendAdapter.kt     Analytics market trends table
│   ├── NotificationAdapter.kt    In-app notification panel list
│   ├── OnboardingAdapter.kt      Onboarding ViewPager
│   └── VehicleAdapter.kt         Main vehicle card list (DiffUtil)
│
├── api/
│   ├── ApiClient.kt              Retrofit client for CarQuery API
│   ├── CarQueryApi.kt            Vehicle makes/models from carqueryapi.com
│   ├── ImgBBApi.kt               ImgBB upload endpoint + response models
│   └── ImgBBClient.kt            Retrofit client for ImgBB (API key here)
│
├── db/
│   └── VehicleDataCache.kt       Room DB — CachedMake, CachedModel entities
│
├── dialogs/
│   └── VehicleDialogs.kt         Edit vehicle dialog, delete confirm, filter
│
├── fragments/
│   ├── AddVehicleFragment.kt     Post Ad form with ImgBB upload
│   ├── AdvancedSearchFragment.kt Advanced filter + save search
│   ├── AnalyticsFragment.kt      Seller analytics dashboard
│   ├── ChatListFragment.kt       Chat conversation list
│   ├── ComparisonFragment.kt     Side-by-side vehicle comparison
│   ├── FavoriteFragment.kt       Saved favorites
│   ├── HomeFragment.kt           Main vehicle listing + category chips
│   ├── MyAdsFragment.kt          Current user's posted ads
│   ├── ReviewsFragment.kt        Seller reviews list
│   ├── SavedSearchesFragment.kt  Saved search preferences
│   ├── SearchFragment.kt         Quick search results
│   ├── SettingsFragment.kt       App preferences + logout
│   ├── UserProfileFragment.kt    Edit profile, avatar, stats
│   └── VerificationFragment.kt   Phone/CNIC verification
│
├── models/
│   ├── ChatMessage.kt
│   ├── OnboardingItem.kt
│   ├── Review.kt
│   ├── SearchHistory.kt
│   ├── SearchPreference.kt
│   ├── User.kt                   @DocumentId annotation for Firestore
│   ├── Vehicle.kt                imageUri + imageList (ImgBB URLs)
│   ├── VehicleAnalytics.kt
│   └── VehicleType.kt            CAR, MOTORCYCLE, TRUCK, BUS, VAN, JEEP…
│
├── receivers/
│   ├── PriceDropBroadcastReceiver.kt
│   └── SystemBroadcastReceiver.kt
│
├── repository/
│   ├── AuthRepository.kt         Firebase Auth + local JSON fallback
│   ├── ChatRepository.kt         Firestore real-time chat
│   ├── ReviewRepository.kt       Seller reviews
│   ├── VehicleDataRepository.kt  Makes/models: Constants → Room → CarQuery API
│   └── VehicleRepository.kt      Vehicles: local JSON ↔ Firestore merge
│
├── services/
│   └── ApniVehicleFcmService.kt  FCM push notifications
│
├── utils/
│   ├── AnalyticsManager.kt
│   ├── AppNotificationManager.kt In-app notification store (SharedPrefs)
│   ├── ClickDebounceUtil.kt      Debounced click listeners
│   ├── Constants.kt              30 cities, 40+ makes, model lists, fuel types…
│   ├── EmailService.kt
│   ├── FileManager.kt            JSON persistence + local image ops
│   ├── FormatUtils.kt            Price, date, relative time formatting
│   ├── ImageSaver.kt             Compress + save image to internal storage
│   ├── ImgBBUploader.kt          Compress → Base64 → POST to ImgBB → URL
│   ├── JsonFileHandler.kt
│   ├── NetworkMonitor.kt         LiveData online/offline state
│   ├── NotificationHelper.kt     System notifications (adds to in-app list)
│   ├── PreferenceManager.kt      EncryptedSharedPreferences wrapper
│   ├── ThemeManager.kt
│   ├── ToolbarActionHandler.kt   Interface for toolbar actions per fragment
│   └── ValidationUtils.kt        Email, phone, password, vehicle field rules
│
└── viewmodels/
    └── VehicleViewModel.kt
```

---

## 🔥 Firebase Setup

The app uses three Firebase services:

### 1. Firebase Authentication
- Sign-in method: **Email/Password** must be enabled
- Go to: Firebase Console → Authentication → Sign-in method → Email/Password → Enable

### 2. Firestore Database
Collections used:

| Collection | Purpose |
|---|---|
| `vehicles` | All vehicle listings (30 sample + user-posted) |
| `users` | User profiles |
| `favorites/{userId}` | Favorite vehicle IDs per user |
| `chats` | Chat conversations |
| `users/{id}/searchPreferences` | Saved search filters |

Firestore Rules (paste in Firebase Console → Firestore → Rules):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /vehicles/{vehicleId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null
        && request.auth.uid == resource.data.sellerId;
    }

    match /users/{userId} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    match /favorites/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    match /chats/{chatId} {
      allow read, write: if request.auth != null;
      match /messages/{messageId} {
        allow read, write: if request.auth != null;
      }
    }

    match /users/{userId}/searchPreferences/{prefId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 3. Firebase Cloud Messaging
- FCM service is registered in `AndroidManifest.xml`
- FCM token saved to user's Firestore document on login
- Used for push notifications (price drops, new messages)

> **Note:** Firebase Storage is NOT used. Vehicle images are hosted on ImgBB (free, no billing required).

---

## 🖼 ImgBB Image Hosting

Vehicle photos are uploaded to **ImgBB** (free image hosting — no credit card, no billing).

### How it works

```
User picks photo from gallery
        ↓
Compressed to 1024px wide JPEG (~150KB)
        ↓
EXIF rotation fixed (portrait photos stay upright)
        ↓
Base64 encoded
        ↓
POST https://api.imgbb.com/1/upload?key=YOUR_KEY
        ↓
Response: { "data": { "url": "https://i.ibb.co/abc/img.jpg" } }
        ↓
URL stored in Firestore:
  imageUri:  "https://i.ibb.co/..."  (cover)
  imageList: ["https://...", "https://...", ...]  (all angles)
        ↓
Glide loads from URL on any device worldwide
```

### Getting your API key

1. Go to **https://imgbb.com** → Sign up (free)
2. Go to **https://api.imgbb.com** → copy your API key
3. Open `app/src/main/java/com/example/apnivehicle/api/ImgBBClient.kt`
4. Replace the value of `API_KEY`:

```kotlin
const val API_KEY = "your_actual_api_key_here"
```

### ImgBB free tier limits

| Limit | Value |
|---|---|
| Storage | Unlimited |
| Max file size | 32 MB (we upload ~150KB) |
| Image expiry | Never (permanent) |
| Bandwidth | Unlimited |
| Credit card | Not required |

---

## 📦 Installation

### Requirements
- Android Studio Hedgehog 2023.1.1 or newer
- JDK 11+
- Android SDK 24+
- `google-services.json` placed in `app/` folder

### Steps

```bash
# 1. Clone
git clone https://github.com/yourusername/ApniVehicle.git
cd ApniVehicle

# 2. Add google-services.json
# Download from Firebase Console → Project Settings → Your apps
# Place at:  app/google-services.json

# 3. Add ImgBB API key
# Open: app/src/main/java/com/example/apnivehicle/api/ImgBBClient.kt
# Set:  const val API_KEY = "your_imgbb_api_key"

# 4. Build
./gradlew assembleDebug

# APK output:
# app/build/outputs/apk/debug/app-debug.apk
```

### Firebase Console checklist before first run

- [ ] Email/Password sign-in enabled (Authentication → Sign-in method)
- [ ] Firestore database created (start in test mode or apply rules above)
- [ ] `google-services.json` downloaded and placed in `app/`
- [ ] Package name in Firebase matches `com.example.apnivehicle`

---

## 🚗 Sample Data

The app ships with **30 realistic Pakistani vehicle listings** that load automatically on first launch and are pushed to Firestore. These cover:

| Brand | Models | Price Range |
|---|---|---|
| Toyota | Corolla GLi, Corolla Altis X, Fortuner, Yaris, Hilux Revo | 2.8M – 11.5M |
| Honda | Civic Oriel, City Aspire, BR-V | 2.95M – 3.8M |
| Suzuki | Alto AGS, Swift GL, Wagon R VXL | 1.98M – 2.3M |
| KIA | Sportage AWD, Picanto Auto | 1.95M – 6.8M |
| Hyundai | Tucson AWD Ultimate, Elantra GLS | 4.5M – 7.5M |
| Changan | Alsvin Lumiere, Oshan X7 Plus | 2.75M – 8.9M |
| MG | HS Exclusive, ZS EV (Electric) | 6.2M – 8.5M |
| Haval | H6 HEV (Hybrid) | 9.8M |
| Nissan | Dayz (import) | 1.65M |
| Daihatsu | Mira ES (import) | 1.55M |
| Mitsubishi | Pajero V6 | 7.2M |
| Honda / Suzuki | CB150F, GR150 (Bikes) | 280K – 350K |
| Land Rover | Defender 90 V8 | 42M |
| Jeep | Wrangler Rubicon | 18.5M |
| Toyota | Hiace Grand Cabin (Van) | 7.8M |
| Isuzu | D-Max 4x4 (Truck) | 8.2M |
| Mercedes-Benz | C200 AMG Line | 16.5M |

Each vehicle has realistic mileage, city, year, condition, seller ratings, view counts, and staggered creation timestamps.

Sample data is version-controlled via `SAMPLE_DATA_VERSION = 2`. Bumping this value forces a re-upload to Firestore on next launch.

---

## 📱 Screen Walkthrough

```
Splash Screen
    ↓ (first launch)
Onboarding (3 slides, skippable)
    ↓
Login / Sign Up
    ↓
MainActivity
    ├── Home          — vehicle list with category chips
    ├── Search        — keyword search results
    ├── Post Ad       — multi-image upload form
    ├── My Ads        — your listings with edit/delete
    └── Settings      — theme, notifications, logout
         │
         ├── Toolbar Search → SearchFragment
         ├── Toolbar Bell → Notification panel popup
         └── Toolbar ⋮ →
               ├── Advanced Search
               ├── Analytics Dashboard
               ├── Favorite Vehicles
               ├── My Profile
               ├── Compare
               ├── Messages / Chat List
               └── Saved Searches

Tap vehicle card → DetailActivity
    ├── Swipeable image gallery
    ├── Full specs
    ├── Chat with Seller → ChatActivity
    └── Call Seller → Phone dialer
```

---

## 🔐 Permissions

| Permission | Why needed |
|---|---|
| `INTERNET` | Firestore, ImgBB upload, Glide image loading |
| `ACCESS_NETWORK_STATE` | Online/offline detection |
| `POST_NOTIFICATIONS` | Price drop and vehicle alerts (Android 13+) |
| `READ_MEDIA_IMAGES` | Pick vehicle photos from gallery (Android 13+) |
| `READ_EXTERNAL_STORAGE` | Pick photos (Android 12 and below) |
| `WRITE_EXTERNAL_STORAGE` | Save images locally (Android 9 and below) |

---

## 🐛 Troubleshooting

### Build fails with "processDebugResources FAILED"
- Check all anim XML files use only public interpolators: `@android:anim/decelerate_interpolator` ✓
- Private ones like `@android:interpolator/decelerate_quint` will cause this error ✗

### Only 3 vehicles showing in Home
- Firestore has old sample data — bump `SAMPLE_DATA_VERSION` in `VehicleRepository.kt` from `2` to `3`
- The app will re-upload all 30 samples to Firestore on next launch

### Images show as blue tint
- `ic_car_rental` was used with a tint applied in the layout — make sure `clearColorFilter()` is called before every Glide load
- Also check `item_vehicle_card.xml` — the gradient overlay should use `bg_image_scrim.xml` not `splash_gradient_background.xml`

### My Ads shows other users' vehicles
- The `getMyAds()` filter must use `it.sellerId == currentUserId` strictly — never `it.isMyAd` alone (sample data has `isMyAd = true`)

### Login fails with "configuration not found"
- Firebase Email/Password sign-in is not enabled — go to Firebase Console → Authentication → Sign-in method → Enable it

### ImgBB upload fails
- Check `ImgBBClient.API_KEY` is set to your actual key (not the placeholder)
- Check internet connection
- Images over 32MB fail — but we compress to ~150KB so this shouldn't happen

### Images not showing on other devices
- If `imageUri` in Firestore is a local path like `/data/data/...` instead of `https://i.ibb.co/...` — the ImgBB upload failed silently
- Check Logcat for `ImgBBUploader` tag to see the error
- Verify your API key at https://api.imgbb.com

---

## 🔮 Future Roadmap

- [ ] Google Sign-In
- [ ] Phone number OTP verification
- [ ] Vehicle inspection report upload (PDF)
- [ ] Loan EMI calculator
- [ ] Map view for listings
- [ ] Dealer accounts with bulk listing
- [ ] Video support (short clips)
- [ ] AI-powered price suggestion
- [ ] Multi-language: Urdu / English toggle
- [ ] iOS version
- [ ] Web dashboard for dealers

---

## 📄 License

```
UET License — Copyright (c) 2026 ApniVehicle
Free to use, modify, and distribute with attribution.
```

---

<div align="center">

**Made with ❤️ in Pakistan**

*ApniVehicle — Pakistan ka apna vehicle marketplace*

</div>
