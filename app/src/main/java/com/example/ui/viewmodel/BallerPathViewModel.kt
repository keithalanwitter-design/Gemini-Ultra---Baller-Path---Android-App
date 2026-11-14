package com.example.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.data.model.AssignmentStatus
import com.example.data.model.AssignmentWorkItem
import com.example.data.model.BallerPathLoopStage
import com.example.data.model.CoachAssignment
import com.example.data.model.DailyDevelopmentStat
import com.example.data.model.DevelopmentPlan
import com.example.data.model.GradeBandPathway
import com.example.data.model.ParentConsentState
import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PlayerSetupState
import com.example.data.model.PracticeReflection
import com.example.data.model.PracticeScopeSpec
import com.example.data.model.SetupStage
import com.example.data.model.SimulatedFamilyProfile
import com.example.data.model.TacticalChatMessage
import com.example.data.model.TodayFocus
import com.example.data.model.TrainingProgramTrack
import com.example.data.model.VideoFilmSubmission
import com.example.data.model.WelcomeGoalChallengeState
import com.example.data.model.WorkflowScenarioId
import com.example.data.repository.BallerPathRepository
import com.example.data.sample.SampleDataSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticeSessionUiState(
  val assignmentId: String = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
  val hasActiveSession: Boolean = false,
  val currentStepIndex: Int = 0,
  val isReflectionStage: Boolean = false,
  val remainingSeconds: Int = SampleDataSource.assignmentOne.steps.first().durationSeconds,
  val isTimerRunning: Boolean = false,
  val hasStartedTimer: Boolean = false,
  val reflectionDraft: String = "",
  val validationError: String? = null,
  val isSubmitting: Boolean = false,
)

