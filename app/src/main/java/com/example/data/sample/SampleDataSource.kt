package com.example.data.sample

import com.example.BuildConfig
import com.example.data.local.AssignmentEntity
import com.example.data.local.ReflectionEntity
import com.example.data.model.CoachAssignment
import com.example.data.model.DevelopmentPlan
import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PracticeStep
import com.example.data.model.SessionStatus
import com.example.data.model.SimulatedFamilyProfile
import com.example.data.model.TodayFocus

object SampleDataSource {

  const val ASSIGNMENT_PASS_LOOK_DECIDE = "assign-pass-look-decide"
  const val ASSIGNMENT_CORNER_RELOCATION = "assign-corner-relocation"
  const val ASSIGNMENT_GENERAL_RHYTHM = "assign-general-rhythm"

  const val DRILL_PASS_LOOK_DECIDE = "demo-pass-look-decide"
  const val DRILL_CORNER_RELOCATION = "w2_d5_corner_relocation"
  const val DRILL_GENERAL_RHYTHM = "general_two_ball_eyes"

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

  val passLookDecideSteps = listOf(
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
  )

  val cornerRelocationSteps = listOf(
    PracticeStep(
      id = "read_drive",
      stepNumber = 1,
      phaseLabel = "READ",
      title = "Read the Drive",
      durationMinutes = 2,
      durationSeconds = 120,
      instruction = "Watch the ball-handler's first step toward the paint and notice if your defender turns their head.",
      coachCue = "Coach Keith: \"The moment your teammate touches the paint, see your defender's chest and eyes.\"",
      keyLookFor = "Spot whether your defender stays attached or steps in toward the lane.",
    ),
    PracticeStep(
      id = "relocate",
      stepNumber = 2,
      phaseLabel = "RELOCATE",
      title = "Slide to the Window",
      durationMinutes = 3,
      durationSeconds = 180,
      instruction = "Drift along the arc into the driver's direct sightline so they don't have to pass through traffic.",
      coachCue = "Coach Keith: \"Don't hide behind a defender. Move two steps toward the corner or slot where the driver can see your hands.\"",
      keyLookFor = "Create a clear passing triangle with feet pointed toward the rim.",
    ),
    PracticeStep(
      id = "catch_ready",
      stepNumber = 3,
      phaseLabel = "FINISH",
      title = "Catch Ready",
      durationMinutes = 3,
      durationSeconds = 180,
      instruction = "Arrive on balance with hands ready before the kick-out arrives, then shoot or swing the extra pass.",
      coachCue = "Coach Keith: \"Be set before the ball gets there—your shot starts with your feet.\"",
      keyLookFor = "Check that your hips are low and your eyes see both the rim and the weak-side help.",
    ),
  )

  val generalRhythmSteps = listOf(
    PracticeStep(
      id = "low_pounds",
      stepNumber = 1,
      phaseLabel = "POUND",
      title = "Low Pocket Pounds",
      durationMinutes = 2,
      durationSeconds = 120,
      instruction = "Dribble strong below your waist while keeping your chin up and scanning the opposite hoop.",
      coachCue = "Coach Keith: \"Keep your eyes on the court, not on your hand.\"",
      keyLookFor = "Feel the ball hit your fingertips at the same height every rep.",
    ),
    PracticeStep(
      id = "cross_scan",
      stepNumber = 2,
      phaseLabel = "SCAN",
      title = "Crossover & Call",
      durationMinutes = 2,
      durationSeconds = 120,
      instruction = "Change hands sharply and call out an open spot on the court before your next dribble.",
      coachCue = "Coach Keith: \"Quick handle, calm eyes—spot your next option before you move.\"",
      keyLookFor = "Keep your shoulders level through the change of direction.",
    ),
    PracticeStep(
      id = "retreat_look",
      stepNumber = 3,
      phaseLabel = "BALANCE",
      title = "Retreat & Look",
      durationMinutes = 2,
      durationSeconds = 120,
      instruction = "Take one hard retreat dribble to create space and hold a balanced stance with eyes forward.",
      coachCue = "Coach Keith: \"Space gives you time to see the whole floor.\"",
      keyLookFor = "Land on two balanced feet at the end of each retreat rep.",
    ),
  )

