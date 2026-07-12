# Photo Caller — Default Dialer App (Ionic Angular + Capacitor + Native Kotlin)

## What this scaffold contains

This is **not** a full Gradle project (that's thousands of generated boilerplate
lines you get for free from Android Studio / `ionic capacitor add android`).
Instead this gives you the *hand-written parts that actually matter*:

```
photo-caller-app/
├── src/app/
│   ├── services/
│   │   ├── contacts.service.ts       # reads device contacts + photos
│   │   └── dialer-role.service.ts    # talks to the native plugin below
│   ├── pages/contacts/               # photo-grid contact list screen
│   └── pages/onboarding/             # "set as default phone app" flow
└── android-native/
    └── app/src/main/
        ├── AndroidManifest_additions.xml   # merge into your real manifest
        ├── java/.../DialerRolePlugin.kt    # Capacitor <-> Android bridge
        ├── java/.../PhotoCallInCallService.kt  # the actual in-call service
        ├── java/.../IncomingCallActivity.kt    # full-screen photo call UI
        └── res/layout/activity_incoming_call.xml
```

## How to actually build this (step by step)

1. **Scaffold the real project first**, don't start from this folder:
   ```bash
   npm install -g @ionic/cli
   ionic start photo-caller blank --type=angular --capacitor
   cd photo-caller
   ionic capacitor add android
   npm install @capacitor-community/contacts
   ```

2. **Copy the `src/app/*` files** from this scaffold into your generated
   project's `src/app/`, adjusting imports/module registration to match
   whatever Ionic CLI version you're on (standalone components vs NgModules
   depends on your Angular version).

3. **Copy the native files** into
   `android/app/src/main/java/com/yourorg/photocaller/` — rename the package
   to match your real `applicationId` in `android/app/build.gradle`, and
   update the `package` line at the top of each `.kt` file to match.

4. **Merge `AndroidManifest_additions.xml`** into
   `android/app/src/main/AndroidManifest.xml` — don't overwrite, merge the
   `<service>`, `<activity>`, and `<uses-permission>` entries in.

5. **Register the Capacitor plugin** — in your `MainActivity.java/kt`
   (Capacitor generates this), add:
   ```kotlin
   registerPlugin(DialerRolePlugin::class.java)
   ```

6. Build with Android Studio (`npx cap open android`) rather than pure CLI —
   you'll want the Logcat/debugger for `InCallService` issues, which are
   notoriously hard to debug blind.

## Why InCallService instead of an overlay

`InCallService` is the API Android gives to apps that hold the
`ROLE_DIALER` role — it's the *only* supported, reliable way to draw your
own full-screen UI for an incoming/active call. Overlay-based approaches
(`SYSTEM_ALERT_WINDOW` + `TelephonyCallback`) get killed by aggressive OEM
battery managers (Xiaomi, Oppo, Vivo, Samsung) and are blocked from
auto-launching on Android 10+. For a user who can't self-troubleshoot,
that fragility isn't acceptable — default-dialer is the right call.

## Testing default-dialer behavior

- You cannot fully test `InCallService` on the Android Emulator's default
  telephony stack for real cellular calls — use a **real device** with a
  SIM, or emulator + `adb emu gsm call <number>` for simulated calls.
- The role prompt (`RoleManager.ROLE_DIALER`) only appears on Android 10+;
  on 9 and below you use the older `TelecomManager.ACTION_CHANGE_DEFAULT_DIALER` intent — worth handling both if you want to support older devices, though I'd suggest just targeting Android 10+ (minSdkVersion 29) given how few users are on older versions now.

## Known gotchas to plan for

- Once your app is default dialer, **you become responsible for actually
  placing/answering calls correctly** — if `PhotoCallInCallService` has a
  bug, the user can't make or receive calls at all. Test thoroughly before
  distributing to your target (non-technical) users.
- Contact photo lookup must match by **normalized phone number**
  (`PhoneNumberUtils.compare()`), not string equality — raw numbers from
  `Call.Details` often differ in formatting from what's stored in Contacts.
- Some OEMs (Samsung especially) have their own dialer deeply tied to
  system UI; setting a third-party default dialer works but some
  users may see a Samsung-specific confirmation dialog first.
