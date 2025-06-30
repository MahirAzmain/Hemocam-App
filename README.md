# Hemocam App

Hemocam is a cutting-edge Android application designed to detect hemoglobin levels through image processing. This project integrates machine learning models with user-friendly Android interfaces to deliver an innovative solution for healthcare.

---

## Directory Structure

```plaintext
mahirazmain-hemocam-app/
├── build.gradle.kts         # Top-level Gradle build configuration
├── gradle.properties        # Global Gradle properties
├── gradlew                  # Gradle wrapper script (Linux/Mac)
├── gradlew.bat              # Gradle wrapper script (Windows)
├── settings.gradle.kts      # Gradle project settings
├── app/                     # Main application directory
│   ├── build.gradle.kts     # App-level Gradle build configuration
│   ├── proguard-rules.pro   # ProGuard rules for code optimization
│   └── src/                 # Source code directory
│       ├── androidTest/     # Instrumented tests
│       │   └── java/
│       │       └── com.example.nail
│       │           └── ExampleInstrumentedTest.kt
│       ├── main/            # Main application source code
│       │   ├── AndroidManifest.xml  # App configuration
│       │   ├── assets/      # Assets like TensorFlow Lite models
│       │   │   └── model.tflite
│       │   ├── java/        # Application logic
│       │   │   └── com.example.nail
│       │   │       ├── BoundingBox.kt         # Data class for bounding boxes
│       │   │       ├── BoundingBoxView.kt     # UI component for bounding boxes
│       │   │       ├── HandDetectionModel.kt  # Logic for model handling
│       │   │       ├── HandDetector.kt        # Hand detection implementation
│       │   │       ├── ImageSelectionActivity.kt  # UI for selecting images
│       │   │       ├── Logger.kt              # Logging utility
│       │   │       ├── MainActivity.kt        # Main entry point of the app
│       │   │       ├── Prediction.kt          # Data class for predictions
│       │   │       └── ResultsActivity.kt     # UI for displaying results
│       │   ├── python/     # Python scripts for preprocessing
│       │   │   ├── hand_detection.py
│       │   │   ├── model.pkl
│       │   │   ├── model.tflite
│       │   │   └── run.py
│       │   └── res/         # Resource files (layouts, images, etc.)
│       │       ├── drawable/         # Icons and background images
│       │       ├── layout/           # UI layouts
│       │       ├── mipmap-anydpi-v26/
│       │       ├── mipmap-hdpi/      # High-resolution assets
│       │       ├── mipmap-mdpi/      # Medium-resolution assets
│       │       ├── mipmap-xhdpi/     # Extra high-resolution assets
│       │       ├── mipmap-xxhdpi/    # Extra-extra high-resolution assets
│       │       ├── mipmap-xxxhdpi/   # Extra-extra-extra high-resolution assets
│       │       ├── values/           # Strings, colors, and themes
│       │       └── xml/              # Backup and extraction rules
│       └── test/         # Unit tests
│           └── java/
│               └── com.example.nail
│                   └── ExampleUnitTest.kt
└── gradle/                # Gradle configuration
    ├── libs.versions.toml
    └── wrapper/
        └── gradle-wrapper.properties
