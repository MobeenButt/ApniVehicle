# Apni Vehicle 🚗

A modern Android marketplace application for buying and selling vehicles, built with Kotlin and Material Design.

## Features

### User Management
- User registration and authentication
- Profile management with verification system
- Secure credential storage using AndroidX Security Crypto
- Onboarding experience for new users

### Vehicle Listings
- Browse vehicles with grid/list view options
- Add and manage vehicle advertisements
- Detailed vehicle information pages with image galleries
- Support for multiple vehicle types (cars, two-wheelers, etc.)
- Favorite/bookmark vehicles for later viewing

### Search & Discovery
- Basic and advanced search functionality
- Search history tracking
- Custom search preferences
- Filter and sort options
- Vehicle comparison feature

### User Experience
- Material Design 3 UI components
- Dark/Light theme support
- Bottom navigation for easy access
- Navigation drawer for additional options
- Push notifications support
- Smooth animations and transitions

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34
- **Architecture**: MVVM with Repository pattern

### Key Libraries
- AndroidX Core KTX
- Material Components
- ViewBinding
- Glide (Image loading)
- Gson (JSON parsing)
- AndroidX Security Crypto (Secure storage)
- Fragment KTX
- RecyclerView & ConstraintLayout

## Project Structure

```
app/src/main/java/com/example/apnivehicle/
├── activities/          # Activity components
├── adapters/           # RecyclerView adapters
├── dialogs/            # Custom dialogs
├── fragments/          # Fragment components
├── models/             # Data models
├── repository/         # Data layer
├── utils/              # Utility classes
└── viewmodels/         # ViewModel layer
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK 24+

### Installation

1. Clone the repository
```bash
git clone <repository-url>
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

## Build

```bash
./gradlew assembleDebug
```

## Permissions

The app requires the following permissions:
- `INTERNET` - For network operations
- `POST_NOTIFICATIONS` - For push notifications
- `READ_MEDIA_IMAGES` - For accessing images
- `WRITE_EXTERNAL_STORAGE` - For devices running Android 9 and below

## Version

- **Version Code**: 1
- **Version Name**: 1.0

## License

[Add your license here]

## Contact

[Add your contact information here]
