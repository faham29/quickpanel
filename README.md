# Quick Panel overlay (React Native, Android 12+)

1. Create a project: `npx @react-native-community/cli init QuickPanel --package-name com.quickpanel`
   (different package? change `package com.quickpanel` in the .kt files and add `import <yourpackage>.R` to QuickPanelModule.kt)
2. Copy `android/` from this overlay into the project (merges with existing folders) and replace `App.tsx`.
3. In `android/app/build.gradle`: `minSdkVersion 31` (via `android/build.gradle` ext), and add
   `implementation "androidx.appcompat:appcompat:1.6.1"` if not already present.
4. Paste `AndroidManifest.snippet.xml` inside `<application>`, and add ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, ACCESS_FINE_LOCATION, SYSTEM_ALERT_WINDOW `<uses-permission>` lines under `<manifest>`.
5. In `MainApplication.kt`, inside `getPackages()`: `add(QuickPanelPackage())`
6. Run `npx react-native run-android`, tap the buttons to add tiles.

Notes: tiles are native Kotlin on purpose (React Native can't run reliably inside a TileService).
Call volume may be ignored by some OEMs outside an active call; test on real devices.
