package com.example

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BallerPathDatabase
import com.example.data.repository.LocalBallerPathRepository
import com.example.data.sample.SampleDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
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
    val repo = LocalBallerPathRepository(
      reflectionDao = db.reflectionDao(),
      practiceDraftDao = db.practiceDraftDao(),
      assignmentDao = db.assignmentDao(),
      setupStateDao = db.setupStateDao(),
      parentConsentDao = db.parentConsentDao(),
    )
    repo.resetDemoData()
    repo.clearParentConsentForTest()
  }

  private fun hasNonEmptyContentDescription(): SemanticsMatcher = SemanticsMatcher(
    description = "has non-empty contentDescription",
  ) { node ->
    val descriptions = node.config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }
    descriptions.any { it.isNotBlank() }
  }

  @Test
  fun largeSystemText_smallPhone_consentAccessibilityNavAndWorkBannerClearance() {
    val assign1Id = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE

    ActivityScenario.launch(MainActivity::class.java).use {
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("parent_consent_card").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.waitForIdle()

      // 1. Parent/Guardian consent screen readable & reachable under 1.5x font scale
      composeRule.onNodeWithTag("parent_consent_card").assertIsDisplayed()
      composeRule.onNodeWithText("Baller Path is for a parent or guardian of a player in grades 4–8.")
        .performScrollTo()
        .assertIsDisplayed()
      composeRule.onNodeWithTag("consent_checkbox_row")
        .performScrollTo()
        .assertHeightIsAtLeast(48.dp)
        .assert(hasNonEmptyContentDescription())
        .performClick()
      composeRule.onNodeWithTag("consent_continue_button")
        .performScrollTo()
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .assert(hasNonEmptyContentDescription())
        .performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("today_primary_action_button").fetchSemanticsNodes().isNotEmpty()
      }

      // 2. Bottom navigation items (including "Work +\nFeedback") readable & >= 48dp with TalkBack description
      composeRule.onNodeWithTag("nav_work_feedback")
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .assert(hasNonEmptyContentDescription())

      // 3. Today primary action reachable, >= 48dp, and has TalkBack description
      composeRule.onNodeWithTag("today_primary_action_button")
        .performScrollTo()
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .assert(hasNonEmptyContentDescription())
        .assertTextEquals("Start")
        .performClick()
      composeRule.waitForIdle()

      // 4. Practice screen: Start/Pause and Next remain reachable under 1.5x font scale with TalkBack descriptions
      composeRule.onNodeWithTag("practice_timer_toggle")
        .performScrollTo()
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .assert(hasNonEmptyContentDescription())
        .performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_timer_toggle").assertTextContains("Pause", substring = true)

      composeRule.onNodeWithTag("practice_next_button")
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .assert(hasNonEmptyContentDescription())
        .performClick()
      composeRule.waitForIdle()

      // Exit back to Today
      composeRule.onNodeWithTag("practice_back_button").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("practice_back_button").performClick()
      composeRule.waitForIdle()

      // 5. Switch to Awaiting Coach via Account and verify Privacy & Parent Permission card
      composeRule.onNodeWithTag("nav_account").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("privacy_consent_card").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("account_privacy_policy_url").performScrollTo().assertIsDisplayed()
      composeRule.onNodeWithTag("scenario_awaiting_coach").performScrollTo().performClick()
      composeRule.waitForIdle()

      // 6. Work + Feedback: verify reveal button, next rep, and bottom banner NOT hidden behind bottom nav
      composeRule.onNodeWithTag("nav_work_feedback").performClick()
      composeRule.waitForIdle()
      composeRule.onNodeWithTag("wf_tab_awaiting")
        .assert(hasNonEmptyContentDescription())
        .performClick()
      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithText(SampleDataSource.FIXTURE_SUBMITTED_TEXT).fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("reveal_feedback_$assign1Id")
        .performScrollTo()
        .assertIsDisplayed()
        .assertHeightIsAtLeast(48.dp)
        .assert(hasNonEmptyContentDescription())
        .performClick()

      composeRule.waitUntil(timeoutMillis = 5_000) {
        composeRule.onAllNodesWithTag("next_rep_${assign1Id}_0").fetchSemanticsNodes().isNotEmpty()
      }
      composeRule.onNodeWithTag("next_rep_${assign1Id}_0").performScrollTo().assertIsDisplayed()

      // Scroll LazyColumn to bottom Work + Feedback banner and verify it sits cleanly ABOVE the bottom navigation bar
      composeRule.onNodeWithTag("work_feedback_list")
        .performScrollToNode(hasTestTag("work_headline_visual"))
      composeRule.onNodeWithTag("work_headline_visual").assertIsDisplayed()
      val bannerBounds = composeRule.onNodeWithTag("work_headline_visual").getUnclippedBoundsInRoot()
      val navBarBounds = composeRule.onNodeWithTag("bottom_navigation_bar").getUnclippedBoundsInRoot()
      assertTrue(
        "Work + Feedback banner (${bannerBounds.bottom}) must stay above bottom nav bar (${navBarBounds.top})",
        bannerBounds.bottom <= navBarBounds.top + 1.dp,
      )
    }
  }
}
