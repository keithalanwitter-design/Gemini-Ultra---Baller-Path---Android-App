package com.example.data.repository

import com.example.data.model.DevelopmentPlan
import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PracticeReflection
import com.example.data.model.PracticeWorkflowState
import com.example.data.model.SimulatedFamilyProfile
import com.example.data.model.TodayFocus
import com.example.data.model.WorkflowScenarioId
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over Baller Path data sources.
 *
 * Currently implemented by [LocalBallerPathRepository] using Room + local sample data.
 * To connect the real Baller Path backend service later, implement a remote-synced
 * repository backed by [BallerPathServiceGateway] without changing ViewModels or Compose UI.
 */
interface BallerPathRepository {
  fun observePlayerProfile(): Flow<PlayerProfile>

  fun observeFamilyProfile(): Flow<SimulatedFamilyProfile>

  fun observeDevelopmentPlan(): Flow<DevelopmentPlan>

  fun observeTodayFocus(): Flow<TodayFocus>

  fun observePlanSessions(): Flow<List<PlanSession>>

  fun observeReflections(): Flow<List<PracticeReflection>>

  fun observeWorkflowState(): Flow<PracticeWorkflowState>

  suspend fun ensureSeeded()

  suspend fun saveWorkflowState(state: PracticeWorkflowState)

  suspend fun submitReflection(
    drillId: String,
    drillTitle: String,
    laneObservation: String,
    readTag: String,
  ): PracticeReflection

  suspend fun revealSampleCoachResponse(reflectionId: Long)

  suspend fun applyDemoScenario(scenario: WorkflowScenarioId)

  suspend fun resetDemoData()
}
