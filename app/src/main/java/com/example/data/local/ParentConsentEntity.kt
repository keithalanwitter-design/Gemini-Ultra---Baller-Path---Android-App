package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ParentConsentState

@Entity(tableName = "parent_consent")
data class ParentConsentEntity(
  @PrimaryKey val id: Int = 1,
  val isAcknowledged: Boolean,
  val acknowledgedAtMillis: Long,
) {
  fun toDomain(): ParentConsentState =
    ParentConsentState(
      isAcknowledged = isAcknowledged,
      acknowledgedAtMillis = acknowledgedAtMillis,
    )
}
