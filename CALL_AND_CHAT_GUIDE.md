# Call and Chat Functionality Guide

## Current Implementation

The call and chat features are currently implemented in the `DetailActivity` when viewing a vehicle's details.

### Location in Code

**File:** `app/src/main/java/com/example/apnivehicle/activities/DetailActivity.kt`

### How It Works

When a user clicks on a vehicle from the home page, they are taken to the `DetailActivity` which shows:
- Vehicle images
- Title, price, description
- Location and year
- **Two action buttons: Chat and Call**

### Current Behavior (Lines 73-86)

```kotlin
btnChat.setOnClickListener {
    Toast.makeText(
        this@DetailActivity,
        getString(R.string.contacting_seller_message, vehicle.title),
        Toast.LENGTH_SHORT
    ).show()
}

btnCall.setOnClickListener {
    Toast.makeText(
        this@DetailActivity,
        getString(R.string.calling_seller_message, vehicle.title),
        Toast.LENGTH_SHORT
    ).show()
}
```

Currently, both buttons show a Toast message. This is a placeholder implementation.

## How to Implement Real Functionality

### For Call Button

Replace the `btnCall.setOnClickListener` with:

```kotlin
btnCall.setOnClickListener {
    val phoneNumber = vehicle.sellerPhone // You need to add this field to Vehicle model
    if (phoneNumber.isNotEmpty()) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    } else {
        Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
    }
}
```

### For Chat Button

Replace the `btnChat.setOnClickListener` with one of these options:

**Option 1: Open WhatsApp**
```kotlin
btnChat.setOnClickListener {
    val phoneNumber = vehicle.sellerPhone // Format: +923001234567
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phoneNumber?text=Hi, I'm interested in ${vehicle.title}")
        }
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }
}
```

**Option 2: Open SMS**
```kotlin
btnChat.setOnClickListener {
    val phoneNumber = vehicle.sellerPhone
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$phoneNumber")
        putExtra("sms_body", "Hi, I'm interested in ${vehicle.title}")
    }
    startActivity(intent)
}
```

**Option 3: In-App Chat (Requires Backend)**
```kotlin
btnChat.setOnClickListener {
    // Navigate to chat screen with seller
    val intent = Intent(this, ChatActivity::class.java).apply {
        putExtra("SELLER_ID", vehicle.sellerId)
        putExtra("VEHICLE_ID", vehicle.id)
    }
    startActivity(intent)
}
```

## Required Changes to Vehicle Model

Add seller information to the `Vehicle` data class:

```kotlin
data class Vehicle(
    // ... existing fields ...
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerPhone: String = "",
    val sellerEmail: String = ""
)
```

## User Flow

1. User browses vehicles on **Home Page**
2. User clicks on a vehicle card
3. App opens **DetailActivity** showing full vehicle details
4. User sees **Chat** and **Call** buttons at the bottom
5. User clicks **Call** → Phone dialer opens with seller's number
6. User clicks **Chat** → WhatsApp/SMS/In-app chat opens

## Permissions Required

Add to `AndroidManifest.xml` if not already present:

```xml
<uses-permission android:name="android.permission.CALL_PHONE" />
```

Note: `ACTION_DIAL` doesn't require permission, but `ACTION_CALL` does.
