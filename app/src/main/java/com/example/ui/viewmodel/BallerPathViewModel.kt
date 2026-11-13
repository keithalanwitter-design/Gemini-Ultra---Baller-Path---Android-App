package com.example.ui.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.data.model.DevelopmentPlan
import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PracticeReflection
import com.example.data.model.PracticeWorkflowState
import com.example.data.model.SimulatedFamilyProfile
import com.example.data.model.TodayFocus
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
  val hasActiveSession: Boolean = false,
  val currentStepIndex: Int = 0,
  val isReflectionStage: Boolean = false,
  val remainingSeconds: Int = SampleDataSource.todayFocus.steps.first().durationSeconds,
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
  val todayFocus: TodayFocus = SampleDataSource.todayFocus,
  val planSessions: List<PlanSession> = SampleDataSource.weekTwoPlan,
  val reflections: List<PracticeReflection> = emptyList(),
  val practice: PracticeSessionUiState = PracticeSessionUiState(),
  val demoStatusMessage: String? = null,
  val isMoreWorkReadyPreview: Boolean = false,
) {
  val latestSubmittedReflection: PracticeReflection?
    get() = reflections.firstOrNull { !it.isSampleSeed }

  val hasSubmittedTodayReflection: Boolean
    get() = latestSubmittedReflection != null

  val currentScenario: WorkflowScenarioId
    get() {
      val submitted = latestSubmittedReflection
      return when {
        practice.reflectionDraft.isNotBlank() || practice.isReflectionStage -> WorkflowScenarioId.DRAFT
        practice.hasActiveSession -> WorkflowScenarioId.IN_PROGRESS
        isMoreWorkReadyPreview -> WorkflowScenarioId.MORE_WORK_READY
        submitted != null && !submitted.isCoachResponseRevealed -> WorkflowScenarioId.AWAITING_COACH
        submitted != null && submitted.isCoachResponseRevealed -> WorkflowScenarioId.FEEDBACK_READY
        else -> WorkflowScenarioId.READY
      }
    }

  val activeStepTitle: String
    get() {
      val steps = todayFocus.steps
      return steps[practice.currentStepIndex.coerceIn(0, steps.lastIndex)].title
    }
}

