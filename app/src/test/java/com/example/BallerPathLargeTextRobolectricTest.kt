package com.example

import android.content.Context
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BallerPathDatabase
import com.example.data.repository.LocalBallerPathRepository
import com.example.data.sample.SampleDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-mdpi", fontScale = 1.5f)
class BallerPathLargeTextRobolectricTest {

  @get:Rule
  val composeRule = createEmptyComposeRule()

  @Before
  fun resetDemoDatabase() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = BallerPathDatabase.getInstance(context)
    LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
    ).resetDemoData()
  }

  @Test
  fun largeSystemText_smallPhone_keyActionsRemainReachableAndReadable() {
    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitForIdle()

      // Today primary action reachable and >= 48dp under 1.5x system font scale
      composeRule.onNodeWithTag("today_primary_action_button")
        .performScrollTo()
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .assertTextContains("Start practice", substring = true)
        .performClick()
      composeRule.waitForIdle()

      // Practice screen: Start/Pause and Next remain reachable under 1.5x font scale
      composeRule.onNodeWithTag("practice_timer_toggle")
        .performScrollTo()
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_timer_toggle").assertTextContains("Pause", substring = true)

      composeRule.onNodeWithTag("practice_next_button")
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .performClick()
      composeRule.waitForIdle()

      // Exit back to Today
      composeRule.onNodeWithTag("practice_back_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_back_button").performClick()
      composeRule.waitForIdle()

      // Switch to Awaiting Coach via Account and verify reveal button + Next Adjustment remain reachable
      composeRule.onNodeWithTag("nav_account").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("scenario_awaiting_coach").performScrollTo().performClick()
      composeRule.waitForIdle()

      composeRule.onNodeWithTag("nav_work_feedback").performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText(SampleDataSource.FIXTURE_SUBMITTED_TEXT).fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("reveal_sample_feedback_button")
        .performScrollTo()
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .performClick()

      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText(SampleDataSource.FIXTURE_COACH_RESPONSE).fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("next_adjustment_box").performScrollTo().assertIsDisplayed()
    }
  }
}
