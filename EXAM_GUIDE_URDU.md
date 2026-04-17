# Android Exam Guide - ApniVehicle App
## Complete Explanation in Simple Words

---

## 1. ACTIVITY (Screen/Page)

### Kya hai?
Activity ek screen hai jahan user interact karta hai. Jaise mobile mein different pages hote hain.

### Hamare App mein:
```
MainActivity → Home screen with bottom navigation
DetailActivity → Vehicle ki details dikhata hai
LoginActivity → User login karta hai
SplashActivity → App start hone pe pehli screen
```

### Business Logic:
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Layout set karo
        setContentView(binding.root)
        
        // 2. Components initialize karo
        setupUI()
        
        // 3. Data load karo
        loadVehicles()
        
        // 4. Events handle karo
        setupListeners()
    }
}
```

### Exam Question:
**Q: Activity lifecycle kya hai?**
**A:** onCreate → onStart → onResume → onPause → onStop → onDestroy
- onCreate: Screen pehli baar banta hai
- onResume: User screen dekh sakta hai
- onPause: User doosri screen pe ja raha hai

---

## 2. XML (User Interface Design)

### Kya hai?
XML file mein hum UI design karte hain - buttons, text, colors, layout sab kuch.

### Example:
```xml
<LinearLayout
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <TextView
        android:text="Welcome"
        android:textSize="24sp" />
    
    <Button
        android:id="@+id/btnLogin"
        android:text="Login" />
</LinearLayout>
```

### Hamare App mein:
- `activity_home.xml` → Main screen layout
- `fragment_advanced_search.xml` → Search filters
- `item_vehicle.xml` → RecyclerView item design

### Exam Question:
**Q: XML mein UI kaise design karte hain?**
**A:** 
1. Layout choose karo (Linear, Relative, Constraint)
2. Views add karo (TextView, Button, EditText)
3. Properties set karo (width, height, color, text)
4. IDs do taake Kotlin mein access kar sako

---

## 3. KOTLIN (Business Logic)

### Kya hai?
Kotlin mein hum app ka logic likhte hain - button click, data save, calculations.

### Example:
```kotlin
// Button click handle karo
binding.btnSearch.setOnClickListener {
    val query = binding.inputSearch.text.toString()
    searchVehicles(query)
}

// Data process karo
fun searchVehicles(query: String) {
    val results = VehicleRepository.searchVehicles(query)
    adapter.updateList(results)
}
```

### Hamare App mein:
- `VehicleRepository.kt` → Data management
- `AdvancedSearchFragment.kt` → Search logic
- `DetailActivity.kt` → Vehicle details show karna

### Exam Question:
**Q: Kotlin mein business logic kaise implement karte hain?**
**A:**
1. Data classes banao (Vehicle, User)
2. Repository pattern use karo (data management)
3. Functions banao (search, filter, sort)
4. UI ko update karo (adapter.notifyDataSetChanged)

---

## 4. MANIFEST (App Configuration)

### Kya hai?
AndroidManifest.xml mein app ki settings hoti hain - permissions, activities, app name.

### Example:
```xml
<manifest>
    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    
    <application
        android:icon="@mipmap/ic_launcher"
        android:label="ApniVehicle">
        
        <!-- Activities -->
        <activity android:name=".MainActivity" />
        <activity android:name=".DetailActivity" />
    </application>
</manifest>
```

### Exam Question:
**Q: Manifest file ka kya kaam hai?**
**A:**
1. App permissions declare karna (Internet, Camera, Storage)
2. Activities register karna
3. App icon aur name set karna
4. Launch activity define karna (MAIN + LAUNCHER)

---

## 5. EDITTEXT (Input Field)

### Kya hai?
User se text input lene ke liye use hota hai.

### Hamare App mein:
```xml
<EditText
    android:id="@+id/input_model"
    android:hint="Enter car model"
    android:inputType="text" />
```

```kotlin
// Value get karo
val model = binding.inputModel.text.toString()

