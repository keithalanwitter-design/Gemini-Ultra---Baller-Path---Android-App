package com.example.data.sample

import com.example.BuildConfig
import com.example.data.local.ReflectionEntity
import com.example.data.model.DevelopmentPlan
import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PracticeStep
import com.example.data.model.SessionStatus
import com.example.data.model.SimulatedFamilyProfile
import com.example.data.model.TodayFocus

object SampleDataSource {

  const val DRILL_PASS_LOOK_DECIDE = "demo-pass-look-decide"

  val fictionalPlayer = PlayerProfile(
    id = "demo-maya",
    displayName = "Maya Brooks",
    jerseyNumber = "#14",
    grade = "7th grade",
    position = "Guard",
    coachName = "Coach Keith",
    demoBadgeLabel = if (BuildConfig.DEBUG) "Fictional Player · Sample Data" else "",
  )

  val simulatedFamily = SimulatedFamilyProfile(
    parentName = "Avery Pine",
    relationshipLabel = "Parent / Family",
    householdLabel = if (BuildConfig.DEBUG) "Simulated Family Profile · Local Demo" else "Baller Path Family",
    scopeNotice = if (BuildConfig.DEBUG) {
      "Authentication, billing, and real player accounts are intentionally kept outside this prototype."
    } else {
      "Your family account and weekly plan are guided by Coach Keith."
    },
  )

  val developmentPlan = DevelopmentPlan(
    focus = "Find useful space after you pass.",
    why = "Give a driving teammate room and stay available for the next pass.",
    successStandard = "Explain what you noticed and choose an action that fits the team’s spacing.",
    reviewLabel = "At your next check-in",
  )

  val todayFocus = TodayFocus(
    drillId = DRILL_PASS_LOOK_DECIDE,
    title = "Pass, Look, Decide",
    dayTag = "WEEK 2 · DAY 3",
    headlineQuestion = "What should I work on today?",
    focusHeadline = developmentPlan.focus,
    totalMinutes = 8,
    coachCue = "After you pass, see what your teammate is doing. If they’re driving, give them room and stay available. If there’s a clear lane and the cut fits your team’s spacing, go.",
    reflectionPrompt = "What did you notice about the lane, and what did you choose to do?",
    steps = listOf(
      PracticeStep(
        id = "pass",
        stepNumber = 1,
        phaseLabel = "PASS",
        title = "Pass",
        durationMinutes = 2,
        durationSeconds = 120,
        instruction = "Make the pass, then get your eyes back to the play.",
        coachCue = "Coach Keith: \"Make the pass, then get your eyes back to the play.\"",
        keyLookFor = "Square your vision to the ball and the paint right after the release.",
      ),
      PracticeStep(
        id = "look",
        stepNumber = 2,
        phaseLabel = "LOOK",
        title = "Look",
        durationMinutes = 3,
        durationSeconds = 180,
        instruction = "See where your teammate is going and where help is standing.",
        coachCue = "Coach Keith: \"After you pass, see what your teammate is doing. If they’re driving, give them room and stay available. If there’s a clear lane and the cut fits your team’s spacing, go.\"",
        keyLookFor = "Notice whether your teammate attacks downhill or holds the ball with an open lane.",
      ),
      PracticeStep(
        id = "decide",
        stepNumber = 3,
        phaseLabel = "DECIDE",
        title = "Decide",
        durationMinutes = 3,
        durationSeconds = 180,
        instruction = "Cut when the lane and your spacing allow it. If your teammate drives, give them room and stay available.",
        coachCue = "Coach Keith: \"Cut when the lane and your spacing allow it. If your teammate drives, give them room and stay available.\"",
        keyLookFor = "Hold your spacing window for a kick-out or cut cleanly to the rim.",
      ),
    ),
  )

