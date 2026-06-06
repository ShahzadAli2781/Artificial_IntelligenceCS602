# Installation Guide - Fintech AI Assistant

## System Requirements
- **OS**: Windows 10/11, macOS, or Linux.
- **RAM**: Minimum 8GB (16GB recommended).
- **Disk Space**: ~5GB for Android Studio and SDKs.

## Development Environment
- **Android Studio**: Ladybug (or latest version).
- **Android SDK**: API Level 34 (Target), API Level 24 (Minimum).
- **Java**: JDK 17.
- **Python**: 3.8+ (for Backend).

## Build & Setup Steps

### 1. Android Application
1. Clone the repository.
2. Open **Android Studio**.
3. Select **Open** and navigate to the `Android_App` directory.
4. Wait for Gradle to sync.
5. Connect an Android device or start an emulator.
6. Click **Run 'app'**.

### 2. Backend Service
1. Navigate to the `Backend/Source_Code` folder.
2. Create a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
3. Install dependencies:
   ```bash
   pip install flask flask-cors pandas scikit-learn
   ```
4. Update the `BASE_URL` in `Android_App/app/src/main/java/com/example/fintechaiassistantapp/network/ApiClient.java` to match your local IP address.
5. Start the server:
   ```bash
   python app.py
   ```

## Troubleshooting
- **Gradle Sync Issues**: Ensure you have a stable internet connection and the correct JDK version (Java 17).
- **ADB Connection**: If the device isn't detected, check if USB Debugging is enabled in Developer Options.
- **API Connection Error**: Verify that the Android device and the Backend server are on the same network and the IP address in `ApiClient.java` is correct.
- **Python Missing Modules**: Run `pip install -r requirements.txt` again to ensure all packages are installed.
