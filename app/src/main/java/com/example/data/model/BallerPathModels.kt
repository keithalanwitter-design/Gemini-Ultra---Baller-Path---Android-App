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

