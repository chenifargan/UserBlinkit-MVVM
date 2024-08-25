# User-Side-MVVM Application

This repository contains the user-side of the application, providing users with a modern and responsive interface built using Kotlin. The app integrates various Android libraries and services like Firebase, Room Database, and Stripe, offering a feature-rich experience.

## Features

- **User Authentication**: Users can register and log in using email or phone number authentication via Firebase.
- **Real-Time Data**: Utilizes Firebase Realtime Database for dynamic content updates.
- **Secure Payments**: Stripe integration for secure payment processing.
- **Push Notifications**: Send real-time notifications using Firebase Cloud Messaging.
- **Modern UI/UX**: Built with Jetpack Compose, Material Design, and other advanced UI components.
- **Offline Support**: Local data persistence using Room Database.
- **Image Handling**: Efficient image loading and manipulation using Glide.

## Installation

### Prerequisites

- Android Studio
- Firebase account
- Stripe account

### Steps

1. **Clone the repository**:

2. **Open the project in Android Studio**.

3. **Sync the project with Gradle files**.

4. **Set up Firebase**:
   - Add the `google-services.json` file to your `app/` directory.
   - Configure Firebase Authentication, Realtime Database, Storage, and Cloud Messaging in the Firebase Console.

5. **Set up Stripe**:
   

6. **Build and run the app** on an Android device or emulator.

## Dependencies

### Android Core Libraries

- **Core KTX**: Simplifies Android development with Kotlin extensions.
- **AppCompat**: Provides backward-compatible support for older Android versions.
- **Material Design**: Implements Google’s Material Design components for a modern UI.
- **ConstraintLayout**: Enables complex UI layouts with a flat view hierarchy.

### UI Development with Jetpack Compose

- **Compose BOM**: Manages dependencies for Jetpack Compose.
- **UI Components**: Implements essential UI components.
- **Tooling and Preview**: Provides preview and debugging tools for Compose UI.

### Navigation and Lifecycle

- **Navigation Component**: Handles in-app navigation and backstack management.
- **Lifecycle Components**: Manages UI lifecycles with ViewModel and LiveData.

### Firebase Services

- **Firebase Analytics**: Tracks user behavior and app usage.
- **Firebase Authentication**: Provides secure user authentication.
- **Firebase Realtime Database**: Stores and syncs data in real-time.
- **Firebase Storage**: Handles file storage and retrieval.
- **Firebase Cloud Messaging**: Sends notifications and updates to users.

### Local Database with Room

- **Room Database**: Provides local data storage and management.
- **Annotation Processing**: Generates code for Room.

### Network Operations

- **Retrofit**: Simplifies network requests with REST APIs.
- **Volley**: Handles asynchronous HTTP requests.

### Payment Integration

- **Stripe**: Manages payments through Stripe’s secure APIs.

### UI Enhancements

- **Shimmer Effect**: Adds a shimmer loading effect for placeholders .
- **Image Slideshow**: Implements an image slider.
- **Glide**: Efficiently loads and displays images.

### Testing

- **JUnit**: Unit testing framework.
- **Espresso**: UI testing for Android.

### Dimension Handling

- **SDP & SSP**: Scales dimensions for different screen sizes.

## Usage

1. **Register or log in** to the app using Firebase Authentication.
2. **Browse and interact** with the app’s features, including real-time updates and content.
3. **Make payments** securely using Stripe.
4. **Receive notifications** through Firebase Cloud Messaging.


