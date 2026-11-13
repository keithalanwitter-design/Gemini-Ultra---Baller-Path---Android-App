package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.SetupStage
import com.example.data.model.WorkflowScenarioId
import com.example.ui.components.CoachKeithAvatar
import com.example.ui.components.FictionalDemoBadge
import com.example.ui.components.HeadlineVisualBanner
import com.example.ui.components.PlayerHeaderBar
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeTint
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.BallerPathUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayScreen(
  uiState: BallerPathUiState,
  onPrimaryAction: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  val setupStage = uiState.setupState.setupStage

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 620.dp)
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      PlayerHeaderBar(
        playerName = uiState.player.displayName,
        jerseyAndPosition = "${uiState.player.position} · ${uiState.player.grade}",
        demoBadgeLabel = uiState.player.demoBadgeLabel,
      )

      when {
        setupStage == SetupStage.SETUP_IN_PROGRESS -> {
          SetupWaitingCard(
            badgeText = "Setup in progress",
            headline = "Getting ${uiState.player.firstName}'s plan ready",
            message = "Coach Keith is reviewing ${uiState.player.firstName}'s info. You'll get access within 2 business days.",
            coachNote = "Once your family profile is reviewed, Coach Keith will publish ${uiState.player.firstName}'s first focus right here.",
            testTag = "today_setup_in_progress_card",
          )
        }
        setupStage == SetupStage.PLAN_BEING_PREPARED && uiState.todayAssignment == null -> {
          SetupWaitingCard(
            badgeText = "Plan being prepared",
            headline = "${uiState.player.firstName} is active",
            message = "Coach Keith is putting together ${uiState.player.firstName}'s personal focus. Any general warm-up work will appear here once published.",
            coachNote = "Check back soon for ${uiState.player.firstName}'s first assigned drill.",
            testTag = "today_plan_being_prepared_card",
          )
        }
        else -> {
          TodayActiveHeroContent(
            uiState = uiState,
            onPrimaryAction = onPrimaryAction,
          )
        }
      }

      HeadlineVisualBanner(
        drawableRes = R.drawable.img_headline_one_priority,
        contentDescription = stringResource(R.string.cd_headline_one_priority),
        modifier = Modifier.testTag("today_headline_visual"),
      )

      val flowAssignment = uiState.todayAssignment
      if (flowAssignment != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "Inside Today's ${flowAssignment.totalMinutes}-Minute Flow",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Outlined.Schedule,
                  contentDescription = null,
                  tint = BurntOrange,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${flowAssignment.steps.size} steps + reflection",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            flowAssignment.steps.forEach { step ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(
                  modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = "${step.stepNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = step.instruction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${step.durationMinutes}m",
                  style = MaterialTheme.typography.labelLarge,
                  color = BurntOrange,
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodayActiveHeroContent(
  uiState: BallerPathUiState,
  onPrimaryAction: () -> Unit,
) {
  val targetAssignment = uiState.todayAssignment
  val scenario = uiState.currentScenario
  val actionLabel = uiState.todayPrimaryActionLabel ?: "Start"

  val stateChipLabel = when {
    uiState.setupState.setupStage == SetupStage.PLAN_BEING_PREPARED && targetAssignment != null -> {
      if (targetAssignment.hasDraftOrSession) "General work in progress" else "General work ready"
    }
    scenario == WorkflowScenarioId.DRAFT -> "Reflection draft saved"
    scenario == WorkflowScenarioId.IN_PROGRESS -> "Practice in progress · ${uiState.todayStepTitle}"
    scenario == WorkflowScenarioId.MORE_WORK_READY ->
      if (BuildConfig.DEBUG) "More work ready" else "Practice ready"
    scenario == WorkflowScenarioId.AWAITING_COACH -> "Awaiting coach"
    scenario == WorkflowScenarioId.FEEDBACK_READY ->
      if (BuildConfig.DEBUG) "Sample feedback ready" else "Coach responded"
    else -> "Practice ready"
  }

  val statusDetailMessage = when {
    uiState.setupState.setupStage == SetupStage.PLAN_BEING_PREPARED && targetAssignment != null ->
      "Coach Keith is preparing ${uiState.player.firstName}'s personal focus. You can start this general work now."
    scenario == WorkflowScenarioId.DRAFT ->
      "Unfinished reflection saved on this phone for ${targetAssignment?.title ?: "this drill"}. Finish and submit when ready."
    scenario == WorkflowScenarioId.IN_PROGRESS ->
      "Left off on ${uiState.todayStepTitle}. Pick up right where you paused."
    targetAssignment != null && uiState.hasAnySubmittedPersonalWork ->
      "Saved. Coach Keith will review it. Next ready drill: ${targetAssignment.title}."
    uiState.isAllPersonalWorkSubmitted && uiState.hasAnyAwaitingPersonalWork ->
      "Saved. Coach Keith will review it. (${uiState.developmentPlan.reviewLabel})"
    uiState.isAllPersonalWorkSubmitted && uiState.hasAnyRevealedPersonalFeedback ->
      "Saved. Coach Keith has reviewed your reflections in Work + Feedback."
    else -> null
  }

  val ctaIcon = when {
    scenario == WorkflowScenarioId.DRAFT -> Icons.Filled.EditNote
    targetAssignment != null -> Icons.Filled.PlayArrow
    uiState.hasAnyAwaitingPersonalWork -> Icons.Filled.HourglassTop
    else -> Icons.Filled.Forum
  }

  val heroTitle = targetAssignment?.title ?: "All assigned drills submitted"
  val heroSubtitle = targetAssignment?.focusHeadline
    ?: "Check Work + Feedback for your reflections and coach notes."
  val heroCoachNote = targetAssignment?.coachCue
    ?: "Check Work + Feedback anytime to review what you noticed and Coach Keith's responses."
  val minutesLabel = targetAssignment?.let { "${it.totalMinutes} min" } ?: "Review"

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("today_focus_card"),
    shape = MaterialTheme.shapes.large,
    shadowElevation = 4.dp,
  ) {
    Box(
      modifier = Modifier
        .background(
          Brush.linearGradient(
            colors = listOf(NavyDeep, NavyPrimary, NavyElevated)
          )
        )
        .padding(14.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = (targetAssignment?.headlineQuestion ?: "TODAY'S STATUS").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrangeTint,
            modifier = Modifier.align(Alignment.CenterVertically),
          )
          FictionalDemoBadge(
            text = stateChipLabel,
            onDarkBackground = true,
            modifier = Modifier.testTag("today_state_badge"),
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = heroTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
          )
          Text(
            text = heroSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.92f),
          )
        }

        if (statusDetailMessage != null) {
          Surface(
            shape = MaterialTheme.shapes.small,
            color = BurntOrange.copy(alpha = 0.28f),
            border = BorderStroke(1.dp, BurntOrange),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("today_status_message"),
          ) {
            Text(
              text = statusDetailMessage,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
          }
        }

        Surface(
          shape = MaterialTheme.shapes.medium,
          color = Color.White.copy(alpha = 0.10f),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            CoachKeithAvatar(sizeDp = 36)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${uiState.player.coachName} · $minutesLabel",
                style = MaterialTheme.typography.labelSmall,
                color = BurntOrangeTint,
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = heroCoachNote,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
              )
            }
          }
        }

        Button(
          onClick = onPrimaryAction,
          modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp)
            .semantics {
              contentDescription = "$actionLabel: $heroTitle"
            }
            .testTag("today_primary_action_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BurntOrange,
            contentColor = Color.White,
          ),
          contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        ) {
          Icon(
            imageVector = ctaIcon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupWaitingCard(
  badgeText: String,
  headline: String,
  message: String,
  coachNote: String,
  testTag: String,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = MaterialTheme.shapes.large,
    shadowElevation = 4.dp,
  ) {
    Box(
      modifier = Modifier
        .background(
          Brush.linearGradient(
            colors = listOf(NavyDeep, NavyPrimary, NavyElevated)
          )
        )
        .padding(16.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "BALLER PATH ONBOARDING",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrangeTint,
            modifier = Modifier.align(Alignment.CenterVertically),
          )
          FictionalDemoBadge(
            text = badgeText,
            onDarkBackground = true,
            modifier = Modifier.testTag("today_state_badge"),
          )
        }

        Text(
          text = headline,
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
        )

        Surface(
          shape = MaterialTheme.shapes.small,
          color = BurntOrange.copy(alpha = 0.28f),
          border = BorderStroke(1.dp, BurntOrange),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("today_status_message"),
        ) {
          Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(12.dp),
          )
        }

        Surface(
          shape = MaterialTheme.shapes.medium,
          color = Color.White.copy(alpha = 0.10f),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            CoachKeithAvatar(sizeDp = 36)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = coachNote,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White,
            )
          }
        }
      }
    }
  }
}