// Validation
if (model.isEmpty()) {
    binding.inputModel.error = "Model required"
}
```

### Use Cases:
- Login: Email, Password
- Search: Vehicle name
- Add Vehicle: Title, Description, Price

### Exam Question:
**Q: EditText se data kaise get karte hain?**
**A:**
```kotlin
val text = editText.text.toString()
if (text.isNotEmpty()) {
    // Process data
}
```

---

## 6. TEXTVIEW (Display Text)

### Kya hai?
Text dikhane ke liye - labels, headings, information.

### Example:
```xml
<TextView
    android:id="@+id/textPrice"
    android:text="Rs. 3,200,000"
    android:textSize="20sp"
    android:textStyle="bold" />
```

```kotlin
// Dynamically set text
binding.textPrice.text = "Rs. ${vehicle.price}"
binding.textTitle.text = vehicle.title
```

### Hamare App mein:
- Vehicle title dikhana
- Price display karna
- User info show karna

---

## 7. BUTTON (Action Trigger)

### Kya hai?
User action perform karne ke liye - click karke kuch hota hai.

### Example:
```xml
<Button
    android:id="@+id/btnSearch"
    android:text="Search"
    android:onClick="onSearchClick" />
```

```kotlin
binding.btnSearch.setOnClickListener {
    performSearch()
}

binding.btnCall.setOnClickListener {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phoneNumber")
    startActivity(intent)
}
```

### Hamare App mein:
- Search button → Search vehicles
- Call button → Open dialer
- Message button → Open SMS
- Add button → Add new vehicle

---

## 8. SPINNER (Dropdown)

### Kya hai?
Dropdown list se ek option select karne ke liye.

### Example:
```xml
<Spinner
    android:id="@+id/spinner_brand"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
// Data set karo
val brands = listOf("Toyota", "Honda", "Suzuki")
val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, brands)
spinner.adapter = adapter

// Selected item get karo
val selected = spinner.selectedItem.toString()
```

### Hamare App mein:
- Brand selection (Toyota, Honda, Suzuki)
- Transmission (Manual, Automatic)
- Fuel Type (Petrol, Diesel, CNG)

---

## 9. RADIOBUTTON (Single Choice)

### Kya hai?
Multiple options mein se sirf EK select kar sakte hain.

### Example:
```xml
<RadioGroup
    android:id="@+id/radio_group_condition">
    
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

```kotlin
// Selected option get karo
val condition = when (binding.radioGroupCondition.checkedRadioButtonId) {
    R.id.radio_new -> "New"
    R.id.radio_used -> "Used"
    R.id.radio_certified -> "Certified"
    else -> "All"
}
```

### Hamare App mein:
- Vehicle condition select karna (New/Used/Certified)
- Payment method (Cash/Bank Transfer)

---

## 10. CHECKBOX (Multiple Choice)

### Kya hai?
Multiple options mein se MULTIPLE select kar sakte hain.

### Example:
```xml
<CheckBox
    android:id="@+id/checkbox_ac"
    android:text="Air Conditioning" />

<CheckBox
    android:id="@+id/checkbox_sunroof"
    android:text="Sunroof" />
```

```kotlin
// Check karo selected hai ya nahi
if (binding.checkboxAc.isChecked) {
    features.add("AC")
}
if (binding.checkboxSunroof.isChecked) {
    features.add("Sunroof")
}
```

### Use Cases:
- Vehicle features (AC, Sunroof, Leather Seats)
- Filter options (Show favorites, Show verified sellers)

---

## 11. INTENT (Navigation + Data Transfer)

### Kya hai?
Ek screen se doosri screen pe jane ke liye aur data pass karne ke liye.

### Types:

