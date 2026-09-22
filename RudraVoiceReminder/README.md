# Rudra Voice Reminder

Premium Android voice-reminder assistant.

## Current v1
- Kotlin + Jetpack Compose
- Premium dark glass-inspired UI
- Room local database
- Manual reminder scheduling
- Android exact alarm scheduling
- High-priority notification
- Text-to-Speech reminder
- Reboot rescheduling foundation
- Notification and microphone permissions
- Codemagic APK workflows

## Build

```bash
./gradlew assembleDebug
```

APK:
`app/build/outputs/apk/debug/app-debug.apk`

## Codemagic

1. Push this repository to GitHub.
2. Open https://codemagic.io/
3. Sign in with GitHub.
4. Add the repository.
5. Codemagic detects `codemagic.yaml`.
6. Run `android-debug`.
7. Download the APK from Artifacts.

For Google Play/release signing, configure an Android keystore in Codemagic rather than committing it to Git.

## Next planned upgrade
The v1 architecture intentionally keeps reminder parsing separate so a Gemini/OpenAI parser can be added later for natural-language Marathi/Hindi/English commands.
