# 🚗 ApniVehicle - Pakistan's Premier Vehicle Marketplace

<div align="center">

![ApniVehicle Logo](app/src/main/res/drawable/ic_logo.xml)

**A modern, feature-rich Android application for buying and selling vehicles in Pakistan**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Material Design 3](https://img.shields.io/badge/Design-Material%203-purple.svg)](https://m3.material.io/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34-red.svg)](https://developer.android.com/about/versions/14)

</div>

---

## 📱 About ApniVehicle

ApniVehicle is a comprehensive vehicle marketplace application designed specifically for the Pakistani market. It provides a seamless platform for users to buy, sell, and explore vehicles with advanced features like price drop alerts, analytics dashboard, and smart search capabilities.

### 🎯 Key Highlights

- **User-Friendly Interface** - Modern Material Design 3 UI with smooth animations
- **Smart Search** - Advanced filtering and search with saved preferences
- **Price Tracking** - Real-time price drop alerts for favorite vehicles
- **Analytics Dashboard** - Comprehensive insights for sellers
- **Secure & Fast** - Encrypted data storage with optimized performance
- **Offline Support** - Works seamlessly with local data storage

---

## ✨ Features

### 🏠 Core Features

#### 1. **Vehicle Listings**
- Browse extensive vehicle catalog
- High-quality image galleries with slider
- Detailed vehicle specifications
- Real-time availability status
- Multiple vehicle categories (Cars, Bikes, SUVs, etc.)

#### 2. **Advanced Search & Filters**
- Quick search with autocomplete
- Advanced filtering by:
  - Brand & Model
  - Price range
  - Year of manufacture
  - Mileage
  - Fuel type
  - Transmission
  - Location/City
  - Condition (New/Used)
- Save search preferences
- Search history tracking

#### 3. **User Management**
- Secure authentication system
- User profiles with ratings
- Seller verification badges
- Contact management (Call/SMS integration)
- Activity history

#### 4. **Favorites & Wishlist**
- Save favorite vehicles
- Quick access to saved items
- Price drop notifications
- Comparison tools
- Share favorites

#### 5. **My Ads Management**
- Post new vehicle listings
- Edit existing ads
- Delete listings
- Track ad performance
- View analytics per listing

### 📊 Advanced Features

#### 6. **Analytics Dashboard** ⭐
**For Sellers:**
- Total views and engagement metrics
- Favorite count tracking
- Contact click analytics
- Performance score (0-100)
- Best performing category
- Market position analysis
- Most/Least viewed vehicles
- Pricing recommendations

**Market Insights:**
- Category-wise trends
- Average prices per segment
- Trend indicators (↑ Up, ↓ Down, → Stable)
- Total listings per category
- Competitive analysis

#### 7. **Price Drop Alerts** 🔔
- Automatic price monitoring
- Real-time notifications
- Percentage drop calculation
- Favorite-based alerts
- Price history tracking
- Manual price check option

#### 8. **Smart Notifications**
- Price drop alerts
- New listings in saved searches
- Message notifications
- Favorite vehicle updates
- Badge counter on toolbar
- System notifications

#### 9. **Vehicle Comparison**
- Side-by-side comparison
- Spec comparison
- Price analysis
- Feature matrix
- Save comparisons

#### 10. **Image Management**
- Multiple image upload
- Image slider/gallery
- Zoom functionality
- High-quality image support
- Efficient caching

### 🎨 UI/UX Features

#### 11. **Professional Navbar**
- Logo on top left
- Dynamic page titles
- Search quick access
- Notification badge
- More options menu
- Consistent across all pages

#### 12. **Beautiful Splash Screen**
- Animated logo
- Smooth transitions
- Progress indicator
- Brand showcase
- Professional animations

#### 13. **Onboarding Experience**
- Feature introduction
- Swipeable screens
- Skip option
- First-time user guide

#### 14. **Theme Support**
- Light/Dark mode
- System theme sync
- Consistent color scheme
- Material Design 3 colors

#### 15. **Responsive Design**
- Grid/List view toggle
- Adaptive layouts
- Multiple screen sizes
- Tablet support
- Landscape mode

### 🔧 Technical Features

#### 16. **Data Management**
- Local JSON storage
- SharedPreferences
- Encrypted storage
- Efficient caching
- Data persistence

#### 17. **Performance**
- Optimized RecyclerView
- Image loading optimization
- Background processing
- Memory management
- Fast app startup

#### 18. **Security**
- Encrypted user data
- Secure authentication
- Permission management
- Data validation
- ProGuard rules

---

## 🏗️ Architecture

### Design Pattern
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** for data management
- **Observer Pattern** for real-time updates
- **Singleton Pattern** for managers

### Project Structure

```
ApniVehicle/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/apnivehicle/
│   │   │   │   ├── activities/          # All activities
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── SplashActivity.kt
│   │   │   │   │   ├── LoginActivity.kt
│   │   │   │   │   ├── SignUpActivity.kt
│   │   │   │   │   ├── DetailActivity.kt
│   │   │   │   │   └── OnboardingActivity.kt
│   │   │   │   │
│   │   │   │   ├── fragments/           # All fragments
│   │   │   │   │   ├── HomeFragment.kt
│   │   │   │   │   ├── SearchFragment.kt
│   │   │   │   │   ├── FavoriteFragment.kt
│   │   │   │   │   ├── AddVehicleFragment.kt
│   │   │   │   │   ├── MyAdsFragment.kt
│   │   │   │   │   ├── SettingsFragment.kt
│   │   │   │   │   ├── AnalyticsFragment.kt
│   │   │   │   │   ├── AdvancedSearchFragment.kt
│   │   │   │   │   ├── ComparisonFragment.kt
│   │   │   │   │   ├── UserProfileFragment.kt
│   │   │   │   │   └── VerificationFragment.kt
│   │   │   │   │
│   │   │   │   ├── adapters/            # RecyclerView adapters
│   │   │   │   │   ├── VehicleAdapter.kt
│   │   │   │   │   ├── ImagePagerAdapter.kt
│   │   │   │   │   ├── MarketTrendAdapter.kt
│   │   │   │   │   └── OnboardingAdapter.kt
│   │   │   │   │
│   │   │   │   ├── models/              # Data models
│   │   │   │   │   ├── Vehicle.kt
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── VehicleAnalytics.kt
│   │   │   │   │   ├── SearchHistory.kt
│   │   │   │   │   └── SearchPreference.kt
│   │   │   │   │
│   │   │   │   ├── repository/          # Data repositories
│   │   │   │   │   ├── VehicleRepository.kt
│   │   │   │   │   └── AuthRepository.kt
│   │   │   │   │
│   │   │   │   ├── utils/               # Utility classes
│   │   │   │   │   ├── AnalyticsManager.kt
│   │   │   │   │   ├── NotificationHelper.kt
│   │   │   │   │   ├── AppNotificationManager.kt
│   │   │   │   │   ├── PreferenceManager.kt
│   │   │   │   │   ├── ThemeManager.kt
│   │   │   │   │   ├── FileManager.kt
│   │   │   │   │   ├── ValidationUtils.kt
│   │   │   │   │   ├── FormatUtils.kt
│   │   │   │   │   └── Constants.kt
│   │   │   │   │
│   │   │   │   ├── receivers/           # Broadcast receivers
│   │   │   │   │   ├── PriceDropBroadcastReceiver.kt
│   │   │   │   │   └── SystemBroadcastReceiver.kt
│   │   │   │   │
│   │   │   │   ├── dialogs/             # Custom dialogs
│   │   │   │   │   └── VehicleDialogs.kt
│   │   │   │   │
│   │   │   │   └── viewmodels/          # ViewModels
│   │   │   │       └── VehicleViewModel.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/              # XML layouts
│   │   │   │   ├── drawable/            # Vector drawables
│   │   │   │   ├── values/              # Strings, colors, themes
│   │   │   │   ├── menu/                # Menu resources
│   │   │   │   └── xml/                 # XML configs
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── build.gradle.kts
│   │
│   └── proguard-rules.pro               # ProGuard configuration
│
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🛠️ Technologies & Libraries

### Core Technologies
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 36

### Android Jetpack
- **AppCompat** - Backward compatibility
- **Fragment KTX** - Fragment management
- **RecyclerView** - Efficient list display
- **ConstraintLayout** - Flexible layouts
- **ViewBinding** - Type-safe view access
- **Security Crypto** - Encrypted storage

### Material Design
- **Material Components** - Material Design 3
- **Bottom Navigation** - Navigation component
- **FAB** - Floating Action Button
- **CardView** - Card layouts
- **AppBarLayout** - Collapsing toolbar

### Third-Party Libraries
- **Glide** - Image loading and caching
- **Gson** - JSON serialization
- **FlexboxLayout** - Flexible layouts

### Build Tools
- **Gradle** - Build automation
- **ProGuard** - Code obfuscation
- **Android Gradle Plugin** - Build configuration

---

## 📦 Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK 24+
- Gradle 8.0+

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/ApniVehicle.git
   cd ApniVehicle
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Click "OK"

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button (▶️)
   - Select your device
   - Wait for installation

### Building APK

#### Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Release APK
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

---

## 🚀 Getting Started

### First Launch

1. **Splash Screen** - Beautiful animated splash screen
2. **Onboarding** - Swipe through feature introduction
3. **Sign Up/Login** - Create account or login
4. **Home Screen** - Browse vehicles immediately

### Quick Actions

- **Browse Vehicles** - Tap on any vehicle card
- **Search** - Use search icon in toolbar
- **Add to Favorites** - Tap heart icon
- **Post Ad** - Tap FAB (+ button)
- **View Analytics** - Menu → Analytics Dashboard
- **Filter Results** - Menu → Filter

---

## 📸 Screenshots

### Main Screens
- **Splash Screen** - Animated logo with progress
- **Onboarding** - Feature introduction slides
- **Home** - Vehicle listings with grid/list view
- **Search** - Advanced search with filters
- **Detail** - Vehicle details with image slider
- **Favorites** - Saved vehicles
- **My Ads** - User's posted vehicles
- **Analytics** - Performance dashboard

### Features
- **Price Drop Alert** - Notification example
- **Comparison** - Side-by-side vehicle comparison
- **Profile** - User profile with ratings
- **Settings** - App preferences

---

## 🎯 Usage Guide

### For Buyers

1. **Browse Vehicles**
   - Open app → Home screen
   - Scroll through listings
   - Switch between grid/list view

2. **Search & Filter**
   - Tap search icon
   - Enter keywords
   - Apply filters (price, brand, etc.)
   - Save search preferences

3. **View Details**
   - Tap on vehicle card
   - Swipe through images
   - Read specifications
   - Check seller info

4. **Contact Seller**
   - Tap "Call" or "Message" button
   - Direct phone/SMS integration

5. **Save Favorites**
   - Tap heart icon
   - Access from Favorites tab
   - Get price drop alerts

### For Sellers

1. **Post Vehicle**
   - Tap FAB (+ button)
   - Fill vehicle details
   - Upload images
   - Set price
   - Submit

2. **Manage Ads**
   - Go to "My Ads" tab
   - Edit/Delete listings
   - Track performance

3. **View Analytics**
   - Menu → Analytics Dashboard
   - Check views, favorites, contacts
   - See performance score
   - Get pricing recommendations

4. **Monitor Performance**
   - Track daily views
   - See engagement metrics
   - Compare with market

---

## 🔔 Notifications

### Types of Notifications

1. **Price Drop Alerts**
   - Triggered when favorite vehicle price drops
   - Shows percentage off
   - Displays old vs new price

2. **New Listings**
   - Based on saved search preferences
   - Matching criteria notifications

3. **Message Notifications**
   - Buyer inquiries
   - Seller responses

4. **System Notifications**
   - App updates
   - Important announcements

### Managing Notifications

- **Enable/Disable** - Settings → Notifications
- **Badge Counter** - Shows unread count
- **Clear All** - Tap notification icon

---

## 📊 Analytics Explained

### Metrics Tracked

#### Vehicle Level
- **View Count** - Total views per vehicle
- **Daily Views** - Views breakdown by date
- **Favorite Count** - Times added to favorites
- **Contact Clicks** - Call/Message button clicks
- **Share Count** - Times shared

#### Seller Level
- **Total Views** - Across all listings
- **Active Listings** - Current ads count
- **Total Favorites** - Sum of all favorites
- **Total Contacts** - Sum of all contact clicks
- **Performance Score** - 0-100 rating
- **Best Category** - Top performing vehicle type
- **Market Position** - Above/Below/At market average

#### Market Level
- **Category Trends** - Price and demand trends
- **Average Prices** - Per vehicle category
- **Total Listings** - Market inventory
- **Trend Direction** - Up/Down/Stable indicators

### Using Analytics

1. **Optimize Pricing**
   - Compare with market average
   - Adjust based on recommendations
   - Track price changes

2. **Improve Listings**
   - Identify low-performing ads
   - Update photos/descriptions
   - Adjust pricing strategy

3. **Best Time to Sell**
   - Monitor view patterns
   - Identify peak interest periods
   - Act on momentum

---

## 🔒 Security & Privacy

### Data Protection
- **Encrypted Storage** - User data encrypted at rest
- **Secure Authentication** - Password hashing
- **Permission Management** - Minimal permissions
- **Data Validation** - Input sanitization

### Privacy Features
- **No Data Sharing** - Data stays on device
- **Optional Permissions** - User control
- **Secure Communication** - HTTPS only
- **Anonymous Browsing** - No tracking

### Permissions Required
- **INTERNET** - Load vehicle data
- **ACCESS_NETWORK_STATE** - Check connectivity
- **POST_NOTIFICATIONS** - Send notifications
- **READ_EXTERNAL_STORAGE** - Load images
- **WRITE_EXTERNAL_STORAGE** - Save images
- **CALL_PHONE** - Direct calling (optional)

---

## 🐛 Troubleshooting

### Common Issues

#### App Crashes on Startup
**Solution:**
1. Uninstall old version completely
2. Clear app data: Settings → Apps → ApniVehicle → Clear Data
3. Reinstall fresh APK
4. Grant all permissions

#### Images Not Loading
**Solution:**
1. Check internet connection
2. Grant storage permissions
3. Clear app cache
4. Restart app

#### Notifications Not Working
**Solution:**
1. Enable notifications: Settings → Apps → ApniVehicle → Notifications
2. Grant POST_NOTIFICATIONS permission
3. Check Do Not Disturb mode
4. Restart device

#### Search Not Working
**Solution:**
1. Clear search history
2. Reset filters
3. Update app to latest version

#### APK Works on Other Phones but Not Mine
**Solution:**
1. Check Android version (must be 7.0+)
2. Uninstall completely and reinstall
3. Clear cache partition (Recovery Mode)
4. Check available storage (need 500MB+)
5. Disable battery optimization for app

---

## 🔄 Updates & Changelog

### Version 1.0 (Current)

#### Features
✅ Complete vehicle marketplace
✅ Advanced search and filters
✅ Analytics dashboard
✅ Price drop alerts
✅ Professional navbar
✅ Beautiful splash screen
✅ Favorites management
✅ My Ads management
✅ User authentication
✅ Image galleries
✅ Notifications system
✅ Theme support
✅ Offline support

#### Bug Fixes
✅ Fixed APK crashes on physical devices
✅ Fixed call/message intents
✅ Fixed card responsiveness
✅ Fixed notification badge
✅ Fixed vehicle detail display
✅ Fixed image slider
✅ Fixed navbar alignment

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### Ways to Contribute
1. **Report Bugs** - Open an issue with details
2. **Suggest Features** - Share your ideas
3. **Submit Pull Requests** - Fix bugs or add features
4. **Improve Documentation** - Help others understand
5. **Test on Devices** - Report compatibility issues

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit pull request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Write clean, readable code
- Test before committing

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 ApniVehicle

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 👥 Team

### Developers
- **Lead Developer** - Full-stack Android development
- **UI/UX Designer** - Interface design and user experience
- **Backend Developer** - Data management and APIs

### Contact
- **Email**: support@apnivehicle.com
- **Website**: www.apnivehicle.com
- **GitHub**: github.com/apnivehicle

---

## 🙏 Acknowledgments

### Libraries & Tools
- **Android Jetpack** - Google's Android components
- **Material Design** - Google's design system
- **Glide** - Bumptech image loading library
- **Gson** - Google's JSON library
- **Kotlin** - JetBrains programming language

### Inspiration
- **OLX Pakistan** - Marketplace concept
- **PakWheels** - Vehicle marketplace features
- **Careem** - UI/UX inspiration

### Resources
- **Material Design Icons** - Icon library
- **Android Developers** - Documentation
- **Stack Overflow** - Community support

---

## 📞 Support

### Need Help?

- **Documentation** - Read this README
- **Issues** - Check GitHub Issues
- **Email** - support@apnivehicle.com
- **FAQ** - See Troubleshooting section

### Report a Bug

1. Go to GitHub Issues
2. Click "New Issue"
3. Describe the bug
4. Include:
   - Android version
   - Device model
   - Steps to reproduce
   - Screenshots (if applicable)

### Request a Feature

1. Go to GitHub Issues
2. Click "New Issue"
3. Label as "Feature Request"
4. Describe the feature
5. Explain use case

---

## 🎓 Learning Resources

### For Developers

- **Android Basics** - [developer.android.com](https://developer.android.com)
- **Kotlin Documentation** - [kotlinlang.org](https://kotlinlang.org)
- **Material Design** - [material.io](https://material.io)
- **MVVM Architecture** - Android Architecture Guide

### Tutorials
- Setting up Android Studio
- Building your first Android app
- Working with RecyclerView
- Implementing Material Design
- Using ViewBinding
- Working with Fragments

---

## 🌟 Star History

If you find this project useful, please consider giving it a ⭐ on GitHub!

---

## 📈 Project Stats

- **Lines of Code**: 15,000+
- **Files**: 100+
- **Activities**: 6
- **Fragments**: 11
- **Adapters**: 4
- **Utilities**: 12
- **Models**: 8
- **Repositories**: 2

---

## 🔮 Future Roadmap

### Planned Features

#### Phase 2
- [ ] Chat system between buyers and sellers
- [ ] Video support for vehicle listings
- [ ] Map integration for location
- [ ] Payment gateway integration
- [ ] Vehicle inspection reports
- [ ] Loan calculator
- [ ] Insurance quotes

#### Phase 3
- [ ] AI-powered price suggestions
- [ ] Image recognition for vehicle details
- [ ] Voice search
- [ ] AR vehicle preview
- [ ] Blockchain verification
- [ ] Multi-language support

#### Phase 4
- [ ] Web application
- [ ] iOS version
- [ ] Admin dashboard
- [ ] Dealer management system
- [ ] API for third-party integration

---

## 💡 Tips & Best Practices

### For Best Experience

1. **Keep App Updated** - Install latest version
2. **Grant Permissions** - Allow necessary permissions
3. **Good Internet** - Use stable connection
4. **Clear Cache** - Periodically clear app cache
5. **Quality Photos** - Upload high-quality images
6. **Detailed Descriptions** - Write comprehensive details
7. **Competitive Pricing** - Check market rates
8. **Quick Response** - Reply to inquiries promptly

---

## 🎉 Success Stories

> "ApniVehicle helped me sell my car in just 3 days! The analytics showed me exactly when to adjust my price." - **Ahmed, Lahore**

> "Best vehicle marketplace app in Pakistan. Clean interface and great features!" - **Sara, Karachi**

> "The price drop alerts saved me PKR 200,000 on my dream car!" - **Hassan, Islamabad**

---

## 📱 Download

### Get ApniVehicle

- **Google Play Store** - Coming Soon
- **Direct APK** - [Download Latest Release](https://github.com/apnivehicle/releases)
- **Beta Testing** - Join our beta program

### System Requirements

- **Android Version**: 7.0 (Nougat) or higher
- **RAM**: 2GB minimum, 4GB recommended
- **Storage**: 100MB free space
- **Internet**: Required for full functionality

---

<div align="center">

## 🚀 Ready to Get Started?

**Download ApniVehicle today and experience the future of vehicle marketplace!**

[Download APK](https://github.com/apnivehicle/releases) | [View Demo](https://demo.apnivehicle.com) | [Report Issue](https://github.com/apnivehicle/issues)

---

**Made with ❤️ in Pakistan**

**ApniVehicle** - *Pakistan's #1 Vehicle Marketplace*

© 2024 ApniVehicle. All rights reserved.

</div>
