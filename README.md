# 🚗 ApniVehicle v2.0

**Pakistan's #1 Vehicle Marketplace App**

A professional Android application for buying and selling vehicles in Pakistan. Built with Kotlin, Material Design 3, and modern Android architecture.

![Version](https://img.shields.io/badge/version-2.0-blue)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Kotlin-purple)
![License](https://img.shields.io/badge/license-MIT-orange)

---

## ✨ Features

### 🔐 Authentication
- Sign up with comprehensive validation
- Login with remember me functionality
- Encrypted credential storage
- Password strength indicator
- Email and phone validation

### 🚙 Vehicle Management
- Add vehicles with detailed information
- Edit and delete your listings
- Upload vehicle images with compression
- Support for multiple vehicle types
- Complete vehicle specifications

### 🔍 Browse & Search
- Browse all available vehicles
- Real-time search functionality
- Advanced filters (price, year, city, brand)
- Multiple sort options
- Grid and list view toggle

### ❤️ Favorites
- Mark vehicles as favorites
- Dedicated favorites screen
- Persistent favorite status

### ⚙️ Settings
- Dark/Light theme toggle
- Language selection (English/Urdu)
- Notification preferences
- About, Privacy Policy, Terms & Conditions
- Secure logout

### 🎨 Design
- Material Design 3 components
- Beautiful dark theme (AMOLED-friendly)
- Consistent 8dp grid system
- Professional typography
- Smooth animations

---

## 📱 Screenshots

### Light Theme
```
[Home Screen] [Vehicle Details] [Add Vehicle] [Settings]
```

### Dark Theme
```
[Home Screen] [Vehicle Details] [Add Vehicle] [Settings]
```

---

## 🛠️ Tech Stack

### Core
- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Architecture:** MVVM + Repository Pattern

### Libraries
- **UI:** Material Components 1.12.0
- **Image Loading:** Glide 4.16.0
- **JSON:** Gson 2.10.1
- **Security:** AndroidX Security Crypto 1.1.0-alpha06
- **Animations:** Lottie 6.3.0
- **Coroutines:** Kotlinx Coroutines 1.8.0
- **Lifecycle:** AndroidX Lifecycle 2.8.7

### Features
- ViewBinding for type-safe view access
- Encrypted SharedPreferences for sensitive data
- JSON file storage for data persistence
- Image compression and optimization
- Theme switching with AppCompatDelegate

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK 34
- Gradle 8.0+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/apnivehicle.git
cd apnivehicle
```

2. **Open in Android Studio**
```
File > Open > Select project folder
```

3. **Sync Gradle**
```
Wait for Gradle sync to complete
```

4. **Run the app**
```
Click Run button or press Shift + F10
```

### Quick Test
```kotlin
// Test credentials
Email: test@example.com
Password: Test@123

// Or create your own account
```

---

## 📖 Documentation

### For Users
- **[QUICK_START.md](QUICK_START.md)** - Get started in 5 minutes
- **[UPGRADE_SUMMARY.md](UPGRADE_SUMMARY.md)** - What's new in v2.0

### For Developers
- **[PROJECT_ANALYSIS_REPORT.md](PROJECT_ANALYSIS_REPORT.md)** - Comprehensive analysis
- **[IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)** - Technical implementation details

---

## 🏗️ Project Structure

```
app/src/main/
├── java/com/example/apnivehicle/
│   ├── activities/          # Activity classes
│   │   ├── SplashActivity.kt
│   │   ├── OnboardingActivity.kt
│   │   ├── LoginActivity.kt
│   │   ├── SignUpActivity.kt
│   │   ├── MainActivity.kt
│   │   └── DetailActivity.kt
│   ├── fragments/           # Fragment classes
│   │   ├── HomeFragment.kt
│   │   ├── SearchFragment.kt
│   │   ├── AddVehicleFragment.kt
│   │   ├── MyAdsFragment.kt
│   │   ├── FavoriteFragment.kt
│   │   └── SettingsFragment.kt
│   ├── adapters/            # RecyclerView adapters
│   │   ├── VehicleAdapter.kt
│   │   └── OnboardingAdapter.kt
│   ├── models/              # Data models
│   │   ├── Vehicle.kt
│   │   ├── User.kt
│   │   └── VehicleType.kt
│   ├── repository/          # Data repositories
│   │   ├── VehicleRepository.kt
│   │   └── AuthRepository.kt
│   ├── utils/               # Utility classes
│   │   ├── FileManager.kt
│   │   ├── ValidationUtils.kt
│   │   ├── ThemeManager.kt
│   │   ├── Constants.kt
│   │   └── PreferenceManager.kt
│   └── viewmodels/          # ViewModels
│       └── VehicleViewModel.kt
└── res/
    ├── layout/              # XML layouts
    ├── values/              # Resources (light theme)
    ├── values-night/        # Resources (dark theme)
    └── drawable/            # Icons and images
```

---

## 🎯 Key Features Explained

### Data Persistence
All data is stored locally using:
- **JSON files** for vehicles, users, and favorites
- **Encrypted SharedPreferences** for sensitive data
- **File storage** for images with compression

### Theme System
- Automatic theme switching
- Persistent theme preference
- AMOLED-friendly dark mode
- Status bar color adaptation

### Validation
- Email format validation
- Pakistani phone format (03XX-XXXXXXX)
- Strong password requirements
- Price range validation (1 - 999,999,999 PKR)
- Year range validation (1980 - current)
- Description minimum length (20 characters)

---

## 🧪 Testing

### Manual Testing
```bash
# Run the app
./gradlew installDebug

# Test data persistence
1. Add a vehicle
2. Close app
3. Reopen app
4. Verify vehicle is still there

# Test dark theme
1. Go to Settings
2. Toggle dark theme
3. Verify theme changes
4. Restart app
5. Verify theme persists
```

### Unit Tests (Coming Soon)
```bash
./gradlew test
```

---

## 📊 Performance

### Optimizations
- Image compression (max 1920x1920, 80% quality)
- Efficient RecyclerView with ViewBinding
- Lazy loading of images with Glide
- Minimal memory footprint
- Fast JSON serialization with Gson

### Metrics
- **App Size:** ~15 MB
- **Min RAM:** 2 GB
- **Storage:** ~50 MB (with images)
- **Startup Time:** <2 seconds

---

## 🔒 Security

### Data Protection
- Encrypted SharedPreferences for passwords
- Local-only data storage
- No data sent to external servers
- Secure file permissions
- Input sanitization

### Privacy
- No tracking or analytics
- No third-party SDKs
- No internet permission required
- User data stays on device

---

## 🌍 Localization

### Supported Languages
- 🇬🇧 English (Default)
- 🇵🇰 Urdu (Partial)

### Adding New Language
```xml
<!-- Create values-ur/strings.xml for Urdu -->
<resources>
    <string name="app_name">اپنی گاڑی</string>
    <!-- Add more translations -->
</resources>
```

---

## 🐛 Known Issues

### Current Limitations
- Single image per vehicle (multiple images coming soon)
- No image carousel in detail view
- No loading states (shimmer effects)
- No forgot password flow
- Search history not displayed

### Planned Fixes
See [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) for roadmap.

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Write clean, readable code

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

- **Your Name** - *Initial work* - [YourGitHub](https://github.com/yourusername)

---

## 🙏 Acknowledgments

- Material Design 3 guidelines
- Android Jetpack libraries
- Kotlin community
- Open source contributors

---

## 📞 Contact

- **Email:** support@apnivehicle.com
- **GitHub:** [ApniVehicle](https://github.com/yourusername/apnivehicle)
- **Issues:** [Report a bug](https://github.com/yourusername/apnivehicle/issues)

---

## 🗺️ Roadmap

### v2.1 (Next Release)
- [ ] Multiple image upload (up to 8 images)
- [ ] Image carousel in detail view
- [ ] Shimmer loading effects
- [ ] Pull-to-refresh on lists

### v2.2
- [ ] Forgot password flow
- [ ] Search history display
- [ ] Enhanced detail screen
- [ ] Share functionality

### v3.0
- [ ] Backend integration
- [ ] Real-time chat
- [ ] Push notifications
- [ ] User ratings and reviews

---

## 📈 Changelog

### v2.0 (Current) - April 11, 2026
- ✅ Complete data persistence
- ✅ Dark theme support
- ✅ Enhanced validation
- ✅ Professional settings screen
- ✅ Theme toggle
- ✅ Language selection
- ✅ About/Privacy/Terms pages
- ✅ Image compression
- ✅ Bug fixes and improvements

### v1.0 - Initial Release
- Basic vehicle listing
- Simple authentication
- Basic search and filters
- Favorites functionality

---

## ⭐ Show Your Support

Give a ⭐️ if this project helped you!

---

## 📱 Download

Coming soon to Google Play Store!

---

**Made with ❤️ in Pakistan**

**Powered by ApniVehicle v2.0 | Enhanced by AI**

---

*Last Updated: April 11, 2026*