class BallerPathViewModel(
  private val repository: BallerPathRepository,
  private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

  private companion object {
    const val KEY_HAS_ACTIVE = "practice_has_active"
    const val KEY_STEP_INDEX = "practice_step_index"
    const val KEY_IS_REFLECTION = "practice_is_reflection"
    const val KEY_REMAINING_SECONDS = "practice_remaining_seconds"
    const val KEY_HAS_STARTED_TIMER = "practice_has_started_timer"
    const val KEY_REFLECTION_DRAFT = "practice_reflection_draft"
  }

  private val practiceState = MutableStateFlow(restorePracticeFromSavedStateHandle())
  private val demoStatusMessage = MutableStateFlow<String?>(null)
  private val moreWorkReadyPreview = MutableStateFlow(false)
  private val seeded = MutableStateFlow(false)
  private var timerJob: Job? = null

  init {
    viewModelScope.launch {
      repository.ensureSeeded()
      val storedWorkflow = repository.observeWorkflowState().first()
      // Hydrate from Room if SavedStateHandle didn't already carry an active in-memory edit
      if (!savedStateHandle.contains(KEY_STEP_INDEX) ||
        storedWorkflow.hasActiveSession ||
        storedWorkflow.reflectionDraft.isNotBlank() ||
        storedWorkflow.isReflectionStage
      ) {
        val hydrated = storedWorkflow.toUiState()
        practiceState.value = hydrated
        persistToSavedStateHandle(hydrated)
      }
      seeded.value = true
    }
  }

  val uiState: StateFlow<BallerPathUiState> = combine(
    repository.observePlayerProfile(),
    repository.observeFamilyProfile(),
    repository.observeDevelopmentPlan(),
    repository.observeTodayFocus(),
    repository.observePlanSessions(),
    repository.observeReflections(),
    combine(
      practiceState,
      demoStatusMessage,
      moreWorkReadyPreview,
      seeded,
    ) { practice, statusMsg, moreWork, isSeeded ->
      listOf(practice, statusMsg, moreWork, isSeeded)
    },
  ) { values ->
    @Suppress("UNCHECKED_CAST")
    val player = values[0] as PlayerProfile
    val family = values[1] as SimulatedFamilyProfile
    val planInfo = values[2] as DevelopmentPlan
    val todayFocus = values[3] as TodayFocus
    @Suppress("UNCHECKED_CAST")
    val planSessions = values[4] as List<PlanSession>
    @Suppress("UNCHECKED_CAST")
    val reflections = values[5] as List<PracticeReflection>
    @Suppress("UNCHECKED_CAST")
    val extra = values[6] as List<Any?>
    val practice = extra[0] as PracticeSessionUiState
    val statusMsg = extra[1] as String?
    val isMoreWork = extra[2] as Boolean
    val isSeeded = extra[3] as Boolean

    BallerPathUiState(
      isLoading = !isSeeded,
      player = player,
      family = family,
      developmentPlan = planInfo,
      todayFocus = todayFocus,
      planSessions = planSessions,
      reflections = reflections,
      practice = practice,
      demoStatusMessage = statusMsg,
      isMoreWorkReadyPreview = isMoreWork,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = BallerPathUiState(),
  )

  fun startOrResumePracticeSession() {
    val current = practiceState.value
    if (current.hasActiveSession || current.isReflectionStage || current.reflectionDraft.isNotBlank()) {
      // Preserve restored step, timer, or draft
      return
    }
    startNewPracticeSession()
  }

  fun startNewPracticeSession() {
    moreWorkReadyPreview.value = false
    stopTimerInternal(persistToRoom = false)
    val firstStep = SampleDataSource.todayFocus.steps.first()
    updatePracticeState {
      PracticeSessionUiState(
        hasActiveSession = true,
        currentStepIndex = 0,
        isReflectionStage = false,
        remainingSeconds = firstStep.durationSeconds,
        isTimerRunning = false,
        hasStartedTimer = false,
        reflectionDraft = "",
        validationError = null,
        isSubmitting = false,
      )
    }
  }

  fun advancePracticeStage() {
    stopTimerInternal(persistToRoom = false)
    val steps = SampleDataSource.todayFocus.steps
    val current = practiceState.value
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
   * Steps backward within Pass, Look, Decide.
   * Returns true if handled internally, or false if on Step 1 and ready to exit to Today.
   */
  fun stepBackInPractice(): Boolean {
    stopTimerInternal(persistToRoom = true)
    val steps = SampleDataSource.todayFocus.steps
    val current = practiceState.value
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
      val stepSeconds = SampleDataSource.todayFocus.steps[current.currentStepIndex].durationSeconds
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
            // Timer finishing NEVER advances steps or submits a reflection.
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
    val stepSeconds = SampleDataSource.todayFocus.steps[current.currentStepIndex].durationSeconds
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
    } else {
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
    val draft = practiceState.value.reflectionDraft.trim()
    if (draft.isEmpty()) {
      practiceState.update {
        it.copy(validationError = "Write one sentence about what you noticed before submitting.")
      }
      return
    }
    if (practiceState.value.isSubmitting) return

    practiceState.update { it.copy(isSubmitting = true, validationError = null) }
    viewModelScope.launch {
      val focus = SampleDataSource.todayFocus
      repository.submitReflection(
        drillId = focus.drillId,
        drillTitle = focus.title,
        laneObservation = draft,
        readTag = "Pass, Look, Decide · 8 min",
      )
      val cleanState = PracticeSessionUiState()
      practiceState.value = cleanState
      persistToSavedStateHandle(cleanState)
      onSubmitted()
    }
  }

  fun revealSampleCoachResponse(reflectionId: Long) {
    viewModelScope.launch {
      repository.revealSampleCoachResponse(reflectionId)
    }
  }

  fun applyDemoScenario(scenario: WorkflowScenarioId) {
    stopTimerInternal(persistToRoom = false)
    moreWorkReadyPreview.value = (scenario == WorkflowScenarioId.MORE_WORK_READY)
    viewModelScope.launch {
      repository.applyDemoScenario(scenario)
      val workflow = repository.observeWorkflowState().first()
      val nextUi = workflow.toUiState()
      practiceState.value = nextUi
      persistToSavedStateHandle(nextUi)
      demoStatusMessage.value = "Sample scenario switched to: ${scenario.label}"
    }
  }

  fun resetDemoData() {
    stopTimerInternal(persistToRoom = false)
    moreWorkReadyPreview.value = false
    viewModelScope.launch {
      repository.resetDemoData()
      val clean = PracticeSessionUiState()
      practiceState.value = clean
      persistToSavedStateHandle(clean)
      demoStatusMessage.value = "Demo reset to Practice ready for Maya Brooks."
    }
  }

  fun clearDemoStatusMessage() {
    demoStatusMessage.value = null
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
          repository.saveWorkflowState(snapshot.toWorkflowState())
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
      repository.saveWorkflowState(state.toWorkflowState())
    }
  }

  private fun persistToSavedStateHandle(state: PracticeSessionUiState) {
    savedStateHandle[KEY_HAS_ACTIVE] = state.hasActiveSession
    savedStateHandle[KEY_STEP_INDEX] = state.currentStepIndex
    savedStateHandle[KEY_IS_REFLECTION] = state.isReflectionStage
    savedStateHandle[KEY_REMAINING_SECONDS] = state.remainingSeconds
    savedStateHandle[KEY_HAS_STARTED_TIMER] = state.hasStartedTimer
    savedStateHandle[KEY_REFLECTION_DRAFT] = state.reflectionDraft
  }

  private fun restorePracticeFromSavedStateHandle(): PracticeSessionUiState {
    val steps = SampleDataSource.todayFocus.steps
    val stepIndex = (savedStateHandle.get<Int>(KEY_STEP_INDEX) ?: 0)
      .coerceIn(0, steps.lastIndex)
    val defaultSeconds = steps[stepIndex].durationSeconds
    return PracticeSessionUiState(
      hasActiveSession = savedStateHandle.get<Boolean>(KEY_HAS_ACTIVE) ?: false,
      currentStepIndex = stepIndex,
      isReflectionStage = savedStateHandle.get<Boolean>(KEY_IS_REFLECTION) ?: false,
      remainingSeconds = savedStateHandle.get<Int>(KEY_REMAINING_SECONDS) ?: defaultSeconds,
      isTimerRunning = false,
      hasStartedTimer = savedStateHandle.get<Boolean>(KEY_HAS_STARTED_TIMER) ?: false,
      reflectionDraft = savedStateHandle.get<String>(KEY_REFLECTION_DRAFT) ?: "",
    )
  }

  private fun PracticeWorkflowState.toUiState(): PracticeSessionUiState {
    val steps = SampleDataSource.todayFocus.steps
    val idx = steps.indexOfFirst { it.id == currentStepId }.let { if (it >= 0) it else 0 }
    return PracticeSessionUiState(
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

  private fun PracticeSessionUiState.toWorkflowState(): PracticeWorkflowState {
    val steps = SampleDataSource.todayFocus.steps
    val stepId = steps[currentStepIndex.coerceIn(0, steps.lastIndex)].id
    return PracticeWorkflowState(
      hasActiveSession = hasActiveSession,
      currentStepId = stepId,
      isReflectionStage = isReflectionStage,
      remainingSeconds = remainingSeconds,
      hasStartedTimer = hasStartedTimer,
      reflectionDraft = reflectionDraft,
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
