# Baller Path — Google Play Internal Testing Prep (Pilot Release)

This document summarizes the Play Console configuration, Data safety answers, privacy policy URL, and release AAB signing steps for the **Baller Path** pilot build (`versionCode = 2`, `versionName = "1.1.0"`). Nothing has been uploaded or submitted to Google Play.

---

## 1. App Identity & First-Use Parent/Guardian Consent

- **App Name:** `Baller Path` (`app/src/main/res/values/strings.xml`)
- **Application ID:** `com.ballerpath.app` (`app/build.gradle.kts`)
- **Version Code:** `2`
- **Version Name:** `1.1.0`
- **Target Audience / First-Use Gate (`ParentConsentScreen`):**
  - Shown before first use and persisted locally in Room (`parent_consent` table via `MIGRATION_3_4`, keeping all reflections, drafts, and coach feedback intact).
  - Clearly states:
    - *"Baller Path is for a parent or guardian of a player in grades 4–8."*
    - *"Coach Keith reviews the work submitted in this app to build your player's weekly plan and coaching notes."*
    - *"The microphone is optional for recording a voice note or using on-device voice-to-text for reflections, and all voice notes, transcripts, drafts, and reflections stay on this device."*
  - Before the system `RECORD_AUDIO` permission prompt is ever shown, the app also displays an in-app explanation stating that the microphone is optional, used only for the player's reflection or a voice note, and recordings stay on this device.

---

## 2. Privacy Policy Link (`https://ballerpath.com/privacy`) & Build Gating

The privacy policy URL is declared in `app/src/main/res/values/strings.xml` and wired as a clickable external browser link (`Intent.ACTION_VIEW`) on both `ParentConsentScreen` and the `Account` tab (`Privacy & Parent Permission` card):

- **String resource:** `privacy_policy_url` in `app/src/main/res/values/strings.xml`
- **URL:** `https://ballerpath.com/privacy`
- **Release vs. Debug Behavior:**
  - Fictional player badges (`Fictional Player · Sample Data` / `Fictional Player`) and `"Simulated locally for prototype demo"` on sample coach cards remain visible in both debug and release builds.
  - In release builds, `Today` displays `"Coach responded"` (not `"Sample feedback ready"`) when a check-in has been answered.
  - Coach feedback authoring (`CoachFeedbackStudioSection`), `"Demo: Reveal Sample Coach Response"`, `"More work ready"`, and the `Demo Scenario Switcher` are strictly gated behind `BuildConfig.DEBUG` so players or parents in release builds cannot type, dictate, or publish anything labeled as Coach Keith.

---

## 3. Google Play Data Safety Form Answers (Pilot Build)

In this pilot release:
- **No accounts, no sign-in, no networking calls, no `INTERNET` permission, no analytics SDKs, no ad SDKs, and no third-party tracking.**
- **Microphone (`android.permission.RECORD_AUDIO`):** Used strictly for **optional on-device voice notes** and **strictly on-device speech recognition** (`SpeechRecognizer.createOnDeviceSpeechRecognizer`, never falling back to a cloud recognizer). Voice recordings and transcripts stay on the device, are never uploaded or shared, and if on-device speech recognition is unavailable on a phone, the player simply types their reflection instead.
- **Local Storage:** All reflections, drafts, active practice progress, voice notes, coach responses, and parent consent status stay strictly on-device (`android:allowBackup="false"` in `AndroidManifest.xml`).

Recommended Play Console **Data safety** selections for this pilot APK/AAB:

1. **Does your app collect or share any of the required user data types?**
   - **No** (all text reflections, drafts, and optional microphone voice notes/transcripts are processed and stored exclusively on the user's device and never leave the device or get shared).
2. **Permissions Explanation (Microphone / `RECORD_AUDIO`):**
   - Optional microphone access is covered by both the first-use Parent/Guardian Consent screen and an in-app pre-permission notice. Audio recordings and on-device transcripts stay on the device and are never transmitted off-device.
3. **Is all of the user data collected by your app encrypted in transit?**
   - N/A (app has no `INTERNET` permission and makes no network transmissions).
4. **Do you provide a way for users to request that their data is deleted / Account creation:**
   - **My app does not allow users to create an account** (local-only pilot; users can clear app data or delete voice notes directly on the device).
5. **Independent Security Review / Data Sale:**
   - Confirm no data is sold or transferred to third parties.

*(Note: When Phase C adds real `ballerpath.com` sign-in and syncing off-device, update both your live Privacy Policy and Play Console Data safety form before shipping Phase C.)*

---

## 4. Building a Signed Release AAB via Environment Variables

`app/build.gradle.kts` reads upload keystore credentials from environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) with key alias `upload` (`*.jks` is ignored in `.gitignore` and never committed):

```bash
export KEYSTORE_PATH=/absolute/path/to/your-upload-key.jks
export STORE_PASSWORD="your-keystore-password"
export KEY_PASSWORD="your-key-password"

./gradlew :app:bundleRelease
```

Output signed Android App Bundle path:
`app/build/outputs/bundle/release/app-release.aab`

---

## 5. Making `gradlew` Executable in Git

If your Git checkout shows `gradlew` without executable permissions, run this once in your local repository root and commit/push:

```bash
git update-index --chmod=+x gradlew
git commit -m "Ensure gradlew is executable"
git push
```