#### A) Explicit Intent (Apni app ki screens)
```kotlin
// Simple navigation
val intent = Intent(this, DetailActivity::class.java)
startActivity(intent)

// Data pass karna
val intent = Intent(this, DetailActivity::class.java)
intent.putExtra("vehicle_id", vehicleId)
intent.putExtra("vehicle_name", "Toyota Corolla")
startActivity(intent)

// Data receive karna
val vehicleId = intent.getStringExtra("vehicle_id")
val vehicleName = intent.getStringExtra("vehicle_name")
```

#### B) Implicit Intent (System apps)
```kotlin
// Phone dialer
val dialIntent = Intent(Intent.ACTION_DIAL)
dialIntent.data = Uri.parse("tel:03001234567")
startActivity(dialIntent)

// SMS
val smsIntent = Intent(Intent.ACTION_VIEW)
smsIntent.data = Uri.parse("sms:03001234567")
smsIntent.putExtra("sms_body", "I'm interested in your car")
startActivity(smsIntent)

// Browser
val browserIntent = Intent(Intent.ACTION_VIEW)
browserIntent.data = Uri.parse("https://www.google.com")
startActivity(browserIntent)
```

### Hamare App mein:
- Home → Detail screen (vehicle details)
- Call button → Phone dialer
- Message button → SMS app

---

## 12. BROADCASTRECEIVER (System Events)

### Kya hai?
System events ko listen karta hai - battery, network, charging.

### Example:
```kotlin
class SystemBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BATTERY_LOW -> {
                // Battery low hai
                showNotification("Battery Low")
            }
            Intent.ACTION_POWER_CONNECTED -> {
                // Charging start hui
                Toast.makeText(context, "Charging", Toast.LENGTH_SHORT).show()
            }
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                // Internet connection change hua
                checkNetworkStatus()
            }
        }
    }
}

// Register karo
val receiver = SystemBroadcastReceiver()
val filter = IntentFilter().apply {
    addAction(Intent.ACTION_BATTERY_LOW)
    addAction(Intent.ACTION_POWER_CONNECTED)
}
registerReceiver(receiver, filter)
```

### Hamare App mein:
- Battery low notification
- Network connectivity check
- Charging status

### Exam Question:
**Q: BroadcastReceiver kab use karte hain?**
**A:**
1. System events listen karne ke liye
2. Battery status monitor karna
3. Network changes detect karna
4. SMS receive karna

---

## 13. TOAST (Quick Message)

### Kya hai?
Screen pe chhota message dikhana - 2-3 seconds ke liye.

### Example:
```kotlin
// Short toast (2 seconds)
Toast.makeText(context, "Vehicle added successfully", Toast.LENGTH_SHORT).show()

// Long toast (3.5 seconds)
Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG).show()

// Custom position
val toast = Toast.makeText(context, "Message", Toast.LENGTH_SHORT)
toast.setGravity(Gravity.TOP, 0, 100)
toast.show()
```

### Hamare App mein:
- "Vehicle added successfully"
- "Please enter vehicle title"
- "Charging started"
- "No internet connection"

---

## 14. IMAGE PICKER (Gallery se Image)

### Kya hai?
User gallery se image select kar sakta hai.

### Example:
```kotlin
// Image picker launch karo
val intent = Intent(Intent.ACTION_PICK)
intent.type = "image/*"
startActivityForResult(intent, REQUEST_IMAGE_PICK)

// Result handle karo
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
        val imageUri = data?.data
        binding.imageVehicle.setImageURI(imageUri)
        
        // Save URI
        vehicle.imageUri = imageUri.toString()
    }
}

// Modern way (Activity Result API)
val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    binding.imageVehicle.setImageURI(uri)
}

// Launch
pickImage.launch("image/*")
```

### Hamare App mein:
- Add Vehicle: Vehicle ki photo upload karna
- Profile: User ki profile picture
- Multiple images: Vehicle ki multiple photos

---

## 15. FLEXBOX & GRID LAYOUTS

### A) FlexboxLayout (Flexible Wrapping)

#### Kya hai?
Items automatically wrap ho jate hain jab space nahi hota.

