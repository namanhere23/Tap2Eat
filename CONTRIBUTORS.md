# Welcome to Tap2Eat! (Contributors & Setup Guide)

---

## Architecture Overview
Tap2Eat is a comprehensive application divided into two main parts within this repository:
1. **Frontend (Android)**: The `app` directory (Kotlin, Jetpack Compose, XML). It integrates multiple services including **Firebase**, **Stripe APIs** for payments, **Google Maps** for location features, and **Gemini AI** for smart assistance.
2. **Backend (Node.js/Express)**: The `Backend` directory, which handles secure **Cloudinary** media management and custom custom endpoints.

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
- A **Stripe Account** (for payment processing and intent generation)

### Step 1: Fork and Clone the Repository
First, fork the repository using the "Fork" button on GitHub, then clone your fork to your local machine.

```bash
# Replace <your_username> with your GitHub username
git clone https://github.com/<your_username>/Tap2Eat.git
cd Tap2Eat
```

### Step 2: Firebase Project Setup
Tap2Eat relies heavily on Firebase. You **MUST** set up your own Firebase project to run it locally.

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Click on the **Android Icon** to add an Android app to the project.
3. **Package Name:** Enter `com.example.tap2eat`.
4. **App Nickname:** Tap2Eat.
5. **SHA-1 & SHA-256 Certificates:** 
   - Open the terminal in Android Studio and run `./gradlew signingreport`.
   - Copy the SHA-1 and SHA-256 keys for the `debug` variant and paste them into Firebase. *(This is mandatory for Google Sign-In and Authentication!)*
6. **Download `google-services.json`:**
   - Download the file and move it into the `app/` directory of your cloned Android project.

### Step 3: Enable Firebase Services
In your Firebase Console, make sure you enable and setup the following:

- **Authentication:** Enable **Email/Password** and **Google** providers.
- **Realtime Database:** 

### Step 4: Configure the Android App Keys
Tap2Eat uses several external APIs that require keys. You must set these up in two places:

**1. Location & Gemini APIs (`local.properties`)**
Navigate to the root directory of the project and open or create the `local.properties` file. Add your actual API keys here:
```properties
# Add these lines to your local.properties file

# 1. Get this from Google Cloud Console (Maps SDK for Android / Location APIs)
API_KEY_LOCATION="YOUR_GOOGLE_MAPS_LOCATION_API_KEY_HERE"

# 2. Get this from Google AI Studio for the Gemini features
API_KEY_GEMINI="YOUR_GEMINI_API_KEY_HERE"
```

**2. Stripe API configuration (`Utils.kt`)**
The checkout system requires Stripe. Visit the [Stripe Developer Dashboard](https://dashboard.stripe.com/test/apikeys) to obtain your Test API keys. Then, navigate to `app/src/main/java/com/example/tap2eat/utils/Utils.kt` and replace the dummy API keys with your actual Stripe testing keys:
```kotlin
object Utils {
    // Obtain these from your Stripe Dashboard (Developers -> API Keys)
    const val PUBLISHIBLE_KEY="your_stripe_publishable_key_here"
    const val SECRET_KEY="your_stripe_secret_key_here"
}
```

### Step 5: Backend Deployment (Cloudinary)
Tap2Eat relies on a Node.js backend to handle Cloudinary media uploading securely.

1. Open the `Backend` directory in your terminal: `cd Backend`.
2. Install dependencies by running:
   ```bash
   npm install
   ```
3. Create a `.env` file inside the `Backend` directory and define your Cloudinary credentials AND port:
   ```env
   PORT=8000
   CORS_ORIGIN=*

   CLOUDINARY_CLOUD_NAME=your_cloud_name
   CLOUDINARY_API_KEY=your_api_key
   CLOUDINARY_API_SECRET=your_api_secret
   ```

4. **Run Locally:**
   ```bash
   npm run dev
   ```
5. **Deploy the backend** on a cloud hosting platform like Render, Heroku, or Vercel. For how to deploy refer [Deploy](Backend\README.md)

### Step 6: Sync and Run the App
1. Open Android Studio.
2. Select **File > Open** and choose your cloned `Tap2Eat` directory.
3. Let Gradle sync completely.
4. Select a physical device or emulator and click the **Run (▶)** button!

---

## 🤝 How to Contribute Code
Once you have everything set up and running, you're ready to contribute!

1. **Sync your Fork:** Ensure your fork is up-to-date with the main repository.
   ```bash
   git fetch upstream
   git merge upstream/main
   ```
2. **Pick an Issue:** Find an open issue or suggest a new feature.
3. **Create a Branch:** Create a branch based on what you are working on.
   - Example: `git checkout -b feature/payment-integration` or `git checkout -b bugfix/map-crash`
4. **Make Changes:** Write clean Kotlin code and respect the architecture and Jetpack Compose standard practices.
5. **Commit:** Use [Conventional Commits](https://www.conventionalcommits.org/).
   - Example: `git commit -m "feat: added Stripe payment UI"` or `git commit -m "fix: resolved crash on restaurant detail screen"`
6. **Push:** Push the changes to your fork: `git push origin your-branch-name`
7. **Pull Request:** Open a PR from your branch on your fork to the original repository. Attach screenshots if you made UI changes!


Thank you for helping us improve Tap2Eat! We look forward to your PRs.
