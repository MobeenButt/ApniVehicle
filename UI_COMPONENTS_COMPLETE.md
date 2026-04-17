# ApniVehicle - Complete UI Components Documentation

## Table of Contents
1. [Basic UI Components](#basic-ui-components)
2. [Input Components](#input-components)
3. [Layout Components](#layout-components)
4. [Navigation Components](#navigation-components)
5. [Display Components](#display-components)
6. [Dialog Components](#dialog-components)
7. [Material Design Components](#material-design-components)

---

## 1. BASIC UI COMPONENTS

### TextView
**Usage**: Display text, labels, titles, descriptions
**Where Used**:
- `activity_splash.xml` - App name, tagline
- `activity_detail.xml` - Vehicle title, price, description, city, year
- `item_vehicle_card.xml` - Vehicle title, price, city, year
- `fragment_home.xml` - Empty state message
- `fragment_settings.xml` - User email, username, verification status
- `fragment_verification.xml` - Verification status labels
- `fragment_comparison.xml` - Vehicle count, comparison labels

**Properties Used**:
```xml
android:text="Text content"
android:textSize="16sp"
android:textColor="@color/black"
android:textStyle="bold"
android:gravity="center"
android:maxLines="2"
android:ellipsize="end"
```

---

### Button
**Usage**: Click actions, submit forms, navigation
**Where Used**:
- `activity_login.xml` - Login button, Sign Up link
- `activity_signup.xml` - Sign Up button, Login link
- `activity_onboarding.xml` - Next button, Skip button
- `fragment_add_vehicle.xml` - Submit button, Add images button
- `fragment_settings.xml` - Logout button
- `fragment_verification.xml` - Verify Email, Verify Phone, Submit CNIC buttons
- `fragment_comparison.xml` - Add to comparison, Clear, Save, Export buttons
- `activity_detail.xml` - Chat button, Call button

**Properties Used**:
```xml
android:text="Button Text"
android:onClick="onButtonClick"
android:enabled="true"
android:background="@drawable/button_background"
style="@style/Widget.Material3.Button"
```

---

### ImageView
**Usage**: Display images, icons, vehicle photos, avatars
**Where Used**:
- `activity_splash.xml` - App logo
- `activity_detail.xml` - Vehicle main image
- `item_vehicle_card.xml` - Vehicle thumbnail
- `item_onboarding.xml` - Onboarding illustrations
- `fragment_user_profile.xml` - User avatar
- `fragment_verification.xml` - CNIC front and back images
- Toolbar icons - Search, filter, sort, notifications

**Properties Used**:
```xml
android:src="@drawable/ic_logo"
android:scaleType="centerCrop"
android:contentDescription="Vehicle image"
android:adjustViewBounds="true"
```

---

### ImageButton
**Usage**: Icon buttons with click actions
**Where Used**:
- `item_vehicle_card.xml` - Favorite button (heart icon)
- `item_vehicle_card.xml` - Edit button (owner only)
- `item_vehicle_card.xml` - Delete button (owner only)

**Properties Used**:
```xml
android:src="@drawable/ic_favorite"
android:background="?attr/selectableItemBackgroundBorderless"
android:contentDescription="Add to favorites"
```

---

## 2. INPUT COMPONENTS

### EditText
**Usage**: Text input fields
**Where Used**:
- `activity_login.xml` - Email input, Password input
- `activity_signup.xml` - Email, Username, Password, Phone inputs
- `fragment_add_vehicle.xml` - Title, Price, Description, Mileage inputs
- `fragment_advanced_search.xml` - Search query, Min price, Max price
- `fragment_user_profile.xml` - Bio, Phone, Location inputs
- `fragment_verification.xml` - CNIC number input
- `dialog_edit_vehicle.xml` - Edit title, price, description

**Types Used**:
```xml
<!-- Text Input -->
<EditText
    android:inputType="text"
    android:hint="Enter title" />

<!-- Email Input -->
<EditText
    android:inputType="textEmailAddress"
    android:hint="Email" />

<!-- Password Input -->
<EditText
    android:inputType="textPassword"
    android:hint="Password" />

<!-- Number Input -->
<EditText
    android:inputType="number"
    android:hint="Price" />

<!-- Phone Input -->
<EditText
    android:inputType="phone"
    android:hint="03XX-XXXXXXX" />

<!-- Multiline Text -->
<EditText
    android:inputType="textMultiLine"
    android:lines="4"
    android:hint="Description" />
```

---

### TextInputLayout (Material Design)
**Usage**: Enhanced text input with floating labels and error messages
**Where Used**:
- `activity_login.xml` - Email and password fields
- `activity_signup.xml` - All registration fields
- `fragment_add_vehicle.xml` - All form fields
- `fragment_user_profile.xml` - Profile edit fields

**Features**:
```xml
<com.google.android.material.textfield.TextInputLayout
    android:hint="Email"
    app:errorEnabled="true"
    app:helperText="Enter your email"
    app:counterEnabled="true"
    app:counterMaxLength="50"
    app:endIconMode="clear_text"
    app:startIconDrawable="@drawable/ic_email">
    
    <com.google.android.material.textfield.TextInputEditText
        android:inputType="textEmailAddress" />
        
</com.google.android.material.textfield.TextInputLayout>
```

**Error Display**:
```kotlin
textInputLayout.error = "Invalid email format"
textInputLayout.isErrorEnabled = true
```

---

### CheckBox
**Usage**: Boolean selections
**Where Used**:
- `activity_login.xml` - "Remember Me" checkbox

**Properties**:
```xml
<CheckBox
    android:text="Remember Me"
    android:checked="false" />
```

**Kotlin Usage**:
```kotlin
binding.checkboxRememberMe.isChecked = true
binding.checkboxRememberMe.setOnCheckedChangeListener { _, isChecked ->
    // Handle state change
}
```

---

### Switch / SwitchMaterial
**Usage**: Toggle settings on/off
**Where Used**:
- `fragment_settings.xml` - Dark theme toggle
- `fragment_settings.xml` - Notifications toggle

**Properties**:
```xml
<com.google.android.material.switchmaterial.SwitchMaterial
    android:text="Dark Theme"
    android:checked="false" />
```

**Kotlin Usage**:
```kotlin
binding.switchTheme.isChecked = preferenceManager.isDarkTheme
binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
    ThemeManager.setTheme(requireContext(), isChecked)
}
```

---

### Spinner / AutoCompleteTextView
**Usage**: Dropdown selections
**Where Used**:
- `fragment_add_vehicle.xml` - Vehicle type, City, Fuel type, Transmission, Condition dropdowns
- `fragment_advanced_search.xml` - Brand, Model, City, Fuel, Transmission dropdowns

**Implementation**:
```xml
<AutoCompleteTextView
    android:hint="Select City"
    android:inputType="none" />
```

**Kotlin Setup**:
```kotlin
val cities = Constants.PAKISTANI_CITIES
val adapter = ArrayAdapter(requireContext(), R.layout.list_item, cities)
binding.spinnerCity.setAdapter(adapter)

// Get selected value
val selectedCity = binding.spinnerCity.text.toString()
```

---

### Slider (Material Design)
**Usage**: Range selection for numbers
**Where Used**:
- `fragment_advanced_search.xml` - Price range slider
- `fragment_advanced_search.xml` - Mileage range slider

**Implementation**:
```xml
<com.google.android.material.slider.RangeSlider
    android:valueFrom="0"
    android:valueTo="10000000"
    android:stepSize="100000"
    app:values="@array/initial_slider_values" />
```

**Kotlin Usage**:
```kotlin
binding.sliderPrice.addOnChangeListener { slider, value, fromUser ->
    val values = slider.values
    val minPrice = values[0].toLong()
    val maxPrice = values[1].toLong()
}
```

---

## 3. LAYOUT COMPONENTS

### LinearLayout
**Usage**: Arrange views in horizontal or vertical line
**Where Used**: Almost all layouts as container

**Vertical Example**:
```xml
<LinearLayout
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <TextView android:text="Title" />
    <EditText android:hint="Input" />
    <Button android:text="Submit" />
    
</LinearLayout>
```

**Horizontal Example**:
```xml
<LinearLayout
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <ImageView android:src="@drawable/icon" />
    <TextView android:text="Label" />
    
</LinearLayout>
```

---

### ConstraintLayout
**Usage**: Flexible positioning with constraints
**Where Used**:
- `activity_detail.xml` - Complex vehicle detail layout
- `item_vehicle_card.xml` - Vehicle card layout
- Most modern layouts

**Example**:
```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    
    <ImageView
        android:id="@+id/imageVehicle"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
    
    <TextView
        android:id="@+id/textTitle"
        app:layout_constraintTop_toBottomOf="@id/imageVehicle"
        app:layout_constraintStart_toStartOf="parent" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

### FrameLayout
**Usage**: Stack views on top of each other
**Where Used**:
- `activity_home.xml` - Fragment container
- Badge overlays on images

**Example**:
```xml
<FrameLayout
    android:id="@+id/fragment_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

---

### ScrollView / NestedScrollView
**Usage**: Make content scrollable
**Where Used**:
- `activity_login.xml` - Login form
- `activity_signup.xml` - Registration form
- `fragment_add_vehicle.xml` - Vehicle form
- `fragment_settings.xml` - Settings list
- `activity_detail.xml` - Vehicle details

**Example**:
```xml
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <LinearLayout
        android:orientation="vertical">
        <!-- Scrollable content -->
    </LinearLayout>
    
</androidx.core.widget.NestedScrollView>
```

---

### CardView
**Usage**: Material Design cards with elevation
**Where Used**:
- `item_vehicle_card.xml` - Vehicle cards in RecyclerView
- `fragment_comparison.xml` - Comparison cards
- `fragment_user_profile.xml` - Profile sections

**Example**:
```xml
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp"
    app:cardUseCompatPadding="true">
    
    <!-- Card content -->
    
</androidx.cardview.widget.CardView>
```

---

## 4. NAVIGATION COMPONENTS

### BottomNavigationView
**Usage**: Bottom navigation bar with 5 tabs
**Where Used**: `activity_home.xml`

**Implementation**:
```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottom_navigation"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:menu="@menu/bottom_nav_menu" />
```

**Menu File** (`menu/bottom_nav_menu.xml`):
```xml
<menu>
    <item
        android:id="@+id/nav_home"
        android:icon="@drawable/ic_home"
        android:title="Home" />
    <item
        android:id="@+id/nav_used_cars"
        android:icon="@drawable/ic_search"
        android:title="Used Cars" />
    <item
        android:id="@+id/nav_new_cars"
        android:icon="@drawable/ic_add"
        android:title="New Cars" />
    <item
        android:id="@+id/nav_bikes"
        android:icon="@drawable/ic_my_ads"
        android:title="Bikes" />
    <item
        android:id="@+id/nav_more"
        android:icon="@drawable/ic_settings"
        android:title="More" />
</menu>
```

**Kotlin Usage**:
```kotlin
binding.bottomNavigation.setOnItemSelectedListener { item ->
    when (item.itemId) {
        R.id.nav_home -> openFragment(HomeFragment())
        R.id.nav_used_cars -> openFragment(SearchFragment())
        // ...
    }
    true
}
```

---

### Toolbar / AppBarLayout
**Usage**: Top app bar with title and action buttons
**Where Used**: `activity_home.xml`

**Implementation**:
```xml
<com.google.android.material.appbar.AppBarLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar_home"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        app:menu="@menu/menu_toolbar" />
        
</com.google.android.material.appbar.AppBarLayout>
```

**Menu File** (`menu/menu_toolbar.xml`):
```xml
<menu>
    <item
        android:id="@+id/action_search"
        android:icon="@drawable/ic_search"
        android:title="Search"
        app:showAsAction="always"
        app:actionViewClass="androidx.appcompat.widget.SearchView" />
    <item
        android:id="@+id/action_notifications"
        android:icon="@drawable/ic_notifications"
        android:title="Notifications"
        app:showAsAction="always" />
    <item
        android:id="@+id/action_sort"
        android:icon="@drawable/ic_sort"
        android:title="Sort"
        app:showAsAction="ifRoom" />
    <item
        android:id="@+id/action_filter"
        android:icon="@drawable/ic_filter"
        android:title="Filter"
        app:showAsAction="ifRoom" />
    <item
        android:id="@+id/action_toggle_layout"
        android:icon="@drawable/ic_grid"
        android:title="Toggle Layout"
        app:showAsAction="ifRoom" />
</menu>
```

---

### ViewPager2
**Usage**: Swipeable pages
**Where Used**:
- `activity_onboarding.xml` - Onboarding screens
- Vehicle image galleries

**Implementation**:
```xml
<androidx.viewpager2.widget.ViewPager2
    android:id="@+id/view_pager"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

**Kotlin Setup**:
```kotlin
val adapter = OnboardingAdapter(onboardingItems)
binding.viewPager.adapter = adapter

binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
        // Handle page change
    }
})
```

---

### TabLayout
**Usage**: Page indicators for ViewPager2
**Where Used**: `activity_onboarding.xml`

**Implementation**:
```xml
<com.google.android.material.tabs.TabLayout
    android:id="@+id/tab_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

**Link with ViewPager2**:
```kotlin
TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
    // Optional: Set tab text or icon
}.attach()
```

---

## 5. DISPLAY COMPONENTS

### RecyclerView
**Usage**: Efficient scrollable lists
**Where Used**:
- `fragment_home.xml` - Vehicle list
- `fragment_search.xml` - Search results
- `fragment_favorite.xml` - Favorite vehicles
- `fragment_my_ads.xml` - User's ads

**Implementation**:
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recycler_vehicles"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

**Kotlin Setup**:
```kotlin
// Linear Layout
binding.recyclerVehicles.layoutManager = LinearLayoutManager(requireContext())

// Grid Layout
binding.recyclerVehicles.layoutManager = GridLayoutManager(requireContext(), 2)

// Set Adapter
val adapter = VehicleAdapter(onItemClick, onFavoriteClick)
binding.recyclerVehicles.adapter = adapter
adapter.submitList(vehicleList)
```

**Adapter Implementation**:
```kotlin
class VehicleAdapter(
    private val onItemClick: (Vehicle) -> Unit,
    private val onFavoriteClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {
    
    private var vehicles = listOf<Vehicle>()
    
    fun submitList(list: List<Vehicle>) {
        vehicles = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVehicleCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }
    
    override fun getItemCount() = vehicles.size
    
    inner class ViewHolder(private val binding: ItemVehicleCardBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(vehicle: Vehicle) {
            binding.textTitle.text = vehicle.title
            binding.textPrice.text = formatPrice(vehicle.price)
            // ... set other views
            
            binding.root.setOnClickListener { onItemClick(vehicle) }
            binding.btnFavorite.setOnClickListener { onFavoriteClick(vehicle) }
        }
    }
}
```

---

### ChipGroup & Chip
**Usage**: Category filters, tags
**Where Used**: `fragment_home.xml` - Vehicle type filters

**Implementation**:
```xml
<com.google.android.material.chip.ChipGroup
    android:id="@+id/chip_group_categories"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:singleSelection="true"
    app:selectionRequired="false">
    
    <com.google.android.material.chip.Chip
        android:id="@+id/chip_all"
        android:text="All"
        style="@style/Widget.Material3.Chip.Filter"
        android:checked="true" />
    
    <com.google.android.material.chip.Chip
        android:id="@+id/chip_cars"
        android:text="Cars"
        style="@style/Widget.Material3.Chip.Filter" />
    
    <com.google.android.material.chip.Chip
        android:id="@+id/chip_bikes"
        android:text="Bikes"
        style="@style/Widget.Material3.Chip.Filter" />
        
</com.google.android.material.chip.ChipGroup>
```

**Kotlin Usage**:
```kotlin
binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
    selectedCategory = when (checkedIds.firstOrNull()) {
        R.id.chip_cars -> VehicleType.CAR
        R.id.chip_bikes -> VehicleType.MOTORCYCLE
        else -> null
    }
    loadVehicles()
}
```

---

### ProgressBar
**Usage**: Loading indicators
**Where Used**: Loading states in fragments

**Types**:
```xml
<!-- Circular Progress -->
<ProgressBar
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:indeterminate="true" />

<!-- Horizontal Progress -->
<ProgressBar
    style="@style/Widget.AppCompat.ProgressBar.Horizontal"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:progress="50"
    android:max="100" />
```

---

### SearchView
**Usage**: Search input in toolbar
**Where Used**: Toolbar menu

**Implementation**:
```kotlin
override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_toolbar, menu)
    val searchItem = menu.findItem(R.id.action_search)
    val searchView = searchItem?.actionView as? SearchView
    
    searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            // Handle search submit
            return false
        }
        
        override fun onQueryTextChange(newText: String?): Boolean {
            // Handle real-time search
            return true
        }
    })
    
    return true
}
```

---

## 6. DIALOG COMPONENTS

### AlertDialog
**Usage**: Confirmation dialogs, info dialogs
**Where Used**:
- `SettingsFragment` - Logout confirmation, About, Privacy, Terms
- `VehicleDialogs` - Delete confirmation
- `VerificationFragment` - Verification code entry

**Basic Dialog**:
```kotlin
AlertDialog.Builder(requireContext())
    .setTitle("Logout")
    .setMessage("Are you sure you want to logout?")
    .setPositiveButton("Logout") { _, _ ->
        // Handle logout
    }
    .setNegativeButton("Cancel", null)
    .show()
```

**Dialog with Input**:
```kotlin
val input = EditText(requireContext()).apply {
    hint = "Enter code"
}

AlertDialog.Builder(requireContext())
    .setTitle("Verify Email")
    .setView(input)
    .setPositiveButton("Verify") { _, _ ->
        val code = input.text.toString()
        // Verify code
    }
    .setNegativeButton("Cancel", null)
    .show()
```

**Single Choice Dialog**:
```kotlin
val options = arrayOf("Option 1", "Option 2", "Option 3")
var selectedIndex = 0

AlertDialog.Builder(requireContext())
    .setTitle("Select Option")
    .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
        selectedIndex = which
        dialog.dismiss()
    }
    .show()
```

**Multi Choice Dialog**:
```kotlin
val items = arrayOf("Item 1", "Item 2", "Item 3")
val checkedItems = BooleanArray(items.size) { false }

AlertDialog.Builder(requireContext())
    .setTitle("Select Items")
    .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
        checkedItems[which] = isChecked
    }
    .setPositiveButton("OK") { _, _ ->
        // Handle selection
    }
    .show()
```

---

### Custom Dialog
**Usage**: Edit vehicle dialog
**Where Used**: `VehicleDialogs.showEditDialog()`

**Layout**: `dialog_edit_vehicle.xml`
```xml
<LinearLayout
    android:orientation="vertical"
    android:padding="16dp">
    
    <TextView
        android:text="Edit Vehicle"
        android:textSize="20sp"
        android:textStyle="bold" />
    
    <EditText
        android:id="@+id/edit_title"
        android:hint="Title" />
    
    <EditText
        android:id="@+id/edit_price"
        android:hint="Price"
        android:inputType="number" />
    
    <Button
        android:id="@+id/btn_save"
        android:text="Save" />
        
</LinearLayout>
```

**Kotlin Implementation**:
```kotlin
val dialogView = layoutInflater.inflate(R.layout.dialog_edit_vehicle, null)
val dialog = AlertDialog.Builder(context)
    .setView(dialogView)
    .create()

dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
    // Save changes
    dialog.dismiss()
}

dialog.show()
```

---

## 7. MATERIAL DESIGN COMPONENTS

### FloatingActionButton (FAB)
**Usage**: Primary action button
**Where Used**: `activity_home.xml` - Post Ad button

**Implementation**:
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab_post_ad"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:src="@drawable/ic_add"
    android:contentDescription="Post Ad"
    app:layout_anchor="@id/bottom_navigation"
    app:layout_anchorGravity="top|center" />
```

**Kotlin Usage**:
```kotlin
binding.fabPostAd.setOnClickListener {
    // Navigate to add vehicle
}
```

---

### Snackbar
**Usage**: Brief messages with optional action
**Where Used**: Throughout app for feedback

**Implementation**:
```kotlin
Snackbar.make(binding.root, "Vehicle added successfully", Snackbar.LENGTH_SHORT)
    .setAction("UNDO") {
        // Handle undo
    }
    .show()
```

---

### Toast
**Usage**: Short messages
**Where Used**: Throughout app for quick feedback

**Implementation**:
```kotlin
Toast.makeText(requireContext(), "Saved to favorites", Toast.LENGTH_SHORT).show()
```

---

### Badge
**Usage**: Notification count indicators
**Where Used**: Bottom navigation, toolbar icons

**Implementation**:
```kotlin
val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_notifications)
badge.isVisible = true
badge.number = 5
```

---

## SUMMARY OF ALL UI COMPONENTS USED

### Input Components (8)
1. EditText - Text input
2. TextInputLayout - Enhanced text input with validation
3. CheckBox - Boolean selection
4. Switch/SwitchMaterial - Toggle settings
5. Spinner/AutoCompleteTextView - Dropdown selection
6. Slider/RangeSlider - Range selection
7. SearchView - Search input
8. RadioButton/RadioGroup - Single choice selection

### Display Components (10)
1. TextView - Text display
2. ImageView - Image display
3. RecyclerView - Scrollable lists
4. CardView - Material cards
5. ChipGroup & Chip - Category filters
6. ProgressBar - Loading indicators
7. Badge - Notification indicators
8. Divider - Visual separators
9. Space - Layout spacing
10. View - Generic view for backgrounds

### Button Components (4)
1. Button - Standard button
2. ImageButton - Icon button
3. FloatingActionButton - Primary action
4. IconButton - Toolbar icons

### Layout Components (5)
1. LinearLayout - Linear arrangement
2. ConstraintLayout - Flexible positioning
3. FrameLayout - Stacked views
4. ScrollView/NestedScrollView - Scrollable content
5. CoordinatorLayout - Complex behaviors

### Navigation Components (4)
1. BottomNavigationView - Bottom tabs
2. Toolbar/AppBarLayout - Top app bar
3. ViewPager2 - Swipeable pages
4. TabLayout - Page indicators

### Dialog Components (3)
1. AlertDialog - Standard dialogs
2. Custom Dialog - Custom layouts
3. BottomSheetDialog - Bottom sheets

### Feedback Components (2)
1. Snackbar - Brief messages with action
2. Toast - Quick messages

---

## TOTAL COUNT: 36+ Different UI Components Used

Yeh saare UI components aapke ApniVehicle project mein use hue hain!
