# Welcome to Tap2Eat! (Contributors & Setup Guide)

---

## Architecture Overview
Tap2Eat is a comprehensive application divided into two main parts within this repository:
1. **Frontend (Android)**: The `app` directory (Kotlin, Jetpack Compose, XML). It integrates multiple services including **Firebase**, **Stripe APIs** for payments, **Google Maps** for location features, and **Gemini AI** for smart assistance.
2. **Backend (Node.js/Express)**: The `Backend` directory, which handles secure **Cloudinary** media management and custom API endpoints.

---

## Complete Step-by-Step Setup Guide

Follow these steps exactly to avoid any crashes or "Missing Configuration" errors.

### Prerequisites
Before you begin, ensure you have the following installed and set up:
- **Android Studio**
- **Git** installed on your system
- A **Firebase Account**
- A **Cloudinary Account**
- A **Google Cloud Console Account** (for Gemini & Maps APIs)
- A **Stripe Account** (for payment processing)

### Step 1: Fork and Clone the Repository
First, fork the repository using the "Fork" button on GitHub, then clone your fork to your local machine.

```bash
# Replace <your_username> with your GitHub username
git clone https://github.com/<your_username>/Tap2Eat.git
cd Tap2Eat
```

Open your Android project on Android Studio.

### Step 2: Firebase Project Setup
Tap2Eat relies heavily on Firebase. You **MUST** set up your own Firebase project to run it locally.

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Click on the **Android Icon** to add an Android app to the project.
3. **Package Name:** Enter `com.example.tap2eat`.  
4. **App Nickname:** `Tap2Eat`.
5. **SHA-1 & SHA-256 Certificates:** 
   - Open the terminal in Android Studio and run:
     ```bash
     ./gradlew signingreport
     ```
   - Copy the SHA-1 and SHA-256 keys for the `debug` variant and add them to Firebase. *(This is mandatory for Google Sign-In and Authentication!)*
6. **Download `google-services.json`:**
   - Download the file and place it in the `app/` directory of your project (select `Project` view in Android Studio to see the proper file structure).

### Step 3: Enable Firebase Services
In your Firebase Console, make sure you enable and setup the following:

- **Authentication:**
    - Go to the `Authentication` section.
    - Enable `Email/Password` and `Google` sign-in methods.
    - Set up your `Support email`.
- **Realtime Database:** 
  - Go to the `Realtime Database` section.
  - Click on `Create Database`.
  - Select a location (e.g., `asia-southeast1` or `us-central1`).
  - Start in **Locked Mode**.
  - Go to the **Rules** tab and paste the exact security rules below to secure user data:

### Step 4: Configure the Android App Keys
Tap2Eat uses several external APIs. You must configure these keys dynamically.

#### 1. Location & Gemini APIs (`local.properties`)
Navigate to the root directory and open `local.properties`. Add your actual API keys:

1. Get API_KEY_LOCATION from [OpenWeatherMap](https://openweathermap.org/) (Reverse Geocoding API)

```properties
# Add these lines to your local.properties file

API_KEY_LOCATION="YOUR_OPENWEATHERMAP_API_KEY_HERE"

# 2. Get this from Google AI Studio for Chatbot functionality
API_KEY_GEMINI="YOUR_GEMINI_API_KEY_HERE"
```

#### 2. Stripe API configuration (`Utils.kt`)
Navigate to `app/src/main/java/com/example/tap2eat/utils/Utils.kt` and replace the placeholder keys with your actual Stripe testing keys:
```kotlin
package com.example.tap2eat.utils

object Utils {
    // Obtain these from your Stripe Dashboard (Developers -> API Keys)
    const val PUBLISHIBLE_KEY="your_stripe_publishable_key_here"
    const val SECRET_KEY="your_stripe_secret_key_here"
}
```

### Step 5: Backend Deployment (Cloudinary)
The backend handles media uploads securely.

1. Open the `Backend` directory in your terminal: `cd Backend`.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Create a `.env` file inside the `Backend` directory:
   ```env
   PORT=8000
   CORS_ORIGIN=*

   # CLOUDINARY
   CLOUDINARY_CLOUD_NAME=your_cloud_name
   CLOUDINARY_API_KEY=your_api_key
   CLOUDINARY_API_SECRET=your_api_secret
   ```
4. **Deploy the backend** on a cloud hosting platform like [Render](https://render.com/). Refer to the [Backend README](Backend/README.md) for detailed deployment steps.

### Step 6: Sync and Run the App
1. Open Android Studio.
2. Select **File > Open** and choose your cloned `Tap2Eat` directory.
3. Let Gradle sync completely.
4. Select a physical device or emulator and click the **Run (▶)** button!

---

## 🤝 How to Contribute Code
Once you have everything set up and running, you're ready to contribute!

1. **Pick an Issue:** Find an open issue or suggest a new feature.
2. **Create a Branch:** Create a branch based on what you are working on.
   - Example: `git checkout -b feature/ui-improvement` or `git checkout -b bugfix/cart-issue`
3. **Make Changes:** Write clean Kotlin code and respect the MVVM architecture and Jetpack Compose standard practices.
4. **Commit:** Use [Conventional Commits](https://www.conventionalcommits.org/).
   - Example: `git commit -m "feat: added skeleton loading for food items"`
5. **Push:** `git push origin your-branch-name`
6. **Pull Request:** Open a PR describing exactly what you fixed or created. Attach screenshots if you made UI changes!

> **Note:** **DO NOT** commit your actual API keys. Always use placeholders if you modify `local.properties` or `Utils.kt`.

Thank you for helping us improve Tap2Eat! We look forward to your PRs.
