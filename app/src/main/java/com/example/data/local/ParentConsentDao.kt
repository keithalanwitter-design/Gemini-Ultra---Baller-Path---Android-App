package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParentConsentDao {
  @Query("SELECT * FROM parent_consent WHERE id = 1")
  fun observeConsent(): Flow<ParentConsentEntity?>

  @Query("SELECT * FROM parent_consent WHERE id = 1")
  suspend fun getConsent(): ParentConsentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertConsent(entity: ParentConsentEntity)
}
