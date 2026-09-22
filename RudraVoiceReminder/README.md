# Rudra Voice Reminder

Premium Android voice-reminder assistant.

## Codemagic

`codemagic.yaml` is in the repository root.

Repository root:

```text
app/
gradle/
build.gradle.kts
settings.gradle.kts
gradle.properties
gradlew
gradlew.bat
codemagic.yaml
README.md
```

1. Unzip this project.
2. Upload the CONTENTS to the root of a GitHub repository.
3. In Codemagic select Android.
4. Select the branch containing `codemagic.yaml`.
5. Click **Check for configuration file**.
6. Run **android-debug** first.
7. Download `app-debug.apk` from Artifacts.

Do not upload the ZIP itself into GitHub.

## Features

- Premium dark glass-inspired Compose UI
- Room local database
- Manual reminder scheduling
- Android exact alarm scheduling
- High-priority notification
- Text-to-Speech reminder
- Reboot rescheduling foundation
- Notification and microphone permissions
- Codemagic APK workflows

The release workflow needs Android signing configured in Codemagic for a distributable signed release.
