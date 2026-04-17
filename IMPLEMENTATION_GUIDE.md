# Implementation Guide: RadioButton/RadioGroup, BroadcastReceiver, and FlexboxLayout

This document explains the implementation of three Android components in the ApniVehicle app.

---

## 1. RadioButton/RadioGroup Implementation

### Location
**File:** `app/src/main/res/layout/fragment_advanced_search.xml`
**Fragment:** `app/src/main/java/com/example/apnivehicle/fragments/AdvancedSearchFragment.kt`

### What It Does
Replaces the Spinner dropdown for vehicle condition selection with RadioButtons, providing a better UX for selecting between mutually exclusive options (All, New, Used, Certified).

### Implementation Details

#### XML Layout (fragment_advanced_search.xml)
```xml
<RadioGroup
    android:id="@+id/radio_group_condition"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <RadioButton
        android:id="@+id/radio_all_condition"
        android:text="All"
        android:checked="true" />

    <RadioButton
        android:id="@+id/radio_new"
        android:text="New" />

    <RadioButton
        android:id="@+id/radio_used"
        android:text="Used" />

    <RadioButton
        android:id="@+id/radio_certified"
        android:text="Certified" />
</RadioGroup>
```

#### Kotlin Code (AdvancedSearchFragment.kt)
```kotlin
// Get selected condition from RadioGroup
private fun getSelectedCondition(): String? {
    return when (binding.radioGroupCondition.checkedRadioButtonId) {
        R.id.radio_new -> "New"
        R.id.radio_used -> "Used"
        R.id.radio_certified -> "Certified"
        else -> null // All
    }
}

// Listen to RadioGroup changes
binding.radioGroupCondition.setOnCheckedChangeListener { _, _ ->
    updateActiveFilters()
}
```

### How to Use
1. Open the app and navigate to Advanced Search (from toolbar menu)
2. Select a vehicle condition using the radio buttons
3. The selection is mutually exclusive - only one can be selected at a time
4. Click "Search" to filter vehicles by the selected condition

---

## 2. BroadcastReceiver Implementation

### Location
**File:** `app/src/main/java/com/example/apnivehicle/receivers/SystemBroadcastReceiver.kt`
**Registered in:** `app/src/main/java/com/example/apnivehicle/activities/MainActivity.kt`

### What It Does
Monitors system-level events like battery status, charging state, and network connectivity changes. Shows notifications and toasts when these events occur.

### Implementation Details

#### SystemBroadcastReceiver.kt
```kotlin
class SystemBroadcastReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BATTERY_LOW -> handleBatteryLow(context)
            Intent.ACTION_BATTERY_OKAY -> handleBatteryOkay(context)
            Intent.ACTION_POWER_CONNECTED -> handlePowerConnected(context)
            Intent.ACTION_POWER_DISCONNECTED -> handlePowerDisconnected(context)
            ConnectivityManager.CONNECTIVITY_ACTION -> handleConnectivityChange(context)
        }
    }
    
    private fun handleBatteryLow(context: Context) {
        val batteryLevel = getBatteryLevel(context)
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showBatteryLowNotification(batteryLevel)
        Toast.makeText(context, "Battery Low: $batteryLevel%", Toast.LENGTH_SHORT).show()
    }
    
    companion object {
        fun register(context: Context): SystemBroadcastReceiver {
            val receiver = SystemBroadcastReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_BATTERY_OKAY)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            }
            context.registerReceiver(receiver, filter)
            return receiver
        }
    }
}
```

#### MainActivity.kt Integration
```kotlin
class MainActivity : AppCompatActivity() {
    private var systemReceiver: SystemBroadcastReceiver? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register BroadcastReceiver
        systemReceiver = SystemBroadcastReceiver.register(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister to prevent memory leaks
        systemReceiver?.let {
            SystemBroadcastReceiver.unregister(this, it)
        }
    }
}
```

