# FinIntelligence - AI-Powered Fintech Assistant

FinIntelligence is a modern Android application designed to help users manage their finances intelligently. It combines traditional expense tracking with advanced AI features like predictive analytics, OCR receipt scanning, and voice-assisted interactions.

## 🚀 Features

- **AI Predictions:** Forecasts future expenses based on historical data using machine learning models.
- **OCR Receipt Scanner:** Automatically extract expense details from receipts using Google ML Kit.
- **AI Chat Assistant:** Interactive voice and text-based assistant to query financial status and insights.
- **Real-time Analytics:** Visual representation of spending patterns using interactive charts.
- **Offline Sync:** Robust local-first architecture with Room database and WorkManager for background synchronization.
- **Biometric Security:** Secure access to financial data using Fingerprint/Face ID.
- **Income & Expense Management:** Comprehensive tracking of financial transactions.

## 🛠️ Tech Stack

- **Language:** Java / Kotlin
- **Architecture:** MVVM / Repository Pattern
- **UI:** Material 3 Design
- **Local Database:** Room Persistence Library
- **Networking:** Retrofit & Gson
- **Background Tasks:** WorkManager
- **AI/ML:** Google ML Kit (Text Recognition) & Custom ML API Integration
- **Charts:** MPAndroidChart
- **Security:** Android Biometric API

## 📋 Prerequisites

- Android Studio Koala or newer
- JDK 17
- Android SDK 24+ (Android 7.0+)

## ⚙️ Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/FinIntelligence.git
   ```
2. Open the project in **Android Studio**.
3. Sync the project with Gradle files.
4. Set up your backend API URL in `ApiClient.java`.
5. Run the app on an emulator or a physical device.

## 📂 Project Structure

- `activities/`: Main UI screens (Dashboard, Prediction, OCR, etc.)
- `ml/`: AI/ML logic and analyzers.
- `network/`: API interfaces and networking client.
- `database/`: Room database configuration and DAOs.
- `repository/`: Data management layer.
- `utils/`: Helper classes for currency, sessions, and threads.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
