package com.example.data.repository

import com.example.data.model.CoachAssignment
import com.example.data.model.DevelopmentPlan
import com.example.data.model.ParentConsentState
import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PlayerSetupState
import com.example.data.model.PracticeReflection
import com.example.data.model.PracticeWorkflowState
import com.example.data.model.SetupStage
import com.example.data.model.SimulatedFamilyProfile
import com.example.data.model.TodayFocus
import com.example.data.model.WorkflowScenarioId
import com.example.data.sample.SampleDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over Baller Path data sources.
 *
 * Currently implemented by [LocalBallerPathRepository] using Room + local sample data.
 */
interface BallerPathRepository {
  fun observePlayerProfile(): Flow<PlayerProfile>
  fun observeFamilyProfile(): Flow<SimulatedFamilyProfile>
  fun observeDevelopmentPlan(): Flow<DevelopmentPlan>
  fun observeTodayFocus(): Flow<TodayFocus>
  fun observePlanSessions(): Flow<List<PlanSession>>
  fun observeReflections(): Flow<List<PracticeReflection>>
  fun observeWorkflowState(): Flow<PracticeWorkflowState>
  fun observeAssignments(): Flow<List<CoachAssignment>>
  fun observeSetupState(): Flow<PlayerSetupState>
  fun observeParentConsent(): Flow<ParentConsentState>

  suspend fun ensureSeeded()

  suspend fun saveWorkflowState(state: PracticeWorkflowState)

  suspend fun saveAssignmentWorkflowState(
    assignmentId: String,
    hasActiveSession: Boolean,
    currentStepId: String,
    isReflectionStage: Boolean,
    remainingSeconds: Int,
    hasStartedTimer: Boolean,
    reflectionDraft: String,
  )

  suspend fun submitReflection(
    assignmentId: String = SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE,
    drillId: String,
    drillTitle: String,
    laneObservation: String,
    readTag: String,
  ): PracticeReflection

  suspend fun revealSampleCoachResponse(reflectionId: Long)
  suspend fun saveCustomCoachFeedback(
    reflectionId: Long,
    coachResponse: String,
    nextAdjustment: String,
  )
  suspend fun setSetupState(setupStage: SetupStage, hasGeneralWorkPublished: Boolean)
  suspend fun acknowledgeParentConsent()
  suspend fun clearParentConsentForTest()
  suspend fun applyDemoScenario(scenario: WorkflowScenarioId)
  suspend fun resetDemoData()
}
