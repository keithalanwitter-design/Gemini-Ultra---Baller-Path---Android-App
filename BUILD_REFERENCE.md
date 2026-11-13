# Baller Path — Android Prototype Build Reference

**Reference Point Date:** 2026-11-12  
**Fictional Player:** Maya Brooks (`demo-maya`, #14 · Guard · 7th grade)  
**Coach:** Coach Keith  
**Simulated Family Account:** Avery Pine (`Parent / Family`)  
**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room (`version = 2`), Coroutines / `StateFlow`

---

## 1. What We Are Building (Product Loop)

Baller Path is a local, zero-auth Android prototype centered on one core player loop on a phone:

1. **Today**: Answers *"What should I work on today?"* with **one primary action** tailored to where Maya left off.
2. **Pass, Look, Decide (8-minute guided practice)**:
   - **Step 1 — Pass (2 min)**
   - **Step 2 — Look (3 min)**
   - **Step 3 — Decide (3 min)**
   - **Step 4 — Post-Drill Reflection**
3. **Work + Feedback**: Keeps unfinished drafts separate from submitted reflections, displays **Awaiting Coach** immediately after submission, and provides a clearly labeled demo button (**Demo: Reveal Sample Coach Response**) to preview Coach Keith's sample response and next adjustment.
4. **Plan**: Displays Coach Keith's current development focus, *why it matters*, success standard, and Week 2 session list.
5. **Account**: Displays the simulated family profile, prototype scope boundaries, **Demo Scenario Switcher** (all 5 workflow states), and **Reset Sample Demo Data**.

Authentication, family billing, and live server data remain intentionally outside this prototype.

---

## 2. State Machine & Where-You-Left-Off Logic

`BallerPathUiState.currentScenario` (`BallerPathViewModel.kt`) derives the active `WorkflowScenarioId` from Room + in-memory UI state in the following priority order:

| Scenario ID (`WorkflowScenarioId`) | Display Chip | Today Primary Action | Today Destination | Status Detail Behavior |
|---|---|---|---|---|
| `DRAFT` | `Reflection draft saved` | **Finish reflection** | Opens Practice at Step 4 (Reflection) with saved draft | Shows draft-saved notice |
| `IN_PROGRESS` | `Practice in progress · <Step>` | **Resume practice** | Opens Practice at saved step + paused timer | Shows which step Maya left off on |
| `AWAITING_COACH` | `Awaiting coach` | **Open work** | Navigates to **Work + Feedback** | Acknowledges local save & pending review; assigns no extra drill |
| `FEEDBACK_READY` | `Sample feedback ready` | **Read feedback** | Navigates to **Work + Feedback** | Announces Coach Keith's sample response & next adjustment |
| `READY` *(default)* | `Practice ready` | **Start practice** | Opens Practice at Step 1 (`Pass`) | Clean initial state |

---

## 3. Changes Made in This Build-Out

### A. Data Models & Fictional Fixture Alignment
- **`BallerPathModels.kt`**:
  - Added `DevelopmentPlan` (`focus`, `why`, `successStandard`, `reviewLabel`).
  - Added stable `id` (`"pass"`, `"look"`, `"decide"`) to `PracticeStep`.
  - Added `sampleCoachLabel` and `isCoachResponseRevealed` to `PracticeReflection`.
  - Added `WorkflowScenarioId` enum (`READY`, `IN_PROGRESS`, `DRAFT`, `AWAITING_COACH`, `FEEDBACK_READY`) and `PracticeWorkflowState` domain model.
- **`SampleDataSource.kt`**:
  - Aligned fictional profile (`demo-maya`, Maya Brooks, 7th grade, Guard, Coach Keith), `developmentPlan`, `demo-pass-look-decide` steps (`pass` / `look` / `decide` + Coach Keith cues), `reflectionPrompt`, and `FIXTURE_*` draft / submission / coach response constants.

### B. Local Room Persistence (`version = 2`)
- **`PracticeDraftStateEntity.kt` & `PracticeDraftDao.kt`**:
  - Added single-row table `practice_workflow_state` to persist `hasActiveSession`, `currentStepId`, `isReflectionStage`, `remainingSeconds`, `hasStartedTimer`, and unfinished `reflectionDraft` across cold restarts.
- **`ReflectionEntity.kt` & `ReflectionDao.kt`**:
  - Added `sampleCoachLabel`, `isCoachResponseRevealed`, and `setCoachResponseRevealed(id, revealed)`.
- **`BallerPathDatabase.kt`**:
  - Bumped schema to `version = 2` with `fallbackToDestructiveMigration(dropAllTables = true)` and exposed `practiceDraftDao()`.
- **`BallerPathRepository.kt` & `LocalBallerPathRepository.kt`**:
  - Added `observeDevelopmentPlan()`, `observeWorkflowState()`, `saveWorkflowState()`, `revealSampleCoachResponse()`, and `applyDemoScenario(scenario)`.
  - Updated `submitReflection()` to save new submissions with `isCoachResponseRevealed = false` (**Awaiting Coach**) and reset `practice_workflow_state` so drafts stay distinct from submitted work.

### C. ViewModel, Timer Behavior & App Lifecycle
- **`BallerPathViewModel.kt`**:
  - Hydrates active step, remaining seconds, timer-started flag, and reflection draft from Room + `SavedStateHandle` on startup.
  - Added `startOrResumePracticeSession()`, `revealSampleCoachResponse()`, `applyDemoScenario()`, and `onAppBackgrounded()` (pauses running timer and flushes state to Room on `Lifecycle.Event.ON_STOP`).
  - Kept the step timer strictly optional: advancing steps works without starting it, and reaching `00:00` never advances steps or submits a reflection.
  - Updated `stepBackInPractice()` so backing out of Step 1 to Today clears `hasActiveSession` if no timer was started and no draft exists.
- **`BallerPathApp.kt`**:
  - Wired `ON_STOP` lifecycle observer, state-driven `handleTodayPrimaryAction()`, `saveState = false` on post-submit navigation so finished practice routes aren't restored later, and `popBackStack(TopLevelTab.TODAY.route, inclusive = false)` when selecting the Today tab.

### D. UI Screens & Small-Phone / Large-Text Polish
- **`TodayScreen.kt`**:
  - Renders the state badge, concise status detail banner, Coach Keith cue, and **one primary CTA button** (`today_primary_action_button`) with tightened hero spacing so the CTA stays cleanly visible on compact screens.
- **`PlanScreen.kt`**:
  - Renders fixture `focus`, `why`, `successStandard`, and `reviewLabel` inside the navy focus card alongside Week 2 sessions.
- **`PracticeFlowScreen.kt`**:
  - Uses `FlowRow` in the timer card so `Start` / `Pause` / `Resume` + `Reset` never clip at `1.5x` system font scale on `360dp` screens.
  - Shows a **Draft saved on this device** badge (`draft_saved_indicator`) whenever draft text is present.
- **`WorkFeedbackScreen.kt`**:
  - Shows an **UNFINISHED DRAFT · NOT SUBMITTED YET** card (`work_unfinished_draft_banner`) distinct from submitted entries.
  - Shows **AWAITING COACH** (`awaiting_coach_card`) and **Demo: Reveal Sample Coach Response** (`reveal_sample_feedback_button`) until revealed, then displays **Sample Coach Response · Coach Keith** alongside **Next Adjustment** (`next_adjustment_box`).
- **`AccountScreen.kt`**:
  - Added the **Demo Scenario Switcher** (`scenario_switcher_card`) for jumping directly to any of the 5 states, plus **Reset Sample Demo Data**.
- **`BallerPathComponents.kt`**:
  - Updated `PlayerHeaderBar` to `FlowRow` so player info and badges wrap cleanly under large text.

---

## 4. Automated Verification Baseline

Verified with `compile_applet` (succeeded) and `gradle :app:testDebugUnitTest` (**8 / 8 tests passed**, 0 failures, via Robolectric at `w360dp-h640dp-mdpi`):

- **`BallerPathFlowRobolectricTest` (5 / 5 passed)**:
  1. `todayScreen_smallPhone_answersFocusWithSinglePrimaryActionAndTouchTargets`
  2. `fullFlow_timerPauseBackground_draftRecoveryAfterRestart_awaitingCoachAndReveal` (Start/Pause/Resume timer, `CREATED` → `RESUMED` backgrounding, Activity recreation + cold `ActivityScenario` restart draft recovery via **Finish reflection**, **AWAITING COACH** + **Open work**, **Demo: Reveal Sample Coach Response**, and cold restart persistence of **Read feedback**)
  3. `accountScenarioSwitcher_updatesTodayAndWorkFeedbackStates`
  4. `practiceBackNavigation_stepsBackwardCleanlyToToday`
  5. `repository_timerExpiryDoesNotSubmitAndDraftIsDistinctFromSubmitted`
- **`BallerPathLargeTextRobolectricTest` (1 / 1 passed, `fontScale = 1.5f`)**:
  - `largeSystemText_smallPhone_keyActionsRemainReachableAndReadable`
- **`ExampleRobolectricTest` (1 / 1 passed)** & **`ExampleUnitTest` (1 / 1 passed)**

---

## 5. Brand Assets & Creative Vision on Record

Official Baller Path assets integrated into the Android build:

- **App Launcher Icon** (`03-clear-ascent-app-icon`):
  - Cropped and edge-matched (`#011C41` navy background) into `res/drawable/img_baller_path_icon.png`, `res/drawable/ic_launcher_background.xml`, `res/drawable/ic_launcher_foreground.xml`, and `res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png` + `ic_launcher_round.png`.
- **Horizontal Logo Lockup** (`04-clear-ascent-horizontal-lockup`):
  - Saved as `res/drawable/logo_baller_path_horizontal.png` and rendered in `PlayerHeaderBar` across **Today**, **Plan**, and **Work + Feedback**.
- **Stacked Logo Lockup** (`BallerPath` mark + stacked wordmark):
  - Saved as `res/drawable/logo_baller_path_stacked.png` and featured in `AccountScreen` (`account_brand_lockup_card`).
- **Headline Visuals Embedded in App**:
  - **Today** (`res/drawable/img_headline_one_priority.jpg`): *"ONE CLEAR PRIORITY. Not ten things. Not everything."*
  - **Plan** (`res/drawable/img_headline_work_purpose.jpg`): *"WORK WITH A PURPOSE. Every drill should connect to the focus."*
  - **Work + Feedback** (`res/drawable/img_headline_notice_grow.jpg`): *"NOTICE. ADJUST. GROW. What worked? What changes next?"*
- **Brand Vision & Founding 10 Concept Recorded**:
  - Core product pillars on record from your uploaded creative suite: *"Start with the focus"*, *"Work with a purpose"*, *"Notice. Adjust. Grow."*, *"Busy isn't the same as progress"*, *"A clear next step. A coach in your corner"*, and the *Founding 10* virtual coaching model (`Get the week's focus -> Put in the reps -> Get feedback -> Take the next step`; billing/auth intentionally kept outside this prototype).