data class BallerPathUiState(
  val isLoading: Boolean = true,
  val player: PlayerProfile = SampleDataSource.fictionalPlayer,
  val family: SimulatedFamilyProfile = SampleDataSource.simulatedFamily,
  val developmentPlan: DevelopmentPlan = SampleDataSource.developmentPlan,
  val assignments: List<CoachAssignment> = listOf(
    SampleDataSource.assignmentOne,
    SampleDataSource.assignmentTwo,
    SampleDataSource.generalWorkAssignment,
  ),
  val setupState: PlayerSetupState = PlayerSetupState(),
  val parentConsent: ParentConsentState = ParentConsentState(),
  val planSessions: List<PlanSession> = SampleDataSource.weekTwoPlan,
  val reflections: List<PracticeReflection> = emptyList(),
  val practice: PracticeSessionUiState = PracticeSessionUiState(),
  val demoStatusMessage: String? = null,
  val isMoreWorkReadyPreview: Boolean = false,
  val welcomeGoalState: WelcomeGoalChallengeState = WelcomeGoalChallengeState(),
  val practiceScopes: List<PracticeScopeSpec> = SampleDataSource.defaultPracticeScopes,
  val videoSubmissions: List<VideoFilmSubmission> = SampleDataSource.defaultVideoSubmissions(),
  val tacticalMessages: List<TacticalChatMessage> = SampleDataSource.defaultTacticalMessages(),
  val isCoachPortalMode: Boolean = false,
) {
  val personalWorkItems: List<AssignmentWorkItem>
    get() = assignments
      .filter { !it.isGeneralWork }
      .sortedBy { it.sortOrder }
      .map { assignment ->
        val subs = reflections.filter { !it.isSampleSeed && it.assignmentId == assignment.assignmentId }
        AssignmentWorkItem(assignment = assignment, submissions = subs)
      }

  val generalWorkItem: AssignmentWorkItem?
    get() = assignments
      .firstOrNull { it.isGeneralWork }
      ?.let { assignment ->
        val subs = reflections.filter { !it.isSampleSeed && it.assignmentId == assignment.assignmentId }
        AssignmentWorkItem(assignment = assignment, submissions = subs)
      }

  val seedReflections: List<PracticeReflection>
    get() = reflections.filter { it.isSampleSeed }

  // B1 & B2 Today selection priority
  val todayAssignmentItem: AssignmentWorkItem?
    get() = when (setupState.setupStage) {
      SetupStage.SETUP_IN_PROGRESS -> null
      SetupStage.PLAN_BEING_PREPARED -> {
        if (setupState.hasGeneralWorkPublished) generalWorkItem else null
      }
      SetupStage.WORK_READY -> {
        val inProgress = personalWorkItems.firstOrNull { it.assignment.hasDraftOrSession }
        val nextReady = personalWorkItems.firstOrNull { !it.assignment.hasDraftOrSession && it.submissions.isEmpty() }
        inProgress ?: nextReady
      }
    }

  val todayAssignment: CoachAssignment?
    get() = todayAssignmentItem?.assignment

  val todayFocus: TodayFocus
    get() = todayAssignment?.toTodayFocus()
      ?: personalWorkItems.firstOrNull()?.assignment?.toTodayFocus()
      ?: SampleDataSource.todayFocus

  val activePracticeAssignment: CoachAssignment
    get() = assignments.firstOrNull { it.assignmentId == practice.assignmentId }
      ?: todayAssignment
      ?: SampleDataSource.assignmentOne

  val isAllPersonalWorkSubmitted: Boolean
    get() = setupState.setupStage == SetupStage.WORK_READY &&
      personalWorkItems.isNotEmpty() &&
      personalWorkItems.all { it.submissions.isNotEmpty() && !it.assignment.hasDraftOrSession }

  val hasAnySubmittedPersonalWork: Boolean
    get() = personalWorkItems.any { it.submissions.isNotEmpty() }

  val hasAnyAwaitingPersonalWork: Boolean
    get() = personalWorkItems.any { it.status == AssignmentStatus.AWAITING_COACH }

  val hasAnyRevealedPersonalFeedback: Boolean
    get() = personalWorkItems.any { item -> item.submissions.any { it.isCoachResponseRevealed } }

  val latestSubmittedReflection: PracticeReflection?
    get() = reflections.firstOrNull { !it.isSampleSeed }

  val hasSubmittedTodayReflection: Boolean
    get() = isAllPersonalWorkSubmitted

  val todayPrimaryActionLabel: String?
    get() = when (setupState.setupStage) {
      SetupStage.SETUP_IN_PROGRESS -> null
      SetupStage.PLAN_BEING_PREPARED -> {
        val general = todayAssignment
        when {
          general == null -> null
          general.hasDraftOrSession -> "Resume"
          else -> "Start"
        }
      }
      SetupStage.WORK_READY -> {
        val target = todayAssignment
        when {
          target != null && target.hasDraftOrSession -> "Resume"
          target != null -> "Start"
          hasAnyRevealedPersonalFeedback && !hasAnyAwaitingPersonalWork -> "Read feedback"
          else -> "Open work"
        }
      }
    }

  val currentScenario: WorkflowScenarioId
    get() {
      val target = todayAssignment
      return when {
        target != null && (target.reflectionDraft.isNotBlank() || target.isReflectionStage) ->
          WorkflowScenarioId.DRAFT
        target != null && target.hasActiveSession ->
          WorkflowScenarioId.IN_PROGRESS
        isMoreWorkReadyPreview && target != null ->
          WorkflowScenarioId.MORE_WORK_READY
        target != null ->
          WorkflowScenarioId.READY
        hasAnyAwaitingPersonalWork ->
          WorkflowScenarioId.AWAITING_COACH
        hasAnyRevealedPersonalFeedback ->
          WorkflowScenarioId.FEEDBACK_READY
        else ->
          WorkflowScenarioId.READY
      }
    }

  val activeStepTitle: String
    get() {
      val steps = activePracticeAssignment.steps
      val idx = practice.currentStepIndex.coerceIn(0, steps.lastIndex)
      return steps[idx].title
    }

  val todayStepTitle: String
    get() {
      val target = todayAssignment ?: return activeStepTitle
      val steps = target.steps
      val idx = steps.indexOfFirst { it.id == target.currentStepId }.let { if (it >= 0) it else 0 }
      return steps[idx].title
    }

  val todoWorkItems: List<AssignmentWorkItem>
    get() = when (setupState.setupStage) {
      SetupStage.SETUP_IN_PROGRESS -> emptyList()
      SetupStage.PLAN_BEING_PREPARED -> {
        val general = generalWorkItem
        if (setupState.hasGeneralWorkPublished && general != null &&
          (general.submissions.isEmpty() || general.assignment.hasDraftOrSession)
        ) {
          listOf(general)
        } else {
          emptyList()
        }
      }
      SetupStage.WORK_READY -> {
        personalWorkItems.filter { it.submissions.isEmpty() || it.assignment.hasDraftOrSession }
      }
    }

  val awaitingCoachItems: List<AssignmentWorkItem>
    get() {
      if (setupState.setupStage == SetupStage.SETUP_IN_PROGRESS) return emptyList()
      val allItems = if (setupState.setupStage == SetupStage.PLAN_BEING_PREPARED) {
        listOfNotNull(generalWorkItem)
      } else {
        personalWorkItems + listOfNotNull(generalWorkItem).filter { it.submissions.isNotEmpty() }
      }
      return allItems.filter { it.latestSubmission?.isCoachResponseRevealed == false }
    }

  val coachRespondedItems: List<AssignmentWorkItem>
    get() {
      if (setupState.setupStage == SetupStage.SETUP_IN_PROGRESS) return emptyList()
      val allItems = if (setupState.setupStage == SetupStage.PLAN_BEING_PREPARED) {
        listOfNotNull(generalWorkItem)
      } else {
        personalWorkItems + listOfNotNull(generalWorkItem).filter { it.submissions.isNotEmpty() }
      }
      return allItems
        .mapNotNull { item ->
          val revealed = item.submissions.filter { it.isCoachResponseRevealed }
          if (revealed.isNotEmpty()) {
            AssignmentWorkItem(assignment = item.assignment, submissions = revealed)
          } else {
            null
          }
        }
    }

  val lastSevenDayStats: List<DailyDevelopmentStat>
    get() = SampleDataSource.buildLastSevenDayStats(
      assignments = assignments,
      reflections = reflections,
    )

  val activeLoopStage: BallerPathLoopStage
    get() = when (currentScenario) {
      WorkflowScenarioId.READY, WorkflowScenarioId.MORE_WORK_READY -> BallerPathLoopStage.FOCUS
      WorkflowScenarioId.IN_PROGRESS -> BallerPathLoopStage.PRACTICE
      WorkflowScenarioId.DRAFT, WorkflowScenarioId.AWAITING_COACH -> BallerPathLoopStage.REFLECT
      WorkflowScenarioId.FEEDBACK_READY -> BallerPathLoopStage.ADJUST
    }
}