  val laneStarterCues = listOf(
    "My teammate started to drive, so I stayed wide to give them room.",
    "Help defender stepped up early and left the corner window open.",
    "Lane was clear and fit our spacing, so I cut to the rim.",
    "I ball-watched after the pass before reading the help defender.",
  )

  val cornerStarterCues = listOf(
    "My defender helped inside, so I slid to the corner window.",
    "I got set early with my hands ready for the kick-out.",
    "My defender stayed home, so I held the spacing lane.",
    "I drifted too late and got caught behind the defender.",
  )

  val generalStarterCues = listOf(
    "I kept my eyes up on the opposite rim through every crossover.",
    "I lost sight of the floor when I sped up my dribble.",
    "My retreat dribble gave me balance and time to look ahead.",
  )

  val assignmentOne = CoachAssignment(
    assignmentId = ASSIGNMENT_PASS_LOOK_DECIDE,
    sortOrder = 1,
    drillId = DRILL_PASS_LOOK_DECIDE,
    title = "Pass, Look, Decide",
    dayTag = "WEEK 2 · DAY 3",
    focusHeadline = developmentPlan.focus,
    totalMinutes = 8,
    coachCue = "After you pass, see what your teammate is doing. If they’re driving, give them room and stay available. If there’s a clear lane and the cut fits your team’s spacing, go.",
    reflectionPrompt = "What did you notice about the lane, and what did you choose to do?",
    starterCues = laneStarterCues,
    steps = passLookDecideSteps,
    isGeneralWork = false,
  )

  val assignmentTwo = CoachAssignment(
    assignmentId = ASSIGNMENT_CORNER_RELOCATION,
    sortOrder = 2,
    drillId = DRILL_CORNER_RELOCATION,
    title = "Corner Relocation Read",
    dayTag = "WEEK 2 · DAY 5",
    focusHeadline = "Drift into the shooter's window when a teammate touches the paint.",
    totalMinutes = 8,
    coachCue = "When your teammate gets two feet in the paint, slide out to their sightline instead of standing behind your defender.",
    reflectionPrompt = "Where did your defender move when your teammate drove, and how did you relocate?",
    starterCues = cornerStarterCues,
    steps = cornerRelocationSteps,
    isGeneralWork = false,
  )

  val generalWorkAssignment = CoachAssignment(
    assignmentId = ASSIGNMENT_GENERAL_RHYTHM,
    sortOrder = 99,
    drillId = DRILL_GENERAL_RHYTHM,
    title = "Eyes-Up Rhythm Dribble",
    dayTag = "GENERAL WORK",
    focusHeadline = "Keep your eyes scanning the floor while your handle stays low and steady.",
    totalMinutes = 6,
    coachCue = "While your personal focus is being built, lock in your ball-handling base with eyes up.",
    reflectionPrompt = "When did it feel easiest to keep your eyes up during the dribble?",
    starterCues = generalStarterCues,
    steps = generalRhythmSteps,
    isGeneralWork = true,
  )

  val todayFocus: TodayFocus = assignmentOne.toTodayFocus()

  fun defaultAssignmentEntities(nowMillis: Long): List<AssignmentEntity> =
    listOf(assignmentOne, assignmentTwo, generalWorkAssignment).map { a ->
      AssignmentEntity(
        assignmentId = a.assignmentId,
        sortOrder = a.sortOrder,
        drillId = a.drillId,
        title = a.title,
        dayTag = a.dayTag,
        focusHeadline = a.focusHeadline,
        totalMinutes = a.totalMinutes,
        coachCue = a.coachCue,
        reflectionPrompt = a.reflectionPrompt,
        isGeneralWork = a.isGeneralWork,
        stepsJson = AssignmentEntity.encodeSteps(a.steps),
        starterCuesJson = AssignmentEntity.encodeStrings(a.starterCues),
        hasActiveSession = false,
        currentStepId = a.steps.first().id,
        isReflectionStage = false,
        remainingSeconds = a.steps.first().durationSeconds,
        hasStartedTimer = false,
        reflectionDraft = "",
        updatedAtMillis = nowMillis,
      )
    }

