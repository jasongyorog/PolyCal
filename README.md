This is a colorful, text-based calendar widget for Android.

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="PolyCal widget showing color-coded events from several calendars" width="480">

The color coding is intended to be especially useful for people who must
coordinate multiple calendars.

It is relatively simple, at least by Android project standards, and attempts
to use the standard practices for each part. By default, it has no calendar
permissions, and so it will be in "screenshot mode" (which was also used to
prepare the app widget preview).

## Features

- Per-widget calendar selection, text size, and date/time formats.
- Auto refresh every 15, 30 or 60 minutes (or never). The refresh does not
  wake the device; if it is asleep, the widget refreshes on the next screen-on.
- Optional hidden launcher icon. Settings stay reachable by long-pressing the
  widget and choosing reconfigure (Android 12+). On Android 10+, launchers
  still show an App Info shortcut for apps that request permissions.
- Lock screen placement where the OS supports lock screen widgets.
  Note that events are then readable without unlocking.
- Settings screens use Material 3 Expressive with dynamic (wallpaper) colors
  and dark mode. The widget keeps its text-based look.

## Building

Requires the Android SDK (platform 36) and JDK 17 or 21. Android Studio's
bundled JDK works; newer JDKs are not supported by the Android build tools.

```
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
./gradlew installDebug
```
