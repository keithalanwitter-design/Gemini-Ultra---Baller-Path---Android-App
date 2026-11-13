package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReflectionDao {
  @Query("SELECT * FROM practice_reflections ORDER BY isSampleSeed ASC, createdAtMillis DESC, id DESC")
  fun observeReflections(): Flow<List<ReflectionEntity>>

  @Query("SELECT COUNT(*) FROM practice_reflections")
  suspend fun countReflections(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReflection(entity: ReflectionEntity): Long

  @Query("UPDATE practice_reflections SET isCoachResponseRevealed = :revealed WHERE id = :id")
  suspend fun setCoachResponseRevealed(id: Long, revealed: Boolean)

  @Query("DELETE FROM practice_reflections")
  suspend fun clearAll()
}