### Monitored Events
1. **Battery Low** - Shows notification when battery drops below 20%
2. **Battery Okay** - Notifies when battery level is restored
3. **Power Connected** - Shows toast when device starts charging
4. **Power Disconnected** - Shows toast when charging stops
5. **Network Connectivity** - Monitors internet connection changes

### How to Test
1. Run the app on a device or emulator
2. Disconnect/reconnect the charger to see charging notifications
3. Toggle airplane mode to see network connectivity changes
4. Let battery drop below 20% to see low battery notification (on real device)

---

## 3. FlexboxLayout Implementation

### Location
**File:** `app/src/main/res/layout/fragment_advanced_search.xml`
**Fragment:** `app/src/main/java/com/example/apnivehicle/fragments/AdvancedSearchFragment.kt`

### What It Does
Displays active search filters as removable chips in a flexible, wrapping layout. Chips automatically wrap to the next line when they don't fit horizontally.

### Dependency Added
**File:** `app/build.gradle.kts`
```kotlin
implementation("com.google.android.flexbox:flexbox:3.0.0")
```

### Implementation Details

#### XML Layout (fragment_advanced_search.xml)
```xml
<com.google.android.material.textview.MaterialTextView
    android:id="@+id/text_active_filters_label"
    android:text="Active Filters"
    android:visibility="gone" />

<com.google.android.flexbox.FlexboxLayout
    android:id="@+id/flexbox_filters"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:visibility="gone"
    app:flexWrap="wrap"
    app:alignItems="flex_start"
    app:alignContent="flex_start" />
```

#### Kotlin Code (AdvancedSearchFragment.kt)
```kotlin
private val activeFilters = mutableMapOf<String, String>()

private fun updateFilterChips() {
    binding.flexboxFilters.removeAllViews()
    
    if (activeFilters.isEmpty()) {
        binding.textActiveFiltersLabel.visibility = View.GONE
        binding.flexboxFilters.visibility = View.GONE
        return
    }
    
    binding.textActiveFiltersLabel.visibility = View.VISIBLE
    binding.flexboxFilters.visibility = View.VISIBLE
    
    activeFilters.forEach { (key, value) ->
        val chip = Chip(requireContext()).apply {
            text = "$key: $value"
            isCloseIconVisible = true
            setChipBackgroundColorResource(R.color.primary)
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            
            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 4, 8, 4)
            layoutParams = params
            
            setOnCloseIconClickListener {
                removeFilter(key)
            }
        }
        binding.flexboxFilters.addView(chip)
    }
}
```

### Features
- **Dynamic Chip Creation**: Chips are created for each active filter
- **Removable**: Each chip has a close icon to remove the filter
- **Flexible Wrapping**: Chips automatically wrap to new lines
- **Visual Feedback**: Shows which filters are currently active
- **Interactive**: Clicking the X on a chip removes that filter

### How to Use
1. Open Advanced Search
2. Select filters (brand, transmission, fuel type, condition, etc.)
3. Active filters appear as chips below the filter controls
4. Click the X on any chip to remove that filter
5. The filter chips update automatically as you change selections

---

## Summary

### Files Modified/Created

#### Created:
- `app/src/main/java/com/example/apnivehicle/receivers/SystemBroadcastReceiver.kt`

#### Modified:
- `app/build.gradle.kts` - Added Flexbox dependency
- `app/src/main/res/layout/fragment_advanced_search.xml` - Added RadioGroup and FlexboxLayout
- `app/src/main/java/com/example/apnivehicle/fragments/AdvancedSearchFragment.kt` - Implemented RadioGroup and FlexboxLayout logic
- `app/src/main/java/com/example/apnivehicle/activities/MainActivity.kt` - Registered BroadcastReceiver

### Key Benefits

1. **RadioButton/RadioGroup**: Better UX for mutually exclusive selections
2. **BroadcastReceiver**: Real-time system event monitoring and user notifications
3. **FlexboxLayout**: Flexible, responsive filter chip display with dynamic wrapping

All implementations follow Android best practices and are production-ready!
