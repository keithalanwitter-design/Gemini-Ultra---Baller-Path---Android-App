package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
    LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
    ).resetDemoData()
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

    // 2. Open BallerPathDatabase v3 using MIGRATION_2_3 (no destructive migration!)
    val migratedDb = Room.databaseBuilder(context, BallerPathDatabase::class.java, dbName)
      .addMigrations(BallerPathDatabase.MIGRATION_2_3)
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
}
