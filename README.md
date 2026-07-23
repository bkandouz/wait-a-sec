# Wait a sec…

Android app that puts a short breathing pause between you and the apps that steal your time.

When you open a watched app (Instagram, YouTube, X, …), a full-screen breathing overlay appears and stays until you choose:

- **Quit to something productive** → goes Home  
- **Continue — burn your time** → unlocks after a short breath, then lets the app through

## Requirements

- Android Studio Ladybug+ (or JDK 17)
- Android device/emulator **API 26+**
- Enable the app’s **Accessibility** service (required for launch detection)

## Build & run

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration.

## First-run setup

1. Launch **Wait a sec…**
2. Tap **Enable accessibility** → find **Wait a sec…** → turn it on
3. In settings, choose which apps to restrict and set the breath duration (default 3s)
4. Keep **Pause watched apps** enabled

## How it works

- An `AccessibilityService` listens for foreground **app switches** (`TYPE_WINDOW_STATE_CHANGED` only)
- If the package is newly opened and restricted, a `TYPE_ACCESSIBILITY_OVERLAY` shows the breath UI
- After you continue, that app is allowed for the rest of the session until you leave it
- **Quit** performs Home so you can walk away

The service only observes which app is in the foreground. It does not read message content or passwords.

## Project layout

```
app/src/main/java/com/waitasec/app/
  data/          # DataStore settings
  service/       # AccessibilityService + overlay controller
  ui/            # Onboarding, settings, breath overlay
  util/          # Installed apps + accessibility helpers
```

## Notes

- `local.properties` (SDK path) is generated locally and not committed
- Suggested apps (Instagram, TikTok, YouTube, X, Reddit, Facebook, …) appear first when installed; any launcher app can be selected