  val weekTwoPlan = listOf(
    PlanSession(
      id = "w2_d1",
      weekLabel = "Week 2 · Spacing After the Pass",
      dayLabel = "Day 1",
      title = "Target Pass & Eyes Up",
      durationMinutes = 7,
      focusSummary = "Keep your chin level after every catch so you spot teammates early.",
      status = SessionStatus.COMPLETED,
      isInteractivePractice = false,
    ),
    PlanSession(
      id = "w2_d3",
      weekLabel = "Week 2 · Spacing After the Pass",
      dayLabel = "Day 3 · Today",
      title = "Pass, Look, Decide",
      durationMinutes = 8,
      focusSummary = developmentPlan.focus,
      status = SessionStatus.TODAY,
      isInteractivePractice = true,
    ),
    PlanSession(
      id = "w2_d5",
      weekLabel = "Week 2 · Spacing After the Pass",
      dayLabel = "Day 5",
      title = "Corner Relocation Read",
      durationMinutes = 8,
      focusSummary = "Drift into the shooter's window when a teammate touches the paint.",
      status = SessionStatus.UPCOMING,
      isInteractivePractice = false,
    ),
    PlanSession(
      id = "w2_d7",
      weekLabel = "Week 2 · Spacing After the Pass",
      dayLabel = "Day 7",
      title = "Closeout Split & Finish",
      durationMinutes = 10,
      focusSummary = "Read high-hand vs. low-hand closeouts at the three-point line.",
      status = SessionStatus.UPCOMING,
      isInteractivePractice = false,
    ),
  )

  val laneStarterCues = listOf(
    "My teammate started to drive, so I stayed wide to give them room.",
    "Help defender stepped up early and left the corner window open.",
    "Lane was clear and fit our spacing, so I cut to the rim.",
    "I ball-watched after the pass before reading the help defender.",
  )

  val fixtureCoachLabel: String
    get() = if (BuildConfig.DEBUG) "Sample Coach Response · Coach Keith" else "Coach Keith"
  const val FIXTURE_DRAFT_TEXT = "My teammate started to drive, so I stayed wide"
  const val FIXTURE_SUBMITTED_TEXT = "My teammate started to drive, so I stayed wide to give them room."
  const val FIXTURE_COACH_RESPONSE =
    "You saw the drive and gave your teammate space. Next time, look again when the driver stops or passes out."
  const val FIXTURE_NEXT_ADJUSTMENT =
    "Walk through two reps. Say what changed in the lane before deciding whether to move."

  fun initialSeedReflection(nowMillis: Long): ReflectionEntity =
    ReflectionEntity(
      drillId = "w2_d1_target_pass",
      drillTitle = "Target Pass & Eyes Up",
      laneObservation = "When I lifted my chin right on the catch, I saw the cutter cross the free-throw line before my defender recovered.",
      readTag = if (BuildConfig.DEBUG) "Day 1 Sample Entry" else "Day 1 Check-In",
      createdAtMillis = nowMillis - 2L * 24L * 60L * 60L * 1000L,
      sampleCoachLabel = fixtureCoachLabel,
      sampleCoachResponse = "Great catch-and-scan timing, Maya. Raising your eyes before squaring up bought you an extra half-second to see the middle seam.",
      nextAdjustment = "Hold your triple-threat balance for a half-beat longer so your eyes sell the pass before you drive.",
      isCoachResponseRevealed = true,
      isSampleSeed = true,
    )

  fun buildSampleCoachFeedback(observation: String): Pair<String, String> {
    val normalized = observation.lowercase()
    return when {
      "drive" in normalized || "drove" in normalized || "wide" in normalized || "room" in normalized -> {
        FIXTURE_COACH_RESPONSE to FIXTURE_NEXT_ADJUSTMENT
      }
      "stepped" in normalized || "help" in normalized || "collapsed" in normalized || "corner" in normalized -> {
        "Strong lane recognition, Maya. Catching the help defender early lets you relocate into the exact passing window." to
          "Walk through two reps. Say where the help defender moved before choosing to cut or space."
      }
      "cut" in normalized || "open" in normalized || "lane" in normalized || "clear" in normalized -> {
        "Nice patience reading the lane, Maya. Cutting only when the team's spacing allows it keeps the paint clean." to
          "Call out the open window out loud after your pass before deciding whether to cut."
      }
      else -> {
        FIXTURE_COACH_RESPONSE to FIXTURE_NEXT_ADJUSTMENT
      }
    }
  }
}