class BallerPathViewModel(
  private val repository: BallerPathRepository,
  private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

  private companion object {
    const val KEY_ACTIVE_ASSIGNMENT_ID = "practice_active_assignment_id"
    const val KEY_HAS_ACTIVE = "practice_has_active"
    const val KEY_STEP_INDEX = "practice_step_index"
    const val KEY_IS_REFLECTION = "practice_is_reflection"
    const val KEY_REMAINING_SECONDS = "practice_remaining_seconds"
    const val KEY_HAS_STARTED_TIMER = "practice_has_started_timer"
    const val KEY_REFLECTION_DRAFT = "practice_reflection_draft"
  }

  private val latestAssignments = MutableStateFlow(
    listOf(
      SampleDataSource.assignmentOne,
      SampleDataSource.assignmentTwo,
      SampleDataSource.generalWorkAssignment,
    )
  )
  private val practiceState = MutableStateFlow(restorePracticeFromSavedStateHandle())
  private val demoStatusMessage = MutableStateFlow<String?>(null)
  private val moreWorkReadyPreview = MutableStateFlow(false)
  private val seeded = MutableStateFlow(false)
  private val welcomeGoalState = MutableStateFlow(WelcomeGoalChallengeState())
  private val practiceScopes = MutableStateFlow(SampleDataSource.defaultPracticeScopes)
  private val videoSubmissions = MutableStateFlow(SampleDataSource.defaultVideoSubmissions())
  private val tacticalMessages = MutableStateFlow(SampleDataSource.defaultTacticalMessages())
  private val isCoachPortalMode = MutableStateFlow(false)
  private var timerJob: Job? = null

  init {
    viewModelScope.launch {
      repository.ensureSeeded()
      val storedAssignments = repository.observeAssignments().first()
      latestAssignments.value = storedAssignments
      val storedSetup = repository.observeSetupState().first()
      val storedReflections = repository.observeReflections().first()

      val savedAssignmentId = savedStateHandle.get<String>(KEY_ACTIVE_ASSIGNMENT_ID)
      val targetAssignment = when {
        savedAssignmentId != null && savedStateHandle.contains(KEY_STEP_INDEX) -> {
          storedAssignments.firstOrNull { it.assignmentId == savedAssignmentId }
        }
        storedSetup.setupStage == SetupStage.PLAN_BEING_PREPARED && storedSetup.hasGeneralWorkPublished -> {
          storedAssignments.firstOrNull { it.isGeneralWork }
        }
        else -> {
          val personal = storedAssignments.filter { !it.isGeneralWork }.sortedBy { it.sortOrder }
          val submittedIds = storedReflections.filter { !it.isSampleSeed }.map { it.assignmentId }.toSet()
          personal.firstOrNull { it.hasDraftOrSession }
            ?: personal.firstOrNull { it.assignmentId !in submittedIds }
            ?: personal.firstOrNull()
        }
      }

      if (targetAssignment != null) {
        val shouldHydrateFromRoom = !savedStateHandle.contains(KEY_STEP_INDEX) ||
          targetAssignment.hasDraftOrSession
        if (shouldHydrateFromRoom) {
          val hydrated = targetAssignment.toPracticeUiState()
          practiceState.value = hydrated
          persistToSavedStateHandle(hydrated)
        }
      }
      seeded.value = true
    }

    viewModelScope.launch {
      repository.observeAssignments().collect { list ->
        latestAssignments.value = list
      }
    }
  }

  private val overhaulStateFlow = combine(
    welcomeGoalState,
    practiceScopes,
    videoSubmissions,
    tacticalMessages,
    isCoachPortalMode,
  ) { welcome, scopes, videos, messages, coachMode ->
    listOf(welcome, scopes, videos, messages, coachMode)
  }

  val uiState: StateFlow<BallerPathUiState> = combine(
    repository.observePlayerProfile(),
    repository.observeFamilyProfile(),
    repository.observeDevelopmentPlan(),
    repository.observeAssignments(),
    repository.observeSetupState(),
    repository.observePlanSessions(),
    repository.observeReflections(),
    combine(
      practiceState,
      repository.observeParentConsent(),
      demoStatusMessage,
      moreWorkReadyPreview,
      seeded,
    ) { practice, consent, statusMsg, moreWork, isSeeded ->
      listOf(practice, consent, statusMsg, moreWork, isSeeded)
    },
    overhaulStateFlow,
  ) { values ->
    @Suppress("UNCHECKED_CAST")
    val player = values[0] as PlayerProfile
    val family = values[1] as SimulatedFamilyProfile
    val planInfo = values[2] as DevelopmentPlan
    @Suppress("UNCHECKED_CAST")
    val assignments = values[3] as List<CoachAssignment>
    val setupState = values[4] as PlayerSetupState
    @Suppress("UNCHECKED_CAST")
    val planSessions = values[5] as List<PlanSession>
    @Suppress("UNCHECKED_CAST")
    val reflections = values[6] as List<PracticeReflection>
    @Suppress("UNCHECKED_CAST")
    val extra = values[7] as List<Any?>
    val practice = extra[0] as PracticeSessionUiState
    val parentConsent = extra[1] as ParentConsentState
    val statusMsg = extra[2] as String?
    val isMoreWork = extra[3] as Boolean
    val isSeeded = extra[4] as Boolean
    @Suppress("UNCHECKED_CAST")
    val overhaul = values[8] as List<Any?>
    val welcome = overhaul[0] as WelcomeGoalChallengeState
    @Suppress("UNCHECKED_CAST")
    val scopes = overhaul[1] as List<PracticeScopeSpec>
    @Suppress("UNCHECKED_CAST")
    val videos = overhaul[2] as List<VideoFilmSubmission>
    @Suppress("UNCHECKED_CAST")
    val messages = overhaul[3] as List<TacticalChatMessage>
    val coachMode = overhaul[4] as Boolean

    BallerPathUiState(
      isLoading = !isSeeded,
      player = player,
      family = family,
      developmentPlan = planInfo,
      assignments = assignments,
      setupState = setupState,
      parentConsent = parentConsent,
      planSessions = planSessions,
      reflections = reflections,
      practice = practice,
      demoStatusMessage = statusMsg,
      isMoreWorkReadyPreview = isMoreWork,
      welcomeGoalState = welcome,
      practiceScopes = scopes,
      videoSubmissions = videos,
      tacticalMessages = messages,
      isCoachPortalMode = coachMode,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = BallerPathUiState(),
  )

  fun openAssignmentPractice(assignmentId: String, forceRestart: Boolean = false) {
    moreWorkReadyPreview.value = false
    stopTimerInternal(persistToRoom = false)
    val assignment = assignmentForId(assignmentId)
    val nextState = if (!forceRestart && assignment.hasDraftOrSession) {
      assignment.toPracticeUiState()
    } else {
      PracticeSessionUiState(
        assignmentId = assignment.assignmentId,
        hasActiveSession = true,
        currentStepIndex = 0,
        isReflectionStage = false,
        remainingSeconds = assignment.steps.first().durationSeconds,
        isTimerRunning = false,
        hasStartedTimer = false,
        reflectionDraft = "",
        validationError = null,
        isSubmitting = false,
      )
    }
    practiceState.value = nextState
    persistPracticeEverywhere(nextState)
  }

  fun startOrResumePracticeSession() {
    val current = practiceState.value
    val currentAssignment = assignmentForId(current.assignmentId)
    if (currentAssignment.hasDraftOrSession ||
      current.hasActiveSession ||
      current.isReflectionStage ||
      current.reflectionDraft.isNotBlank()
    ) {
      return
    }
    openAssignmentPractice(currentAssignment.assignmentId, forceRestart = true)
  }

  fun startNewPracticeSession(assignmentId: String = practiceState.value.assignmentId) {
    openAssignmentPractice(assignmentId, forceRestart = true)
  }

  fun advancePracticeStage() {
    stopTimerInternal(persistToRoom = false)
    val current = practiceState.value
    val assignment = assignmentForId(current.assignmentId)
    val steps = assignment.steps
    if (!current.isReflectionStage) {
      if (current.currentStepIndex < steps.lastIndex) {
        val nextIdx = current.currentStepIndex + 1
        updatePracticeState {
          it.copy(
            hasActiveSession = true,
            currentStepIndex = nextIdx,
            remainingSeconds = steps[nextIdx].durationSeconds,
            isTimerRunning = false,
            hasStartedTimer = false,
            validationError = null,
          )
        }
      } else {
        updatePracticeState {
          it.copy(
            hasActiveSession = true,
            isReflectionStage = true,
            isTimerRunning = false,
            validationError = null,
          )
        }
      }
    }
  }

  /**
   * Steps backward within the active assignment's drill.
   * Returns true if handled internally, or false if on Step 1 and ready to exit to Today.
   */
  fun stepBackInPractice(): Boolean {
    stopTimerInternal(persistToRoom = true)
    val current = practiceState.value
    val steps = assignmentForId(current.assignmentId).steps
    return when {
      current.isReflectionStage -> {
        updatePracticeState {
          it.copy(
            hasActiveSession = true,
            isReflectionStage = false,
            currentStepIndex = steps.lastIndex,
            remainingSeconds = steps.last().durationSeconds,
            isTimerRunning = false,
            hasStartedTimer = false,
            validationError = null,
          )
        }
        true
      }
      current.currentStepIndex > 0 -> {
        val prevIdx = current.currentStepIndex - 1
        updatePracticeState {
          it.copy(
            hasActiveSession = true,
            currentStepIndex = prevIdx,
            remainingSeconds = steps[prevIdx].durationSeconds,
            isTimerRunning = false,
            hasStartedTimer = false,
            validationError = null,
          )
        }
        true
      }
      else -> {
        if (!current.hasStartedTimer && current.reflectionDraft.isBlank()) {
          updatePracticeState {
            it.copy(hasActiveSession = false)
          }
        }
        false
      }
    }
  }

  fun toggleTimer() {
    val current = practiceState.value
    if (current.isTimerRunning) {
      stopTimerInternal(persistToRoom = true)
    } else {
      val steps = assignmentForId(current.assignmentId).steps
      val stepSeconds = steps[current.currentStepIndex.coerceIn(0, steps.lastIndex)].durationSeconds
      val startSeconds = if (current.remainingSeconds <= 0) stepSeconds else current.remainingSeconds
      updatePracticeState {
        it.copy(
          hasActiveSession = true,
          remainingSeconds = startSeconds,
          isTimerRunning = true,
          hasStartedTimer = true,
        )
      }
      timerJob?.cancel()
      timerJob = viewModelScope.launch {
        while (practiceState.value.isTimerRunning && practiceState.value.remainingSeconds > 0) {
          delay(1_000L)
          updatePracticeState { state ->
            val nextSec = (state.remainingSeconds - 1).coerceAtLeast(0)
            state.copy(
              remainingSeconds = nextSec,
              isTimerRunning = nextSec > 0 && state.isTimerRunning,
            )
          }
        }
      }
    }
  }

  fun resetCurrentStepTimer() {
    stopTimerInternal(persistToRoom = false)
    val current = practiceState.value
    val steps = assignmentForId(current.assignmentId).steps
    val stepSeconds = steps[current.currentStepIndex.coerceIn(0, steps.lastIndex)].durationSeconds
    updatePracticeState {
      it.copy(
        remainingSeconds = stepSeconds,
        isTimerRunning = false,
        hasStartedTimer = false,
      )
    }
  }

  fun onAppBackgrounded() {
    if (practiceState.value.isTimerRunning) {
      stopTimerInternal(persistToRoom = true)
    } else if (practiceState.value.hasActiveSession || practiceState.value.reflectionDraft.isNotBlank()) {
      persistPracticeEverywhere(practiceState.value)
    }
  }

  fun updateReflectionDraft(text: String) {
    updatePracticeState {
      it.copy(
        hasActiveSession = true,
        isReflectionStage = true,
        reflectionDraft = text,
        validationError = null,
      )
    }
  }

  fun applyStarterCue(cue: String) {
    updatePracticeState { current ->
      val updatedText = if (current.reflectionDraft.isBlank()) {
        cue
      } else if (current.reflectionDraft.contains(cue, ignoreCase = true)) {
        current.reflectionDraft
      } else {
        "${current.reflectionDraft.trimEnd()}. $cue"
      }
      current.copy(
        hasActiveSession = true,
        isReflectionStage = true,
        reflectionDraft = updatedText,
        validationError = null,
      )
    }
  }

  fun submitReflection(onSubmitted: () -> Unit) {
    val current = practiceState.value
    val draft = current.reflectionDraft.trim()
    if (draft.isEmpty()) {
      practiceState.update {
        it.copy(validationError = "Write one sentence about what you noticed before submitting.")
      }
      return
    }
    if (current.isSubmitting) return

    practiceState.update { it.copy(isSubmitting = true, validationError = null) }
    viewModelScope.launch {
      val assignment = assignmentForId(current.assignmentId)
      repository.submitReflection(
        assignmentId = assignment.assignmentId,
        drillId = assignment.drillId,
        drillTitle = assignment.title,
        laneObservation = draft,
        readTag = "${assignment.title} · ${assignment.totalMinutes} min",
      )
      val nextTarget = latestAssignments.value
        .filter { !it.isGeneralWork && it.assignmentId != assignment.assignmentId }
        .sortedBy { it.sortOrder }
        .firstOrNull { it.hasDraftOrSession }
        ?: latestAssignments.value
          .filter { !it.isGeneralWork && it.assignmentId != assignment.assignmentId }
          .sortedBy { it.sortOrder }
          .firstOrNull()
        ?: assignment

      val cleanState = PracticeSessionUiState(
        assignmentId = nextTarget.assignmentId,
        hasActiveSession = false,
        currentStepIndex = 0,
        isReflectionStage = false,
        remainingSeconds = nextTarget.steps.first().durationSeconds,
        isTimerRunning = false,
        hasStartedTimer = false,
        reflectionDraft = "",
      )
      practiceState.value = cleanState
      persistToSavedStateHandle(cleanState)
      welcomeGoalState.update { state ->
        state.copy(
          completedMilestonesCount = state.completedMilestonesCount + 1,
          latestCelebrationHeadline = "Congratulations, Maya! '${assignment.title}' reflection sent to Coach Keith.",
          latestNextSentencePrompt = "Finish the sentence: Attach your practice film clip or message Coach Keith a question about '${assignment.title}'.",
        )
      }
      onSubmitted()
    }
  }

  fun selectWelcomeGoal(goal: String) {
    if (goal.isBlank()) return
    welcomeGoalState.update { current ->
      current.copy(
        selectedGoal = goal.trim(),
        latestNextSentencePrompt = "Goal updated: \"${goal.trim()}\". Tap 'I'm Up for the Challenge' to lock it in for Coach Keith!",
      )
    }
  }

  fun acceptWelcomeChallenge() {
    welcomeGoalState.update { current ->
      val nextCount = if (current.isChallengeAccepted) current.completedMilestonesCount else current.completedMilestonesCount + 1
      current.copy(
        isChallengeAccepted = true,
        completedMilestonesCount = nextCount,
        latestCelebrationHeadline = "Challenge Accepted! Maya is locked in on today's tactical goal.",
        latestNextSentencePrompt = "Next sentence in your flow: Start today's 8-minute scope of work or attach a court video clip for Coach Keith.",
      )
    }
  }

  fun setCoachPortalMode(enabled: Boolean) {
    isCoachPortalMode.value = enabled
  }

  fun selectGradeBandPathway(gradeBand: GradeBandPathway) {
    welcomeGoalState.update { current ->
      current.copy(
        gradeBand = gradeBand,
        selectedGoal = gradeBand.focusSummary,
        latestCelebrationHeadline = "Pathway Updated: ${gradeBand.label}",
        latestNextSentencePrompt = "Website & App synced to ${gradeBand.label}: \"${gradeBand.focusSummary}\"",
      )
    }
  }

  fun selectTrainingProgramTrack(track: TrainingProgramTrack) {
    welcomeGoalState.update { current ->
      current.copy(
        programTrack = track,
        latestCelebrationHeadline = "Program Track Aligned: ${track.label}",
        latestNextSentencePrompt = "${track.subtitle} — your weekly scopes and check-ins now mirror this track.",
      )
    }
  }

  fun saveCheckInPreparation(
    checkInType: String,
    agendaTopics: List<String>,
    confidenceRating: Int,
    familyQuestionNote: String,
  ) {
    welcomeGoalState.update { current ->
      val updatedCheckIn = current.checkInState.copy(
        selectedCheckInType = checkInType,
        selectedAgendaTopics = agendaTopics.ifEmpty { current.checkInState.selectedAgendaTopics },
        confidenceRating = confidenceRating.coerceIn(1, 5),
        familyQuestionNote = familyQuestionNote.trim(),
        isCheckInPrepSaved = true,
      )
      current.copy(
        checkInState = updatedCheckIn,
        completedMilestonesCount = current.completedMilestonesCount + 1,
        latestCelebrationHeadline = "Check-In Agenda Saved for Coach Keith ($checkInType)!",
        latestNextSentencePrompt = "Your pre-call agenda & ${confidenceRating}/5 confidence rating are ready for your live Google Meet check-in with Coach Keith.",
      )
    }
  }

  fun selectRoughWeekChoice(choice: com.example.data.model.RoughWeekChoice) {
    welcomeGoalState.update { current ->
      current.copy(
        roughWeekChoice = choice,
        latestCelebrationHeadline = when (choice) {
          com.example.data.model.RoughWeekChoice.NONE -> "Weekly Rhythm Active"
          com.example.data.model.RoughWeekChoice.LIGHT_WEEK -> "Light Week Selected — Nothing to Make Up"
          com.example.data.model.RoughWeekChoice.RESET_FOCUS -> "Focus Reset Requested for Coach Keith"
          com.example.data.model.RoughWeekChoice.TALK_TO_COACH -> "Note Queued for Coach Keith"
        },
        latestNextSentencePrompt = choice.note,
      )
    }
  }

  fun submitPlayerTeachBack(
    whatWillDoDifferently: String,
    whatWillNotice: String,
  ) {
    val cleanChange = whatWillDoDifferently.trim()
    if (cleanChange.isEmpty()) return
    welcomeGoalState.update { current ->
      current.copy(
        teachBackEntry = current.teachBackEntry.copy(
          whatWillDoDifferently = cleanChange,
          whatWillNotice = whatWillNotice.trim(),
          isSubmittedByPlayer = true,
          coachMark = "unmarked",
        ),
        completedMilestonesCount = current.completedMilestonesCount + 1,
        latestCelebrationHeadline = "Teach-Back ('In Your Words') Sent to Coach Keith!",
        latestNextSentencePrompt = "Coach Keith will mark 'Got it' or open a quick clarification note. Your next rep never waits!",
      )
    }
  }

  fun markCoachTeachBack(
    mark: String,
    clarifyNote: String,
  ) {
    welcomeGoalState.update { current ->
      current.copy(
        teachBackEntry = current.teachBackEntry.copy(
          coachMark = mark,
          coachClarifyNote = clarifyNote.trim().ifEmpty {
            if (mark == "got-it") "Got it ✓ — eyes up before the catch is the exact key."
            else "Close! Make sure your eyes scan the rim before the ball touches your hands."
          },
        ),
        latestCelebrationHeadline = if (mark == "got-it") {
          "Coach Keith Marked Teach-Back: Got It ✓"
        } else {
          "Coach Keith Added a Quick Teach-Back Clarification"
        },
      )
    }
  }

  fun submitStuckPrivateNote(
    whatHappened: String,
    whatWouldHelp: String,
    howFeeling: String,
  ) {
    if (whatHappened.isBlank() && whatWouldHelp.isBlank() && howFeeling.isBlank()) return
    welcomeGoalState.update { current ->
      current.copy(
        stuckNote = current.stuckNote.copy(
          whatHappened = whatHappened.trim(),
          whatWouldHelp = whatWouldHelp.trim(),
          howFeeling = howFeeling.trim(),
          isSent = true,
        ),
        latestCelebrationHeadline = "Private 'I'm Stuck' Note Sent to Coach Keith",
        latestNextSentencePrompt = "Only Coach Keith sees this private note. He writes back himself within 1 business day.",
      )
    }
  }

  fun logGameTransferNote(
    opponent: String,
    carriedOver: String,
    didntYet: String,
    feelLabel: String,
  ) {
    val cleanCarried = carriedOver.trim()
    if (cleanCarried.isEmpty()) return
    val newNote = com.example.data.model.GameTransferNote(
      id = "game_${System.currentTimeMillis()}",
      dateLabel = if (opponent.isNotBlank()) "Today · vs ${opponent.trim()}" else "Today · Scrimmage / Game",
      opponent = opponent.trim(),
      carriedOver = cleanCarried,
      didntYet = didntYet.trim(),
      feelLabel = feelLabel.ifBlank { "Good" },
      coachMark = "unread",
    )
    welcomeGoalState.update { current ->
      current.copy(
        gameTransferNotes = listOf(newNote) + current.gameTransferNotes,
        completedMilestonesCount = current.completedMilestonesCount + 1,
        latestCelebrationHeadline = "Post-Game Transfer Note Saved for Coach Keith!",
        latestNextSentencePrompt = "Practice builds it. Games show it. Coach Keith reads every game note and marks what carried over.",
      )
    }
  }

  fun markCoachGameNote(
    gameId: String,
    coachMark: String,
    coachNote: String,
  ) {
    welcomeGoalState.update { current ->
      val updatedGames = current.gameTransferNotes.map { g ->
        if (g.id == gameId) {
          g.copy(
            coachMark = coachMark,
            coachNote = coachNote.trim().ifEmpty {
              "Read by Coach Keith — keep building that pre-catch scan under game pressure."
            },
            isTaggedWin = coachMark == "carried",
          )
        } else {
          g
        }
      }
      current.copy(
        gameTransferNotes = updatedGames,
        latestCelebrationHeadline = "Coach Keith Marked Game Note: ${coachMark.uppercase()}",
      )
    }
  }

  fun publishNewScopeOfWork(
    title: String,
    tacticalObjective: String,
    repBreakdown: String,
    requiredFilmAngle: String,
    successStandard: String,
    durationMinutes: Int = 10,
  ): PracticeScopeSpec? {
    val cleanTitle = title.trim()
    val cleanObjective = tacticalObjective.trim()
    if (cleanTitle.isEmpty() || cleanObjective.isEmpty()) return null

    val scopeId = "scope_custom_${System.currentTimeMillis()}"
    val newScope = PracticeScopeSpec(
      scopeId = scopeId,
      sessionId = scopeId,
      assignmentId = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
      title = cleanTitle,
      weekDayBadge = "COACH KEITH PUBLISHED · NEW SCOPE OF WORK",
      durationMinutes = durationMinutes.coerceIn(5, 45),
      tacticalObjective = cleanObjective,
      repBreakdown = repBreakdown.trim().ifEmpty {
        "Phase 1 (3m): Footwork & vision prep • Phase 2 (4m): Game-speed decision reps • Phase 3 (3m): Film & reflection check."
      },
      requiredFilmAngle = requiredFilmAngle.trim().ifEmpty {
        "Wing / 45° Slot Camera — keep ball-handler, cutter, and help defender in frame."
      },
      successStandard = successStandard.trim().ifEmpty {
        "Read the help defender's chest before making your cut or relocation decision."
      },
      status = "Active Scope",
    )
    practiceScopes.update { current -> listOf(newScope) + current }
    welcomeGoalState.update { state ->
      state.copy(
        latestCelebrationHeadline = "New Scope of Work Published by Coach Keith: $cleanTitle",
        latestNextSentencePrompt = "Coach Keith added '$cleanTitle' to your Plan. Review the rep breakdown and required film angle!",
      )
    }
    return newScope
  }

  fun submitPracticeVideo(
    assignmentId: String,
    drillTitle: String,
    videoUri: String,
    cameraAngleTag: String,
    playerNote: String,
  ): VideoFilmSubmission {
    val cleanNote = playerNote.trim().ifEmpty {
      "Focused on keeping my chin up and reading the help defender's chest on every rep."
    }
    val submission = VideoFilmSubmission(
      videoId = "vid_${System.currentTimeMillis()}",
      assignmentId = assignmentId,
      drillTitle = drillTitle,
      videoUri = videoUri.ifBlank { "content://com.ballerpath.app/court_clip_${System.currentTimeMillis()}.mp4" },
      cameraAngleTag = cameraAngleTag.ifBlank { "Wing / Corner Read (0:18)" },
      durationLabel = "0:18 · Court Practice Clip",
      playerNote = cleanNote,
      status = "awaiting_critique",
      createdAtMillis = System.currentTimeMillis(),
    )
    videoSubmissions.update { current -> listOf(submission) + current }
    welcomeGoalState.update { state ->
      state.copy(
        completedMilestonesCount = state.completedMilestonesCount + 1,
        latestCelebrationHeadline = "Congratulations! Practice film attached for '$drillTitle' ($cameraAngleTag).",
        latestNextSentencePrompt = "Your video is queued for Coach Keith's timestamped breakdown. Send him a quick note below about which rep felt sharpest!",
      )
    }
    return submission
  }

  fun saveCoachVideoCritique(
    videoId: String,
    coachCritique: String,
    timestampNotes: String,
    nextRepAdjustment: String,
  ): VideoFilmSubmission? {
    val cleanCritique = coachCritique.trim()
    val cleanTimestamps = timestampNotes.trim()
    val cleanNextRep = nextRepAdjustment.trim()
    if (cleanCritique.isEmpty()) return null

    var updatedItem: VideoFilmSubmission? = null
    videoSubmissions.update { list ->
      list.map { item ->
        if (item.videoId == videoId) {
          val modified = item.copy(
            status = "critiqued",
            coachCritique = cleanCritique,
            timestampNotes = cleanTimestamps.ifEmpty {
              "[0:04] Balanced catch-and-scan • [0:11] Clean relocation angle away from help defender"
            },
            nextRepAdjustment = cleanNextRep.ifEmpty {
              "Hold your corner spacing window for one full count before lifting to the wing."
            },
          )
          updatedItem = modified
          modified
        } else {
          item
        }
      }
    }
    if (updatedItem != null) {
      welcomeGoalState.update { state ->
        state.copy(
          completedMilestonesCount = state.completedMilestonesCount + 1,
          latestCelebrationHeadline = "Coach Keith Critiqued Your Film: '${updatedItem?.drillTitle}'!",
          latestNextSentencePrompt = "Next sentence: Read Coach Keith's timestamped notes and reply in the message thread to confirm your next-rep adjustment.",
        )
      }
    }
    return updatedItem
  }

  fun sendTacticalMessage(
    senderRole: String,
    text: String,
    relatedTitle: String = "Pass, Look, Decide",
  ): TacticalChatMessage? {
    val cleanText = text.trim()
    if (cleanText.isEmpty()) return null
    val isCoach = senderRole == "coach"
    val msg = TacticalChatMessage(
      messageId = "msg_${System.currentTimeMillis()}",
      senderRole = if (isCoach) "coach" else "player",
      senderName = if (isCoach) "Coach Keith" else "Maya Brooks (#14)",
      text = cleanText,
      relatedTitle = relatedTitle,
      createdAtMillis = System.currentTimeMillis(),
    )
    tacticalMessages.update { current -> current + msg }
    welcomeGoalState.update { state ->
      if (isCoach) {
        state.copy(
          latestCelebrationHeadline = "New Tactical Note from Coach Keith!",
          latestNextSentencePrompt = "Coach Keith replied on '$relatedTitle': \"${cleanText.take(70)}...\" — apply this cue on your next court rep!",
        )
      } else {
        state.copy(
          latestCelebrationHeadline = "Message Sent to Coach Keith!",
          latestNextSentencePrompt = "Great communication, Maya! While Coach Keith reviews your note, check your 3-step Scope of Work or attach a video clip.",
        )
      }
    }
    return msg
  }

  fun syncCloudScopes(cloudScopes: List<PracticeScopeSpec>) {
    if (cloudScopes.isEmpty()) return
    practiceScopes.update { existing ->
      val byId = existing.associateBy { it.scopeId }.toMutableMap()
      cloudScopes.forEach { byId[it.scopeId] = it }
      byId.values.toList()
    }
  }

  fun syncCloudVideos(cloudVideos: List<VideoFilmSubmission>) {
    if (cloudVideos.isEmpty()) return
    videoSubmissions.update { existing ->
      val byId = existing.associateBy { it.videoId }.toMutableMap()
      cloudVideos.forEach { byId[it.videoId] = it }
      byId.values.sortedByDescending { it.createdAtMillis }
    }
  }

  fun syncCloudMessages(cloudMsgs: List<TacticalChatMessage>) {
    if (cloudMsgs.isEmpty()) return
    tacticalMessages.update { existing ->
      val byId = existing.associateBy { it.messageId }.toMutableMap()
      cloudMsgs.forEach { byId[it.messageId] = it }
      byId.values.sortedBy { it.createdAtMillis }
    }
  }

  fun syncCloudGoalState(goal: String, accepted: Boolean, streakCount: Int) {
    welcomeGoalState.update { current ->
      current.copy(
        selectedGoal = goal.ifBlank { current.selectedGoal },
        isChallengeAccepted = accepted,
        completedMilestonesCount = streakCount.coerceAtLeast(current.completedMilestonesCount),
      )
    }
  }

  fun revealSampleCoachResponse(reflectionId: Long) {
    viewModelScope.launch {
      repository.revealSampleCoachResponse(reflectionId)
    }
  }

  fun saveCustomCoachFeedback(
    reflectionId: Long,
    coachResponse: String,
    nextAdjustment: String,
    onSaved: () -> Unit = {},
  ) {
    viewModelScope.launch {
      repository.saveCustomCoachFeedback(
        reflectionId = reflectionId,
        coachResponse = coachResponse,
        nextAdjustment = nextAdjustment,
      )
      onSaved()
    }
  }

  fun setSetupState(setupStage: SetupStage, hasGeneralWorkPublished: Boolean) {
    stopTimerInternal(persistToRoom = false)
    viewModelScope.launch {
      repository.setSetupState(setupStage, hasGeneralWorkPublished)
      demoStatusMessage.value = when (setupStage) {
        SetupStage.SETUP_IN_PROGRESS -> "Setup state: Waiting for Coach Keith review."
        SetupStage.PLAN_BEING_PREPARED ->
          if (hasGeneralWorkPublished) "Setup state: Plan being prepared (general work ready)."
          else "Setup state: Plan being prepared (no general work yet)."
        SetupStage.WORK_READY -> "Setup state: Work ready."
      }
    }
  }

  fun acknowledgeParentConsent() {
    viewModelScope.launch {
      repository.acknowledgeParentConsent()
    }
  }

  fun applyDemoScenario(scenario: WorkflowScenarioId) {
    stopTimerInternal(persistToRoom = false)
    moreWorkReadyPreview.value = (scenario == WorkflowScenarioId.MORE_WORK_READY)
    viewModelScope.launch {
      repository.applyDemoScenario(scenario)
      val storedAssignments = repository.observeAssignments().first()
      latestAssignments.value = storedAssignments
      val storedReflections = repository.observeReflections().first()
      val submittedIds = storedReflections.filter { !it.isSampleSeed }.map { it.assignmentId }.toSet()
      val personal = storedAssignments.filter { !it.isGeneralWork }.sortedBy { it.sortOrder }
      val target = personal.firstOrNull { it.hasDraftOrSession }
        ?: personal.firstOrNull { it.assignmentId !in submittedIds }
        ?: personal.firstOrNull()
        ?: SampleDataSource.assignmentOne

      val nextUi = target.toPracticeUiState()
      practiceState.value = nextUi
      persistToSavedStateHandle(nextUi)
      demoStatusMessage.value = "Sample scenario switched to: ${scenario.label}"
    }
  }

  fun resetDemoData() {
    stopTimerInternal(persistToRoom = false)
    moreWorkReadyPreview.value = false
    welcomeGoalState.value = WelcomeGoalChallengeState()
    practiceScopes.value = SampleDataSource.defaultPracticeScopes
    videoSubmissions.value = SampleDataSource.defaultVideoSubmissions()
    tacticalMessages.value = SampleDataSource.defaultTacticalMessages()
    isCoachPortalMode.value = false
    viewModelScope.launch {
      repository.resetDemoData()
      val storedAssignments = repository.observeAssignments().first()
      latestAssignments.value = storedAssignments
      val clean = SampleDataSource.assignmentOne.toPracticeUiState()
      practiceState.value = clean
      persistToSavedStateHandle(clean)
      demoStatusMessage.value = "Demo reset to Practice ready for Maya Brooks."
    }
  }

  fun clearDemoStatusMessage() {
    demoStatusMessage.value = null
  }

  private fun assignmentForId(assignmentId: String): CoachAssignment {
    return latestAssignments.value.firstOrNull { it.assignmentId == assignmentId }
      ?: when (assignmentId) {
        SampleDataSource.ASSIGNMENT_CORNER_RELOCATION -> SampleDataSource.assignmentTwo
        SampleDataSource.ASSIGNMENT_GENERAL_RHYTHM -> SampleDataSource.generalWorkAssignment
        else -> SampleDataSource.assignmentOne
      }
  }

  private fun stopTimerInternal(persistToRoom: Boolean) {
    timerJob?.cancel()
    timerJob = null
    practiceState.update { current ->
      val updated = current.copy(isTimerRunning = false)
      persistToSavedStateHandle(updated)
      if (persistToRoom) {
        val snapshot = updated
        viewModelScope.launch {
          saveAssignmentStateToRepository(snapshot)
        }
      }
      updated
    }
  }

  private inline fun updatePracticeState(
    transform: (PracticeSessionUiState) -> PracticeSessionUiState,
  ) {
    practiceState.update { current ->
      val updated = transform(current)
      persistPracticeEverywhere(updated)
      updated
    }
  }

  private fun persistPracticeEverywhere(state: PracticeSessionUiState) {
    persistToSavedStateHandle(state)
    viewModelScope.launch {
      saveAssignmentStateToRepository(state)
    }
  }

  private suspend fun saveAssignmentStateToRepository(state: PracticeSessionUiState) {
    val assignment = assignmentForId(state.assignmentId)
    val steps = assignment.steps
    val stepId = steps[state.currentStepIndex.coerceIn(0, steps.lastIndex)].id
    repository.saveAssignmentWorkflowState(
      assignmentId = state.assignmentId,
      hasActiveSession = state.hasActiveSession,
      currentStepId = stepId,
      isReflectionStage = state.isReflectionStage,
      remainingSeconds = state.remainingSeconds,
      hasStartedTimer = state.hasStartedTimer,
      reflectionDraft = state.reflectionDraft,
    )
  }

  private fun persistToSavedStateHandle(state: PracticeSessionUiState) {
    savedStateHandle[KEY_ACTIVE_ASSIGNMENT_ID] = state.assignmentId
    savedStateHandle[KEY_HAS_ACTIVE] = state.hasActiveSession
    savedStateHandle[KEY_STEP_INDEX] = state.currentStepIndex
    savedStateHandle[KEY_IS_REFLECTION] = state.isReflectionStage
    savedStateHandle[KEY_REMAINING_SECONDS] = state.remainingSeconds
    savedStateHandle[KEY_HAS_STARTED_TIMER] = state.hasStartedTimer
    savedStateHandle[KEY_REFLECTION_DRAFT] = state.reflectionDraft
  }

  private fun restorePracticeFromSavedStateHandle(): PracticeSessionUiState {
    val assignmentId = savedStateHandle.get<String>(KEY_ACTIVE_ASSIGNMENT_ID)
      ?: SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE
    val steps = assignmentForId(assignmentId).steps
    val stepIndex = (savedStateHandle.get<Int>(KEY_STEP_INDEX) ?: 0)
      .coerceIn(0, steps.lastIndex)
    val defaultSeconds = steps[stepIndex].durationSeconds
    return PracticeSessionUiState(
      assignmentId = assignmentId,
      hasActiveSession = savedStateHandle.get<Boolean>(KEY_HAS_ACTIVE) ?: false,
      currentStepIndex = stepIndex,
      isReflectionStage = savedStateHandle.get<Boolean>(KEY_IS_REFLECTION) ?: false,
      remainingSeconds = savedStateHandle.get<Int>(KEY_REMAINING_SECONDS) ?: defaultSeconds,
      isTimerRunning = false,
      hasStartedTimer = savedStateHandle.get<Boolean>(KEY_HAS_STARTED_TIMER) ?: false,
      reflectionDraft = savedStateHandle.get<String>(KEY_REFLECTION_DRAFT) ?: "",
    )
  }

  private fun CoachAssignment.toPracticeUiState(): PracticeSessionUiState {
    val idx = steps.indexOfFirst { it.id == currentStepId }.let { if (it >= 0) it else 0 }
    return PracticeSessionUiState(
      assignmentId = assignmentId,
      hasActiveSession = hasActiveSession,
      currentStepIndex = idx,
      isReflectionStage = isReflectionStage,
      remainingSeconds = remainingSeconds.coerceAtLeast(0),
      isTimerRunning = false,
      hasStartedTimer = hasStartedTimer,
      reflectionDraft = reflectionDraft,
      validationError = null,
      isSubmitting = false,
    )
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }

  class Factory(
    private val repository: BallerPathRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null,
  ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
      key: String,
      modelClass: Class<T>,
      handle: SavedStateHandle,
    ): T {
      return BallerPathViewModel(repository, handle) as T
    }
  }
}
