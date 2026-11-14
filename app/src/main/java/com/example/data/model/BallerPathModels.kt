package com.example.data.model

data class PlayerProfile(
  val id: String,
  val displayName: String,
  val jerseyNumber: String,
  val grade: String,
  val position: String,
  val coachName: String,
  val demoBadgeLabel: String,
) {
  val positionAndGrade: String
    get() = "$position · $grade"

  val firstName: String
    get() = displayName.substringBefore(" ").ifBlank { displayName }
}

data class SimulatedFamilyProfile(
  val parentName: String,
  val relationshipLabel: String,
  val householdLabel: String,
  val scopeNotice: String,
)

data class PracticeStep(
  val id: String,
  val stepNumber: Int,
  val phaseLabel: String,
  val title: String,
  val durationMinutes: Int,
  val durationSeconds: Int,
  val instruction: String,
  val coachCue: String,
  val keyLookFor: String,
)

data class DevelopmentPlan(
  val focus: String,
  val why: String,
  val successStandard: String,
  val reviewLabel: String,
)

data class TodayFocus(
  val assignmentId: String = "assign-pass-look-decide",
  val drillId: String,
  val title: String,
  val dayTag: String,
  val headlineQuestion: String,
  val focusHeadline: String,
  val totalMinutes: Int,
  val coachCue: String,
  val reflectionPrompt: String,
  val steps: List<PracticeStep>,
)

data class CoachAssignment(
  val assignmentId: String,
  val sortOrder: Int,
  val drillId: String,
  val title: String,
  val dayTag: String,
  val headlineQuestion: String = "What should I work on today?",
  val focusHeadline: String,
  val totalMinutes: Int,
  val coachCue: String,
  val reflectionPrompt: String,
  val starterCues: List<String>,
  val steps: List<PracticeStep>,
  val isGeneralWork: Boolean = false,
  val hasActiveSession: Boolean = false,
  val currentStepId: String = steps.firstOrNull()?.id ?: "pass",
  val isReflectionStage: Boolean = false,
  val remainingSeconds: Int = steps.firstOrNull()?.durationSeconds ?: 120,
  val hasStartedTimer: Boolean = false,
  val reflectionDraft: String = "",
  val updatedAtMillis: Long = 0L,
) {
  val hasDraftOrSession: Boolean
    get() = reflectionDraft.isNotBlank() || isReflectionStage || hasActiveSession

  fun toTodayFocus(): TodayFocus =
    TodayFocus(
      assignmentId = assignmentId,
      drillId = drillId,
      title = title,
      dayTag = dayTag,
      headlineQuestion = headlineQuestion,
      focusHeadline = focusHeadline,
      totalMinutes = totalMinutes,
      coachCue = coachCue,
      reflectionPrompt = reflectionPrompt,
      steps = steps,
    )
}

enum class AssignmentStatus {
  READY,
  IN_PROGRESS,
  DRAFT,
  AWAITING_COACH,
  COACH_RESPONDED,
}

data class AssignmentWorkItem(
  val assignment: CoachAssignment,
  val submissions: List<PracticeReflection>,
) {
  val latestSubmission: PracticeReflection?
    get() = submissions.firstOrNull()

  val status: AssignmentStatus
    get() {
      val latest = latestSubmission
      return when {
        assignment.reflectionDraft.isNotBlank() || assignment.isReflectionStage -> AssignmentStatus.DRAFT
        assignment.hasActiveSession -> AssignmentStatus.IN_PROGRESS
        latest != null && !latest.isCoachResponseRevealed -> AssignmentStatus.AWAITING_COACH
        latest != null && latest.isCoachResponseRevealed -> AssignmentStatus.COACH_RESPONDED
        else -> AssignmentStatus.READY
      }
    }
}

enum class SetupStage(val id: String, val label: String) {
  SETUP_IN_PROGRESS("setup_in_progress", "Setup in progress"),
  PLAN_BEING_PREPARED("plan_being_prepared", "Plan being prepared"),
  WORK_READY("work_ready", "Work ready");

  companion object {
    fun fromId(id: String): SetupStage =
      entries.firstOrNull { it.id == id } ?: WORK_READY
  }
}

data class PlayerSetupState(
  val setupStage: SetupStage = SetupStage.WORK_READY,
  val hasGeneralWorkPublished: Boolean = true,
)

