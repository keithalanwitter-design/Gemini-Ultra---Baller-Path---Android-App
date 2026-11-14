package com.example.data.cloud

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BallerPathCloudRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun upsertAndObservePlayerProfile_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(MAYA_EMAIL)
    val repo = BallerPathCloudRepository(firestore)

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.upsertPlayerProfile(
        playerName = "Maya Brooks",
        jerseyNumber = "#14",
        positionAndGrade = "Guard · 7th Grade",
        activeGoal = "Read the help defender early before driving",
        challengeAccepted = true,
        courtStrengths = "Wide spacing · Quick corner relocation",
        completedScopesCount = 2L,
      )
    }
    assertTrue(result.isSuccess)

    val observed = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observePlayerProfile(uid).first { it != null && it.challengeAccepted }
    }
    assertNotNull(observed)
    assertEquals("Maya Brooks", observed?.playerName)
    assertEquals(true, observed?.challengeAccepted)
  }

  @Test
  fun createScopeSubmitVideoAndCoachCritique_andMessaging_succeedsForOwner() = runBlocking {
    val uid = signInTestUser(MAYA_EMAIL)
    val repo = BallerPathCloudRepository(firestore)

    val scopeId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.createPracticeScope(
        assignmentId = "assign-pass-look-decide",
        title = "Pass, Look, Decide — Scope of Work",
        tacticalObjective = "Spot whether the lane opens after giving up the ball",
        scopeBreakdown = "Step 1: Entry pass & scan · Step 2: Drive or space read · Step 3: Live reps",
        requiredFilmAngle = "Wing / Corner 45-degree angle",
        successStandard = "Read the help defender's chest before cutting or spacing",
        durationMinutes = 8L,
      ).getOrThrow()
    }
    assertTrue(scopeId.isNotEmpty())

    val submissionId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.submitVideoCritique(
        assignmentId = "assign-pass-look-decide",
        drillTitle = "Pass, Look, Decide",
        videoLabel = "wing_spacing_rep_01.mp4",
        cameraAngle = "Wing / Corner Read",
        playerObservation = "My teammate drove middle so I held the corner window.",
      ).getOrThrow()
    }
    assertTrue(submissionId.isNotEmpty())

    val critiqueRes = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.postCoachVideoCritique(
        submissionId = submissionId,
        coachCritique = "You read the help defender early and slid into a clean passing lane.",
        filmTimestampNotes = "[0:04] Clean pass follow-through · [0:09] Wide corner slide",
        nextRepAdjustment = "Hold corner balance for one full count before lifting.",
      )
    }
    assertTrue(critiqueRes.isSuccess)

    val msgId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.sendCoachPlayerMessage(
        assignmentId = "assign-pass-look-decide",
        senderRole = "player",
        senderName = "Maya Brooks",
        messageText = "Uploaded my wing spacing clip for Coach Keith!",
        followUpPrompt = "Next step: Review Coach Keith's timestamped notes.",
      ).getOrThrow()
    }
    assertTrue(msgId.isNotEmpty())
  }

  @Test
  fun crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val mayaUid = signInTestUser(MAYA_EMAIL)
    val repo = BallerPathCloudRepository(firestore)
    val submissionId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.submitVideoCritique(
        assignmentId = "assign-pass-look-decide",
        drillTitle = "Pass, Look, Decide",
        videoLabel = "wing_spacing_rep_01.mp4",
        cameraAngle = "Wing / Corner Read",
        playerObservation = "My teammate drove middle so I held the corner window.",
      ).getOrThrow()
    }

    // Sign in as a different non-admin household and attempt to read Maya's video submission
    signInTestUser(OTHER_EMAIL)
    val crossResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.getVideoSubmissionById(submissionId = submissionId, targetUserId = mayaUid)
    }
    assertTrue(crossResult.isFailure)
    val fsEx = crossResult.exceptionOrNull()?.unwrapFirestoreException()
    assertNotNull(fsEx)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
  }

  @Test
  fun unauthenticatedUser_observePracticeScopes_failsWithPermissionDenied() = runBlocking {
    val mayaUid = signInTestUser(MAYA_EMAIL)
    auth.signOut()
    val repo = BallerPathCloudRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observePracticeScopes(targetUserId = mayaUid).first()
      }
      fail("Expected exception for unauthenticated caller")
    } catch (e: Exception) {
      val fsEx = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException, got $e", fsEx)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }
  }

  private companion object {
    const val MAYA_EMAIL = "maya.family@test.com"
    const val OTHER_EMAIL = "other.family@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
