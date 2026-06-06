# Technical Documentation - Fintech AI Assistant

## Architecture Overview
The Fintech AI Assistant is built using a modern decoupled architecture. The Android application serves as the primary user interface, while a Python-based backend handles computationally intensive AI and ML tasks.

### Core Components
1. **Android App (Java)**: Handles data entry, OCR scanning, voice recognition, and local data persistence.
2. **Local Database (Room)**: Provides offline capability and fast data access.
3. **Remote API (Retrofit)**: Synchronizes data with the backend and fetches AI insights.
4. **Backend (Python)**: Processes data, runs prediction models, and provides NLP parsing.

## Module Descriptions

### Android Modules
- **`activities` & `fragments`**: UI components for Dashboard, History, Trends, etc.
- **`repository`**: Acts as a single source of truth for data, deciding between local DB or Network.
- **`database`**: Defines the Room database, DAOs, and Entities.
- **`network`**: Retrofit interfaces and API clients.
- **`ml`**: Integration with Google ML Kit for Text Recognition (OCR).
- **`voice`**: Logic for voice command processing.
- **`utils`**: Helper classes for session management, thread handling, and currency formatting.

### Backend Modules
- **`app.py`**: Main API entry point.
- **`predictor.py`**: spending prediction algorithms using Scikit-learn.
- **`nlp_parser.py`**: Natural Language Processing for voice/text commands.
- **`ai_assistant.py`**: Core logic for generating financial insights.

## Folder Structure Explanation
- `/Android_App`: The root of the Gradle project.
- `/Backend/Source_Code`: Python scripts and models.
- `/Documentation`: Markdown files for guides and manuals.
- `/Screenshots`: Visual representation of the app's UI.

## Database Design
The application uses Room (SQLite) with the following main entities:
- **IncomeEntity**: Stores income records (amount, source, date).
- **ExpenseEntity**: Stores expense records (amount, category, date, payment method).
- **AiInsightEntity**: Caches AI-generated tips and predictions.

## Dependency Explanation
- **Room**: For local persistence.
- **Retrofit**: For RESTful API communication.
- **ML Kit**: For high-performance OCR on-device.
- **MPAndroidChart**: For rendering spending trends and pie charts.
- **WorkManager**: For scheduling periodic data synchronization.

## Security Considerations
- **Biometric Authentication**: Utilizes the Android Biometric library for secure login.
- **Session Management**: Securely handles user sessions using Encrypted SharedPreferences (recommended improvement).
- **Data Privacy**: Local data is stored within the app's private directory.