enum class SessionStatus {
  COMPLETED,
  TODAY,
  UPCOMING,
}

data class PlanSession(
  val id: String,
  val assignmentId: String? = null,
  val weekLabel: String,
  val dayLabel: String,
  val title: String,
  val durationMinutes: Int,
  val focusSummary: String,
  val status: SessionStatus,
  val isInteractivePractice: Boolean = false,
)

data class PracticeReflection(
  val id: Long = 0L,
  val assignmentId: String = "assign-pass-look-decide",
  val drillId: String,
  val drillTitle: String,
  val laneObservation: String,
  val readTag: String,
  val createdAtMillis: Long,
  val sampleCoachLabel: String,
  val sampleCoachResponse: String,
  val nextAdjustment: String,
  val isCoachResponseRevealed: Boolean = false,
  val isSampleSeed: Boolean = false,
)

enum class WorkFeedbackTab(val label: String) {
  TO_DO("To do"),
  AWAITING_COACH("Awaiting coach"),
  COACH_RESPONDED("Coach responded"),
}

enum class WorkflowScenarioId(val id: String, val label: String, val primaryActionLabel: String) {
  READY("ready", "Practice ready", "Start"),
  IN_PROGRESS("in_progress", "Practice in progress", "Resume"),
  DRAFT("draft", "Reflection draft saved", "Resume"),
  AWAITING_COACH("awaiting_coach", "Awaiting coach", "Open work"),
  FEEDBACK_READY("feedback_ready", "Sample feedback ready", "Read feedback"),
  MORE_WORK_READY("more_work_ready", "More work ready", "Start"),
}

data class PracticeWorkflowState(
  val assignmentId: String = "assign-pass-look-decide",
  val hasActiveSession: Boolean = false,
  val currentStepId: String = "pass",
  val isReflectionStage: Boolean = false,
  val remainingSeconds: Int = 120,
  val hasStartedTimer: Boolean = false,
  val reflectionDraft: String = "",
  val updatedAtMillis: Long = 0L,
)

data class ParentConsentState(
  val isAcknowledged: Boolean = false,
  val acknowledgedAtMillis: Long = 0L,
)

data class DailyDevelopmentStat(
  val dayOffsetFromToday: Int,
  val dayLabel: String,
  val fullDateLabel: String,
  val practiceMinutes: Int,
  val samplePreviewMinutes: Int = 0,
  val focusTag: String,
  val hasLiveSubmission: Boolean = false,
) {
  val isSampleReferenceDay: Boolean
    get() = !hasLiveSubmission && samplePreviewMinutes > 0

  val isRestDay: Boolean
    get() = !hasLiveSubmission && samplePreviewMinutes == 0
}

data class PracticeScopeSpec(
  val scopeId: String,
  val sessionId: String,
  val assignmentId: String?,
  val title: String,
  val weekDayBadge: String,
  val durationMinutes: Int,
  val tacticalObjective: String,
  val repBreakdown: String,
  val requiredFilmAngle: String,
  val successStandard: String,
  val status: String = "Active Scope",
)

data class VideoFilmSubmission(
  val videoId: String,
  val assignmentId: String,
  val drillTitle: String,
  val videoUri: String,
  val cameraAngleTag: String,
  val durationLabel: String,
  val playerNote: String,
  val status: String, // "awaiting_critique" or "critiqued"
  val coachCritique: String = "",
  val timestampNotes: String = "",
  val nextRepAdjustment: String = "",
  val createdAtMillis: Long = System.currentTimeMillis(),
) {
  val isCritiqued: Boolean
    get() = status == "critiqued" && coachCritique.isNotBlank()
}

data class TacticalChatMessage(
  val messageId: String,
  val senderRole: String, // "player" or "coach"
  val senderName: String,
  val text: String,
  val relatedTitle: String,
  val createdAtMillis: Long = System.currentTimeMillis(),
) {
  val isFromCoach: Boolean
    get() = senderRole == "coach"
}

