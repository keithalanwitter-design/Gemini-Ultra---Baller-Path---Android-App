package com.example.data.cloud

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BallerPathCloudRepository(
  private val db: FirebaseFirestore,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  // 1. Player Cloud Profile (Welcome Goal Check-In, Challenge Commitment & Tactical Card)
  fun observePlayerProfile(targetUserId: String? = null): Flow<PlayerCloudProfile?> = flow {
    val uid = targetUserId ?: requireUserId()
    val docRef = db.collection("users").document(uid).collection("player_profiles").document("main")
    val path = docRef.path
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(PlayerCloudProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          } else {
            null
          }
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.GET, path)
          }
          throw error
        }
    )
  }

  suspend fun upsertPlayerProfile(
    playerName: String,
    jerseyNumber: String,
    positionAndGrade: String,
    activeGoal: String,
    challengeAccepted: Boolean,
    courtStrengths: String,
    completedScopesCount: Long,
    targetUserId: String? = null,
  ): Result<String> = runCatching {
    val uid = targetUserId ?: requireUserId()
    val docRef = db.collection("users").document(uid).collection("player_profiles").document("main")
    try {
      val existingSnap = docRef.get().await()
      if (!existingSnap.exists()) {
        val payload = mapOf(
          "userId" to uid,
          "playerName" to playerName.trim(),
          "jerseyNumber" to jerseyNumber.trim(),
          "positionAndGrade" to positionAndGrade.trim(),
          "activeGoal" to activeGoal.trim(),
          "challengeAccepted" to challengeAccepted,
          "courtStrengths" to courtStrengths.trim(),
          "completedScopesCount" to completedScopesCount,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.set(payload).await()
      } else {
        val updates = mapOf(
          "playerName" to playerName.trim(),
          "jerseyNumber" to jerseyNumber.trim(),
          "positionAndGrade" to positionAndGrade.trim(),
          "activeGoal" to activeGoal.trim(),
          "challengeAccepted" to challengeAccepted,
          "courtStrengths" to courtStrengths.trim(),
          "completedScopesCount" to completedScopesCount,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.update(updates).await()
      }
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  // 2. Practice Scopes of Work
  fun observePracticeScopes(targetUserId: String? = null): Flow<List<PracticeScopeOfWork>> = flow {
    val uid = targetUserId ?: requireUserId()
    val colRef = db.collection("users").document(uid).collection("practice_scopes")
    val path = "users/$uid/practice_scopes"
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(PracticeScopeOfWork::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun createPracticeScope(
    assignmentId: String,
    title: String,
    tacticalObjective: String,
    scopeBreakdown: String,
    requiredFilmAngle: String,
    successStandard: String,
    durationMinutes: Long,
    targetUserId: String? = null,
  ): Result<String> = runCatching {
    val uid = targetUserId ?: requireUserId()
    val docRef = db.collection("users").document(uid).collection("practice_scopes").document()
    val payload = mapOf(
      "userId" to uid,
      "assignmentId" to assignmentId.trim(),
      "title" to title.trim(),
      "tacticalObjective" to tacticalObjective.trim(),
      "scopeBreakdown" to scopeBreakdown.trim(),
      "requiredFilmAngle" to requiredFilmAngle.trim(),
      "successStandard" to successStandard.trim(),
      "durationMinutes" to durationMinutes.coerceIn(1L, 180L),
      "status" to "assigned",
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun updatePracticeScopeStatus(
    scopeId: String,
    status: String,
    targetUserId: String? = null,
  ): Result<Unit> = runCatching {
    val uid = targetUserId ?: requireUserId()
    require(scopeId.isNotBlank()) { "Scope ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("practice_scopes").document(scopeId)
    try {
      docRef.update(
        mapOf(
          "status" to status,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // 3. Video Critique Submissions (Player Film Upload & Coach Keith Tactical Critique)
  fun observeVideoSubmissions(targetUserId: String? = null): Flow<List<VideoCritiqueSubmission>> = flow {
    val uid = targetUserId ?: requireUserId()
    val colRef = db.collection("users").document(uid).collection("video_submissions")
    val path = "users/$uid/video_submissions"
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(VideoCritiqueSubmission::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun getVideoSubmissionById(
    submissionId: String,
    targetUserId: String? = null,
  ): Result<VideoCritiqueSubmission?> = runCatching {
    val uid = targetUserId ?: requireUserId()
    val docRef = db.collection("users").document(uid).collection("video_submissions").document(submissionId)
    try {
      docRef.get().await()
        .toObject(VideoCritiqueSubmission::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      throw e
    }
  }

  suspend fun submitVideoCritique(
    assignmentId: String,
    drillTitle: String,
    videoLabel: String,
    cameraAngle: String,
    playerObservation: String,
    targetUserId: String? = null,
  ): Result<String> = runCatching {
    val uid = targetUserId ?: requireUserId()
    val docRef = db.collection("users").document(uid).collection("video_submissions").document()
    val payload = mapOf(
      "userId" to uid,
      "assignmentId" to assignmentId.trim(),
      "drillTitle" to drillTitle.trim(),
      "videoLabel" to videoLabel.trim(),
      "cameraAngle" to cameraAngle.trim(),
      "playerObservation" to playerObservation.trim(),
      "coachCritique" to "",
      "filmTimestampNotes" to "",
      "nextRepAdjustment" to "",
      "status" to "awaiting_coach",
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun postCoachVideoCritique(
    submissionId: String,
    coachCritique: String,
    filmTimestampNotes: String,
    nextRepAdjustment: String,
    targetUserId: String? = null,
  ): Result<Unit> = runCatching {
    val uid = targetUserId ?: requireUserId()
    require(submissionId.isNotBlank()) { "Submission ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("video_submissions").document(submissionId)
    try {
      docRef.update(
        mapOf(
          "coachCritique" to coachCritique.trim(),
          "filmTimestampNotes" to filmTimestampNotes.trim(),
          "nextRepAdjustment" to nextRepAdjustment.trim(),
          "status" to "coach_reviewed",
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // 4. Coach <-> Player Two-Way Tactical Messaging Thread
  fun observeCoachMessages(targetUserId: String? = null): Flow<List<CoachPlayerMessage>> = flow {
    val uid = targetUserId ?: requireUserId()
    val colRef = db.collection("users").document(uid).collection("coach_messages")
    val path = "users/$uid/coach_messages"
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(CoachPlayerMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.createdAt?.seconds ?: 0L }
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun sendCoachPlayerMessage(
    assignmentId: String,
    senderRole: String,
    senderName: String,
    messageText: String,
    followUpPrompt: String,
    targetUserId: String? = null,
  ): Result<String> = runCatching {
    val uid = targetUserId ?: requireUserId()
    val docRef = db.collection("users").document(uid).collection("coach_messages").document()
    val payload = mapOf(
      "userId" to uid,
      "assignmentId" to assignmentId.trim().ifEmpty { "general" },
      "senderRole" to senderRole.trim(),
      "senderName" to senderName.trim(),
      "messageText" to messageText.trim(),
      "followUpPrompt" to followUpPrompt.trim(),
      "createdAt" to FieldValue.serverTimestamp(),
    )
    try {
      docRef.set(payload).await()
      docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }
}
