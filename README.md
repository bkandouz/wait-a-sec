# Wait a sec…

Android app that puts a short breathing pause between you and the apps that steal your time.

When you open a watched app (Instagram, YouTube, X, …), a full-screen inhale/exhale overlay appears for a few seconds. Finish the breath to continue, or tap **Leave** to go Home.

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

- An `AccessibilityService` listens for foreground window changes
- If the package is in your restricted list, a `TYPE_ACCESSIBILITY_OVERLAY` shows the breath UI
- After the delay, the overlay dismisses and a short cooldown prevents immediate re-prompts
- **Leave** performs Home so you can walk away

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
