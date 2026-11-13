# Baller Path — Google Play Internal Testing Prep (Pilot Release)

This document summarizes the Play Console configuration, Data safety answers, privacy policy placeholder, and release AAB signing steps for the **Baller Path** pilot build (`versionCode = 2`, `versionName = "1.1.0"`). Nothing has been uploaded or submitted to Google Play.

---

## 1. App Identity & Version

- **App Name:** `Baller Path` (`app/src/main/res/values/strings.xml`)
- **Application ID:** `com.ballerpath.app` (`app/build.gradle.kts`)
- **Version Code:** `2`
- **Version Name:** `1.1.0`
- **Target Audience / First-Use Gate:**
  - Before first use, `ParentConsentScreen` states clearly:
    - *"Baller Path is for a parent or guardian of a player in grades 4–8."*
    - *"Coach Keith reviews the submitted work."*
  - Acknowledgment is persisted locally in Room (`parent_consent` table via `MIGRATION_3_4`).

---

## 2. Placeholder Privacy Policy URL (Replace Before Play Upload)

The placeholder URL is declared once in `app/src/main/res/values/strings.xml` and displayed in both `ParentConsentScreen` and the `Account` tab (`Privacy & Parent Permission` card):

- **String resource:** `privacy_policy_url` in `app/src/main/res/values/strings.xml`
- **Current placeholder value:** `https://ballerpath.com/privacy-policy-placeholder`

Replace that URL with your live privacy policy URL before publishing to Google Play Console or running an internal/closed test track targeting families or minors.

---

## 3. Google Play Data Safety Form Answers (Pilot Build)

In this pilot release, **there are no accounts, no sign-in, no networking calls, no analytics SDKs, no ad SDKs, and no third-party tracking.** All reflections, drafts, active practice progress, sample coach responses, and parent consent status are stored strictly locally in the on-device SQLite/Room database (`android:allowBackup="false"` in `AndroidManifest.xml`).

Recommended Play Console **Data safety** selections for this pilot APK/AAB:

1. **Does your app collect or share any of the required user data types?**
   - **No** (for this local-only pilot build — data entered by the player/parent remains exclusively on-device inside Room SQLite and is never transmitted off the device, collected by a backend, or shared with third parties).
2. **Is all of the user data collected by your app encrypted in transit?**
   - N/A (app makes no network transmissions in the pilot build; `INTERNET` permission is not used for external transmission).
3. **Do you provide a way for users to request that their data is deleted / Account creation:**
   - **My app does not allow users to create an account** (local-only pilot).
4. **Independent Security Review / Data Deletion:**
   - Confirm no data is sold or transferred to third parties.

*(Note: When Phase C adds real `ballerpath.com` sign-in and syncing of reflections/coach responses off-device, update both your live Privacy Policy and Play Console Data safety form before shipping Phase C.)*

---

## 4. Building a Signed Release AAB via Environment Variables

`app/build.gradle.kts` reads upload keystore credentials from environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) with key alias `upload`:

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