data class WelcomeGoalChallengeState(
  val selectedGoal: String = "Create clean passing angles & hold corner spacing for 1.5s",
  val challengeQuestion: String = "Can you spot the help defender's chest before your ball-handler hits the paint on every rep today?",
  val isChallengeAccepted: Boolean = false,
  val completedMilestonesCount: Int = 2,
  val latestCelebrationHeadline: String = "Welcome back, Maya! Ready to lock in today's tactical focus?",
  val latestNextSentencePrompt: String = "Lock in your goal above, then start 'Pass, Look, Decide' or attach a court video for Coach Keith.",
  val courtStrengths: List<String> = listOf(
    "Eyes-Up Court Vision",
    "Weak-Side Corner Relocation",
    "On-Balance Catch & Shoot",
  ),
  val gradeBand: GradeBandPathway = GradeBandPathway.GRADES_6_8,
  val programTrack: TrainingProgramTrack = TrainingProgramTrack.REMOTE_ANYWHERE,
  val checkInState: CoachCheckInState = CoachCheckInState(),
  val roughWeekChoice: RoughWeekChoice = RoughWeekChoice.NONE,
  val teachBackEntry: TeachBackEntry = TeachBackEntry(),
  val stuckNote: StuckPrivateNote = StuckPrivateNote(),
  val gameTransferNotes: List<GameTransferNote> = listOf(
    GameTransferNote(
      id = "game_seed_1",
      dateLabel = "Sat · vs Metro East Falcons",
      opponent = "Metro East Falcons",
      carriedOver = "Kept my eyes on the weak-side corner before catching on the wing and hit two skip passes on time.",
      didntYet = "Picked up my dribble once against the high trap instead of retreating one dribble to create space.",
      feelLabel = "Good",
      coachMark = "carried",
      coachNote = "That weak-side scan before the catch is showing up in live quarters now. Keep the retreat dribble ready when trapped.",
      isTaggedWin = true,
    ),
  ),
  val firstLookAndCycle: FirstLookAndSinceWeek1State = FirstLookAndSinceWeek1State(),
)

enum class RoughWeekChoice(
  val key: String,
  val label: String,
  val note: String,
) {
  NONE(
    key = "none",
    label = "Normal Weekly Rhythm",
    note = "Standard weekly clip & reflection rhythm.",
  ),
  LIGHT_WEEK(
    key = "light",
    label = "A light week",
    note = "Skip clips this week. Nothing to make up.",
  ),
  RESET_FOCUS(
    key = "reset",
    label = "A reset",
    note = "Coach Keith picks a fresh, smaller focus.",
  ),
  TALK_TO_COACH(
    key = "talk",
    label = "Talk to my coach",
    note = "Send a quick note. Coach Keith writes back.",
  ),
}

data class TeachBackEntry(
  val whatWillDoDifferently: String = "I'll keep my eyes up to scan the weak-side corner before the ball hits my hands.",
  val whatWillNotice: String = "Fewer rushed passes and cleaner relocation windows against help defenders.",
  val isSubmittedByPlayer: Boolean = false,
  val coachMark: String = "got-it", // "unmarked", "got-it", "clarify"
  val coachClarifyNote: String = "Spot on, Maya. Eyes up before the catch gives you that extra half-second.",
)

data class StuckPrivateNote(
  val whatHappened: String = "",
  val whatWouldHelp: String = "",
  val howFeeling: String = "",
  val isSent: Boolean = false,
  val slaPromise: String = "Coach Keith reads this himself and writes back within 1 business day. He only loops in a parent if something needs a grown-up, and tells you first.",
  val coachReply: String = "",
)

data class GameTransferNote(
  val id: String,
  val dateLabel: String,
  val opponent: String,
  val carriedOver: String,
  val didntYet: String,
  val feelLabel: String, // "Rough", "Hard", "Okay", "Good", "Locked in"
  val coachMark: String = "unread", // "unread", "carried", "partly", "not-yet"
  val coachNote: String = "",
  val isTaggedWin: Boolean = false,
)

