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
git remote add origin git@github.com:YOUR_USERNAME/YOUR_REPO.git
git push -u origin main
```

Notes:
- You must set the app as the default dialer on a device/emulator to exercise
  the `InCallService` behavior (see `DialerRolePlugin.kt`).
- If you want me to push directly to your GitHub, provide a repository URL
  and a personal access token with repo permissions (or create the remote
  yourself and then tell me to push). I will not request or store credentials
  without explicit consent.
