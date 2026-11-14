package com.example.data.cloud

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PlayerCloudProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val playerName: String = "Maya Brooks",
  val jerseyNumber: String = "#14",
  val positionAndGrade: String = "Guard · 7th Grade",
  val activeGoal: String = "Read the help defender early before putting the ball on the floor",
  val challengeAccepted: Boolean = false,
  val courtStrengths: String = "Wide corner spacing · Early chest reads · Chin up on middle drives",
  val completedScopesCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class PracticeScopeOfWork(
  @DocumentId val id: String = "",
  val userId: String = "",
  val assignmentId: String = "",
  val title: String = "",
  val tacticalObjective: String = "",
  val scopeBreakdown: String = "",
  val requiredFilmAngle: String = "",
  val successStandard: String = "",
  val durationMinutes: Long = 8L,
  val status: String = "assigned",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class VideoCritiqueSubmission(
  @DocumentId val id: String = "",
  val userId: String = "",
  val assignmentId: String = "",
  val drillTitle: String = "",
  val videoLabel: String = "",
  val cameraAngle: String = "",
  val playerObservation: String = "",
  val coachCritique: String = "",
  val filmTimestampNotes: String = "",
  val nextRepAdjustment: String = "",
  val status: String = "awaiting_coach",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CoachPlayerMessage(
  @DocumentId val id: String = "",
  val userId: String = "",
  val assignmentId: String = "general",
  val senderRole: String = "player",
  val senderName: String = "",
  val messageText: String = "",
  val followUpPrompt: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)
