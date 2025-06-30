Directory structure:
└── mahirazmain-hemocam-app/
    ├── build.gradle.kts
    ├── gradle.properties
    ├── gradlew
    ├── gradlew.bat
    ├── settings.gradle.kts
    ├── app/
    │   ├── build.gradle.kts
    │   ├── proguard-rules.pro
    │   └── src/
    │       ├── androidTest/
    │       │   └── java/
    │       │       └── com/
    │       │           └── example/
    │       │               └── nail/
    │       │                   └── ExampleInstrumentedTest.kt
    │       ├── main/
    │       │   ├── AndroidManifest.xml
    │       │   ├── assets/
    │       │   │   └── model.tflite
    │       │   ├── java/
    │       │   │   └── com/
    │       │   │       └── example/
    │       │   │           └── nail/
    │       │   │               ├── BoundingBox.kt
    │       │   │               ├── BoundingBoxView.kt
    │       │   │               ├── HandDetectionModel.kt
    │       │   │               ├── HandDetector.kt
    │       │   │               ├── ImageSelectionActivity.kt
    │       │   │               ├── Logger.kt
    │       │   │               ├── MainActivity.kt
    │       │   │               ├── Prediction.kt
    │       │   │               └── ResultsActivity.kt
    │       │   ├── python/
    │       │   │   ├── hand_detection.py
    │       │   │   ├── model.pkl
    │       │   │   ├── model.tflite
    │       │   │   └── run.py
    │       │   └── res/
    │       │       ├── drawable/
    │       │       │   ├── baseline_refresh_24.xml
    │       │       │   ├── ic_launcher_background.xml
    │       │       │   └── ic_launcher_foreground.xml
    │       │       ├── layout/
    │       │       │   ├── activity_image_selection.xml
    │       │       │   ├── activity_main.xml
    │       │       │   └── activity_results.xml
    │       │       ├── mipmap-anydpi-v26/
    │       │       │   ├── ic_launcher.xml
    │       │       │   └── ic_launcher_round.xml
    │       │       ├── mipmap-hdpi/
    │       │       │   ├── ic_launcher.webp
    │       │       │   └── ic_launcher_round.webp
    │       │       ├── mipmap-mdpi/
    │       │       │   ├── ic_launcher.webp
    │       │       │   └── ic_launcher_round.webp
    │       │       ├── mipmap-xhdpi/
    │       │       │   ├── ic_launcher.webp
    │       │       │   └── ic_launcher_round.webp
    │       │       ├── mipmap-xxhdpi/
    │       │       │   ├── ic_launcher.webp
    │       │       │   └── ic_launcher_round.webp
    │       │       ├── mipmap-xxxhdpi/
    │       │       │   ├── ic_launcher.webp
    │       │       │   └── ic_launcher_round.webp
    │       │       ├── values/
    │       │       │   ├── colors.xml
    │       │       │   ├── strings.xml
    │       │       │   └── themes.xml
    │       │       └── xml/
    │       │           ├── backup_rules.xml
    │       │           ├── data_extraction_rules.xml
    │       │           └── provider_paths.xml
    │       └── test/
    │           └── java/
    │               └── com/
    │                   └── example/
    │                       └── nail/
    │                           └── ExampleUnitTest.kt
    └── gradle/
        ├── libs.versions.toml
        └── wrapper/
            └── gradle-wrapper.properties
