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

enum class SessionStatus {
  COMPLETED,
  TODAY,
  UPCOMING,
}

data class PlanSession(
  val id: String,
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

enum class WorkflowScenarioId(val id: String, val label: String, val primaryActionLabel: String) {
  READY("ready", "Practice ready", "Start practice"),
  IN_PROGRESS("in_progress", "Practice in progress", "Resume practice"),
  DRAFT("draft", "Reflection draft saved", "Finish reflection"),
  AWAITING_COACH("awaiting_coach", "Awaiting coach", "Open work"),
  FEEDBACK_READY("feedback_ready", "Sample feedback ready", "Read feedback"),
  MORE_WORK_READY("more_work_ready", "More work ready", "Start practice"),
}

data class PracticeWorkflowState(
  val hasActiveSession: Boolean = false,
  val currentStepId: String = "pass",
  val isReflectionStage: Boolean = false,
  val remainingSeconds: Int = 120,
  val hasStartedTimer: Boolean = false,
  val reflectionDraft: String = "",
  val updatedAtMillis: Long = 0L,
)
