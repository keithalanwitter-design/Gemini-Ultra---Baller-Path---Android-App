# Baller Path Android App: Master Brief for Google AI Studio
_Nov 12, 2026. This brief replaces Round 1. It combines the Baller Path GM review with ChatGPT's audit of website release 198._

## What Baller Path is
Baller Path is coach-led virtual basketball development for players in grades 4–8, at ballerpath.com.
- Coach Keith personally builds each player's plan, assigns drills, and gives feedback.
- The first users are the **Founding 10**: hand-picked families paying $59/mo, locked for 12 months.
- The website is the source of truth. It owns accounts, families, players, assignments, feedback, scheduling, messages, and billing.
- The Android app is the player's daily tool: what to do today, how to do it, and Coach Keith's response.

**Voice:** a real coach in your corner. The product is never described as AI, an algorithm, or "smart." Don't use sparkle or AI-style icons.

## What's already built (keep it)
- Native Android, written in Kotlin with Jetpack Compose.
- Four tabs: **Today, Plan, Work + Feedback, Account**.
- Today shows one clear priority.
- The 8-minute drill flow with an optional timer, ending in a written reflection.
- Saved drafts.
- An "Awaiting Coach" state after the player submits.
- "Try the next rep" notes under Coach Keith's feedback.
- Local data is in Room/SQLite. Sample player: Maya Brooks, Guard, 7th grade.

## Rules for every change
1. **Names:** never use "Jordan" anywhere, including the parent "Jordan Brooks." Sample parent and player names may only be Avery Pine, Riley Moss, Sam Quill, or the existing Maya Brooks.
2. **No new backend code in this phase.** No login, no network calls, no payments. Login comes in Phase C, through the website's back end.
3. **Images:** don't generate images of people. Coach Keith uses a real photo that Keith supplies.
4. **Data:** never lose a player's reflection or Coach Keith's feedback. Resubmitting creates a new entry and keeps the history.
5. **Database:** every change ships with a migration that keeps existing reflections, feedback, and drafts.
6. **Wrap-up:** finish each phase by listing the files you changed and confirming its "Done when" checks.

---

## Phase A: Cleanup and identity
- Replace every "Jordan" with Avery Pine. Check code, README, BUILD_REFERENCE, and tests, starting with `SampleDataSource.kt`.
- Hide the demo labels from release builds: "Fictional Player," "Sample Data," "Sample Seed," "Simulated locally," and "Sample Coach Response."
  - The demo scenario switcher, including "More work ready," shows only when `BuildConfig.DEBUG` is on.
- Coach Keith's avatar becomes `res/drawable/coach_keith.jpg`, a placeholder for now, shown as a circular crop.
- Replace the computer-generated people in the banner images with court, ball, or text-only art.
- Replace the sparkle icon on "Next Adjustment" with an arrow or target icon.
- Brand colors are navy `#08263E` and action orange `#B64908`. The brighter orange appears only inside the logo. There's no Storm branding in the app.
- Change the application ID from `com.aistudio.ballerpath.bpktqh` to **`com.ballerpath.app`**.
  - App name: "Baller Path."
  - Make a new adaptive icon: the logo on navy.
- Add the Gradle wrapper (`gradlew`, `gradlew.bat`, and the `gradle/wrapper/` files).
- Turn off Android backup for player data (`allowBackup="false"` or backup rules).

**Done when:** `./gradlew assembleDebug` builds and the tests pass. A repo search finds no "Jordan," and the demo labels appear only in debug code. The package is `com.ballerpath.app`.

## Phase B: A real coaching week (the main build)

### B1. Today follows the right assignment
Right now, any earlier submission counts as today's, and practice always loads one fixed sample drill. Change it so:
- Each assignment has its own draft, reflection, status, and feedback.
- Today picks what to show in this order:
  1. An assignment with a draft in progress: **Resume**.
  2. An assignment that's ready and not started: **Start**.
  3. Everything submitted: "Saved. Coach Keith will review it." Show the next ready assignment, if there is one.
  4. Nothing assigned: the setup state that fits (see B2).
- Practice opens the drill for the assignment the player selected, not a fixed sample.

### B2. Setup states, matching the website
- **Setup in progress:** the family is waiting for activation. Show what happens next: "Coach Keith is reviewing [Player]'s info. You'll get access within 2 business days."
- **Plan being prepared:** the player is active, but there's no personal focus yet. If general work has been published, the player can start it now.
- **Work ready:** the normal Today screen.

### B3. Work + Feedback is easy to scan
- Three views: **To do**, **Awaiting coach**, and **Coach responded**.
- Each assignment card keeps these together:
  - the drill name
  - the player's reflection
  - Coach Keith's response
  - its **Try the next rep** note
- Feedback history is kept per assignment, never overwritten.

### B4. The two-assignment test, written as an instrumented test
1. Maya has assignment 1 awaiting review and assignment 2 ready.
2. Today offers to start assignment 2. Start it, write a draft, and leave.
3. Restart the app. Today reopens assignment 2 with the draft intact.
4. Submit assignment 2. Assignment 1's reflection is unchanged.
5. Reveal feedback for assignment 1. Its feedback and "Try the next rep" note appear only under assignment 1.
6. The database migration from the old schema keeps the original reflection, feedback, and draft.

**Done when:** all six steps pass as an automated test and the build succeeds.

## Phase C (later): connect to ballerpath.com. Don't start yet.
These steps come after the website adds its mobile access:
- Google sign-in, with a parent-owned account.
- **Read-only** Today and Plan from the website, using the same rules as the site:
  - Pending families can't see unpublished work.
  - One family can never see another family's player.
- Submissions from the app wait until the website stops clearing earlier coach feedback when a reflection is resubmitted.
- `/join?invite=` links open the app.
- Scheduling, messages, and billing stay on the website for the first Android pilot.
- Pilot release: Google Play internal testing with 3–5 Founding families. The app needs parent consent before any public release, because the players are under 13.