```xml
<com.google.android.flexbox.FlexboxLayout
    android:id="@+id/flexbox_filters"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:flexWrap="wrap"
    app:alignItems="flex_start" />
```

```kotlin
// Dynamically chips add karo
activeFilters.forEach { (key, value) ->
    val chip = Chip(context).apply {
        text = "$key: $value"
        isCloseIconVisible = true
    }
    binding.flexboxFilters.addView(chip)
}
```

#### Hamare App mein:
- Search filters display (Brand: Toyota, Fuel: Petrol)
- Tags/Categories
- Dynamic buttons

### B) GridLayout (Grid Pattern)

```xml
<GridLayout
    android:columnCount="2"
    android:rowCount="3">
    
    <ImageView android:src="@drawable/car1" />
    <ImageView android:src="@drawable/car2" />
    <ImageView android:src="@drawable/car3" />
    <ImageView android:src="@drawable/car4" />
</GridLayout>
```

#### Use Cases:
- Image gallery (2x2 grid)
- Dashboard buttons
- Product catalog

---

## 16. RECYCLERVIEW (Scrollable List)

### Kya hai?
Badi list efficiently show karne ke liye - memory save hoti hai.

### Complete Implementation:

#### Step 1: XML Layout
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

#### Step 2: Item Layout (item_vehicle.xml)
```xml
<CardView>
    <LinearLayout>
        <ImageView android:id="@+id/imageVehicle" />
        <TextView android:id="@+id/textTitle" />
        <TextView android:id="@+id/textPrice" />
        <Button android:id="@+id/btnFavorite" />
    </LinearLayout>
</CardView>
```

#### Step 3: Adapter Class
```kotlin
class VehicleAdapter(
    private val onItemClick: (Vehicle) -> Unit,
    private val onFavoriteClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {
    
    private var vehicles = listOf<Vehicle>()
    
    // ViewHolder - item views hold karta hai
    class ViewHolder(val binding: ItemVehicleBinding) : 
        RecyclerView.ViewHolder(binding.root)
    
    // Layout inflate karo
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVehicleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    // Data bind karo
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vehicle = vehicles[position]
        holder.binding.apply {
            textTitle.text = vehicle.title
            textPrice.text = "Rs. ${vehicle.price}"
            imageVehicle.setImageResource(vehicle.image)
            
            // Click listeners
            root.setOnClickListener { onItemClick(vehicle) }
            btnFavorite.setOnClickListener { onFavoriteClick(vehicle) }
        }
    }
    
    // Total items
    override fun getItemCount() = vehicles.size
    
    // Update list
    fun updateList(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}
```

#### Step 4: Activity/Fragment mein use karo
```kotlin
// Setup
val adapter = VehicleAdapter(
    onItemClick = { vehicle ->
        // Detail screen pe jao
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("vehicle_id", vehicle.id)
        startActivity(intent)
    },
    onFavoriteClick = { vehicle ->
        // Favorite toggle karo
        VehicleRepository.toggleFavorite(vehicle.id)
        loadVehicles()
    }
)

binding.recyclerView.apply {
    layoutManager = LinearLayoutManager(context)
    adapter = this@MainActivity.adapter
}

// Data load karo
fun loadVehicles() {
    val vehicles = VehicleRepository.getVehicles()
    adapter.updateList(vehicles)
}
```

### Hamare App mein:
- Home screen: Vehicle list
- Search results: Filtered vehicles
- Favorites: Saved vehicles
- My Ads: User's posted vehicles

---

## COMPLETE FLOW EXAMPLE: Add Vehicle Feature

### 1. XML (UI Design)
```xml
<LinearLayout>
    <EditText android:id="@+id/inputTitle" android:hint="Vehicle Title" />
    <EditText android:id="@+id/inputPrice" android:inputType="number" />
    <Spinner android:id="@+id/spinnerBrand" />
    <RadioGroup android:id="@+id/radioCondition">
        <RadioButton android:text="New" />
        <RadioButton android:text="Used" />
    </RadioGroup>
    <Button android:id="@+id/btnAddImage" android:text="Add Photo" />
    <Button android:id="@+id/btnSubmit" android:text="Post Ad" />
</LinearLayout>
```

