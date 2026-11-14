package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.BallerPathLoopStage
import com.example.data.model.WorkFeedbackTab
import com.example.ui.auth.rememberBallerPathCloudController
import com.example.ui.components.BallerPathBottomBar
import com.example.ui.components.BallerPathNavRail
import com.example.ui.components.TopLevelTab
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.ParentConsentScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.PracticeFlowScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.WorkFeedbackScreen
import com.example.ui.viewmodel.BallerPathViewModel

const val ROUTE_PRACTICE_FLOW = "practice_pass_look_decide"

@Composable
fun BallerPathApp(
  viewModel: BallerPathViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, viewModel) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_STOP) {
        viewModel.onAppBackgrounded()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  if (uiState.isLoading) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
    )
    return
  }

  if (!uiState.parentConsent.isAcknowledged) {
    ParentConsentScreen(
      onConfirmConsent = { viewModel.acknowledgeParentConsent() },
      modifier = modifier,
    )
    return
  }

  val cloudController = rememberBallerPathCloudController(viewModel = viewModel)

  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: TopLevelTab.TODAY.route
  var selectedWorkTab by rememberSaveable { mutableStateOf(WorkFeedbackTab.TO_DO) }

  val currentTab = TopLevelTab.entries.firstOrNull { it.route == currentRoute }
  val isPracticeRoute = currentRoute == ROUTE_PRACTICE_FLOW

  fun navigateToTab(tab: TopLevelTab) {
    viewModel.clearDemoStatusMessage()
    if (tab == TopLevelTab.TODAY) {
      navController.popBackStack(TopLevelTab.TODAY.route, inclusive = false)
    } else {
      navController.navigate(tab.route) {
        popUpTo(navController.graph.findStartDestination().id) {
          saveState = true
        }
        launchSingleTop = true
        restoreState = true
      }
    }
  }

  fun openPracticeFlow(assignmentId: String, forceRestart: Boolean = false) {
    viewModel.openAssignmentPractice(
      assignmentId = assignmentId,
      forceRestart = forceRestart,
    )
    navController.navigate(ROUTE_PRACTICE_FLOW) {
      launchSingleTop = true
    }
  }

  fun handleTodayPrimaryAction() {
    val target = uiState.todayAssignment
    if (target != null) {
      openPracticeFlow(assignmentId = target.assignmentId, forceRestart = false)
    } else if (uiState.hasAnyRevealedPersonalFeedback && !uiState.hasAnyAwaitingPersonalWork) {
      selectedWorkTab = WorkFeedbackTab.COACH_RESPONDED
      navigateToTab(TopLevelTab.WORK_FEEDBACK)
    } else {
      selectedWorkTab = if (uiState.hasAnyAwaitingPersonalWork) {
        WorkFeedbackTab.AWAITING_COACH
      } else {
        WorkFeedbackTab.TO_DO
      }
      navigateToTab(TopLevelTab.WORK_FEEDBACK)
    }
  }

  // On secondary tabs (Plan, Work + Feedback, Account), system Back returns to Today
  if (!isPracticeRoute && currentTab != null && currentTab != TopLevelTab.TODAY) {
    BackHandler {
      navigateToTab(TopLevelTab.TODAY)
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      bottomBar = {
        if (!isPracticeRoute && !useNavigationRail && currentTab != null) {
          BallerPathBottomBar(
            currentTab = currentTab,
            onSelectTab = ::navigateToTab,
          )
        }
      },
    ) { innerPadding ->
      val contentModifier = if (isPracticeRoute) {
        Modifier.fillMaxSize()
      } else {
        Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .consumeWindowInsets(innerPadding)
          .statusBarsPadding()
      }

      Row(modifier = contentModifier) {
        if (!isPracticeRoute && useNavigationRail && currentTab != null) {
          BallerPathNavRail(
            currentTab = currentTab,
            onSelectTab = ::navigateToTab,
          )
        }

        NavHost(
          navController = navController,
          startDestination = TopLevelTab.TODAY.route,
          modifier = Modifier.weight(1f),
        ) {
          composable(TopLevelTab.TODAY.route) {
            TodayScreen(
              uiState = uiState,
              onPrimaryAction = ::handleTodayPrimaryAction,
              onSelectGoal = { goal ->
                viewModel.selectWelcomeGoal(goal)
                cloudController.syncGoalAndChallengeToCloud(
                  playerName = uiState.player.displayName,
                  jerseyNumber = uiState.player.jerseyNumber,
                  positionAndGrade = uiState.player.positionAndGrade,
                  activeGoal = goal,
                  challengeQuestion = uiState.welcomeGoalState.challengeQuestion,
                  challengeAccepted = uiState.welcomeGoalState.isChallengeAccepted,
                  streakCount = uiState.welcomeGoalState.completedMilestonesCount,
                )
              },
              onAcceptChallenge = {
                viewModel.acceptWelcomeChallenge()
                cloudController.syncGoalAndChallengeToCloud(
                  playerName = uiState.player.displayName,
                  jerseyNumber = uiState.player.jerseyNumber,
                  positionAndGrade = uiState.player.positionAndGrade,
                  activeGoal = uiState.welcomeGoalState.selectedGoal,
                  challengeQuestion = uiState.welcomeGoalState.challengeQuestion,
                  challengeAccepted = true,
                  streakCount = uiState.welcomeGoalState.completedMilestonesCount + 1,
                )
              },
              onOpenFilmAndMessages = {
                navigateToTab(TopLevelTab.WORK_FEEDBACK)
              },
              onSelectLoopStage = { stage ->
                when (stage) {
                  BallerPathLoopStage.FOCUS -> navigateToTab(TopLevelTab.PLAN)
                  BallerPathLoopStage.PRACTICE -> handleTodayPrimaryAction()
                  BallerPathLoopStage.REFLECT -> {
                    selectedWorkTab = WorkFeedbackTab.AWAITING_COACH
                    navigateToTab(TopLevelTab.WORK_FEEDBACK)
                  }
                  BallerPathLoopStage.ADJUST -> {
                    selectedWorkTab = WorkFeedbackTab.COACH_RESPONDED
                    navigateToTab(TopLevelTab.WORK_FEEDBACK)
                  }
                }
              },
              onSelectRoughWeekChoice = { choice ->
                viewModel.selectRoughWeekChoice(choice)
              },
              onSubmitStuckNote = { happened, want, feeling ->
                viewModel.submitStuckPrivateNote(happened, want, feeling)
              },
            )
          }
          composable(TopLevelTab.PLAN.route) {
            PlanScreen(
              uiState = uiState,
              onOpenPractice = { assignmentId, forceRestart ->
                openPracticeFlow(assignmentId = assignmentId, forceRestart = forceRestart)
              },
              onSelectGradeBand = { gradeBand ->
                viewModel.selectGradeBandPathway(gradeBand)
              },
              onSelectProgramTrack = { track ->
                viewModel.selectTrainingProgramTrack(track)
              },
              onSaveCheckInPrep = { checkInType, agendaTopics, confidenceRating, familyQuestionNote ->
                viewModel.saveCheckInPreparation(
                  checkInType = checkInType,
                  agendaTopics = agendaTopics,
                  confidenceRating = confidenceRating,
                  familyQuestionNote = familyQuestionNote,
                )
              },
              onSubmitTeachBack = { change, notice ->
                viewModel.submitPlayerTeachBack(change, notice)
              },
              onCoachMarkTeachBack = { mark, clarifyNote ->
                viewModel.markCoachTeachBack(mark, clarifyNote)
              },
              onLogGameNote = { opponent, carried, missed, feel ->
                viewModel.logGameTransferNote(opponent, carried, missed, feel)
              },
              onCoachMarkGame = { gameId, mark, note ->
                viewModel.markCoachGameNote(gameId, mark, note)
              },
            )
          }
          composable(TopLevelTab.WORK_FEEDBACK.route) {
            val activeDrill = uiState.todayAssignment ?: uiState.assignments.first()
            WorkFeedbackScreen(
              uiState = uiState,
              selectedTab = selectedWorkTab,
              onSelectTab = { selectedWorkTab = it },
              onStartOrResumeAssignment = { assignmentId ->
                openPracticeFlow(assignmentId = assignmentId, forceRestart = false)
              },
              onRevealSampleFeedback = { reflectionId ->
                viewModel.revealSampleCoachResponse(reflectionId)
                selectedWorkTab = WorkFeedbackTab.COACH_RESPONDED
              },
              onSaveCustomCoachFeedback = { reflectionId, coachResponse, nextAdjustment ->
                viewModel.saveCustomCoachFeedback(
                  reflectionId = reflectionId,
                  coachResponse = coachResponse,
                  nextAdjustment = nextAdjustment,
                ) {
                  selectedWorkTab = WorkFeedbackTab.COACH_RESPONDED
                }
              },
              onSubmitPracticeVideo = { videoUri, cameraAngleTag, playerNote ->
                val submitted = viewModel.submitPracticeVideo(
                  assignmentId = activeDrill.assignmentId,
                  drillTitle = activeDrill.title,
                  videoUri = videoUri,
                  cameraAngleTag = cameraAngleTag,
                  playerNote = playerNote,
                )
                cloudController.syncVideoSubmissionToCloud(submitted)
              },
              onSendPlayerMessage = { text ->
                val msg = viewModel.sendTacticalMessage(
                  senderRole = "player",
                  text = text,
                  relatedTitle = activeDrill.title,
                )
                if (msg != null) cloudController.syncMessageToCloud(msg)
              },
              onToggleCoachPortalMode = { enabled ->
                viewModel.setCoachPortalMode(enabled)
              },
              onPublishScopeOfWork = { title, objective, reps, filmAngle, standard ->
                val spec = viewModel.publishNewScopeOfWork(
                  title = title,
                  tacticalObjective = objective,
                  repBreakdown = reps,
                  requiredFilmAngle = filmAngle,
                  successStandard = standard,
                )
                if (spec != null) cloudController.syncNewScopeToCloud(spec)
              },
              onSaveCoachVideoCritique = { videoId, critique, timestamps, nextRep ->
                val updated = viewModel.saveCoachVideoCritique(
                  videoId = videoId,
                  coachCritique = critique,
                  timestampNotes = timestamps,
                  nextRepAdjustment = nextRep,
                )
                if (updated != null) cloudController.syncVideoCritiqueToCloud(updated)
              },
              onSendCoachMessage = { reply ->
                val msg = viewModel.sendTacticalMessage(
                  senderRole = "coach",
                  text = reply,
                  relatedTitle = activeDrill.title,
                )
                if (msg != null) cloudController.syncMessageToCloud(msg)
              },
              onSubmitTeachBack = { change, notice ->
                viewModel.submitPlayerTeachBack(change, notice)
              },
              onCoachMarkTeachBack = { mark, clarifyNote ->
                viewModel.markCoachTeachBack(mark, clarifyNote)
              },
              onLogGameNote = { opponent, carried, missed, feel ->
                viewModel.logGameTransferNote(opponent, carried, missed, feel)
              },
              onCoachMarkGame = { gameId, mark, note ->
                viewModel.markCoachGameNote(gameId, mark, note)
              },
            )
          }
          composable(TopLevelTab.ACCOUNT.route) {
            val activeDrill = uiState.todayAssignment ?: uiState.assignments.first()
            AccountScreen(
              uiState = uiState,
              onSelectScenario = { scenario -> viewModel.applyDemoScenario(scenario) },
              onSelectSetupState = { stage, hasGeneral ->
                viewModel.setSetupState(stage, hasGeneral)
              },
              onResetDemoData = { viewModel.resetDemoData() },
              currentUser = cloudController.currentUser,
              authErrorMessage = cloudController.authErrorMessage,
              onAuthError = cloudController.onAuthError,
              onSignOutSuccess = { viewModel.setCoachPortalMode(false) },
              onToggleCoachPortalMode = { enabled ->
                viewModel.setCoachPortalMode(enabled)
              },
              onPublishScopeOfWork = { title, objective, reps, filmAngle, standard ->
                val spec = viewModel.publishNewScopeOfWork(
                  title = title,
                  tacticalObjective = objective,
                  repBreakdown = reps,
                  requiredFilmAngle = filmAngle,
                  successStandard = standard,
                )
                if (spec != null) cloudController.syncNewScopeToCloud(spec)
              },
              onSaveCoachVideoCritique = { videoId, critique, timestamps, nextRep ->
                val updated = viewModel.saveCoachVideoCritique(
                  videoId = videoId,
                  coachCritique = critique,
                  timestampNotes = timestamps,
                  nextRepAdjustment = nextRep,
                )
                if (updated != null) cloudController.syncVideoCritiqueToCloud(updated)
              },
              onSendCoachMessage = { reply ->
                val msg = viewModel.sendTacticalMessage(
                  senderRole = "coach",
                  text = reply,
                  relatedTitle = activeDrill.title,
                )
                if (msg != null) cloudController.syncMessageToCloud(msg)
              },
            )
          }
          composable(ROUTE_PRACTICE_FLOW) {
            val activeDrill = uiState.activePracticeAssignment
            PracticeFlowScreen(
              uiState = uiState,
              onNavigateBack = {
                val handledInsidePractice = viewModel.stepBackInPractice()
                if (!handledInsidePractice) {
                  navController.popBackStack()
                }
              },
              onNextStage = { viewModel.advancePracticeStage() },
              onToggleTimer = { viewModel.toggleTimer() },
              onResetTimer = { viewModel.resetCurrentStepTimer() },
              onReflectionChanged = { viewModel.updateReflectionDraft(it) },
              onStarterCueClicked = { viewModel.applyStarterCue(it) },
              onSubmitReflection = {
                viewModel.submitReflection {
                  selectedWorkTab = WorkFeedbackTab.AWAITING_COACH
                  navController.navigate(TopLevelTab.WORK_FEEDBACK.route) {
                    popUpTo(TopLevelTab.TODAY.route) {
                      saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                  }
                }
              },
              onSubmitPracticeVideo = { videoUri, cameraAngleTag, playerNote ->
                val submitted = viewModel.submitPracticeVideo(
                  assignmentId = activeDrill.assignmentId,
                  drillTitle = activeDrill.title,
                  videoUri = videoUri,
                  cameraAngleTag = cameraAngleTag,
                  playerNote = playerNote,
                )
                cloudController.syncVideoSubmissionToCloud(submitted)
              },
            )
          }
        }
      }
    }
  }
}
