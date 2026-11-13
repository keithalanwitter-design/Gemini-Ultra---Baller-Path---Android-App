# Baller Path — Android Prototype

A native Android prototype of **Baller Path**, a basketball development app guided by **Coach Keith**, built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Navigation Compose**, and **Room**.

## How to Run the Demo

1. **Android Studio / Physical Phone**:
   - Export the project ZIP from AI Studio and open it in Android Studio (or build locally with `gradle assembleDebug`).
   - Run the `:app` configuration on an Android emulator or physical Android phone (minSdk 24, targetSdk 36), or install the debug APK directly via Android Studio / `adb install app/build/outputs/apk/debug/app-debug.apk`.
2. **Run Local Verification Tests (Robolectric + Compose UI Tests)**:
   ```bash
   gradle :app:testDebugUnitTest
   ```
   The suite tests the `360dp × 640dp` small-phone layout at both normal and `1.5x` system font scales, optional step timer start/pause/resume and background preservation, unfinished reflection draft recovery across app restarts, **Awaiting Coach** vs. **Sample feedback ready**, back navigation, and scenario switching on **Account**.

## What Is Simulated

- **Fictional Player**: **Maya Brooks (7th grade · Guard)** — clearly labeled across screens.
- **Simulated Family Profile**: **Avery Pine (`Parent / Family`)** on the **Account** screen.
- **Today's State-Driven Primary Action**: Shows one clear action based on where Maya left off (`Start practice`, `Resume practice`, `Finish reflection`, `Open work`, or `Read feedback`).
- **Awaiting Coach & Demo Reveal**: Submitting a reflection saves it locally to Room and shows **Awaiting Coach** in **Work + Feedback**. In debug builds, a **`Demo: Reveal Sample Coach Response`** control reveals Coach Keith's prewritten sample response and next adjustment.
- **Demo Scenario Switcher**: On **Account** (visible when `BuildConfig.DEBUG` is true), testers can jump between scenarios (`Practice ready`, `Practice in progress`, `Reflection draft saved`, `Awaiting coach`, `Sample feedback ready`, `More work ready`) or reset the demo.
- **Excluded from Prototype**: Authentication, family billing, and live server data are intentionally kept outside this prototype.

## Where the Real Data Connection Connects Later

The UI and `BallerPathViewModel` depend only on `BallerPathRepository`:

- **Current Prototype**: `LocalBallerPathRepository` combines fixture sample data (`SampleDataSource`) with Room persistence (`BallerPathDatabase`, `ReflectionDao`, and `PracticeDraftDao`) so active steps, unfinished drafts, submitted reflections, and revealed feedback states survive app restarts.
- **Production Integration Seam**: `BallerPathServiceGateway` documents where the authenticated Baller Path mobile API endpoints connect for player selection, published plan/focus reads, reflection uploads, and coach feedback delivery.
