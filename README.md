# Tap2Eat

Tap2Eat is a food delivery Android application that provides a seamless ordering experience with integrated payment processing, location services, and AI-powered assistance.

## Features

### 🔐 Authentication
- Email/Password authentication with email verification
- Google Sign-In integration using Credentials API
- Password reset functionality
- User profile management with photo upload

### 🍕 Food Ordering
- Browse food items and restaurants
- Add items to cart with quantity management
- Real-time cart updates
- Order placement
- Order history and details

### 💳 Payment Integration
- Stripe payment processing
- Secure payment handling
- Payment intent management

### 📍 Location Services
- Google Maps integration
- Location-based restaurant discovery
- Delivery address selection
- Real-time location tracking

### 🤖 AI Chatbot
- Gemini-powered AI assistant
- Food recommendations
- Order assistance
- Natural language conversation interface

### 🎁 Offers & Discounts
- Special offers and promotions
- Discount code application
- Deal notifications

### 📦 Order Management
- Order history tracking
- Order status updates
- Order details and receipts

## Tech Stack

### Core Technologies
- **Language**: Kotlin
- **Minimum SDK**: 28 (Android 9.0)
- **Target SDK**: 35 (Android 15)
- **Build System**: Gradle with Kotlin DSL

### Architecture & UI
- **UI Framework**: Jetpack Compose + ViewBinding
- **Architecture Components**: ViewModel, LiveData
- **Navigation**: Activity-based navigation with Intent extras

### Backend Services
- **Firebase Authentication**: User authentication
- **Firebase Realtime Database**: Real-time data synchronization
- **Firebase Functions**: Serverless functions for email services
- **Node.js Backend**: Express.js REST API
- **Cloudinary**: Media storage and management

### Payment Services
- **Stripe**: Payment processing

### Location Services
- **Google Maps SDK**: Maps and location services
- **Google Play Services Location**: Location tracking

### AI Integration
- **Google Generative AI**: Gemini model for chatbot

### Networking
- **Retrofit**: REST API client
- **OkHttp**: HTTP client
- **Gson**: JSON serialization

### Image Loading
- **Glide**: Image loading and caching

### Other Libraries
- **Material Design Components**: UI components
- **Splash Screen API**: App launch experience
- **Credentials API**: Google Sign-In integration
- **Coroutines**: Asynchronous programming

## Project Structure

### Android App
```
app/src/main/java/com/example/tap2eat/
├── adapter/          # RecyclerView adapters (Cart, Orders)
├── API/              # API interfaces and utilities
├── models/           # Data models (Customer, Payment, Media)
├── CartFragment.kt   # Shopping cart fragment
├── CheckoutPage.kt   # Checkout activity
├── Details_Page.kt   # User details page
├── FoodPage.kt       # Main food browsing page
├── Gemini.kt         # AI chatbot activity
├── History.kt        # Order history
├── MainActivity.kt   # Authentication and main entry
├── Maps.kt           # Maps and location services
├── Payment.kt        # Payment processing
├── Profile.kt        # User profile
├── SplashScreen.kt   # Splash screen
├── YourOrders.kt     # Order details
└── Utils.kt          # Utility functions
```

### Backend
```
Backend/
├── src/
│   ├── controllers/      # Route handlers
│   │   └── user.conrollers.js
│   ├── middlewares/      # Express middlewares
│   │   └── multer.middleware.js
│   ├── routes/           # API routes
│   │   └── user.routes.js
│   ├── utils/            # Utility functions
│   │   ├── ApiError.js
│   │   ├── ApiRespose.js
│   │   ├── asyncHandler.js
│   │   └── cloudinary.js
│   ├── app.js            # Express app configuration
│   ├── constants.js      # Constants
│   └── index.js          # Server entry point
├── public/               # Static files
└── package.json          # Dependencies
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 8 or higher
- Android SDK with API level 28+
- Node.js (v14 or higher) and npm
- Firebase project with the following services enabled:
  - Authentication
  - Firestore
  - Realtime Database
  - Functions
- Cloudinary account (for media uploads)
- Stripe account (for payments)
- Google Cloud Console project (for Maps and Sign-In)

### Android App Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Tap2Eat
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json` and place it in:
     - `app/google-services.json`
   - Enable the required Firebase services mentioned above

