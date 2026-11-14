package com.example.ui.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.data.cloud.BallerPathCloudRepository
import com.example.data.model.PracticeScopeSpec
import com.example.data.model.TacticalChatMessage
import com.example.data.model.VideoFilmSubmission
import com.example.ui.viewmodel.BallerPathViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BallerPathCloudController(
  val currentUser: FirebaseUser?,
  val authErrorMessage: String?,
  private val context: Context,
  private val scope: CoroutineScope,
  private val repository: BallerPathCloudRepository?,
  val onAuthError: (String?) -> Unit,
) {
  val isSignedIn: Boolean
    get() = currentUser != null

  fun syncGoalAndChallengeToCloud(
    playerName: String,
    jerseyNumber: String,
    positionAndGrade: String,
    activeGoal: String,
    challengeQuestion: String,
    challengeAccepted: Boolean,
    streakCount: Int,
  ) {
    val uid = currentUser?.uid ?: return
    val repo = repository ?: return
    scope.launch {
      runCatching {
        repo.upsertPlayerProfile(
          playerName = playerName,
          jerseyNumber = jerseyNumber,
          positionAndGrade = positionAndGrade,
          activeGoal = activeGoal,
          challengeAccepted = challengeAccepted,
          courtStrengths = "Eyes-Up Court Vision • Corner Relocation • Balanced Catch",
          completedScopesCount = streakCount.toLong(),
          targetUserId = uid,
        )
      }
    }
  }

  fun syncNewScopeToCloud(spec: PracticeScopeSpec) {
    val uid = currentUser?.uid ?: return
    val repo = repository ?: return
    scope.launch {
      runCatching {
        repo.createPracticeScope(
          assignmentId = spec.assignmentId ?: "assign-pass-look-decide",
          title = spec.title,
          tacticalObjective = spec.tacticalObjective,
          scopeBreakdown = spec.repBreakdown,
          requiredFilmAngle = spec.requiredFilmAngle,
          successStandard = spec.successStandard,
          durationMinutes = spec.durationMinutes.toLong(),
          targetUserId = uid,
        )
      }
    }
  }

  fun syncVideoSubmissionToCloud(submission: VideoFilmSubmission) {
    val uid = currentUser?.uid ?: return
    val repo = repository ?: return
    scope.launch {
      runCatching {
        repo.submitVideoCritique(
          assignmentId = submission.assignmentId,
          drillTitle = submission.drillTitle,
          videoLabel = submission.videoUri,
          cameraAngle = submission.cameraAngleTag,
          playerObservation = submission.playerNote,
          targetUserId = uid,
        )
      }
    }
  }

  fun syncVideoCritiqueToCloud(submission: VideoFilmSubmission) {
    val uid = currentUser?.uid ?: return
    val repo = repository ?: return
    scope.launch {
      runCatching {
        repo.postCoachVideoCritique(
          submissionId = submission.videoId,
          coachCritique = submission.coachCritique,
          filmTimestampNotes = submission.timestampNotes,
          nextRepAdjustment = submission.nextRepAdjustment,
          targetUserId = uid,
        )
      }
    }
  }

  fun syncMessageToCloud(message: TacticalChatMessage) {
    val uid = currentUser?.uid ?: return
    val repo = repository ?: return
    scope.launch {
      runCatching {
        repo.sendCoachPlayerMessage(
          assignmentId = message.relatedTitle,
          senderRole = message.senderRole,
          senderName = message.senderName,
          messageText = message.text,
          followUpPrompt = "Reply or upload your next court rep",
          targetUserId = uid,
        )
      }
    }
  }
}

@Composable
fun rememberBallerPathCloudController(
  viewModel: BallerPathViewModel,
): BallerPathCloudController {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var currentUser by remember {
    mutableStateOf(
      runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
    )
  }
  var authErrorMessage by remember { mutableStateOf<String?>(null) }

  DisposableEffect(Unit) {
    val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      currentUser = firebaseAuth.currentUser
    }
    auth?.addAuthStateListener(listener)
    onDispose {
      auth?.removeAuthStateListener(listener)
    }
  }

  // Strictly instantiate and observe Firestore ONLY when currentUser != null
  val activeUser = currentUser
  val cloudRepository = remember(activeUser?.uid) {
    if (activeUser != null) {
      runCatching { BallerPathCloudRepository(context.applicationContext) }.getOrNull()
    } else {
      null
    }
  }

  if (activeUser != null && cloudRepository != null) {
    LaunchedEffect(activeUser.uid, cloudRepository) {
      launch {
        runCatching {
          cloudRepository.observePlayerProfile(activeUser.uid).collect { profile ->
            if (profile != null) {
              viewModel.syncCloudGoalState(
                goal = profile.activeGoal,
                accepted = profile.challengeAccepted,
                streakCount = profile.completedScopesCount.toInt(),
              )
            }
          }
        }
      }
      launch {
        runCatching {
          cloudRepository.observePracticeScopes(activeUser.uid).collect { list ->
            val mapped = list.map { item ->
              PracticeScopeSpec(
                scopeId = item.id.ifBlank { "scope_${item.title.hashCode()}" },
                sessionId = item.id.ifBlank { "scope_${item.title.hashCode()}" },
                assignmentId = item.assignmentId.ifBlank { "assign-pass-look-decide" },
                title = item.title,
                weekDayBadge = "COACH KEITH SCOPE · ${item.status.uppercase()}",
                durationMinutes = item.durationMinutes.toInt().coerceAtLeast(5),
                tacticalObjective = item.tacticalObjective,
                repBreakdown = item.scopeBreakdown,
                requiredFilmAngle = item.requiredFilmAngle,
                successStandard = item.successStandard,
                status = item.status,
              )
            }
            viewModel.syncCloudScopes(mapped)
          }
        }
      }
      launch {
        runCatching {
          cloudRepository.observeVideoSubmissions(activeUser.uid).collect { list ->
            val mapped = list.map { v ->
              VideoFilmSubmission(
                videoId = v.id.ifBlank { "vid_${v.drillTitle.hashCode()}" },
                assignmentId = v.assignmentId,
                drillTitle = v.drillTitle,
                videoUri = v.videoLabel,
                cameraAngleTag = v.cameraAngle,
                durationLabel = "0:18 · Cloud Synced Clip",
                playerNote = v.playerObservation,
                status = if (v.status == "coach_reviewed" || v.coachCritique.isNotBlank()) "critiqued" else "awaiting_critique",
                coachCritique = v.coachCritique,
                timestampNotes = v.filmTimestampNotes,
                nextRepAdjustment = v.nextRepAdjustment,
                createdAtMillis = v.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
              )
            }
            viewModel.syncCloudVideos(mapped)
          }
        }
      }
      launch {
        runCatching {
          cloudRepository.observeCoachMessages(activeUser.uid).collect { list ->
            val mapped = list.map { m ->
              TacticalChatMessage(
                messageId = m.id.ifBlank { "msg_${m.messageText.hashCode()}" },
                senderRole = m.senderRole,
                senderName = m.senderName,
                text = m.messageText,
                relatedTitle = m.assignmentId,
                createdAtMillis = m.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
              )
            }
            viewModel.syncCloudMessages(mapped)
          }
        }
      }
    }
  }

  return remember(currentUser, authErrorMessage, cloudRepository) {
    BallerPathCloudController(
      currentUser = currentUser,
      authErrorMessage = authErrorMessage,
      context = context,
      scope = scope,
      repository = cloudRepository,
      onAuthError = { authErrorMessage = it },
    )
  }
}
