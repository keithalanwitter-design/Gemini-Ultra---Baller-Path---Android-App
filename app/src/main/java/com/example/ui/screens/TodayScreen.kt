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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
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
  val focus = uiState.todayFocus
  val scenario = uiState.currentScenario

  val stateChipLabel = when (scenario) {
    WorkflowScenarioId.READY -> "Practice ready"
    WorkflowScenarioId.IN_PROGRESS -> "Practice in progress · ${uiState.activeStepTitle}"
    WorkflowScenarioId.DRAFT -> "Reflection draft saved"
    WorkflowScenarioId.AWAITING_COACH -> "Awaiting coach"
    WorkflowScenarioId.FEEDBACK_READY ->
      if (BuildConfig.DEBUG) "Sample feedback ready" else "Coach responded"
    WorkflowScenarioId.MORE_WORK_READY -> "More work ready"
  }

  val statusDetailMessage = when (scenario) {
    WorkflowScenarioId.READY -> null
    WorkflowScenarioId.IN_PROGRESS ->
      "Left off on ${uiState.activeStepTitle}. Pick up right where you paused."
    WorkflowScenarioId.DRAFT ->
      "Unfinished reflection saved on this phone. Finish and submit when ready."
    WorkflowScenarioId.AWAITING_COACH ->
      "Reflection saved locally. Coach review pending (${uiState.developmentPlan.reviewLabel})."
    WorkflowScenarioId.FEEDBACK_READY ->
      if (BuildConfig.DEBUG) {
        "Coach Keith's sample response and next adjustment are ready below."
      } else {
        "Coach Keith's response and next adjustment are ready in Work + Feedback."
      }
    WorkflowScenarioId.MORE_WORK_READY ->
      "Previous work saved. Next drill is ready whenever you are."
  }

  val ctaIcon = when (scenario) {
    WorkflowScenarioId.READY,
    WorkflowScenarioId.IN_PROGRESS,
    WorkflowScenarioId.MORE_WORK_READY -> Icons.Filled.PlayArrow
    WorkflowScenarioId.DRAFT -> Icons.Filled.EditNote
    WorkflowScenarioId.AWAITING_COACH -> Icons.Filled.HourglassTop
    WorkflowScenarioId.FEEDBACK_READY -> Icons.Filled.Forum
  }

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

      // Hero Focus Card: answers "What should I work on today?" with ONE primary action for where Maya left off
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
                text = focus.headlineQuestion.uppercase(),
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
                text = focus.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
              )
              Text(
                text = focus.focusHeadline,
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

            // Coach Keith cue
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
                    text = "${uiState.player.coachName} · ${focus.totalMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = BurntOrangeTint,
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = focus.coachCue,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                  )
                }
              }
            }

            // ONE Main Action driven by where the player left off
            Button(
              onClick = onPrimaryAction,
              modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 50.dp)
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
                text = scenario.primaryActionLabel,
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

      HeadlineVisualBanner(
        drawableRes = R.drawable.img_headline_one_priority,
        contentDescription = stringResource(R.string.cd_headline_one_priority),
        modifier = Modifier.testTag("today_headline_visual"),
      )

      // Compact 8-minute breakdown below the fold
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
              text = "Inside Today's 8-Minute Flow",
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
                text = "3 steps + reflection",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          focus.steps.forEach { step ->
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

      Spacer(modifier = Modifier.height(6.dp))
    }
  }
}
