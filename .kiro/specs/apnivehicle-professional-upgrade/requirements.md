# Requirements Document: ApniVehicle Professional Upgrade

## Introduction

This document specifies the requirements for upgrading the ApniVehicle Android application from a basic vehicle marketplace prototype to a production-ready, professional-grade platform. The upgrade encompasses comprehensive authentication, vehicle management, search and filtering, favorites system, settings management, and robust data persistence using JSON-based file storage. The application targets the Pakistani vehicle marketplace with localized features including Pakistani cities, phone formats, currency (PKR), and popular vehicle makes.

## Glossary

- **Auth_System**: The authentication and user management subsystem responsible for sign up, login, session management, and profile operations
- **Vehicle_Manager**: The subsystem responsible for creating, editing, deleting, and managing vehicle listings
- **Search_Engine**: The subsystem that handles vehicle search, filtering, sorting, and result presentation
- **Favorites_System**: The subsystem that manages user's favorite vehicle listings
- **File_Handler**: The utility subsystem responsible for JSON serialization/deserialization and file I/O operations
- **Image_Manager**: The subsystem responsible for storing, retrieving, and managing vehicle images in local storage
- **Settings_Manager**: The subsystem that manages user preferences including theme, language, and profile settings
- **Validation_Engine**: The utility subsystem that validates user inputs according to business rules
- **UI_Theme_Manager**: The subsystem that manages Material Design 3 theming and dark/light mode transitions
- **Session_Manager**: The subsystem that manages user session state using SharedPreferences
- **Pakistani_Phone**: A phone number in the format 03XX-XXXXXXX (11 digits starting with '03')
- **PKR_Currency**: Pakistani Rupee currency formatted as "PKR XX,XX,XXX"
- **Vehicle_Category**: One of: Car, Bike, Truck, Bus, Rickshaw, Other
- **Vehicle_Condition**: One of: New, Used, Certified
- **Pakistani_City**: One of the predefined cities: Karachi, Lahore, Islamabad, Rawalpindi, Faisalabad, Multan, Peshawar, Quetta, Sialkot, Gujranwala
- **Password_Strength**: Password meeting criteria: minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 number, 1 special character
- **Valid_Email**: Email address matching standard email format regex pattern
- **Year_Range**: Vehicle manufacturing year between 1980 and current year (2024)
- **Price_Range**: Vehicle price between 0 and 999,999,999 PKR
- **User_Session**: Active authenticated user state persisted in SharedPreferences
- **JSON_Storage**: Application data stored in JSON format in app's internal storage directory
- **Material_Design_3**: Google's Material Design 3 design system with Material Components
- **MVVM_Architecture**: Model-View-ViewModel architectural pattern for separation of concerns

## Requirements

### Requirement 1: User Registration

**User Story:** As a new user, I want to create an account with my personal information, so that I can access the vehicle marketplace and post listings.

#### Acceptance Criteria

1. THE Auth_System SHALL provide a sign-up form with fields for name, email, phone number, password, and confirm password
2. WHEN a user submits the sign-up form, THE Validation_Engine SHALL validate that the name contains 3-50 characters and consists only of letters and spaces
3. WHEN a user submits the sign-up form, THE Validation_Engine SHALL validate that the email matches the Valid_Email format
4. WHEN a user submits the sign-up form, THE Validation_Engine SHALL validate that the phone number is a Pakistani_Phone format (11 digits starting with '03')
5. WHEN a user submits the sign-up form, THE Validation_Engine SHALL validate that the password meets Password_Strength criteria
6. WHEN a user submits the sign-up form, THE Validation_Engine SHALL validate that the confirm password matches the password field
7. WHEN a user submits the sign-up form with a duplicate email, THE Auth_System SHALL reject the registration and display an error message "Email already registered"
8. WHEN a user submits the sign-up form with a duplicate phone number, THE Auth_System SHALL reject the registration and display an error message "Phone number already registered"
9. WHEN all validation passes, THE Auth_System SHALL encrypt the password before storage
10. WHEN registration is successful, THE File_Handler SHALL persist the user data to users.json file
11. WHEN registration is successful, THE Auth_System SHALL create a User_Session and navigate to the main application screen

### Requirement 2: User Authentication

**User Story:** As a registered user, I want to log in with my credentials, so that I can access my account and personalized features.

#### Acceptance Criteria

1. THE Auth_System SHALL provide a login form with fields for email/phone and password
2. WHEN a user submits the login form, THE Validation_Engine SHALL validate that the email/phone field is not empty and matches either Valid_Email or Pakistani_Phone format
3. WHEN a user submits the login form, THE Validation_Engine SHALL validate that the password field is not empty
4. WHEN a user submits valid credentials, THE Auth_System SHALL verify the encrypted password matches the stored password
5. WHEN credentials are invalid, THE Auth_System SHALL display a specific error message indicating whether email/phone or password is incorrect
6. WHEN login is successful, THE Session_Manager SHALL create a User_Session and persist it to SharedPreferences
7. WHERE the "Remember Me" option is selected, THE Session_Manager SHALL persist the session across app restarts
8. WHEN login is successful, THE Auth_System SHALL navigate to the main application screen
9. WHEN a user opens the app with an active User_Session, THE Auth_System SHALL bypass the login screen and navigate directly to the main application

### Requirement 3: Vehicle Listing Creation

**User Story:** As a seller, I want to create a vehicle listing with complete details and images, so that potential buyers can view my vehicle.

#### Acceptance Criteria

1. THE Vehicle_Manager SHALL provide a form with fields for make, model, year, price, mileage, condition, category, city, description, and images
2. WHEN a user submits the vehicle form, THE Validation_Engine SHALL validate that all fields except description are non-empty
3. WHEN a user submits the vehicle form, THE Validation_Engine SHALL validate that the price is a positive number within Price_Range
4. WHEN a user submits the vehicle form, THE Validation_Engine SHALL validate that the year is within Year_Range
5. WHEN a user submits the vehicle form, THE Validation_Engine SHALL validate that the mileage is a non-negative integer
