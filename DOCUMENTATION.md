# ApniVehicle - Complete Project Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Architecture](#architecture)
4. [UI Components](#ui-components)
5. [Data Models](#data-models)
6. [Repositories](#repositories)
7. [Utilities](#utilities)
8. [Navigation Flow](#navigation-flow)

---

## Project Overview

**ApniVehicle** is a comprehensive Android vehicle marketplace application built for Pakistan. It enables users to buy, sell, compare, and manage vehicle listings with advanced features like seller verification, multi-criteria search, and vehicle comparison.

### Tech Stack
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI**: View Binding, Material Design 3
- **Data Persistence**: JSON files, Encrypted SharedPreferences
- **Image Loading**: Glide
- **Async Operations**: Kotlin Coroutines with LiveData

### Key Dependencies
```kotlin
- androidx.core:core-ktx
- androidx.appcompat
- com.google.android.material:material
- androidx.fragment:fragment-ktx
- androidx.recyclerview
- com.github.bumptech.glide:glide
- com.google.code.gson:gson
- androidx.security:security-crypto
```

---

## Features

### 1. Authentication & User Management