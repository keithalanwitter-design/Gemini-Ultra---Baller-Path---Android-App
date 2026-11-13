package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.PlanSession
import com.example.data.model.SessionStatus
import com.example.data.model.SetupStage
import com.example.ui.components.CoachKeithAvatar
import com.example.ui.components.FictionalDemoBadge
import com.example.ui.components.HeadlineVisualBanner
import com.example.ui.components.PlayerHeaderBar
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.NavyDeep
import com.example.ui.viewmodel.BallerPathUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanScreen(
  uiState: BallerPathUiState,
  onOpenPractice: (assignmentId: String, forceRestart: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  val setupStage = uiState.setupState.setupStage
  val completedCount = uiState.planSessions.count { it.status == SessionStatus.COMPLETED }
  val totalCount = uiState.planSessions.size.coerceAtLeast(1)
  val plan = uiState.developmentPlan

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
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      PlayerHeaderBar(
        playerName = uiState.player.displayName,
        jerseyAndPosition = "${uiState.player.position} · ${uiState.player.grade}",
        demoBadgeLabel = if (BuildConfig.DEBUG) "Sample Plan · ${uiState.currentScenario.label}" else "Fictional Player · Sample Plan",
      )

      when (setupStage) {
        SetupStage.SETUP_IN_PROGRESS -> {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("plan_setup_in_progress_card"),
            shape = MaterialTheme.shapes.large,
            color = NavyDeep,
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "SETUP IN PROGRESS",
                style = MaterialTheme.typography.labelMedium,
                color = BurntOrange,
              )
              Text(
                text = "Coach Keith is reviewing ${uiState.player.firstName}'s info.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
              )
              Text(
                text = "You'll get access within 2 business days. Personal weekly drills stay hidden until your family plan is activated.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f),
              )
            }
          }
        }
        SetupStage.PLAN_BEING_PREPARED -> {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("plan_being_prepared_card"),
            shape = MaterialTheme.shapes.large,
            color = NavyDeep,
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "PLAN BEING PREPARED",
                style = MaterialTheme.typography.labelMedium,
                color = BurntOrange,
              )
              Text(
                text = "${uiState.player.firstName}'s personal focus is on the way.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
              )
              Text(
                text = if (uiState.setupState.hasGeneralWorkPublished) {
                  "While Coach Keith finishes ${uiState.player.firstName}'s weekly focus, general work is published and ready to start below."
                } else {
                  "Coach Keith is finalizing ${uiState.player.firstName}'s first personal drills now."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f),
              )
            }
          }

          val generalItem = uiState.generalWorkItem
          if (uiState.setupState.hasGeneralWorkPublished && generalItem != null) {
            val general = generalItem.assignment
            PlanSessionCard(
              session = PlanSession(
                id = general.assignmentId,
                assignmentId = general.assignmentId,
                weekLabel = "General Work",
                dayLabel = "Published now",
                title = general.title,
                durationMinutes = general.totalMinutes,
                focusSummary = general.focusHeadline,
                status = if (generalItem.submissions.isNotEmpty() && !general.hasDraftOrSession) {
                  SessionStatus.COMPLETED
                } else {
                  SessionStatus.TODAY
                },
                isInteractivePractice = true,
              ),
              actionLabel = when {
                general.hasDraftOrSession -> "Resume"
                generalItem.submissions.isNotEmpty() -> "Practice ${general.title} Again"
                else -> "Start"
              },
              onOpenPractice = {
                val restart = generalItem.submissions.isNotEmpty() && !general.hasDraftOrSession
                onOpenPractice(general.assignmentId, restart)
              },
            )
          }
        }
        SetupStage.WORK_READY -> {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = NavyDeep,
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = "CURRENT FOCUS",
                  style = MaterialTheme.typography.labelMedium,
                  color = BurntOrange,
                )
                FictionalDemoBadge(
                  text = "$completedCount of $totalCount done",
                  onDarkBackground = true,
                )
              }

              Text(
                text = plan.focus,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
              )

              Text(
                text = "Why it matters: ${plan.why}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f),
              )

              LinearProgressIndicator(
                progress = { completedCount.toFloat() / totalCount.toFloat() },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(50)),
                color = BurntOrange,
                trackColor = Color.White.copy(alpha = 0.2f),
              )

              Surface(
                shape = MaterialTheme.shapes.small,
                color = Color.White.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    CoachKeithAvatar(sizeDp = 34)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = "Success standard · ${plan.reviewLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BurntOrange,
                      )
                      Text(
                        text = plan.successStandard,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                      )
                    }
                  }
                }
              }
            }
          }

          Text(
            text = "This Week's Sessions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )

          uiState.planSessions.forEach { session ->
            val workItem = session.assignmentId?.let { aId ->
              uiState.personalWorkItems.firstOrNull { it.assignment.assignmentId == aId }
            }
            val hasDraft = workItem?.assignment?.hasDraftOrSession == true
            val isSubmitted = workItem?.submissions?.isNotEmpty() == true
            val label = when {
              hasDraft -> "Resume"
              isSubmitted -> "Practice ${session.title} Again"
              else -> "Start"
            }

            PlanSessionCard(
              session = session,
              actionLabel = label,
              onOpenPractice = {
                val targetId = session.assignmentId
                if (targetId != null) {
                  onOpenPractice(targetId, isSubmitted && !hasDraft)
                }
              },
            )
          }
        }
      }

      HeadlineVisualBanner(
        drawableRes = R.drawable.img_headline_work_purpose,
        contentDescription = stringResource(R.string.cd_headline_work_purpose),
        modifier = Modifier.testTag("plan_headline_visual"),
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun PlanSessionCard(
  session: PlanSession,
  actionLabel: String,
  onOpenPractice: () -> Unit,
) {
  val isToday = session.status == SessionStatus.TODAY

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("plan_session_${session.id}"),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(
      width = if (isToday) 1.5.dp else 1.dp,
      color = if (isToday) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    ),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${session.dayLabel} · ${session.durationMinutes} min",
          style = MaterialTheme.typography.labelMedium,
          color = if (isToday) BurntOrange else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SessionStatusChip(status = session.status)
      }

      Text(
        text = session.title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text = session.focusSummary,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (session.isInteractivePractice && session.assignmentId != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Button(
          onClick = onOpenPractice,
          modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
              contentDescription = "$actionLabel: ${session.title}"
            }
            .testTag("plan_open_practice_${session.id}"),
          colors = ButtonDefaults.buttonColors(
            containerColor = BurntOrange,
            contentColor = Color.White,
          ),
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}

@Composable
private fun SessionStatusChip(status: SessionStatus) {
  val (label, bg, fg, icon) = when (status) {
    SessionStatus.COMPLETED -> Quadruple(
      "Done",
      MaterialTheme.colorScheme.tertiaryContainer,
      MaterialTheme.colorScheme.onTertiaryContainer,
      Icons.Filled.CheckCircle,
    )
    SessionStatus.TODAY -> Quadruple(
      "Today's Focus",
      BurntOrange,
      Color.White,
      Icons.Filled.PlayArrow,
    )
    SessionStatus.UPCOMING -> Quadruple(
      "Up Next",
      MaterialTheme.colorScheme.surfaceVariant,
      MaterialTheme.colorScheme.onSurfaceVariant,
      Icons.Outlined.LockClock,
    )
  }

  Surface(
    shape = RoundedCornerShape(50),
    color = bg,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = fg,
        modifier = Modifier.size(14.dp),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
      )
    }
  }
}

private data class Quadruple<A, B, C, D>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D,
)
