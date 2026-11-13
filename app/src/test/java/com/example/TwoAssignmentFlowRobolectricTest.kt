package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BallerPathDatabase
import com.example.data.model.SetupStage
import com.example.data.model.WorkflowScenarioId
import com.example.data.repository.LocalBallerPathRepository
import com.example.data.sample.SampleDataSource
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h892dp-mdpi")
class TwoAssignmentFlowRobolectricTest {

  @get:Rule
  val composeRule = createEmptyComposeRule()

  private val assign1Id = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE
  private val assign2Id = SampleDataSource.ASSIGNMENT_CORNER_RELOCATION

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

  /**
   * B4 Steps 1-5:
   * 1. Maya has assignment 1 awaiting review and assignment 2 ready.
   * 2. Today offers to start assignment 2. Start it, write a draft, and leave.
   * 3. Restart the app. Today reopens assignment 2 with the draft intact.
   * 4. Submit assignment 2. Assignment 1's reflection is unchanged.
   * 5. Reveal feedback for assignment 1. Its feedback and "Try the next rep" note appear only under assignment 1.
   */
  @Test
  fun twoAssignmentFlow_draftIsolationRestartSubmitAndPerAssignmentFeedback() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = BallerPathDatabase.getInstance(context)
    val repo = LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
      parentConsentDao = db.parentConsentDao(),
    )

    // Step 1: Maya has assignment 1 awaiting review and assignment 2 ready.
    runBlocking {
      repo.applyDemoScenario(WorkflowScenarioId.AWAITING_COACH)
    }

    val assignment2Draft = "My defender stepped into the paint, so I drifted to the corner"
    val assignment2Submitted = "$assignment2Draft with my hands ready."

    // Step 2: Today offers to start assignment 2. Start it, write a draft, and leave.
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText(SampleDataSource.assignmentTwo.title).fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithText("Corner Relocation Read").assertIsDisplayed()
      composeRule.onNodeWithTag("today_primary_action_button")
        .assertIsDisplayed()
        .assertTextEquals("Start")
        .performClick()
      composeRule.waitForIdle()

      // Confirm Practice opened Assignment 2's drill, NOT Assignment 1's!
      composeRule.onNodeWithTag("practice_drill_title").assertTextEquals("Corner Relocation Read")
      composeRule.onNodeWithText("Read the Drive").assertIsDisplayed()

      // Advance all 3 steps to Assignment 2's reflection screen
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()

      composeRule.onNodeWithText(SampleDataSource.assignmentTwo.reflectionPrompt).assertIsDisplayed()
      composeRule.onNodeWithTag("reflection_input").performScrollTo().performTextInput(assignment2Draft)
      composeRule.waitForIdle()

      // Leave / background so Room flushes Assignment 2's draft
      scenario.moveToState(Lifecycle.State.CREATED)
      ShadowLooper.idleMainLooper(300, TimeUnit.MILLISECONDS)
    }

    // Step 3: Restart the app. Today reopens Assignment 2 with the draft intact.
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Resume").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithText("Corner Relocation Read").assertIsDisplayed()
      composeRule.onNodeWithTag("today_primary_action_button")
        .assertIsDisplayed()
        .assertTextEquals("Resume")
        .performClick()
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("practice_drill_title").assertTextEquals("Corner Relocation Read")
      composeRule.onNodeWithTag("reflection_input")
        .assertTextContains(assignment2Draft, substring = true)

      // Step 4: Submit Assignment 2. Assignment 1's reflection is unchanged.
      composeRule.onNodeWithTag("reflection_input").performScrollTo().performTextReplacement(assignment2Submitted)
      composeRule.onNodeWithTag("submit_reflection_button").performClick()

      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("awaiting_card_$assign2Id").fetchSemanticsNodes().isNotEmpty()
      }

      // Both Assignment 1 (unchanged reflection) and Assignment 2 are in Awaiting coach
      composeRule.onNodeWithTag("awaiting_card_$assign1Id").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithText(SampleDataSource.FIXTURE_SUBMITTED_TEXT).performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("awaiting_card_$assign2Id").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithText(assignment2Submitted).performScrollTo().assertIsDisplayed()

      // Step 5: Reveal feedback for Assignment 1.
      // Its feedback and "Try the next rep" note appear ONLY under Assignment 1.
      composeRule.onNodeWithTag("reveal_feedback_$assign1Id").performScrollTo().performClick()

      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("responded_card_$assign1Id").fetchSemanticsNodes().isNotEmpty()
      }

      composeRule.onNodeWithTag("responded_card_$assign1Id").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("coach_response_${assign1Id}_0").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("next_rep_${assign1Id}_0").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithText(SampleDataSource.FIXTURE_COACH_RESPONSE).performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithText(SampleDataSource.FIXTURE_NEXT_ADJUSTMENT).performScrollTo().assertIsDisplayed()

      // Assignment 2's feedback & next rep note must NOT appear in Coach responded
      assertTrue(composeRule.onAllNodesWithTag("responded_card_$assign2Id").fetchSemanticsNodes().isEmpty())
      assertTrue(composeRule.onAllNodesWithText(SampleDataSource.FIXTURE_ASSIGNMENT_TWO_COACH_RESPONSE).fetchSemanticsNodes().isEmpty())

      // Assignment 2 remains intact in Awaiting coach
      composeRule.onNodeWithTag("wf_tab_awaiting").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("awaiting_card_$assign2Id").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithText(assignment2Submitted).performScrollTo().assertIsDisplayed()

      // Also check Today shows "Saved. Coach Keith will review it." since all assigned work is submitted
      composeRule.onNodeWithTag("nav_today").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithText("Saved. Coach Keith will review it.", substring = true).assertIsDisplayed()
    }
  }

  /**
   * B4 Step 6:
   * Database migration from old schema (v2) to v3 keeps original reflections, feedback, and drafts.
   */
  @Test
  fun migration2To3_preservesExistingReflectionsFeedbackAndDraft(): Unit = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "migration_v2_v3_test.db"
    context.deleteDatabase(dbName)

    val originalObservation = "V2 saved reflection before update."
    val originalCoachFeedback = "V2 coach Keith feedback kept forever."
    val originalNextRep = "V2 try the next rep cue kept forever."
    val originalDraft = "V2 unfinished draft before app update"

    // 1. Create authentic v2 SQLite schema and populate rows directly
    val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
      .name(dbName)
      .callback(
        object : SupportSQLiteOpenHelper.Callback(2) {
          override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `practice_reflections` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `drillId` TEXT NOT NULL,
                `drillTitle` TEXT NOT NULL,
                `laneObservation` TEXT NOT NULL,
                `readTag` TEXT NOT NULL,
                `createdAtMillis` INTEGER NOT NULL,
                `sampleCoachLabel` TEXT NOT NULL,
                `sampleCoachResponse` TEXT NOT NULL,
                `nextAdjustment` TEXT NOT NULL,
                `isCoachResponseRevealed` INTEGER NOT NULL,
                `isSampleSeed` INTEGER NOT NULL
              )
              """.trimIndent()
            )
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `practice_workflow_state` (
                `id` INTEGER NOT NULL,
                `hasActiveSession` INTEGER NOT NULL,
                `currentStepId` TEXT NOT NULL,
                `isReflectionStage` INTEGER NOT NULL,
                `remainingSeconds` INTEGER NOT NULL,
                `hasStartedTimer` INTEGER NOT NULL,
                `reflectionDraft` TEXT NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
              )
              """.trimIndent()
            )
          }

          override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
      )
      .build()

    val rawHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
    val v2Db = rawHelper.writableDatabase
    v2Db.insert(
      "practice_reflections",
      SQLiteDatabase.CONFLICT_ABORT,
      android.content.ContentValues().apply {
        put("drillId", SampleDataSource.DRILL_PASS_LOOK_DECIDE)
        put("drillTitle", "Pass, Look, Decide")
        put("laneObservation", originalObservation)
        put("readTag", "Pass, Look, Decide · 8 min")
        put("createdAtMillis", 1_700_000_000_000L)
        put("sampleCoachLabel", "Coach Keith")
        put("sampleCoachResponse", originalCoachFeedback)
        put("nextAdjustment", originalNextRep)
        put("isCoachResponseRevealed", 1)
        put("isSampleSeed", 0)
      },
    )
    v2Db.insert(
      "practice_workflow_state",
      SQLiteDatabase.CONFLICT_ABORT,
      android.content.ContentValues().apply {
        put("id", 1)
        put("hasActiveSession", 1)
        put("currentStepId", "decide")
        put("isReflectionStage", 1)
        put("remainingSeconds", 0)
        put("hasStartedTimer", 1)
        put("reflectionDraft", originalDraft)
        put("updatedAtMillis", 1_700_000_500_000L)
      },
    )
    v2Db.close()
    rawHelper.close()

    // 2. Open BallerPathDatabase at current version using MIGRATION_2_3 + MIGRATION_3_4 (no destructive migration!)
    val migratedDb = Room.databaseBuilder(context, BallerPathDatabase::class.java, dbName)
      .addMigrations(BallerPathDatabase.MIGRATION_2_3, BallerPathDatabase.MIGRATION_3_4)
      .allowMainThreadQueries()
      .build()

    val reflections = migratedDb.reflectionDao().observeReflections().first()
    val migratedReflection = reflections.first { it.laneObservation == originalObservation }
    assertEquals(assign1Id, migratedReflection.assignmentId)
    assertEquals(originalCoachFeedback, migratedReflection.sampleCoachResponse)
    assertEquals(originalNextRep, migratedReflection.nextAdjustment)
    assertTrue(migratedReflection.isCoachResponseRevealed)

    val migratedAssignment1 = migratedDb.assignmentDao().getAssignment(assign1Id)!!
    assertEquals(originalDraft, migratedAssignment1.reflectionDraft)
    assertTrue(migratedAssignment1.hasActiveSession)
    assertTrue(migratedAssignment1.isReflectionStage)

    // Verify Assignment 2 and general work were seeded cleanly by migration
    val migratedAssignment2 = migratedDb.assignmentDao().getAssignment(assign2Id)
    assertTrue(migratedAssignment2 != null)
    assertEquals("", migratedAssignment2?.reflectionDraft)

    migratedDb.close()
    context.deleteDatabase(dbName)
  }

  /**
   * B2 Setup states + B3 resubmission history preservation:
   */
  @Test
  fun setupStates_hidePersonalWorkAndOfferPublishedGeneralWork_resubmissionPreservesHistory() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = BallerPathDatabase.getInstance(context)
    val repo = LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
      parentConsentDao = db.parentConsentDao(),
    )

    // B2a: Setup in progress -> shows 2 business day notice, hides personal drills
    runBlocking {
      repo.setSetupState(SetupStage.SETUP_IN_PROGRESS, hasGeneralWorkPublished = false)
    }
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_setup_in_progress_card").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithText(
        "Coach Keith is reviewing Maya's info. You'll get access within 2 business days."
      ).assertIsDisplayed()
      assertTrue(composeRule.onAllNodesWithText("Pass, Look, Decide").fetchSemanticsNodes().isEmpty())

      // B2b: Plan being prepared with general work published -> general work can be started
      runBlocking {
        repo.setSetupState(SetupStage.PLAN_BEING_PREPARED, hasGeneralWorkPublished = true)
      }
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Eyes-Up Rhythm Dribble").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("today_primary_action_button").assertTextEquals("Start")

      // Return to WORK_READY and test B3 resubmission preserves earlier feedback history
      runBlocking {
        repo.setSetupState(SetupStage.WORK_READY, hasGeneralWorkPublished = true)
        val first = repo.submitReflection(
          assignmentId = assign1Id,
          drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
          drillTitle = "Pass, Look, Decide",
          laneObservation = "First attempt reading the drive.",
          readTag = "Pass, Look, Decide · 8 min",
        )
        repo.revealSampleCoachResponse(first.id)
        val second = repo.submitReflection(
          assignmentId = assign1Id,
          drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
          drillTitle = "Pass, Look, Decide",
          laneObservation = "Second attempt after adjusting spacing.",
          readTag = "Pass, Look, Decide · 8 min",
        )
        repo.revealSampleCoachResponse(second.id)
      }

      composeRule.onNodeWithTag("nav_work_feedback").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("wf_tab_responded").performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("responded_card_$assign1Id").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithText("Second attempt after adjusting spacing.").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithText("First attempt reading the drive.").performScrollTo().assertIsDisplayed()
    }
  }

  /**
   * Pilot Prep Item 4:
   * First use requires Parent/Guardian consent stating grades 4-8 & Coach Keith review,
   * persists locally in Room, and skips the gate after acknowledgment.
   */
  @Test
  fun parentConsentGate_blocksFirstUseUntilAcknowledged_andPersistsAcrossRestart() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = BallerPathDatabase.getInstance(context)
    val repo = LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
      parentConsentDao = db.parentConsentDao(),
    )
    runBlocking { repo.clearParentConsentForTest() }

    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("parent_consent_card").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithText("Baller Path is for a parent or guardian of a player in grades 4–8.")
        .assertIsDisplayed()
      composeRule.onNodeWithText("Coach Keith reviews the work", substring = true).assertIsDisplayed()
      assertTrue(composeRule.onAllNodesWithTag("bottom_navigation_bar").fetchSemanticsNodes().isEmpty())

      // Require confirmation checkbox first
      composeRule.onNodeWithTag("consent_continue_button").performScrollTo().performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("consent_validation_error").performScrollTo().assertIsDisplayed()

      // Check box and continue -> enters Today
      composeRule.onNodeWithTag("consent_checkbox_row").performScrollTo().performClick()
      composeRule.onNodeWithTag("consent_continue_button").performScrollTo().performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_primary_action_button").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("today_primary_action_button").assertIsDisplayed()
    }

    // Cold restart remembers parent consent and opens directly into Today
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_primary_action_button").fetchSemanticsNodes().isNotEmpty()
      }
      assertTrue(composeRule.onAllNodesWithTag("parent_consent_card").fetchSemanticsNodes().isEmpty())
    }
  }

  /**
   * Pilot Prep Item 4:
   * Real v3 -> v4 database migration keeps every reflection, draft, and coach response intact
   * while adding the parent_consent table.
   */
  @Test
  fun migration3To4_keepsAllReflectionsDraftsAndFeedback_andAddsParentConsent(): Unit = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbName = "migration_v3_v4_test.db"
    context.deleteDatabase(dbName)

    val a1Reflection = "V3 reflection awaiting Coach Keith."
    val a2Reflection = "V3 reflection with Coach Keith reply."
    val a2CoachReply = "V3 Coach Keith reply kept safely."
    val a2NextRep = "V3 Try the next rep cue kept safely."
    val a1Draft = "V3 assignment 1 draft kept safely."
    val a2Draft = "V3 assignment 2 draft kept safely."

    val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
      .name(dbName)
      .callback(
        object : SupportSQLiteOpenHelper.Callback(3) {
          override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `practice_reflections` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `assignmentId` TEXT NOT NULL DEFAULT 'assign-pass-look-decide',
                `drillId` TEXT NOT NULL,
                `drillTitle` TEXT NOT NULL,
                `laneObservation` TEXT NOT NULL,
                `readTag` TEXT NOT NULL,
                `createdAtMillis` INTEGER NOT NULL,
                `sampleCoachLabel` TEXT NOT NULL,
                `sampleCoachResponse` TEXT NOT NULL,
                `nextAdjustment` TEXT NOT NULL,
                `isCoachResponseRevealed` INTEGER NOT NULL,
                `isSampleSeed` INTEGER NOT NULL
              )
              """.trimIndent()
            )
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `practice_workflow_state` (
                `id` INTEGER NOT NULL,
                `hasActiveSession` INTEGER NOT NULL,
                `currentStepId` TEXT NOT NULL,
                `isReflectionStage` INTEGER NOT NULL,
                `remainingSeconds` INTEGER NOT NULL,
                `hasStartedTimer` INTEGER NOT NULL,
                `reflectionDraft` TEXT NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
              )
              """.trimIndent()
            )
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `assignments` (
                `assignmentId` TEXT NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                `drillId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `dayTag` TEXT NOT NULL,
                `focusHeadline` TEXT NOT NULL,
                `totalMinutes` INTEGER NOT NULL,
                `coachCue` TEXT NOT NULL,
                `reflectionPrompt` TEXT NOT NULL,
                `isGeneralWork` INTEGER NOT NULL,
                `stepsJson` TEXT NOT NULL,
                `starterCuesJson` TEXT NOT NULL,
                `hasActiveSession` INTEGER NOT NULL,
                `currentStepId` TEXT NOT NULL,
                `isReflectionStage` INTEGER NOT NULL,
                `remainingSeconds` INTEGER NOT NULL,
                `hasStartedTimer` INTEGER NOT NULL,
                `reflectionDraft` TEXT NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL,
                PRIMARY KEY(`assignmentId`)
              )
              """.trimIndent()
            )
            db.execSQL(
              """
              CREATE TABLE IF NOT EXISTS `setup_state` (
                `id` INTEGER NOT NULL,
                `setupStage` TEXT NOT NULL,
                `hasGeneralWorkPublished` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
              )
              """.trimIndent()
            )
          }

          override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
      )
      .build()

    val rawHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
    val v3Db = rawHelper.writableDatabase
    v3Db.insert(
      "practice_reflections",
      SQLiteDatabase.CONFLICT_ABORT,
      android.content.ContentValues().apply {
        put("assignmentId", assign1Id)
        put("drillId", SampleDataSource.DRILL_PASS_LOOK_DECIDE)
        put("drillTitle", "Pass, Look, Decide")
        put("laneObservation", a1Reflection)
        put("readTag", "Pass, Look, Decide · 8 min")
        put("createdAtMillis", 1_700_000_000_000L)
        put("sampleCoachLabel", "Coach Keith")
        put("sampleCoachResponse", "Pending")
        put("nextAdjustment", "Next rep")
        put("isCoachResponseRevealed", 0)
        put("isSampleSeed", 0)
      },
    )
    v3Db.insert(
      "practice_reflections",
      SQLiteDatabase.CONFLICT_ABORT,
      android.content.ContentValues().apply {
        put("assignmentId", assign2Id)
        put("drillId", SampleDataSource.DRILL_CORNER_RELOCATION)
        put("drillTitle", "Corner Relocation Read")
        put("laneObservation", a2Reflection)
        put("readTag", "Corner Relocation Read · 9 min")
        put("createdAtMillis", 1_700_000_100_000L)
        put("sampleCoachLabel", "Coach Keith")
        put("sampleCoachResponse", a2CoachReply)
        put("nextAdjustment", a2NextRep)
        put("isCoachResponseRevealed", 1)
        put("isSampleSeed", 0)
      },
    )
    v3Db.insert(
      "assignments",
      SQLiteDatabase.CONFLICT_ABORT,
      android.content.ContentValues().apply {
        put("assignmentId", assign1Id)
        put("sortOrder", 0)
        put("drillId", SampleDataSource.DRILL_PASS_LOOK_DECIDE)
        put("title", "Pass, Look, Decide")
        put("dayTag", "Monday")
        put("focusHeadline", "Focus")
        put("totalMinutes", 8)
        put("coachCue", "Cue")
        put("reflectionPrompt", "Prompt")
        put("isGeneralWork", 0)
        put("stepsJson", "[]")
        put("starterCuesJson", "[]")
        put("hasActiveSession", 1)
        put("currentStepId", "decide")
        put("isReflectionStage", 1)
        put("remainingSeconds", 0)
        put("hasStartedTimer", 1)
        put("reflectionDraft", a1Draft)
        put("updatedAtMillis", 1_700_000_200_000L)
      },
    )
    v3Db.insert(
      "assignments",
      SQLiteDatabase.CONFLICT_ABORT,
      android.content.ContentValues().apply {
        put("assignmentId", assign2Id)
        put("sortOrder", 1)
        put("drillId", SampleDataSource.DRILL_CORNER_RELOCATION)
        put("title", "Corner Relocation Read")
        put("dayTag", "Wednesday")
        put("focusHeadline", "Focus 2")
        put("totalMinutes", 9)
        put("coachCue", "Cue 2")
        put("reflectionPrompt", "Prompt 2")
        put("isGeneralWork", 0)
        put("stepsJson", "[]")
        put("starterCuesJson", "[]")
        put("hasActiveSession", 1)
        put("currentStepId", "corner_read")
        put("isReflectionStage", 0)
        put("remainingSeconds", 120)
        put("hasStartedTimer", 1)
        put("reflectionDraft", a2Draft)
        put("updatedAtMillis", 1_700_000_300_000L)
      },
    )
    v3Db.insert(
      "setup_state",
      SQLiteDatabase.CONFLICT_ABORT,
      android.content.ContentValues().apply {
        put("id", 1)
        put("setupStage", "WORK_READY")
        put("hasGeneralWorkPublished", 1)
      },
    )
    v3Db.close()
    rawHelper.close()

    val migratedDb = Room.databaseBuilder(context, BallerPathDatabase::class.java, dbName)
      .addMigrations(BallerPathDatabase.MIGRATION_2_3, BallerPathDatabase.MIGRATION_3_4)
      .allowMainThreadQueries()
      .build()

    val reflections = migratedDb.reflectionDao().observeReflections().first()
    val migratedA1 = reflections.first { it.assignmentId == assign1Id }
    val migratedA2 = reflections.first { it.assignmentId == assign2Id }
    assertEquals(a1Reflection, migratedA1.laneObservation)
    assertEquals(a2Reflection, migratedA2.laneObservation)
    assertEquals(a2CoachReply, migratedA2.sampleCoachResponse)
    assertEquals(a2NextRep, migratedA2.nextAdjustment)
    assertTrue(migratedA2.isCoachResponseRevealed)

    assertEquals(a1Draft, migratedDb.assignmentDao().getAssignment(assign1Id)?.reflectionDraft)
    assertEquals(a2Draft, migratedDb.assignmentDao().getAssignment(assign2Id)?.reflectionDraft)
    assertEquals(false, migratedDb.parentConsentDao().observeConsent().first()?.isAcknowledged ?: false)

    migratedDb.close()
    context.deleteDatabase(dbName)
  }

  /**
   * Pilot Prep Item 3:
   * Backgrounding the app saves the active assignment draft immediately, and cold restart
   * restores the exact same assignment and draft.
   */
  @Test
  fun backgroundingApp_savesActiveAssignmentDraft_andColdRestartRestoresSameAssignmentAndDraft() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = BallerPathDatabase.getInstance(context)
    val repo = LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
      parentConsentDao = db.parentConsentDao(),
    )
    runBlocking { repo.applyDemoScenario(WorkflowScenarioId.AWAITING_COACH) }

    val draftText = "Background flush saved this Corner Relocation draft safely"

    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Corner Relocation Read").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("today_primary_action_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_drill_title").assertTextEquals("Corner Relocation Read")

      // Navigate to reflection stage and enter draft
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("reflection_input").performScrollTo().performTextInput(draftText)
      composeRule.waitForIdle()

      // Background the app (ON_STOP)
      scenario.moveToState(Lifecycle.State.CREATED)
      ShadowLooper.idleMainLooper(300, TimeUnit.MILLISECONDS)
    }

    // Verify Room persisted the exact assignment & draft synchronously on backgrounding
    val savedAssignment = runBlocking { db.assignmentDao().getAssignment(assign2Id) }
    assertEquals(draftText, savedAssignment?.reflectionDraft)
    assertTrue(savedAssignment?.isReflectionStage == true)

    // Cold restart restores Corner Relocation Read + the saved draft
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText("Resume").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithText("Corner Relocation Read").assertIsDisplayed()
      composeRule.onNodeWithTag("today_primary_action_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_drill_title").assertTextEquals("Corner Relocation Read")
      composeRule.onNodeWithTag("reflection_input").assertTextContains(draftText, substring = true)
    }
  }

  /**
   * Pilot Fixes Check:
   * Both privacy links (ParentConsentScreen and AccountScreen) open https://ballerpath.com/privacy,
   * fictional player labeling stays visible, and Today -> Practice -> Reflection -> Work + Feedback
   * lets testers reveal a clearly labeled sample coach response.
   */
  @Test
  fun privacyLinks_openBallerPathPrivacyUrl_andReleaseAvailableFlowLabelsWorkEndToEnd() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as Application
    val db = BallerPathDatabase.getInstance(context)
    val repo = LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
      parentConsentDao = db.parentConsentDao(),
    )
    runBlocking { repo.clearParentConsentForTest() }

    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("consent_privacy_policy_link").fetchSemanticsNodes().isNotEmpty()
      }

      // 1. Privacy link on ParentConsentScreen
      composeRule.onNodeWithTag("consent_privacy_policy_link")
        .performScrollTo()
        .assertTextContains("https://ballerpath.com/privacy", substring = true)
        .performClick()
      composeRule.waitForIdle()
      val consentIntent = Shadows.shadowOf(app).nextStartedActivity
      assertEquals(Intent.ACTION_VIEW, consentIntent?.action)
      assertEquals(Uri.parse("https://ballerpath.com/privacy"), consentIntent?.data)

      // Accept parent consent to enter Today
      composeRule.onNodeWithTag("consent_checkbox_row").performScrollTo().performClick()
      composeRule.onNodeWithTag("consent_continue_button").performScrollTo().performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_primary_action_button").fetchSemanticsNodes().isNotEmpty()
      }

      // 2. Fictional player badge visible on Today, walk Today -> Practice -> Reflection -> Work + Feedback
      composeRule.onNodeWithText("Fictional Player · Sample Data").assertIsDisplayed()
      composeRule.onNodeWithTag("today_primary_action_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("reflection_input")
        .performScrollTo()
        .performTextInput("My teammate drove the lane so I held the corner window.")
      composeRule.onNodeWithTag("submit_reflection_button").performClick()

      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("reveal_feedback_$assign1Id").fetchSemanticsNodes().isNotEmpty()
      }

      // 3. Reveal sample coach response & verify clear sample labels
      composeRule.onNodeWithTag("reveal_feedback_$assign1Id").performScrollTo().performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("coach_response_${assign1Id}_0").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("coach_response_${assign1Id}_0")
        .performScrollTo()
        .assertIsDisplayed()
      composeRule.onAllNodesWithText("SAMPLE COACH RESPONSE · COACH KEITH")
        .onFirst()
        .assertIsDisplayed()
      composeRule.onAllNodesWithText("Simulated locally for prototype demo")
        .onFirst()
        .assertIsDisplayed()

      // 4. Privacy link + Fictional Player label on AccountScreen
      composeRule.onNodeWithTag("nav_account").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithText("Fictional Player").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("account_privacy_policy_link")
        .performScrollTo()
        .assertTextContains("https://ballerpath.com/privacy", substring = true)
        .performClick()
      composeRule.waitForIdle()
      val accountIntent = Shadows.shadowOf(app).nextStartedActivity
      assertEquals(Intent.ACTION_VIEW, accountIntent?.action)
      assertEquals(Uri.parse("https://ballerpath.com/privacy"), accountIntent?.data)
    }
  }

  @Test
  fun workFeedback_sevenDayDevelopmentChart_displaysInteractiveBarsMetricsAndLiveSubmissions() {
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("nav_work_feedback").fetchSemanticsNodes().isNotEmpty()
      }

      // 1. Open Work + Feedback and scroll to the 7-day bar chart card
      composeRule.onNodeWithTag("nav_work_feedback").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("work_feedback_list")
        .performScrollToNode(hasTestTag("dev_stats_chart_card"))
      composeRule.onNodeWithTag("dev_stats_chart_card").assertIsDisplayed()
      composeRule.onNodeWithTag("dev_stats_bar_chart").assertIsDisplayed()

      // Verify all 7 day bars exist and initial total is 51 min (10+8+0+14+7+12+0)
      composeRule.onNodeWithTag("stats_total_value")
        .performScrollTo()
        .assertTextContains("51 min", substring = true)
      (0..6).forEach { idx ->
        composeRule.onNodeWithTag("stats_day_bar_$idx")
          .performScrollTo()
          .assertIsDisplayed()
      }

      // 2. Tap an earlier day bar (offset -3 -> index 3: 14 min / 20 reps) and verify detail card
      composeRule.onNodeWithTag("stats_day_bar_3").performScrollTo().performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("stats_selected_day_detail")
        .performScrollTo()
        .assertTextContains("Spacing After the Pass", substring = true)
        .assertTextContains("14 practice minutes", substring = true)

      // 3. Toggle to Focus Reps metric and verify totals update (14+12+0+20+10+18+0 = 74 reps)
      composeRule.onNodeWithTag("stats_metric_reps").performScrollTo().performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("stats_total_value")
        .performScrollTo()
        .assertTextContains("74 reps", substring = true)

      // Switch back to Practice Min
      composeRule.onNodeWithTag("stats_metric_minutes").performScrollTo().performClick()
      composeRule.waitForIdle()

      // 4. Submit Today's Pass, Look, Decide (8 min) and verify Today's bar updates live
      composeRule.onNodeWithTag("nav_today").performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_primary_action_button").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("today_primary_action_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("reflection_input")
        .performScrollTo()
        .performTextInput("My teammate drove downhill, so I held wide spacing.")
      composeRule.onNodeWithTag("submit_reflection_button").performClick()

      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("work_feedback_list").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("work_feedback_list")
        .performScrollToNode(hasTestTag("dev_stats_chart_card"))
      composeRule.onNodeWithTag("stats_total_value")
        .performScrollTo()
        .assertTextContains("59 min", substring = true)
      composeRule.onNodeWithTag("stats_day_bar_6").performScrollTo().performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("stats_selected_day_detail")
        .performScrollTo()
        .assertTextContains("Pass, Look, Decide", substring = true)
        .assertTextContains("Logged by Maya", substring = true)
    }
  }

  @Test
  fun voiceRecorder_playerJournalingTranscription_andCoachVoiceToTextFeedbackStudio_workEndToEnd() {
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_primary_action_button").fetchSemanticsNodes().isNotEmpty()
      }

      // 1. Start Today's drill and advance to the Reflection stage
      composeRule.onNodeWithTag("today_primary_action_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_next_button").performClick()
      composeRule.waitForIdle()

      // 2. Tap the Speech-to-Text microphone icon inside the reflection text input field
      val expectedPlayerTranscript = SampleDataSource.assignmentOne.starterCues.first()
      composeRule.onNodeWithTag("reflection_mic_button")
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("reflection_stt_listening_banner")
        .performScrollTo()
        .assertIsDisplayed()
      composeRule.onNodeWithTag("reflection_input")
        .performScrollTo()
        .assertTextContains(expectedPlayerTranscript, substring = true)

      // Tap microphone icon again to stop active Speech-to-Text listening
      composeRule.onNodeWithTag("reflection_mic_button")
        .performScrollTo()
        .performClick()
      composeRule.waitForIdle()

      // Also verify the Voice Recorder & Transcription Studio card and Play Voice Note
      composeRule.onNodeWithTag("player_voice_recorder_card")
        .performScrollTo()
        .assertIsDisplayed()
      composeRule.onNodeWithTag("player_voice_record_button")
        .performScrollTo()
        .assertTextContains("Record & Transcribe", substring = true)
        .performClick()
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("player_voice_record_button")
        .performScrollTo()
        .assertTextContains("Stop & Transcribe", substring = true)
        .performClick()
      composeRule.waitForIdle()

      // Verify transcribed speech populated the reflection input, saved the draft, and enabled Play Voice Note
      composeRule.onNodeWithTag("reflection_input")
        .performScrollTo()
        .assertTextContains(expectedPlayerTranscript, substring = true)
      composeRule.onNodeWithTag("draft_saved_indicator")
        .performScrollTo()
        .assertIsDisplayed()
      composeRule.onNodeWithTag("player_voice_play_button")
        .performScrollTo()
        .assertIsDisplayed()

      // Submit the voice-transcribed reflection
      composeRule.onNodeWithTag("submit_reflection_button").performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("awaiting_card_$assign1Id").fetchSemanticsNodes().isNotEmpty()
      }

      // 3. Open Coach Keith Voice-to-Text & Feedback Studio in Awaiting coach
      composeRule.onNodeWithTag("toggle_coach_studio_$assign1Id")
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("coach_feedback_studio_$assign1Id")
        .performScrollTo()
        .assertIsDisplayed()

      // Start & stop Coach Keith voice dictation
      composeRule.onNodeWithTag("coach_voice_record_$assign1Id")
        .performScrollTo()
        .assertTextContains("Dictate Coach Note", substring = true)
        .performClick()
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("coach_voice_record_$assign1Id")
        .performScrollTo()
        .assertTextContains("Stop & Transcribe Coach Note", substring = true)
        .performClick()
      composeRule.waitForIdle()

      // Verify Coach Keith's voice note transcribed into the feedback input and Save works
      composeRule.onNodeWithTag("coach_response_input_$assign1Id")
        .performScrollTo()
        .assertTextContains("Voice note from Coach Keith", substring = true)
      composeRule.onNodeWithTag("save_custom_coach_feedback_$assign1Id")
        .performScrollTo()
        .performClick()

      // 4. Verify transition to Coach responded with the transcribed coach note and Listen button
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("responded_card_$assign1Id").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("coach_response_${assign1Id}_0")
        .performScrollTo()
        .assertIsDisplayed()
      composeRule.onNodeWithText("Voice note from Coach Keith", substring = true)
        .performScrollTo()
        .assertIsDisplayed()
      composeRule.onNodeWithTag("listen_coach_response_${assign1Id}_0")
        .performScrollTo()
        .assertIsDisplayed()
        .performClick()
      composeRule.waitForIdle()
    }
  }
}