data class FirstLookAndSinceWeek1State(
  val firstLookSaw: String = "You already play unselfishly and want to move the ball. When we get your eyes up before the catch and hold corner spacing for one extra count, the game slows right down for you.",
  val firstLookSourceLabel: String = "Coach Keith · First Look from your intake clip & session",
  val inPersonSessionLine: String = "From your session on Tue at Storm",
  val inPersonSessionNote: String = "Handled the wing trap much cleaner once you took one retreat dribble instead of picking the ball up early.",
  val inPersonSessionFocus: String = "Scan the help defender's chest before the ball arrives",
  val inPersonSessionCue: String = "Chin up, catch on balance, hold the corner 1.5s",
  val week1ClipTitle: String = "Week 1 · Pass, Look, Decide (Initial Clip)",
  val week1ClipNote: String = "Caught the ball first, then looked at the rim — defense recovered into the passing lane.",
  val nowClipTitle: String = "Now (Week 4) · Pass, Look, Decide",
  val nowClipNote: String = "Eyes up before the catch; spotted the nail help defender and hit the corner relocation window cleanly.",
  val coachCycleComparisonNote: String = "4-Week Re-Check: Maya's pre-catch scan is now automatic. Next cycle builds off-the-catch closeout attacks.",
  val parentWeeklySentence: String = "Maya is working on scanning the weak-side help before the catch. Keith's one adjustment right now: hold the corner window for 1.5 seconds before lifting.",
)

enum class BallerPathLoopStage(
  val stepNumber: Int,
  val title: String,
  val tagline: String,
  val webAndAppConnection: String,
) {
  FOCUS(
    stepNumber = 1,
    title = "Focus",
    tagline = "One clear priority from Coach Keith",
    webAndAppConnection = "Published by Coach Keith on ballerpath.com and delivered as your single primary priority on Today.",
  ),
  PRACTICE(
    stepNumber = 2,
    title = "Practice",
    tagline = "Purposeful reps with a reason",
    webAndAppConnection = "Guided step-by-step on court in the app with optional timers and Scope of Work rep targets.",
  ),
  REFLECT(
    stepNumber = 3,
    title = "Reflect",
    tagline = "Share what you noticed & saw",
    webAndAppConnection = "Captured on court via text, on-device voice notes, or practice film clips synced for Coach Keith.",
  ),
  ADJUST(
    stepNumber = 4,
    title = "Adjust",
    tagline = "Coach Keith sets your next step",
    webAndAppConnection = "Coach Keith reviews your work and publishes your 'Try the next rep' adjustment and Teach-Back check.",
  ),
}

enum class GradeBandPathway(
  val id: String,
  val label: String,
  val focusSummary: String,
) {
  GRADES_4_5(
    id = "grades_4_5",
    label = "Grades 4–5 · Foundation & Footwork",
    focusSummary = "Eyes-up ball control, balanced stops, and spotting open teammates early.",
  ),
  GRADES_6_8(
    id = "grades_6_8",
    label = "Grades 6–8 · Middle School Game IQ",
    focusSummary = "Spacing after the pass, reading help defenders, and relocating into passing windows.",
  ),
  GRADES_9_12(
    id = "grades_9_12",
    label = "Grades 9–12 · High School Tactical",
    focusSummary = "Punishing closeout angles, nail-help reads, and second-side actions at game speed.",
  ),
}

enum class TrainingProgramTrack(
  val id: String,
  val label: String,
  val subtitle: String,
) {
  REMOTE_ANYWHERE(
    id = "remote_anywhere",
    label = "Baller Path Remote (Anywhere)",
    subtitle = "1-on-1 virtual coaching, weekly scopes of work, film breakdowns & Google Meet check-ins",
  ),
  STORM_IBL_ABL(
    id = "storm_ibl_abl",
    label = "Storm Basketball · IBL / ABL Track",
    subtitle = "Connects in-person Instructional / Advanced League play with Coach Keith's weekly home scopes",
  ),
  HYBRID_SKILL_LAB(
    id = "hybrid_skill_lab",
    label = "Small Group Skill Lab + Virtual",
    subtitle = "Pairs court training sessions with between-session film review and reflection loops",
  ),
}

data class CoachCheckInState(
  val selectedCheckInType: String = "Development Check-In (20 min · Google Meet)",
  val selectedAgendaTopics: List<String> = listOf(
    "Review 'Pass, Look, Decide' wing-angle film clip",
    "Confirm next week's spacing adjustment",
  ),
  val confidenceRating: Int = 4, // 1..5
  val familyQuestionNote: String = "Maya is seeing the nail help defender much earlier this week—ready to talk about baseline drive reads.",
  val isCheckInPrepSaved: Boolean = false,
)



