package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import com.example.data.model.WorkflowScenarioId
import com.example.ui.components.BallerPathBottomBar
import com.example.ui.components.BallerPathNavRail
import com.example.ui.components.TopLevelTab
import com.example.ui.screens.AccountScreen
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
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: TopLevelTab.TODAY.route

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

  fun openPracticeFlow(forceRestart: Boolean = false) {
    if (forceRestart) {
      viewModel.startNewPracticeSession()
    } else {
      viewModel.startOrResumePracticeSession()
    }
    navController.navigate(ROUTE_PRACTICE_FLOW) {
      launchSingleTop = true
    }
  }

  fun handleTodayPrimaryAction() {
    when (uiState.currentScenario) {
      WorkflowScenarioId.READY,
      WorkflowScenarioId.IN_PROGRESS,
      WorkflowScenarioId.DRAFT -> openPracticeFlow(forceRestart = false)
      WorkflowScenarioId.MORE_WORK_READY -> openPracticeFlow(forceRestart = true)
      WorkflowScenarioId.AWAITING_COACH,
      WorkflowScenarioId.FEEDBACK_READY -> navigateToTab(TopLevelTab.WORK_FEEDBACK)
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
            )
          }
          composable(TopLevelTab.PLAN.route) {
            PlanScreen(
              uiState = uiState,
              onOpenPractice = {
                openPracticeFlow(forceRestart = uiState.hasSubmittedTodayReflection)
              },
            )
          }
          composable(TopLevelTab.WORK_FEEDBACK.route) {
            WorkFeedbackScreen(
              uiState = uiState,
              onStartOrResumePractice = { openPracticeFlow(forceRestart = false) },
              onRevealSampleFeedback = { reflectionId ->
                viewModel.revealSampleCoachResponse(reflectionId)
              },
            )
          }
          composable(TopLevelTab.ACCOUNT.route) {
            AccountScreen(
              uiState = uiState,
              onSelectScenario = { scenario -> viewModel.applyDemoScenario(scenario) },
              onResetDemoData = { viewModel.resetDemoData() },
            )
          }
          composable(ROUTE_PRACTICE_FLOW) {
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
                  navController.navigate(TopLevelTab.WORK_FEEDBACK.route) {
                    popUpTo(TopLevelTab.TODAY.route) {
                      saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                  }
                }
              },
            )
          }
        }
      }
    }
  }
}
