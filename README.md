# Smart Tourism Kebumen 🌴

A modern, intelligent, and comprehensive Android application designed to digitalize and enhance the tourism experience in Kebumen Regency. Built completely with **Jetpack Compose** and **Kotlin**, this app integrates AI trip planning, real-time weather forecasting, and a complete e-ticketing system with QR code validation.

## 📱 Features

### 🧑‍💼 For Tourists (Users)
*   **Explore Destinations:** Discover tourist attractions across Kebumen with beautiful UI and integrated Google Maps.
*   **🤖 AI Trip Planner:** Automatically generate a personalized itinerary based on your budget, group size, duration, vehicle, and interests. The AI also considers real-time weather forecasts to optimize your route!
*   **🌦️ Real-Time Weather:** View the current weather (temperature, humidity, wind speed) and recommendations directly on the destination cards.
*   **🎟️ Smart E-Ticketing:** Book tickets seamlessly within the app and generate an offline QR Code ticket for entry.
*   **⭐ Reviews & Ratings:** Share your experiences and see what others think about specific destinations.
*   **❤️ Favorites:** Save your favorite spots for quick access, accessible even offline.

### 🛡️ For Administrators
*   **Admin Dashboard:** Monitor ticket sales and validation statistics in real-time.
*   **Manage Destinations:** Full CRUD (Create, Read, Update, Delete) capabilities to manage tourist spots directly from the app.
*   **📷 QR Code Scanner:** Built-in fast barcode scanner using **Google ML Kit** and **CameraX** to scan and validate visitor tickets efficiently at the entrance.

## 🏗️ Architecture & Tech Stack

This project follows the official Android Architecture guidelines, utilizing the **MVVM (Model-View-ViewModel)** architectural pattern along with the **Repository Pattern** to ensure a clean separation of concerns and robust offline support.

### Tech Stack:
*   **[Kotlin](https://kotlinlang.org/):** Primary programming language.
*   **[Jetpack Compose](https://developer.android.com/jetpack/compose):** Modern declarative UI toolkit used for 100% of the screens.
*   **[Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/):** For asynchronous programming and reactive UI states.
*   **[Room Database](https://developer.android.com/training/data-storage/room):** Local SQLite abstraction for offline caching of destinations, weather, and reviews.
*   **[Retrofit](https://square.github.io/retrofit/) & OkHttp:** For handling network API requests.
*   **[Coil](https://coil-kt.github.io/coil/compose/):** Fast and lightweight image loading for Jetpack Compose.
*   **[Google Maps Compose](https://github.com/googlemaps/android-maps-compose):** Interactive mapping for destinations.
*   **[CameraX](https://developer.android.com/training/camerax) & [ML Kit](https://developers.google.com/ml-kit/vision/barcode-scanning):** For high-performance, on-device QR Code scanning.
*   **[ZXing](https://github.com/zxing/zxing):** For generating QR codes for booked tickets.

## 📂 Project Structure

```text
app/src/main/java/com/yuudev/wisatakebumen/
├── component/      # Reusable Jetpack Compose UI components (Cards, Dialogs, Navbars)
├── data/           # Local Data Layer: Room Database, DAOs, and Entities
├── manajer/        # Session and State Managers (Login session, Favorites)
├── model/          # Data classes representing Domain and API responses
├── network/        # Remote Data Layer: Retrofit Client and API Services
├── repository/     # Repositories abstracting local and remote data sources
├── screen/         # Main Jetpack Compose Screens (Home, Admin, Ticket, Planner)
├── ui/theme/       # Jetpack Compose Theme, Colors, and Typography
├── util/           # Helper classes (Location Service, Network Monitoring)
└── viewmodel/      # ViewModels handling business logic and UI states
```

## 🚀 Getting Started

### Prerequisites
*   [Android Studio](https://developer.android.com/studio) (Koala or latest recommended).
*   JDK 17.
*   An Android device or emulator running API level 24 (Android 7.0) or higher.

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/smart-tourism-kebumen.git
    ```
2.  **Open the project:**
    Open Android Studio and select `File > Open`, then navigate to the cloned directory.
3.  **Sync Gradle:**
    Allow Android Studio to download the necessary dependencies and sync the project.
4.  **Run the app:**
    Click the Run button (`Shift + F10`) to deploy the app to your device or emulator.

*(Note: Ensure you grant Camera and Location permissions when prompted to use the QR scanner and Maps features properly).*

## 💡 System Design Note (Backend)
The application is designed to fetch dynamic data from a remote endpoint. Based on the `ApiService`, it relies on a serverless architecture (likely Google Apps Script acting as an intermediary to Google Sheets or an equivalent system) handling actions such as `action=getTripPlan`, `action=validasiTiket`, etc. 

## 🛡️ License

```text
Copyright 2024 Smart Tourism Kebumen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
