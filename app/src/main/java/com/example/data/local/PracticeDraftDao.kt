package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDraftDao {
  @Query("SELECT * FROM practice_workflow_state WHERE id = 1")
  fun observeWorkflowState(): Flow<PracticeDraftStateEntity?>

  @Query("SELECT * FROM practice_workflow_state WHERE id = 1")
  suspend fun getWorkflowState(): PracticeDraftStateEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertWorkflowState(state: PracticeDraftStateEntity)

  @Query("DELETE FROM practice_workflow_state")
  suspend fun clearWorkflowState()
}
