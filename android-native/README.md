# PhotoCaller Android module

This folder contains a minimal Android Gradle project that hosts the native
in-call UI used by the PhotoCaller app. It includes the `InCallService`, a
full-screen `IncomingCallActivity`, and resources required to build.

Quick build (requires Java 17 and Android SDK / Gradle):

1. From this folder run Gradle (if you have the wrapper, use `./gradlew`):

```bash
cd android-native
gradle wrapper # optional: create wrapper if you don't have one
./gradlew assembleDebug
```

2. Install the APK or merge this module into your existing Capacitor/Android
   project and then build from Android Studio.

To push to your GitHub repository:

```bash
cd /path/to/photo-caller-app
git init
git add .
git commit -m "Add Android native in-call UI module"
git remote add origin https://github.com/mukesh271/photo-caller-app.git
git branch -M main
git push -u origin main
```

This repository now includes a GitHub Actions workflow that builds the APK on
push to `main` and uploads it as a workflow artifact:

- `.github/workflows/android-build.yml`

Notes:
- You must set the app as the default dialer on a device/emulator to exercise
  the `InCallService` behavior (see `DialerRolePlugin.kt`).
- The workflow uses Java 17 and Gradle 8.1.1 for the `android-native` module.
  If you need a different build variant, I can update the workflow.
