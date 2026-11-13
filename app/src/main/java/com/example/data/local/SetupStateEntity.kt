package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PlayerSetupState
import com.example.data.model.SetupStage

@Entity(tableName = "setup_state")
data class SetupStateEntity(
  @PrimaryKey val id: Int = 1,
  val setupStage: String,
  val hasGeneralWorkPublished: Boolean,
) {
  fun toDomain(): PlayerSetupState =
    PlayerSetupState(
      setupStage = SetupStage.fromId(setupStage),
      hasGeneralWorkPublished = hasGeneralWorkPublished,
    )
}
