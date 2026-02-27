# Calendar App - February 30-Day Calendar

A fully functional Android Calendar app built with Jetpack Compose that displays every month as February with exactly 30 days. This is a premium calendar experience with Material Design 3, event management, reminders, and dark mode support.

## Features

- **Custom Calendar Logic**: Every month displays as February with 30 days
- **Material Design 3**: Modern, clean UI following Google's design guidelines
- **Event Management**: Create, edit, and delete events with title, time, description, and reminders
- **Swipe Navigation**: Swipe left/right to navigate between months
- **Today Button**: Quickly jump to the current date
- **Dark Mode Support**: Automatic dark theme based on system settings
- **Event Reminders**: Get notifications for upcoming events
- **Local Storage**: All events stored locally using Room database

## Technical Specifications

- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 26 (Android 8.0)
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModel
- **Database**: Room with coroutines support
- **Language**: Kotlin

## Project Structure

```
CalendarApp/
├── app/
│   ├── src/main/java/com/calendar/app/
│   │   ├── data/                    # Room database entities and DAOs
│   │   │   ├── Event.kt
│   │   │   ├── EventDao.kt
│   │   │   ├── AppDatabase.kt
│   │   │   └── Converters.kt
│   │   ├── notification/            # Notification system
│   │   │   ├── EventReminderReceiver.kt
│   │   │   ├── NotificationHelper.kt
│   │   │   ├── BootReceiver.kt
│   │   │   └── EventReminderService.kt
│   │   ├── ui/                      # UI layer
│   │   │   ├── screens/
│   │   │   │   ├── CalendarScreen.kt
│   │   │   │   └── EventDialog.kt
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt
│   │   │   └── CalendarViewModel.kt
│   │   ├── utils/                   # Custom calendar logic
│   │   │   └── CustomCalendar.kt
│   │   ├── MainActivity.kt
│   │   └── CalendarApplication.kt
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Building the APK

### Prerequisites

1. **Android Studio Hedgehog (2023.1.1) or later**
2. **JDK 17 or later**
3. **Android SDK 34**

### Build Steps

1. **Open the project in Android Studio**:
   ```bash
   # Open Android Studio and select "Open an existing project"
   # Navigate to the CalendarApp folder
   ```

2. **Sync Gradle**:
   - Android Studio will automatically sync the project
   - If not, click `File > Sync Project with Gradle Files`

3. **Build the Release APK**:
   - Go to `Build > Generate Signed Bundle / APK...`
   - Select `APK`
   - Use the existing keystore:
     - Key store path: `app/keystore.jks`
     - Key store password: `calendar123`
     - Key alias: `calendar`
     - Key password: `calendar123`
   - Select `release` build variant
   - Click `Finish`

4. **Or use command line**:
   ```bash
   ./gradlew assembleRelease
   ```

The signed APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

### Alternative: Build Debug APK

For testing purposes, you can build a debug APK:

```bash
./gradlew assembleDebug
```

The debug APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Installation

### Install on Physical Device

1. **Enable Developer Options**:
   - Go to `Settings > About phone`
   - Tap `Build number` 7 times
   - Go back to `Settings > System > Developer options`
   - Enable `USB debugging`

2. **Install via ADB**:
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

3. **Or transfer and install**:
   - Copy the APK to your device
   - Use a file manager to open and install it
   - You may need to enable "Install from unknown sources"

## Usage

1. **Launch the app** - It opens to the current date in the February 30-day calendar
2. **Navigate months** - Swipe left/right or use the arrows in the app bar
3. **Add events** - Tap any date or use the FAB (Floating Action Button)
4. **View events** - Events are shown as dots below the date number
5. **Edit/Delete events** - Tap on an event to edit or delete it
6. **Go to today** - Use the Today button in the bottom navigation

## Permissions

The app requires the following permissions:
- `POST_NOTIFICATIONS` - For event reminders (Android 13+)
- `SCHEDULE_EXACT_ALARM` - For precise reminder timing
- `RECEIVE_BOOT_COMPLETED` - To reschedule reminders after reboot

## Customization

### Changing the Keystore

To use your own signing keystore:

1. Generate a new keystore:
   ```bash
   keytool -genkey -v -keystore mykeystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias myalias
   ```

2. Update `app/build.gradle.kts`:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("mykeystore.jks")
           storePassword = "yourpassword"
           keyAlias = "myalias"
           keyPassword = "yourpassword"
       }
   }
   ```

## Troubleshooting

### Build Errors

1. **Gradle sync failed**:
   - Check your internet connection
   - Try `File > Invalidate Caches / Restart`

2. **Out of memory**:
   - Increase heap size in `gradle.properties`:
     ```properties
     org.gradle.jvmargs=-Xmx4096m
     ```

3. **Kotlin version mismatch**:
   - Ensure Kotlin plugin version matches the one in `build.gradle.kts`

### Runtime Issues

1. **Notifications not working**:
   - Check notification permissions in app settings
   - Ensure battery optimization is disabled for the app

2. **Events not saving**:
   - Check storage permissions
   - Clear app data and try again

## License

This project is provided as-is for educational and development purposes.

## Credits

Built with:
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
