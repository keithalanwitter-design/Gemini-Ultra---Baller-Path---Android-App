package com.example.data.repository

import com.example.data.model.PlanSession
import com.example.data.model.PlayerProfile
import com.example.data.model.PracticeReflection
import com.example.data.model.TodayFocus

/**
 * Integration seam for the existing Baller Path service.
 *
 * Authentication, family billing, and live coach feedback delivery are intentionally excluded
 * from this local prototype. When wiring the production backend:
 * 1. Provide an authenticated HTTP/Ktor/Retrofit or Firestore implementation of this gateway.
 * 2. Sync local Room reflections to `uploadReflection` and fetch Coach Keith's real review.
 * 3. Replace [com.example.data.sample.SampleDataSource] calls in [LocalBallerPathRepository].
 */
interface BallerPathServiceGateway {
  suspend fun fetchAssignedPlayerProfile(familySessionToken: String): Result<PlayerProfile>

  suspend fun fetchTodayFocus(playerId: String): Result<TodayFocus>

  suspend fun fetchPlanSessions(playerId: String): Result<List<PlanSession>>

  suspend fun uploadReflection(
    playerId: String,
    drillId: String,
    laneObservation: String,
  ): Result<PracticeReflection>
}
