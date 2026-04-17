# Quick Reference Card - Android Components

## 1-Line Definitions

| Component | Definition | Example |
|-----------|------------|---------|
| **Activity** | Screen/Page of app | MainActivity, LoginActivity |
| **XML** | UI design file | Layout, colors, styles |
| **Kotlin** | Business logic code | Functions, data processing |
| **Manifest** | App configuration | Permissions, activities |
| **EditText** | Input field | Email, password, search |
| **TextView** | Display text | Labels, titles, info |
| **Button** | Action trigger | Click to do something |
| **Spinner** | Dropdown list | Select one from many |
| **RadioButton** | Single choice | New/Used (only one) |
| **CheckBox** | Multiple choice | Features (AC, Sunroof) |
| **Intent** | Navigation + Data | Go to screen, pass data |
| **BroadcastReceiver** | System events listener | Battery, network, SMS |
| **Toast** | Quick message | 2-3 second popup |
| **ImagePicker** | Select image | Gallery se photo |
| **FlexboxLayout** | Flexible wrapping | Auto-wrap chips/tags |
| **RecyclerView** | Efficient list | Scrollable vehicle list |

---

## Code Snippets (Copy-Paste Ready)

### Button Click
```kotlin
binding.btnSearch.setOnClickListener {
    val query = binding.inputSearch.text.toString()
    performSearch(query)
}
```

### Intent Navigation
```kotlin
val intent = Intent(this, DetailActivity::class.java)
intent.putExtra("vehicle_id", vehicleId)
startActivity(intent)
```

### Intent Call/SMS
```kotlin
// Call
val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
startActivity(dialIntent)

// SMS
val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone"))
smsIntent.putExtra("sms_body", "Message")
startActivity(smsIntent)
```

### Toast
```kotlin
Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
```

### Spinner
```kotlin
val items = listOf("Toyota", "Honda", "Suzuki")
val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
spinner.adapter = adapter
val selected = spinner.selectedItem.toString()
```

### RadioButton
```kotlin
val selected = when (radioGroup.checkedRadioButtonId) {
    R.id.radio_new -> "New"
    R.id.radio_used -> "Used"
    else -> "All"
}
```

### CheckBox
```kotlin
if (checkbox.isChecked) {
    // Do something
}
```

### RecyclerView Setup
```kotlin
val adapter = VehicleAdapter(onItemClick = { vehicle ->
    // Handle click
})
recyclerView.layoutManager = LinearLayoutManager(context)
recyclerView.adapter = adapter
adapter.updateList(vehicles)
```

### BroadcastReceiver
```kotlin
class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BATTERY_LOW -> handleBatteryLow()
        }
    }
}
```

### Image Picker
```kotlin
val pickImage = registerForActivityResult(GetContent()) { uri ->
    imageView.setImageURI(uri)
}
pickImage.launch("image/*")
```

---

## ApniVehicle App - Component Usage

| Component | Where Used | Purpose |
|-----------|------------|---------|
| **Activity** | MainActivity, DetailActivity | Main screens |
| **Fragment** | HomeFragment, SearchFragment | Reusable screens |
| **RecyclerView** | Home, Search, Favorites | Vehicle lists |
| **EditText** | Search, Add Vehicle | User input |
| **Spinner** | Advanced Search | Brand, Fuel selection |
| **RadioButton** | Advanced Search | Condition (New/Used) |
| **Button** | Call, Message, Search | Actions |
| **Intent** | Navigation, Call, SMS | Screen change, system apps |
| **BroadcastReceiver** | MainActivity | Battery, network events |
| **FlexboxLayout** | Advanced Search | Filter chips |
| **Toast** | Throughout app | Quick feedback |
| **ImagePicker** | Add Vehicle | Upload photos |

---

## Exam Answer Templates

### Q: Activity lifecycle explain karo
**A:** Activity ke 7 states hain:
1. onCreate() - Screen create hota hai
2. onStart() - Screen visible hota hai
3. onResume() - User interact kar sakta hai
4. onPause() - Screen partially hidden
5. onStop() - Screen completely hidden
6. onDestroy() - Screen destroy hota hai
7. onRestart() - Stopped se wapas start

### Q: Intent kya hai?
**A:** Intent ek message object hai jo:
1. Ek screen se doosri screen pe jane ke liye (Explicit)
2. System apps use karne ke liye (Implicit)
3. Data pass karne ke liye
Example: MainActivity se DetailActivity pe vehicle ID pass karna

### Q: RecyclerView kaise kaam karta hai?
**A:** RecyclerView efficiently list dikhata hai:
1. ViewHolder pattern use karta hai (memory save)
2. Only visible items create karta hai
3. Scroll karne pe items reuse karta hai
4. Adapter se data bind hota hai

### Q: BroadcastReceiver ka use?
**A:** System events listen karne ke liye:
1. Battery low/okay
2. Network connectivity change
3. SMS receive
4. Charging status
Example: Battery low pe notification show karna

### Q: UI interactive kaise banate hain?
**A:** 
1. Click listeners add karo (buttons, items)
2. Real-time feedback do (Toast, Snackbar)
3. Animations use karo
4. Smooth transitions
5. Loading indicators
6. Error handling with messages

---

## Common Mistakes to Avoid

❌ **Wrong:**
```kotlin
// Null pointer exception
val text = editText.text.toString()
```

✅ **Correct:**
```kotlin
val text = binding.editText.text?.toString() ?: ""
```

❌ **Wrong:**
```kotlin
// Memory leak
registerReceiver(receiver, filter)
// Forgot to unregister
```

✅ **Correct:**
```kotlin
override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(receiver)
}
```

❌ **Wrong:**
```kotlin
// UI update on background thread
Thread {
    textView.text = "Updated"
}.start()
```

✅ **Correct:**
```kotlin
runOnUiThread {
    textView.text = "Updated"
}
```

---

## Project Demo Script

**Examiner:** "Apni app demo do"

**You:** 
1. "Yeh ApniVehicle app hai - vehicle marketplace"
2. "Home screen pe RecyclerView mein vehicles list hai"
3. "Search functionality hai with filters"
4. "Vehicle pe click karne se DetailActivity open hoti hai"
5. "Call button se Intent.ACTION_DIAL use karke dialer open hota hai"
6. "Message button se SMS app open hota hai"
7. "Advanced Search mein RadioButtons se condition select karte hain"
8. "FlexboxLayout mein active filters chips dikhte hain"
9. "BroadcastReceiver battery aur network events monitor karta hai"
10. "Add Vehicle mein ImagePicker se photos upload karte hain"

**Examiner:** "Code dikhao"

**You:** (Open any file and explain line by line)

---

## Final Tips

1. ✅ Har component ka ek clear example yaad rakho
2. ✅ ApniVehicle app mein kahan use hua, woh bata sako
3. ✅ Code snippet ready rakho (copy-paste)
4. ✅ Lifecycle, Intent, RecyclerView - yeh 3 important hain
5. ✅ Practical example do, theory kam bolo
6. ✅ "Hamare app mein..." se start karo answers

**All the best! 🚀**
