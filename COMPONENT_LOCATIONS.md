# Component Locations in ApniVehicle App

## Visual Overview

```
ApniVehicle App
│
├── MainActivity.kt
│   └── 🔔 BroadcastReceiver (SystemBroadcastReceiver)
│       ├── Monitors: Battery Low/Okay
│       ├── Monitors: Power Connected/Disconnected
│       └── Monitors: Network Connectivity Changes
│
└── AdvancedSearchFragment
    ├── 📻 RadioGroup (Vehicle Condition)
    │   ├── RadioButton: All
    │   ├── RadioButton: New
    │   ├── RadioButton: Used
    │   └── RadioButton: Certified
    │
    └── 📦 FlexboxLayout (Active Filters Display)
        └── Dynamic Chips showing:
            ├── Brand: Toyota
            ├── Transmission: Automatic
            ├── Fuel: Petrol
            └── Condition: Used
```

## Component Details

### 1. RadioButton/RadioGroup
```
Location: AdvancedSearchFragment
Path: app/src/main/java/com/example/apnivehicle/fragments/AdvancedSearchFragment.kt
Layout: app/src/main/res/layout/fragment_advanced_search.xml

Purpose: Select vehicle condition (mutually exclusive selection)
User Flow: Home → Menu → Advanced Search → Condition Section
```

### 2. BroadcastReceiver
```
Location: SystemBroadcastReceiver (registered in MainActivity)
Path: app/src/main/java/com/example/apnivehicle/receivers/SystemBroadcastReceiver.kt
Registered: app/src/main/java/com/example/apnivehicle/activities/MainActivity.kt

Purpose: Monitor system events (battery, charging, network)
User Flow: Runs automatically in background when app is active
```

### 3. FlexboxLayout
```
Location: AdvancedSearchFragment
Path: app/src/main/java/com/example/apnivehicle/fragments/AdvancedSearchFragment.kt
Layout: app/src/main/res/layout/fragment_advanced_search.xml

Purpose: Display active search filters as removable chips
User Flow: Home → Menu → Advanced Search → Select Filters → See Active Filters
```

## Navigation to Components

### To Access RadioGroup & FlexboxLayout:
1. Launch app
2. Tap menu icon (three dots) in toolbar
3. Select "Advanced Search"
4. Scroll to "Condition" section → See RadioGroup
5. Select any filters → See FlexboxLayout with filter chips

### To Test BroadcastReceiver:
1. Launch app (receiver auto-registers)
2. Plug/unplug charger → See charging toast
3. Toggle airplane mode → See network connectivity toast
4. Let battery drop below 20% → See low battery notification

## File Structure

```
app/src/main/
├── java/com/example/apnivehicle/
│   ├── activities/
│   │   └── MainActivity.kt ..................... [BroadcastReceiver Registration]
│   ├── fragments/
│   │   └── AdvancedSearchFragment.kt ........... [RadioGroup + FlexboxLayout Logic]
│   └── receivers/
│       └── SystemBroadcastReceiver.kt .......... [BroadcastReceiver Implementation]
└── res/
    └── layout/
        └── fragment_advanced_search.xml ........ [RadioGroup + FlexboxLayout XML]
```

## Dependencies

```gradle
// build.gradle.kts (app level)
dependencies {
    // ... existing dependencies
    implementation("com.google.android.flexbox:flexbox:3.0.0") // For FlexboxLayout
}
```

## Quick Test Checklist

- [ ] RadioGroup: Open Advanced Search, select condition radio buttons
- [ ] FlexboxLayout: Select multiple filters, see chips appear
- [ ] FlexboxLayout: Click X on chips to remove filters
- [ ] BroadcastReceiver: Plug/unplug charger, see toast
- [ ] BroadcastReceiver: Toggle airplane mode, see connectivity toast
- [ ] BroadcastReceiver: Check battery level notification (if below 20%)