### 2. Kotlin (Business Logic)
```kotlin
class AddVehicleFragment : Fragment() {
    
    private var selectedImageUri: Uri? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupImagePicker()
        setupSubmitButton()
    }
    
    private fun setupImagePicker() {
        val pickImage = registerForActivityResult(GetContent()) { uri ->
            selectedImageUri = uri
            binding.imagePreview.setImageURI(uri)
        }
        
        binding.btnAddImage.setOnClickListener {
            pickImage.launch("image/*")
        }
    }
    
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                addVehicle()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val title = binding.inputTitle.text.toString()
        if (title.isEmpty()) {
            Toast.makeText(context, "Enter title", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val price = binding.inputPrice.text.toString()
        if (price.isEmpty()) {
            Toast.makeText(context, "Enter price", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun addVehicle() {
        val vehicle = Vehicle(
            title = binding.inputTitle.text.toString(),
            price = binding.inputPrice.text.toString().toLong(),
            brand = binding.spinnerBrand.selectedItem.toString(),
            condition = getSelectedCondition(),
            imageUri = selectedImageUri?.toString()
        )
        
        VehicleRepository.addVehicle(vehicle)
        
        // Notification
        NotificationHelper(requireContext()).showVehicleAdded(vehicle.title)
        
        // Toast
        Toast.makeText(context, "Vehicle added!", Toast.LENGTH_SHORT).show()
        
        // Navigate back
        requireActivity().onBackPressed()
    }
    
    private fun getSelectedCondition(): String {
        return when (binding.radioCondition.checkedRadioButtonId) {
            R.id.radio_new -> "New"
            else -> "Used"
        }
    }
}
```

---

## EXAM TIPS

### Common Questions:

**Q1: Activity aur Fragment mein kya difference hai?**
**A:** 
- Activity = Complete screen
- Fragment = Screen ka part (reusable)
- Ek Activity mein multiple Fragments ho sakte hain

**Q2: RecyclerView ListView se better kyun hai?**
**A:**
- Memory efficient (ViewHolder pattern)
- Smooth scrolling
- Flexible layouts (Linear, Grid, Staggered)

**Q3: Intent ke types?**
**A:**
- Explicit: Apni app ki screens (MainActivity → DetailActivity)
- Implicit: System apps (Phone, SMS, Browser)

**Q4: BroadcastReceiver kab use karte hain?**
**A:** System events listen karne ke liye (Battery, Network, SMS)

**Q5: UI ko interactive kaise banate hain?**
**A:**
1. Click listeners add karo
2. Animations use karo
3. Real-time updates do
4. Feedback do (Toast, Snackbar)
5. Smooth transitions

---

## PROJECT EXPLANATION SEQUENCE

Jab examiner puchhe: "Apni app explain karo"

### Step-by-step bolo:

1. **App Overview**
   - "ApniVehicle ek vehicle marketplace app hai"
   - "Users vehicles buy aur sell kar sakte hain"

2. **Architecture**
   - "Maine MVVM pattern use kiya"
   - "Repository pattern se data manage kiya"

3. **UI Components**
   - "RecyclerView se vehicle list dikhata hai"
   - "FlexboxLayout se filters display karta hai"
   - "RadioButtons se condition select karte hain"

4. **Features**
   - "Advanced search with multiple filters"
   - "Image picker se photos upload"
   - "Intent se call aur message"
   - "BroadcastReceiver se system events"

5. **Data Management**
   - "JSON files mein data store karta hai"
   - "SharedPreferences se user settings"
   - "Repository pattern se centralized data"

6. **User Experience**
   - "Toast messages se feedback"
   - "Notifications se updates"
   - "Smooth navigation with Intents"

---

Yeh sab samajh lo, exam mein koi problem nahi hogi! 🚀
