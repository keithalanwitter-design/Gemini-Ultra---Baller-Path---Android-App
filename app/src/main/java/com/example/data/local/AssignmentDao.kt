package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
  @Query("SELECT * FROM assignments ORDER BY isGeneralWork ASC, sortOrder ASC")
  fun observeAssignments(): Flow<List<AssignmentEntity>>

  @Query("SELECT * FROM assignments ORDER BY isGeneralWork ASC, sortOrder ASC")
  suspend fun getAssignments(): List<AssignmentEntity>

  @Query("SELECT * FROM assignments WHERE assignmentId = :assignmentId")
  suspend fun getAssignment(assignmentId: String): AssignmentEntity?

  @Query("SELECT COUNT(*) FROM assignments")
  suspend fun countAssignments(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAssignment(entity: AssignmentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAssignments(entities: List<AssignmentEntity>)

  @Query(
    """
    UPDATE assignments
    SET hasActiveSession = :hasActiveSession,
        currentStepId = :currentStepId,
        isReflectionStage = :isReflectionStage,
        remainingSeconds = :remainingSeconds,
        hasStartedTimer = :hasStartedTimer,
        reflectionDraft = :reflectionDraft,
        updatedAtMillis = :updatedAtMillis
    WHERE assignmentId = :assignmentId
    """
  )
  suspend fun updateDraftAndSession(
    assignmentId: String,
    hasActiveSession: Boolean,
    currentStepId: String,
    isReflectionStage: Boolean,
    remainingSeconds: Int,
    hasStartedTimer: Boolean,
    reflectionDraft: String,
    updatedAtMillis: Long,
  )

  @Query("DELETE FROM assignments")
  suspend fun clearAll()
}
