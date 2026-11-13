package com.example.data.repository

import com.example.data.local.PracticeDraftDao
import com.example.data.local.PracticeDraftStateEntity
import com.example.data.local.ReflectionDao
import com.example.data.local.ReflectionEntity
import com.example.data.model.DevelopmentPlan
import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PracticeReflection
import com.example.data.model.PracticeWorkflowState
import com.example.data.model.SessionStatus
import com.example.data.model.SimulatedFamilyProfile
import com.example.data.model.TodayFocus
import com.example.data.model.WorkflowScenarioId
import com.example.data.sample.SampleDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LocalBallerPathRepository(
  private val reflectionDao: ReflectionDao,
  private val practiceDraftDao: PracticeDraftDao,
  private val clockMillis: () -> Long = { System.currentTimeMillis() },
) : BallerPathRepository {

  override fun observePlayerProfile(): Flow<PlayerProfile> =
    flowOf(SampleDataSource.fictionalPlayer)

  override fun observeFamilyProfile(): Flow<SimulatedFamilyProfile> =
    flowOf(SampleDataSource.simulatedFamily)

  override fun observeDevelopmentPlan(): Flow<DevelopmentPlan> =
    flowOf(SampleDataSource.developmentPlan)

  override fun observeTodayFocus(): Flow<TodayFocus> =
    flowOf(SampleDataSource.todayFocus)

  override fun observePlanSessions(): Flow<List<PlanSession>> =
    reflectionDao.observeReflections().map { entities ->
      val completedToday = entities.any {
        !it.isSampleSeed && it.drillId == SampleDataSource.DRILL_PASS_LOOK_DECIDE
      }
      SampleDataSource.weekTwoPlan.map { session ->
        if (session.id == "w2_d3" && completedToday) {
          session.copy(status = SessionStatus.COMPLETED)
        } else {
          session
        }
      }
    }

  override fun observeReflections(): Flow<List<PracticeReflection>> =
    reflectionDao.observeReflections().map { entities ->
      entities.map { it.toDomain() }
    }

  override fun observeWorkflowState(): Flow<PracticeWorkflowState> =
    practiceDraftDao.observeWorkflowState().map { entity ->
      entity?.toDomain() ?: PracticeWorkflowState(
        remainingSeconds = SampleDataSource.todayFocus.steps.first().durationSeconds,
      )
    }

  override suspend fun ensureSeeded() {
    if (reflectionDao.countReflections() == 0) {
      reflectionDao.insertReflection(
        SampleDataSource.initialSeedReflection(clockMillis())
      )
    }
    if (practiceDraftDao.getWorkflowState() == null) {
      practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity())
    }
  }

  override suspend fun saveWorkflowState(state: PracticeWorkflowState) {
    practiceDraftDao.upsertWorkflowState(
      PracticeDraftStateEntity(
        id = 1,
        hasActiveSession = state.hasActiveSession,
        currentStepId = state.currentStepId,
        isReflectionStage = state.isReflectionStage,
        remainingSeconds = state.remainingSeconds,
        hasStartedTimer = state.hasStartedTimer,
        reflectionDraft = state.reflectionDraft,
        updatedAtMillis = clockMillis(),
      )
    )
  }

  override suspend fun submitReflection(
    drillId: String,
    drillTitle: String,
    laneObservation: String,
    readTag: String,
  ): PracticeReflection {
    val trimmed = laneObservation.trim()
    val (coachResponse, adjustment) = SampleDataSource.buildSampleCoachFeedback(trimmed)
    val entity = ReflectionEntity(
      drillId = drillId,
      drillTitle = drillTitle,
      laneObservation = trimmed,
      readTag = readTag,
      createdAtMillis = clockMillis(),
      sampleCoachLabel = SampleDataSource.fixtureCoachLabel,
      sampleCoachResponse = coachResponse,
      nextAdjustment = adjustment,
      isCoachResponseRevealed = false,
      isSampleSeed = false,
    )
    val newId = reflectionDao.insertReflection(entity)
    // Clear draft & active session once reflection is submitted so draft stays distinct from submitted work
    practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity())
    return entity.copy(id = newId).toDomain()
  }

  override suspend fun revealSampleCoachResponse(reflectionId: Long) {
    reflectionDao.setCoachResponseRevealed(id = reflectionId, revealed = true)
  }

  override suspend fun applyDemoScenario(scenario: WorkflowScenarioId) {
    val now = clockMillis()
    reflectionDao.clearAll()
    reflectionDao.insertReflection(SampleDataSource.initialSeedReflection(now))

    val steps = SampleDataSource.todayFocus.steps
    val passSeconds = steps.first().durationSeconds
    val lookSeconds = steps[1].durationSeconds

    when (scenario) {
      WorkflowScenarioId.READY -> {
        practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity())
      }
      WorkflowScenarioId.IN_PROGRESS -> {
        practiceDraftDao.upsertWorkflowState(
          PracticeDraftStateEntity(
            id = 1,
            hasActiveSession = true,
            currentStepId = "look",
            isReflectionStage = false,
            remainingSeconds = lookSeconds,
            hasStartedTimer = false,
            reflectionDraft = "",
            updatedAtMillis = now,
          )
        )
      }
      WorkflowScenarioId.DRAFT -> {
        practiceDraftDao.upsertWorkflowState(
          PracticeDraftStateEntity(
            id = 1,
            hasActiveSession = true,
            currentStepId = "decide",
            isReflectionStage = true,
            remainingSeconds = 0,
            hasStartedTimer = false,
            reflectionDraft = SampleDataSource.FIXTURE_DRAFT_TEXT,
            updatedAtMillis = now,
          )
        )
      }
      WorkflowScenarioId.AWAITING_COACH -> {
        practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity(passSeconds))
        reflectionDao.insertReflection(
          ReflectionEntity(
            drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
            drillTitle = SampleDataSource.todayFocus.title,
            laneObservation = SampleDataSource.FIXTURE_SUBMITTED_TEXT,
            readTag = "Pass, Look, Decide · 8 min",
            createdAtMillis = now,
            sampleCoachLabel = SampleDataSource.fixtureCoachLabel,
            sampleCoachResponse = SampleDataSource.FIXTURE_COACH_RESPONSE,
            nextAdjustment = SampleDataSource.FIXTURE_NEXT_ADJUSTMENT,
            isCoachResponseRevealed = false,
            isSampleSeed = false,
          )
        )
      }
      WorkflowScenarioId.FEEDBACK_READY -> {
        practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity(passSeconds))
        reflectionDao.insertReflection(
          ReflectionEntity(
            drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
            drillTitle = SampleDataSource.todayFocus.title,
            laneObservation = SampleDataSource.FIXTURE_SUBMITTED_TEXT,
            readTag = "Pass, Look, Decide · 8 min",
            createdAtMillis = now,
            sampleCoachLabel = SampleDataSource.fixtureCoachLabel,
            sampleCoachResponse = SampleDataSource.FIXTURE_COACH_RESPONSE,
            nextAdjustment = SampleDataSource.FIXTURE_NEXT_ADJUSTMENT,
            isCoachResponseRevealed = true,
            isSampleSeed = false,
          )
        )
      }
      WorkflowScenarioId.MORE_WORK_READY -> {
        practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity(passSeconds))
        reflectionDao.insertReflection(
          ReflectionEntity(
            drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
            drillTitle = SampleDataSource.todayFocus.title,
            laneObservation = SampleDataSource.FIXTURE_SUBMITTED_TEXT,
            readTag = "Pass, Look, Decide · 8 min",
            createdAtMillis = now - 3_600_000L,
            sampleCoachLabel = SampleDataSource.fixtureCoachLabel,
            sampleCoachResponse = SampleDataSource.FIXTURE_COACH_RESPONSE,
            nextAdjustment = SampleDataSource.FIXTURE_NEXT_ADJUSTMENT,
            isCoachResponseRevealed = true,
            isSampleSeed = false,
          )
        )
      }
    }
  }

  override suspend fun resetDemoData() {
    applyDemoScenario(WorkflowScenarioId.READY)
  }

  private fun defaultWorkflowEntity(
    firstStepSeconds: Int = SampleDataSource.todayFocus.steps.first().durationSeconds,
  ): PracticeDraftStateEntity =
    PracticeDraftStateEntity(
      id = 1,
      hasActiveSession = false,
      currentStepId = "pass",
      isReflectionStage = false,
      remainingSeconds = firstStepSeconds,
      hasStartedTimer = false,
      reflectionDraft = "",
      updatedAtMillis = clockMillis(),
    )
}
