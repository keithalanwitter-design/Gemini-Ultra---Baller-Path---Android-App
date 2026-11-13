package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SetupStateDao {
  @Query("SELECT * FROM setup_state WHERE id = 1")
  fun observeSetupState(): Flow<SetupStateEntity?>

  @Query("SELECT * FROM setup_state WHERE id = 1")
  suspend fun getSetupState(): SetupStateEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSetupState(entity: SetupStateEntity)
}
