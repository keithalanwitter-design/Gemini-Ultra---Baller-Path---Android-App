package com.example

import android.content.Context
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BallerPathDatabase
import com.example.data.model.WorkflowScenarioId
import com.example.data.repository.LocalBallerPathRepository
import com.example.data.sample.SampleDataSource
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-mdpi")
class BallerPathFlowRobolectricTest {

  @get:Rule
  val composeRule = createEmptyComposeRule()

  private val coachKeithCue = SampleDataSource.todayFocus.coachCue

  @Before
  fun resetDemoDatabase() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = BallerPathDatabase.getInstance(context)
    val repo = LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
      parentConsentDao = db.parentConsentDao(),
    )
    repo.resetDemoData()
    repo.acknowledgeParentConsent()
  }

  @Test
  fun todayScreen_smallPhone_answersFocusWithSinglePrimaryActionAndTouchTargets() {
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_primary_action_button").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.waitForIdle()

      composeRule.onNodeWithText("Maya Brooks · Guard · 7th grade").assertIsDisplayed()
      composeRule.onNodeWithText("Fictional Player · Sample Data").assertIsDisplayed()
      composeRule.onNodeWithText("WHAT SHOULD I WORK ON TODAY?").assertIsDisplayed()
      composeRule.onNodeWithText("Pass, Look, Decide").assertIsDisplayed()
      composeRule.onNodeWithText(coachKeithCue).assertIsDisplayed()
      composeRule.onNodeWithTag("today_primary_action_button")
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .assertTextEquals("Start")

      composeRule.onNodeWithTag("nav_today").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
      composeRule.onNodeWithTag("nav_plan").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
      composeRule.onNodeWithTag("nav_work_feedback").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
      composeRule.onNodeWithTag("nav_account").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
    }
  }

  @Test
  fun fullFlow_timerPauseBackground_draftRecoveryAfterRestart_awaitingCoachAndReveal() {
    val draftText = "My teammate started to drive, so I stayed wide"
    val fullSubmitted = "$draftText to give them room."
    val assign1Id = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE

    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_primary_action_button").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.waitForIdle()

      // 1. Start practice from Today
      composeRule.onNodeWithTag("today_primary_action_button").performClick()
      composeRule.waitForIdle()

      // Step 1: Pass (2 min) — advance freely without starting timer
      composeRule.onNodeWithText("Step 1 of 4 · PASS (2 min)").assertIsDisplayed()
      composeRule.onNodeWithTag("practice_timer_toggle").assertTextContains("Start", substring = true)
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()

      // Step 2: Look (3 min) — start timer, tick, pause -> Resume, background, return
      composeRule.onNodeWithText("Step 2 of 4 · LOOK (3 min)").assertIsDisplayed()
      composeRule.onNodeWithTag("practice_timer_text").assertTextEquals("03:00")
      composeRule.onNodeWithTag("practice_timer_toggle").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_timer_toggle").assertTextContains("Pause", substring = true)

      ShadowLooper.idleMainLooper(1_500, TimeUnit.MILLISECONDS)
      composeRule.waitForIdle()
      assertTrue(composeRule.onAllNodesWithText("03:00").fetchSemanticsNodes().isEmpty())

      composeRule.onNodeWithTag("practice_timer_toggle").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_timer_toggle").assertTextContains("Resume", substring = true)

      // Briefly background and return -> Step 2 + Resume preserved
      scenario.moveToState(Lifecycle.State.CREATED)
      scenario.moveToState(Lifecycle.State.RESUMED)
      composeRule.waitForIdle()
      composeRule.onNodeWithText("Step 2 of 4 · LOOK (3 min)").assertIsDisplayed()
      composeRule.onNodeWithTag("practice_timer_toggle").assertTextContains("Resume", substring = true)

      // Move to Decide and then Reflection
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithText("Step 3 of 4 · DECIDE (3 min)").assertIsDisplayed()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()

      // 2. Type unfinished reflection draft, background to flush Room write, then close activity
      composeRule.onNodeWithText(SampleDataSource.assignmentOne.reflectionPrompt).assertIsDisplayed()
      composeRule.onNodeWithTag("reflection_input").performScrollTo().performTextInput(draftText)
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("draft_saved_indicator").performScrollTo().assertIsDisplayed()

      // Activity recreation preserves Practice reflection screen & draft
      scenario.recreate()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("reflection_input").assertTextContains(draftText, substring = true)

      scenario.moveToState(Lifecycle.State.CREATED)
      ShadowLooper.idleMainLooper(200, TimeUnit.MILLISECONDS)
    }

    // 3. Cold app restart -> lands on Today with "Resume" -> opens Practice & recovers draft
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Resume").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("today_primary_action_button")
        .assertIsDisplayed()
        .assertTextEquals("Resume")
        .performClick()
      composeRule.waitForIdle()

      // Draft is recovered on the reflection screen!
      composeRule.onNodeWithTag("reflection_input").assertTextContains(draftText, substring = true)

      // Finish writing and submit
      composeRule.onNodeWithTag("reflection_input").performScrollTo().performTextReplacement(fullSubmitted)
      composeRule.onNodeWithTag("submit_reflection_button").performClick()

      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("awaiting_coach_card_$assign1Id").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.waitForIdle()

      // 4. Work + Feedback Awaiting coach shows Assignment 1 + Demo reveal control
      composeRule.onNodeWithText(fullSubmitted).performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("awaiting_coach_card_$assign1Id").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("reveal_feedback_$assign1Id")
        .performScrollTo()
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)

      // 5. Verify Today offers next ready assignment (Assignment 2) while Assignment 1 awaits review
      composeRule.onNodeWithTag("nav_today").performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Corner Relocation Read").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("today_primary_action_button")
        .assertIsDisplayed()
        .assertTextEquals("Start")

      // 6. Go to Work + Feedback -> Awaiting coach -> Reveal feedback for Assignment 1
      composeRule.onNodeWithTag("nav_work_feedback").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("wf_tab_awaiting").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("reveal_feedback_$assign1Id").performScrollTo().performClick()

      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("next_rep_${assign1Id}_0").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("coach_response_${assign1Id}_0").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("next_rep_${assign1Id}_0").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithText("TRY THE NEXT REP").performScrollTo().assertIsDisplayed()
    }

    // 7. Cold restart -> Today offers next ready Assignment 2, and Coach responded preserves Assignment 1 feedback
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Corner Relocation Read").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("nav_work_feedback").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("wf_tab_responded").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithText(fullSubmitted).performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("coach_response_${assign1Id}_0").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("next_rep_${assign1Id}_0").performScrollTo().assertIsDisplayed()
    }
  }

  @Test
  fun accountScenarioSwitcher_updatesTodayAndWorkFeedbackStates() {
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("nav_account").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("nav_account").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithText("Avery Pine").assertIsDisplayed()
      composeRule.onNodeWithText("Demo Scenario Switcher").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("scenario_more_work_ready").performScrollTo().assertIsDisplayed()

      // Switch to "Practice in progress" (Step: Look)
      composeRule.onNodeWithTag("scenario_in_progress").performScrollTo().performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Sample scenario switched to: Practice in progress")
          .fetchSemanticsNodes().isNotEmpty()
      }

      composeRule.onNodeWithTag("nav_today").performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Resume").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("today_primary_action_button")
        .assertIsDisplayed()
        .assertTextEquals("Resume")
        .performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithText("Step 2 of 4 · LOOK (3 min)").assertIsDisplayed()

      // Step back to Today and reset demo back to Practice ready
      composeRule.onNodeWithTag("practice_back_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_back_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("nav_account").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("reset_demo_button").performScrollTo().performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Demo reset to Practice ready for Maya Brooks.")
          .fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("nav_today").performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Start").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("today_primary_action_button")
        .assertTextEquals("Start")
    }
  }

  @Test
  fun practiceBackNavigation_stepsBackwardCleanlyToToday() {
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("today_primary_action_button").performClick()
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithText("Step 2 of 4 · LOOK (3 min)").assertIsDisplayed()

      composeRule.onNodeWithTag("practice_back_button")
        .assertHeightIsAtLeast(48.dp)
        .performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithText("Step 1 of 4 · PASS (2 min)").assertIsDisplayed()

      composeRule.onNodeWithTag("practice_back_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("today_primary_action_button")
        .assertIsDisplayed()
        .assertTextEquals("Start")
    }
  }

  @Test
  fun repository_timerExpiryDoesNotSubmitAndDraftIsDistinctFromSubmitted(): Unit = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "workflow_state_verify.db"
    context.deleteDatabase(dbName)

    val db = Room.databaseBuilder(context, BallerPathDatabase::class.java, dbName)
      .allowMainThreadQueries()
      .build()
    val repo = LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
      parentConsentDao = db.parentConsentDao(),
    )
    repo.ensureSeeded()

    // Save an unfinished draft and verify no user-submitted reflection exists yet
    repo.saveWorkflowState(
      com.example.data.model.PracticeWorkflowState(
        assignmentId = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
        hasActiveSession = true,
        currentStepId = "decide",
        isReflectionStage = true,
        remainingSeconds = 0,
        hasStartedTimer = true,
        reflectionDraft = "Unfinished note",
      )
    )
    val reflectionsBeforeSubmit = repo.observeReflections().first()
    assertTrue(reflectionsBeforeSubmit.none { !it.isSampleSeed })

    // Submit reflection -> clears draft and saves reflection in Awaiting Coach state
    val created = repo.submitReflection(
      assignmentId = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
      drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
      drillTitle = "Pass, Look, Decide",
      laneObservation = "Submitted note",
      readTag = "Pass, Look, Decide · 8 min",
    )
    assertFalse(created.isCoachResponseRevealed)
    assertEquals("", repo.observeWorkflowState().first().reflectionDraft)

    // Reveal sample feedback -> updates Room
    repo.revealSampleCoachResponse(created.id)
    val updated = repo.observeReflections().first().first { !it.isSampleSeed }
    assertTrue(updated.isCoachResponseRevealed)

    // Apply awaiting_coach scenario -> restores fixture waiting state cleanly
    repo.applyDemoScenario(WorkflowScenarioId.AWAITING_COACH)
    val awaiting = repo.observeReflections().first().first { !it.isSampleSeed }
    assertFalse(awaiting.isCoachResponseRevealed)
    assertEquals(SampleDataSource.FIXTURE_SUBMITTED_TEXT, awaiting.laneObservation)

    db.close()
    context.deleteDatabase(dbName)
    Unit
  }
}