3. **Add SHA Keys:**
   - In the Firebase Console, go to `Project settings` > `General` > `Your apps`.
   - Click on `Add fingerprint` and add your SHA-1 and SHA-256 keys. You can find these keys in Android Studio:
     - Go to the terminal in Android Studio and run:
       ```
       ./gradlew signingReport
       ```
   - If it shows any error, it means that your JDK is not properly set up.
   - Copy the SHA-1 and SHA-256 keys from the report and add them to Firebase.
   - After adding SHA-1 and SHA-256 keys, download the `google-services.json` file provided and place it in the `app` directory of your project.

4. **Configure API Keys**
   - Create a `local.properties` file in the root directory with:
     ```
     API_KEY_LOCATION=<your-google-maps-api-key>
     API_KEY_GEMINI=<your-gemini-api-key>
     ```
   - Add your Google Sign-In Web Client ID in `app/src/main/res/values/strings.xml` as `default_web_client_id`
   - Add your Google Maps API key in `app/src/main/res/values/strings.xml` as `google_maps_key`

5. **Build the project**
   ```
   ./gradlew build
   ```

6. **Run the app**
   - Connect an Android device or start an emulator
   - Run the app from Android Studio or use:
     ```
     ./gradlew installDebug
     ```

### Backend Installation

1. **Navigate to Backend directory**
   ```bash
   cd Backend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**
   Create a `.env` file in the root directory (not in Backend folder) with:
   ```
   CORS_ORIGIN=<your-client-url>
   CLOUDNINARY_CLOUD_NAME=<your-cloudinary-name>
   CLOUDNINARY_API_KEY=<your-cloudinary-key>
   CLOUDNINARY_API_SECRET=<your-cloudinary-secret>
   PORT=<port-number>
   ```

4. **Run the backend server**
   ```bash
   # Development mode (with hot reload)
   npm run dev
   
   # Production mode
   npm start
   ```

## Configuration

### Firebase Setup
1. Enable Email/Password authentication in Firebase Console
2. Configure OAuth consent screen for Google Sign-In
3. Set up Firestore database with appropriate security rules
4. Configure Realtime Database rules
5. Deploy Firebase Functions for email services

### Stripe Setup
1. Create a Stripe account at [Stripe Dashboard](https://dashboard.stripe.com/)
2. Get your publishable and secret keys
3. Configure Stripe in your app (keys should be stored securely)

### Google Maps Setup
1. Enable Google Maps SDK for Android in Google Cloud Console
2. Create API key and restrict it to your app's package name and SHA-1 fingerprint
3. Add the API key to `strings.xml`

### Cloudinary Setup
1. Create a Cloudinary account at [Cloudinary](https://cloudinary.com/)
2. Get your cloud name, API key, and API secret
3. Add them to your `.env` file

### Permissions
The app requires the following permissions:
- `INTERNET`: Network access
- `ACCESS_NETWORK_STATE`: Check network connectivity
- `POST_NOTIFICATIONS`: Push notifications (Android 13+)
- `WRITE_EXTERNAL_STORAGE`: File operations
- `ACCESS_FINE_LOCATION`: Location services
- `ACCESS_COARSE_LOCATION`: Location services

## Building

### Debug Build
```
./gradlew assembleDebug
```

### Release Build
```
./gradlew assembleRelease
```

The APK will be generated in `app/build/outputs/apk/`

## Dependencies

Key dependencies are managed in `gradle/libs.versions.toml`. Major dependencies include:

- AndroidX Core KTX
- Material Design Components
- Firebase BOM (32.2.2)
- Jetpack Compose
- Stripe Android SDK (21.28.1)
- Google Play Services (Maps, Location, Wallet, Auth)
- Retrofit & OkHttp
- Glide
- Google Generative AI (0.8.0)
- Kotlin Coroutines

## Backend API Endpoints

### Media Upload
- **POST** `/api/v1/uploadMedia`
  - **Description**: Upload a media file (image/video)
  - **Request**: Form data with `media` file
  - **Response**: Success/error message with uploaded media details

### Health Check
- **GET** `/checks`
  - **Description**: Server health check
  - **Response**: `{ message: "ok" }`

## Version

- **Version Code**: 1
- **Version Name**: 1.0

## Authors

- **Naman Gulati**
