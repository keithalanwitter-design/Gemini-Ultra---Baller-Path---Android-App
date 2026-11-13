package com.example.data.repository

import com.example.data.local.AssignmentDao
import com.example.data.local.ParentConsentDao
import com.example.data.local.ParentConsentEntity
import com.example.data.local.PracticeDraftDao
import com.example.data.local.PracticeDraftStateEntity
import com.example.data.local.ReflectionDao
import com.example.data.local.ReflectionEntity
import com.example.data.local.SetupStateDao
import com.example.data.local.SetupStateEntity
import com.example.data.model.CoachAssignment
import com.example.data.model.DevelopmentPlan
import com.example.data.model.ParentConsentState
import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PlayerSetupState
import com.example.data.model.PracticeReflection
import com.example.data.model.PracticeWorkflowState
import com.example.data.model.SessionStatus
import com.example.data.model.SetupStage
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
  private val assignmentDao: AssignmentDao? = null,
  private val setupStateDao: SetupStateDao? = null,
  private val parentConsentDao: ParentConsentDao? = null,
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
      val submittedAssignmentIds = entities
        .filter { !it.isSampleSeed }
        .map { it.assignmentId }
        .toSet()
      var assignedToday = false
      SampleDataSource.weekTwoPlan.map { session ->
        val aId = session.assignmentId
        when {
          aId != null && aId in submittedAssignmentIds ->
            session.copy(status = SessionStatus.COMPLETED)
          aId != null && !assignedToday -> {
            assignedToday = true
            session.copy(status = SessionStatus.TODAY)
          }
          aId != null -> session.copy(status = SessionStatus.UPCOMING)
          else -> session
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

  override fun observeAssignments(): Flow<List<CoachAssignment>> {
    val dao = assignmentDao
      ?: return flowOf(listOf(SampleDataSource.assignmentOne, SampleDataSource.assignmentTwo, SampleDataSource.generalWorkAssignment))
    return dao.observeAssignments().map { list ->
      if (list.isEmpty()) {
        listOf(SampleDataSource.assignmentOne, SampleDataSource.assignmentTwo, SampleDataSource.generalWorkAssignment)
      } else {
        list.map { it.toDomain() }
      }
    }
  }

  override fun observeSetupState(): Flow<PlayerSetupState> {
    val dao = setupStateDao ?: return flowOf(PlayerSetupState())
    return dao.observeSetupState().map { entity ->
      entity?.toDomain() ?: PlayerSetupState()
    }
  }

  override fun observeParentConsent(): Flow<ParentConsentState> {
    val dao = parentConsentDao ?: return flowOf(ParentConsentState(isAcknowledged = false))
    return dao.observeConsent().map { entity ->
      entity?.toDomain() ?: ParentConsentState(isAcknowledged = false)
    }
  }

  override suspend fun ensureSeeded() {
    val now = clockMillis()
    if (reflectionDao.countReflections() == 0) {
      reflectionDao.insertReflection(
        SampleDataSource.initialSeedReflection(now)
      )
    }
    if (practiceDraftDao.getWorkflowState() == null) {
      practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity())
    }
    assignmentDao?.let { dao ->
      val existingIds = dao.getAssignments().map { it.assignmentId }.toSet()
      val missing = SampleDataSource.defaultAssignmentEntities(now).filter { it.assignmentId !in existingIds }
      if (missing.isNotEmpty()) {
        dao.upsertAssignments(missing)
      }
    }
    setupStateDao?.let { dao ->
      if (dao.getSetupState() == null) {
        dao.upsertSetupState(
          SetupStateEntity(
            id = 1,
            setupStage = SetupStage.WORK_READY.id,
            hasGeneralWorkPublished = true,
          )
        )
      }
    }
    parentConsentDao?.let { dao ->
      if (dao.getConsent() == null) {
        dao.upsertConsent(
          ParentConsentEntity(
            id = 1,
            isAcknowledged = false,
            acknowledgedAtMillis = 0L,
          )
        )
      }
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
    saveAssignmentWorkflowState(
      assignmentId = state.assignmentId,
      hasActiveSession = state.hasActiveSession,
      currentStepId = state.currentStepId,
      isReflectionStage = state.isReflectionStage,
      remainingSeconds = state.remainingSeconds,
      hasStartedTimer = state.hasStartedTimer,
      reflectionDraft = state.reflectionDraft,
    )
  }

  override suspend fun saveAssignmentWorkflowState(
    assignmentId: String,
    hasActiveSession: Boolean,
    currentStepId: String,
    isReflectionStage: Boolean,
    remainingSeconds: Int,
    hasStartedTimer: Boolean,
    reflectionDraft: String,
  ) {
    assignmentDao?.updateDraftAndSession(
      assignmentId = assignmentId,
      hasActiveSession = hasActiveSession,
      currentStepId = currentStepId,
      isReflectionStage = isReflectionStage,
      remainingSeconds = remainingSeconds,
      hasStartedTimer = hasStartedTimer,
      reflectionDraft = reflectionDraft,
      updatedAtMillis = clockMillis(),
    )
  }

  override suspend fun submitReflection(
    assignmentId: String,
    drillId: String,
    drillTitle: String,
    laneObservation: String,
    readTag: String,
  ): PracticeReflection {
    val trimmed = laneObservation.trim()
    val (coachResponse, adjustment) = SampleDataSource.buildSampleCoachFeedback(
      observation = trimmed,
      assignmentId = assignmentId,
    )
    val entity = ReflectionEntity(
      assignmentId = assignmentId,
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

    // Clear ONLY this assignment's draft & active session so history and other assignments stay intact
    val assignment = assignmentDao?.getAssignment(assignmentId)?.toDomain()
    val firstStep = assignment?.steps?.firstOrNull() ?: SampleDataSource.todayFocus.steps.first()
    assignmentDao?.updateDraftAndSession(
      assignmentId = assignmentId,
      hasActiveSession = false,
      currentStepId = firstStep.id,
      isReflectionStage = false,
      remainingSeconds = firstStep.durationSeconds,
      hasStartedTimer = false,
      reflectionDraft = "",
      updatedAtMillis = clockMillis(),
    )
    if (assignmentId == SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE) {
      practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity())
    }
    return entity.copy(id = newId).toDomain()
  }

  override suspend fun revealSampleCoachResponse(reflectionId: Long) {
    reflectionDao.setCoachResponseRevealed(id = reflectionId, revealed = true)
  }

  override suspend fun saveCustomCoachFeedback(
    reflectionId: Long,
    coachResponse: String,
    nextAdjustment: String,
  ) {
    if (!com.example.BuildConfig.DEBUG) return
    val cleanResponse = coachResponse.trim()
    val cleanNextRep = nextAdjustment.trim()
    if (cleanResponse.isEmpty() || cleanNextRep.isEmpty()) return
    reflectionDao.updateCoachFeedback(
      id = reflectionId,
      coachResponse = cleanResponse,
      nextAdjustment = cleanNextRep,
      coachLabel = SampleDataSource.fixtureCoachLabel,
    )
  }

  override suspend fun setSetupState(setupStage: SetupStage, hasGeneralWorkPublished: Boolean) {
    setupStateDao?.upsertSetupState(
      SetupStateEntity(
        id = 1,
        setupStage = setupStage.id,
        hasGeneralWorkPublished = hasGeneralWorkPublished,
      )
    )
  }

  override suspend fun acknowledgeParentConsent() {
    parentConsentDao?.upsertConsent(
      ParentConsentEntity(
        id = 1,
        isAcknowledged = true,
        acknowledgedAtMillis = clockMillis(),
      )
    )
  }

  override suspend fun clearParentConsentForTest() {
    parentConsentDao?.upsertConsent(
      ParentConsentEntity(
        id = 1,
        isAcknowledged = false,
        acknowledgedAtMillis = 0L,
      )
    )
  }

  override suspend fun applyDemoScenario(scenario: WorkflowScenarioId) {
    val now = clockMillis()
    reflectionDao.clearAll()
    reflectionDao.insertReflection(SampleDataSource.initialSeedReflection(now))
    setupStateDao?.upsertSetupState(
      SetupStateEntity(
        id = 1,
        setupStage = SetupStage.WORK_READY.id,
        hasGeneralWorkPublished = true,
      )
    )
    assignmentDao?.upsertAssignments(SampleDataSource.defaultAssignmentEntities(now))

    val steps = SampleDataSource.assignmentOne.steps
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
        assignmentDao?.updateDraftAndSession(
          assignmentId = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
          hasActiveSession = true,
          currentStepId = "look",
          isReflectionStage = false,
          remainingSeconds = lookSeconds,
          hasStartedTimer = false,
          reflectionDraft = "",
          updatedAtMillis = now,
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
        assignmentDao?.updateDraftAndSession(
          assignmentId = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
          hasActiveSession = true,
          currentStepId = "decide",
          isReflectionStage = true,
          remainingSeconds = 0,
          hasStartedTimer = false,
          reflectionDraft = SampleDataSource.FIXTURE_DRAFT_TEXT,
          updatedAtMillis = now,
        )
      }
      WorkflowScenarioId.AWAITING_COACH -> {
        // Assignment 1 awaiting review; Assignment 2 ready (exact B4 setup)
        practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity(passSeconds))
        reflectionDao.insertReflection(
          ReflectionEntity(
            assignmentId = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
            drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
            drillTitle = SampleDataSource.assignmentOne.title,
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
        // Both assignments submitted, Assignment 1 & 2 revealed so Today shows "Saved. Coach Keith will review it." / Read feedback
        practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity(passSeconds))
        reflectionDao.insertReflection(
          ReflectionEntity(
            assignmentId = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
            drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
            drillTitle = SampleDataSource.assignmentOne.title,
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
        reflectionDao.insertReflection(
          ReflectionEntity(
            assignmentId = SampleDataSource.ASSIGNMENT_CORNER_RELOCATION,
            drillId = SampleDataSource.DRILL_CORNER_RELOCATION,
            drillTitle = SampleDataSource.assignmentTwo.title,
            laneObservation = SampleDataSource.FIXTURE_ASSIGNMENT_TWO_SUBMITTED,
            readTag = "Corner Relocation Read · 8 min",
            createdAtMillis = now,
            sampleCoachLabel = SampleDataSource.fixtureCoachLabel,
            sampleCoachResponse = SampleDataSource.FIXTURE_ASSIGNMENT_TWO_COACH_RESPONSE,
            nextAdjustment = SampleDataSource.FIXTURE_ASSIGNMENT_TWO_NEXT_REP,
            isCoachResponseRevealed = true,
            isSampleSeed = false,
          )
        )
      }
      WorkflowScenarioId.MORE_WORK_READY -> {
        // Assignment 1 revealed, Assignment 2 ready (shows More work ready in BuildConfig.DEBUG)
        practiceDraftDao.upsertWorkflowState(defaultWorkflowEntity(passSeconds))
        reflectionDao.insertReflection(
          ReflectionEntity(
            assignmentId = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
            drillId = SampleDataSource.DRILL_PASS_LOOK_DECIDE,
            drillTitle = SampleDataSource.assignmentOne.title,
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
