package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PracticeWorkflowState

@Entity(tableName = "practice_workflow_state")
data class PracticeDraftStateEntity(
  @PrimaryKey val id: Int = 1,
  val hasActiveSession: Boolean,
  val currentStepId: String,
  val isReflectionStage: Boolean,
  val remainingSeconds: Int,
  val hasStartedTimer: Boolean,
  val reflectionDraft: String,
  val updatedAtMillis: Long,
) {
  fun toDomain(): PracticeWorkflowState =
    PracticeWorkflowState(
      hasActiveSession = hasActiveSession,
      currentStepId = currentStepId,
      isReflectionStage = isReflectionStage,
      remainingSeconds = remainingSeconds,
      hasStartedTimer = hasStartedTimer,
      reflectionDraft = reflectionDraft,
      updatedAtMillis = updatedAtMillis,
    )
}
