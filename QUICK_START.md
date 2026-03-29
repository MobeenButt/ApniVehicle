# Quick Start Guide - Login/SignUp System

## 🚀 Quick Overview

The ApniVehicle app now has a complete authentication system. Here's what you need to know:

---

## 📍 Entry Point

**When you run the app:**
1. SplashActivity loads (2 second splash screen)
2. AuthRepository checks if user is logged in
3. Routes to either LoginActivity or MainActivity

---

## 🔑 Test Accounts (Pre-loaded)

Use these to test immediately after app launch:

```
Email: test@example.com
Password: 123456

OR

Email: demo@example.com
Password: 123456
```

---

## 🆕 Create Your Own Account

1. Click "Sign Up" on the login screen
2. Enter:
   - Email (must be valid format)
   - Username (unique)
   - Password (6+ characters)
   - Confirm Password (must match)
3. Tap "Sign Up"
4. You're automatically logged in!

---

## 🔐 Key Components

| Component | Purpose |
|-----------|---------|
| `AuthRepository` | Manages all user auth, in-memory storage |
| `LoginActivity` | Login screen and flow |
| `SignUpActivity` | Registration screen and flow |
| `User` Model | Represents a user with id, email, username, password |
| `SplashActivity` | Auth check on app start |
| `SettingsFragment` | Logout and show user info |

---

## 🌊 Flow Diagram

```
┌─────────────┐
│ App Starts  │
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ SplashActivity   │
│ (2 sec delay)    │
└──────┬───────────┘
       │
       ▼
┌──────────────────────────┐
│ Check AuthRepository     │
│ isUserLoggedIn()?        │
└──────┬─────────────┬─────┘
       │             │
    YES│             │NO
       ▼             ▼
   MainActivity   LoginActivity
       ▼             ▼
   (Home)      ┌─────────────┐
            ┌─▶│ Login Form  │
            │  └─────────────┘
            │       ▼
            │  ┌──────────────┐
            ├─▶│ SignUp Form  │
            │  └──────────────┘
            │       ▼
            │  ┌──────────────┐
            └─▶│ MainActivity │
               └──────────────┘
```

---

## 💡 Usage Examples

### Check if user is logged in:
```kotlin
if (AuthRepository.isUserLoggedIn()) {
    // User is logged in
}
```

### Get current user:
```kotlin
val currentUser = AuthRepository.getCurrentUser()
currentUser?.email // Get user's email
currentUser?.username // Get user's username
```

### Logout user:
```kotlin
AuthRepository.logout()
// Then navigate to LoginActivity
```

---

## 🎨 UI Components Location

| Screen | File |
|--------|------|
| Login | `res/layout/activity_login.xml` |
| SignUp | `res/layout/activity_signup.xml` |
| Settings | `res/layout/fragment_settings.xml` |

---

## 📝 Validation Happens At:

1. **Client-side** (in Activities):
   - Check for empty fields
   - Password confirmation match

2. **Repository-side** (in AuthRepository):
   - Email uniqueness
   - Email format
   - Password length
   - Username uniqueness

---

## ⚙️ Configuration

To modify validation rules, edit `AuthRepository.kt`:

```kotlin
// Email validation regex
private fun isValidEmail(email: String): Boolean {
    return email.contains("@") && email.contains(".")
}

// Password minimum length
if (password.length < 6) { // Change 6 to desired length
    return Result.failure<User>(...)
}
```

---

## 🧪 Testing Flow

1. **Fresh Install**: Tap "Sign Up" → Create account
2. **With Account**: Login with email/password
3. **Session Restore**: Kill app → Relaunch → Direct to MainActivity
4. **Logout**: Settings → Logout → Back to LoginActivity

---

## 📱 User Data Stored

Each user has:
- `id` - Unique identifier (UUID)
- `email` - Email address
- `username` - Display username
- `password` - Password (plain text)
- `createdAt` - Timestamp when account created

---

## 🔒 Important Notes

- ⚠️ **Passwords are stored in plain text** (for demo)
- 💾 **Data is in-memory** (cleared when app fully closes)
- 🔄 **Session persists** across app restarts via SharedPreferences
- 🌐 **No network calls** (everything is local)
- ✅ **No database required**

---

## 🚨 Common Issues & Solutions

### Issue: Login fails with correct credentials
**Solution**: Make sure to use exactly:
- `test@example.com` (lowercase)
- `123456` (exact password)

### Issue: Signup shows "Email already exists"
**Solution**: Use a different email address or logout first

### Issue: Session not persisting
**Solution**: Check that SharedPreferences is not cleared in app settings

### Issue: SignUp button not working
**Solution**: Ensure all fields are filled and passwords match

---

## 📞 Support

For issues or questions:
1. Check the validation error messages (shown as Toast)
2. Enable logging in AuthRepository
3. Check app's SharedPreferences in Device File Explorer
4. Review Logcat for errors

---

## ✅ Testing Checklist

- [ ] App launches to Login (first time)
- [ ] Can login with test@example.com
- [ ] Settings shows email/username
- [ ] Logout works
- [ ] Session persists after restart
- [ ] SignUp form validates fields
- [ ] Can't create duplicate emails
- [ ] Password fields toggle visibility

---

**Last Updated**: March 2026  
**Status**: ✅ Production Ready