  val weekTwoPlan = listOf(
    PlanSession(
      id = "w2_d1",
      assignmentId = null,
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
      assignmentId = ASSIGNMENT_PASS_LOOK_DECIDE,
      weekLabel = "Week 2 · Spacing After the Pass",
      dayLabel = "Day 3",
      title = "Pass, Look, Decide",
      durationMinutes = 8,
      focusSummary = developmentPlan.focus,
      status = SessionStatus.TODAY,
      isInteractivePractice = true,
    ),
    PlanSession(
      id = "w2_d5",
      assignmentId = ASSIGNMENT_CORNER_RELOCATION,
      weekLabel = "Week 2 · Spacing After the Pass",
      dayLabel = "Day 5",
      title = "Corner Relocation Read",
      durationMinutes = 8,
      focusSummary = "Drift into the shooter's window when a teammate touches the paint.",
      status = SessionStatus.UPCOMING,
      isInteractivePractice = true,
    ),
    PlanSession(
      id = "w2_d7",
      assignmentId = null,
      weekLabel = "Week 2 · Spacing After the Pass",
      dayLabel = "Day 7",
      title = "Closeout Split & Finish",
      durationMinutes = 10,
      focusSummary = "Read high-hand vs. low-hand closeouts at the three-point line.",
      status = SessionStatus.UPCOMING,
      isInteractivePractice = false,
    ),
  )

  val fixtureCoachLabel: String
    get() = if (BuildConfig.DEBUG) "Sample Coach Response · Coach Keith" else "Coach Keith"
  const val FIXTURE_DRAFT_TEXT = "My teammate started to drive, so I stayed wide"
  const val FIXTURE_SUBMITTED_TEXT = "My teammate started to drive, so I stayed wide to give them room."
  const val FIXTURE_COACH_RESPONSE =
    "You saw the drive and gave your teammate space. Next time, look again when the driver stops or passes out."
  const val FIXTURE_NEXT_ADJUSTMENT =
    "Walk through two reps. Say what changed in the lane before deciding whether to move."

  const val FIXTURE_ASSIGNMENT_TWO_SUBMITTED =
    "My defender stepped into the paint, so I slid to the corner window with my hands ready."
  const val FIXTURE_ASSIGNMENT_TWO_COACH_RESPONSE =
    "Great timing reading the help defender's chest, Maya. Getting your feet set early made the kick-out pass easy."
  const val FIXTURE_ASSIGNMENT_TWO_NEXT_REP =
    "Next rep, hold your corner spacing for a full beat before lifting if the driver picks up their dribble."

  fun initialSeedReflection(nowMillis: Long): ReflectionEntity =
    ReflectionEntity(
      assignmentId = "w2_d1_target_pass",
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

  fun buildSampleCoachFeedback(
    observation: String,
    assignmentId: String = ASSIGNMENT_PASS_LOOK_DECIDE,
  ): Pair<String, String> {
    if (assignmentId == ASSIGNMENT_CORNER_RELOCATION) {
      return FIXTURE_ASSIGNMENT_TWO_COACH_RESPONSE to FIXTURE_ASSIGNMENT_TWO_NEXT_REP
    }
    if (assignmentId == ASSIGNMENT_GENERAL_RHYTHM) {
      return (
        "Strong eyes-up focus, Maya. Keeping your chin level through every dribble builds court vision." to
          "Next rep, name an open teammate spot out loud right after your crossover."
      )
    }
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
