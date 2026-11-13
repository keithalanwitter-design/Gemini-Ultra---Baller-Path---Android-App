package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PracticeReflection

@Entity(tableName = "practice_reflections")
data class ReflectionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  @ColumnInfo(defaultValue = "assign-pass-look-decide")
  val assignmentId: String = "assign-pass-look-decide",
  val drillId: String,
  val drillTitle: String,
  val laneObservation: String,
  val readTag: String,
  val createdAtMillis: Long,
  val sampleCoachLabel: String,
  val sampleCoachResponse: String,
  val nextAdjustment: String,
  val isCoachResponseRevealed: Boolean,
  val isSampleSeed: Boolean,
) {
  fun toDomain(): PracticeReflection =
    PracticeReflection(
      id = id,
      assignmentId = assignmentId,
      drillId = drillId,
      drillTitle = drillTitle,
      laneObservation = laneObservation,
      readTag = readTag,
      createdAtMillis = createdAtMillis,
      sampleCoachLabel = sampleCoachLabel,
      sampleCoachResponse = sampleCoachResponse,
      nextAdjustment = nextAdjustment,
      isCoachResponseRevealed = isCoachResponseRevealed,
      isSampleSeed = isSampleSeed,
    )
}
