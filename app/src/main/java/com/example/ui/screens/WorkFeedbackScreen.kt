package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.AssignmentWorkItem
import com.example.data.model.PracticeReflection
import com.example.data.model.SetupStage
import com.example.data.model.WorkFeedbackTab
import com.example.ui.components.CoachAudioListenButton
import com.example.ui.components.CoachFeedbackStudioSection
import com.example.ui.components.CoachKeithAvatar
import com.example.ui.components.DevelopmentStatsChartCard
import com.example.ui.components.FictionalDemoBadge
import com.example.ui.components.HeadlineVisualBanner
import com.example.ui.components.InYourWordsTeachBackCard
import com.example.ui.components.PlayerHeaderBar
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.NavyDeep
import com.example.ui.viewmodel.BallerPathUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkFeedbackScreen(
  uiState: BallerPathUiState,
  selectedTab: WorkFeedbackTab,
  onSelectTab: (WorkFeedbackTab) -> Unit,
  onStartOrResumeAssignment: (String) -> Unit,
  onRevealSampleFeedback: (Long) -> Unit,
  onSaveCustomCoachFeedback: (reflectionId: Long, coachResponse: String, nextAdjustment: String) -> Unit = { _, _, _ -> },
  onSaveTeachBack: (whatWillDoDifferently: String, whatWillNotice: String) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier,
) {
  val setupStage = uiState.setupState.setupStage
  val todoItems = uiState.todoWorkItems
  val awaitingItems = uiState.awaitingCoachItems
  val respondedItems = uiState.coachRespondedItems
  val seedEntries = if (setupStage == SetupStage.WORK_READY) uiState.seedReflections else emptyList()
  val listState = rememberLazyListState()

  LaunchedEffect(selectedTab) {
    listState.scrollToItem(0)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      state = listState,
      modifier = Modifier
        .widthIn(max = 720.dp)
        .fillMaxSize()
        .testTag("work_feedback_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        PlayerHeaderBar(
          playerName = uiState.player.displayName,
          jerseyAndPosition = "${uiState.player.position} · ${uiState.player.grade}",
          demoBadgeLabel = if (BuildConfig.DEBUG) uiState.currentScenario.label else uiState.player.demoBadgeLabel,
        )
      }

      item {
        Text(
          text = "Work + Feedback",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }

      if (setupStage == SetupStage.SETUP_IN_PROGRESS) {
        item {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("work_setup_in_progress_card"),
            shape = MaterialTheme.shapes.large,
            color = NavyDeep,
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "SETUP IN PROGRESS",
                style = MaterialTheme.typography.labelMedium,
                color = BurntOrange,
              )
              Text(
                text = "Coach Keith reviews introductions within 2 business days and activates access separately.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
              )
            }
          }
        }
      } else {
        // Three clean views: To do, Awaiting coach, Coach responded
        item {
          FlowRow(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("work_feedback_tabs"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            WorkTabPill(
              label = "To do",
              count = todoItems.size,
              selected = selectedTab == WorkFeedbackTab.TO_DO,
              testTag = "wf_tab_todo",
              onClick = { onSelectTab(WorkFeedbackTab.TO_DO) },
            )
            WorkTabPill(
              label = "Awaiting coach",
              count = awaitingItems.size,
              selected = selectedTab == WorkFeedbackTab.AWAITING_COACH,
              testTag = "wf_tab_awaiting",
              onClick = { onSelectTab(WorkFeedbackTab.AWAITING_COACH) },
            )
            WorkTabPill(
              label = "Coach responded",
              count = respondedItems.size + seedEntries.size,
              selected = selectedTab == WorkFeedbackTab.COACH_RESPONDED,
              testTag = "wf_tab_responded",
              onClick = { onSelectTab(WorkFeedbackTab.COACH_RESPONDED) },
            )
          }
        }

        when (selectedTab) {
          WorkFeedbackTab.TO_DO -> {
            if (todoItems.isEmpty()) {
              item {
                EmptyWorkTabCard(
                  title = "Nothing in To do right now",
                  body = "All assigned drills are submitted. Check Awaiting coach or Coach responded.",
                  testTag = "wf_empty_todo",
                )
              }
            } else {
              items(
                items = todoItems,
                key = { "todo_${it.assignment.assignmentId}" },
              ) { item ->
                TodoAssignmentCard(
                  workItem = item,
                  onStartOrResume = { onStartOrResumeAssignment(item.assignment.assignmentId) },
                )
              }
            }
          }

          WorkFeedbackTab.AWAITING_COACH -> {
            if (awaitingItems.isEmpty()) {
              item {
                EmptyWorkTabCard(
                  title = "Nothing waiting for review",
                  body = "Finish a drill in To do and submit your reflection for Coach Keith.",
                  testTag = "wf_empty_awaiting",
                )
              }
            } else {
              items(
                items = awaitingItems,
                key = { "awaiting_${it.assignment.assignmentId}" },
              ) { item ->
                val latest = item.latestSubmission
                if (latest != null) {
                  AwaitingCoachAssignmentCard(
                    workItem = item,
                    reflection = latest,
                    playerName = uiState.player.displayName,
                    reviewLabel = uiState.developmentPlan.reviewLabel,
                    onRevealSampleFeedback = { onRevealSampleFeedback(latest.id) },
                    onSaveCustomCoachFeedback = { coachResponse, nextAdjustment ->
                      onSaveCustomCoachFeedback(latest.id, coachResponse, nextAdjustment)
                    },
                  )
                }
              }
            }
          }

          WorkFeedbackTab.COACH_RESPONDED -> {
            if (respondedItems.isEmpty() && seedEntries.isEmpty()) {
              item {
                EmptyWorkTabCard(
                  title = "No coach responses yet",
                  body = "Once Coach Keith reviews a reflection, his response and Try the next rep note appear together here.",
                  testTag = "wf_empty_responded",
                )
              }
            } else {
              items(
                items = respondedItems,
                key = { "responded_${it.assignment.assignmentId}" },
              ) { item ->
                CoachRespondedAssignmentCard(
                  assignmentId = item.assignment.assignmentId,
                  drillTitle = item.assignment.title,
                  submissions = item.submissions,
                  playerName = uiState.player.displayName,
                  isSampleSeed = false,
                  onSaveCustomCoachFeedback = { reflectionId, coachResponse, nextAdjustment ->
                    onSaveCustomCoachFeedback(reflectionId, coachResponse, nextAdjustment)
                  },
                )
              }

              items(
                items = seedEntries,
                key = { "seed_${it.id}" },
              ) { seed ->
                CoachRespondedAssignmentCard(
                  assignmentId = seed.assignmentId,
                  drillTitle = seed.drillTitle,
                  submissions = listOf(seed),
                  playerName = uiState.player.displayName,
                  isSampleSeed = true,
                )
              }

              item {
                InYourWordsTeachBackCard(
                  teachBackEntry = uiState.teachBackEntry,
                  onSaveTeachBack = onSaveTeachBack,
                )
              }
            }
          }
        }

        item {
          DevelopmentStatsChartCard(
            stats = uiState.lastSevenDayStats,
            playerFirstName = uiState.player.firstName,
          )
        }
      }

      item {
        HeadlineVisualBanner(
          drawableRes = R.drawable.img_headline_notice_grow,
          contentDescription = stringResource(R.string.cd_headline_notice_grow),
          modifier = Modifier.testTag("work_headline_visual"),
        )
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun WorkTabPill(
  label: String,
  count: Int,
  selected: Boolean,
  testTag: String,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    modifier = Modifier
      .defaultMinSize(minHeight = 48.dp)
      .semantics(mergeDescendants = true) {
        contentDescription = "$label filter tab, $count items${if (selected) ", selected" else ""}"
      }
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    color = if (selected) NavyDeep else MaterialTheme.colorScheme.surface,
    border = BorderStroke(
      width = if (selected) 1.5.dp else 1.dp,
      color = if (selected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    ),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      )
      Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) BurntOrange else MaterialTheme.colorScheme.surfaceVariant,
      ) {
        Text(
          text = "$count",
          style = MaterialTheme.typography.labelSmall,
          color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
      }
    }
  }
}

@Composable
private fun EmptyWorkTabCard(
  title: String,
  body: String,
  testTag: String,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = body,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodoAssignmentCard(
  workItem: AssignmentWorkItem,
  onStartOrResume: () -> Unit,
) {
  val assignment = workItem.assignment
  val hasDraft = assignment.hasDraftOrSession

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("todo_card_${assignment.assignmentId}"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(
      width = if (hasDraft) 1.5.dp else 1.dp,
      color = if (hasDraft) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    ),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Column {
          Text(
            text = assignment.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${assignment.dayTag} · ${assignment.totalMinutes} min",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        FictionalDemoBadge(
          text = when {
            assignment.reflectionDraft.isNotBlank() || assignment.isReflectionStage -> "Draft saved"
            assignment.hasActiveSession -> "In progress"
            else -> "Ready"
          },
        )
      }

      Text(
        text = assignment.focusHeadline,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (assignment.reflectionDraft.isNotBlank()) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("draft_preview_${assignment.assignmentId}"),
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Text(
            text = "Draft: \"${assignment.reflectionDraft}\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(10.dp),
          )
        }
      }

      val buttonAction = if (hasDraft) "Resume" else "Start"
      Button(
        onClick = onStartOrResume,
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 48.dp)
          .semantics {
            contentDescription = "$buttonAction ${assignment.title}"
          }
          .testTag("todo_action_${assignment.assignmentId}"),
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
          text = if (hasDraft) "Resume" else "Start",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AwaitingCoachAssignmentCard(
  workItem: AssignmentWorkItem,
  reflection: PracticeReflection,
  playerName: String,
  reviewLabel: String,
  onRevealSampleFeedback: () -> Unit,
  onSaveCustomCoachFeedback: (coachResponse: String, nextAdjustment: String) -> Unit,
) {
  val dateFormatter = SimpleDateFormat("MMM d · h:mm a", Locale.getDefault())
  val timestampLabel = dateFormatter.format(Date(reflection.createdAtMillis))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("awaiting_card_${workItem.assignment.assignmentId}"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, BurntOrange),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Column {
          Text(
            text = workItem.assignment.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Submitted · $timestampLabel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        FictionalDemoBadge(
          text = "Awaiting Coach",
          modifier = Modifier.align(Alignment.CenterVertically),
        )
      }

      PlayerObservationBox(
        playerName = playerName,
        observation = reflection.laneObservation,
        modifier = Modifier.fillMaxWidth(),
      )

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("awaiting_coach_card_${workItem.assignment.assignmentId}"),
        shape = MaterialTheme.shapes.medium,
        color = NavyDeep,
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.HourglassTop,
              contentDescription = null,
              tint = BurntOrange,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "AWAITING COACH",
              style = MaterialTheme.typography.labelMedium,
              color = BurntOrange,
            )
          }
          Text(
            text = "Saved. Coach Keith will review it ${reviewLabel.lowercase()}.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
          )
        }
      }

      if (BuildConfig.DEBUG) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.primaryContainer,
          border = BorderStroke(1.dp, BurntOrange.copy(alpha = 0.5f)),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "PROTOTYPE DEMO CONTROL",
              style = MaterialTheme.typography.labelSmall,
              color = BurntOrange,
            )
            Text(
              text = "Want to preview what appears after Coach Keith replies?",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            OutlinedButton(
              onClick = onRevealSampleFeedback,
              modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .semantics {
                  contentDescription = "Demo: Reveal sample Coach Keith response for ${workItem.assignment.title}"
                }
                .testTag("reveal_feedback_${workItem.assignment.assignmentId}"),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.5.dp, BurntOrange),
            ) {
              Icon(
                imageVector = Icons.Filled.Visibility,
                contentDescription = null,
                tint = BurntOrange,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Demo: Reveal Sample Coach Response",
                style = MaterialTheme.typography.labelLarge,
                color = BurntOrange,
              )
            }
          }
        }

        CoachFeedbackStudioSection(
          assignmentId = workItem.assignment.assignmentId,
          drillTitle = workItem.assignment.title,
          initialCoachResponse = "",
          initialNextAdjustment = "",
          onSaveCoachFeedback = onSaveCustomCoachFeedback,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoachRespondedAssignmentCard(
  assignmentId: String,
  drillTitle: String,
  submissions: List<PracticeReflection>,
  playerName: String,
  isSampleSeed: Boolean,
  onSaveCustomCoachFeedback: (reflectionId: Long, coachResponse: String, nextAdjustment: String) -> Unit = { _, _, _ -> },
) {
  val dateFormatter = SimpleDateFormat("MMM d · h:mm a", Locale.getDefault())
  val latest = submissions.first()
  val timestampLabel = dateFormatter.format(Date(latest.createdAtMillis))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(if (isSampleSeed) "seed_reflection_card" else "responded_card_$assignmentId"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(
      width = if (isSampleSeed) 1.dp else 1.5.dp,
      color = if (isSampleSeed) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
      } else {
        BurntOrange
      },
    ),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Column {
          Text(
            text = drillTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (isSampleSeed) {
              "${latest.readTag} · $timestampLabel"
            } else {
              "Coach responded · $timestampLabel"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        FictionalDemoBadge(
          text = if (isSampleSeed) {
            "Sample Seed"
          } else {
            "Sample Coach Response"
          },
          modifier = Modifier.align(Alignment.CenterVertically),
        )
      }

      submissions.forEachIndexed { index, entry ->
        if (index > 0) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(15.dp),
            )
            Text(
              text = "Earlier submission · ${dateFormatter.format(Date(entry.createdAtMillis))}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Keeps together: player's reflection, Coach Keith's response, and Try the next rep note
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
          val useSideBySide = maxWidth >= 540.dp
          if (useSideBySide) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              PlayerObservationBox(
                playerName = playerName,
                observation = entry.laneObservation,
                modifier = Modifier.weight(1f),
              )
              SampleCoachResponseBox(
                label = entry.sampleCoachLabel,
                response = entry.sampleCoachResponse,
                nextAdjustment = entry.nextAdjustment,
                listenSessionKey = "${assignmentId}_$index",
                listenTestTag = "listen_coach_response_${assignmentId}_$index",
                testTag = "coach_response_${assignmentId}_$index",
                modifier = Modifier.weight(1f),
              )
            }
          } else {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              PlayerObservationBox(
                playerName = playerName,
                observation = entry.laneObservation,
                modifier = Modifier.fillMaxWidth(),
              )
              SampleCoachResponseBox(
                label = entry.sampleCoachLabel,
                response = entry.sampleCoachResponse,
                nextAdjustment = entry.nextAdjustment,
                listenSessionKey = "${assignmentId}_$index",
                listenTestTag = "listen_coach_response_${assignmentId}_$index",
                testTag = "coach_response_${assignmentId}_$index",
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        }

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("next_rep_${assignmentId}_$index"),
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.primaryContainer,
          border = BorderStroke(1.dp, BurntOrange.copy(alpha = 0.45f)),
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
          ) {
            Icon(
              imageVector = Icons.Outlined.TrackChanges,
              contentDescription = null,
              tint = BurntOrange,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = stringResource(R.string.next_adjustment_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = BurntOrange,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = entry.nextAdjustment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
      }

      if (BuildConfig.DEBUG && !isSampleSeed) {
        CoachFeedbackStudioSection(
          assignmentId = assignmentId,
          drillTitle = drillTitle,
          initialCoachResponse = latest.sampleCoachResponse,
          initialNextAdjustment = latest.nextAdjustment,
          toggleButtonLabelOpen = "Update Coach Feedback (Voice or Text)",
          onSaveCoachFeedback = { updatedResponse, updatedNextRep ->
            onSaveCustomCoachFeedback(latest.id, updatedResponse, updatedNextRep)
          },
        )
      }
    }
  }
}

@Composable
private fun PlayerObservationBox(
  playerName: String,
  observation: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surfaceVariant,
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.EditNote,
          contentDescription = null,
          tint = NavyDeep,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "WHAT YOU NOTICED · ${playerName.uppercase()}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(
        text = observation,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
private fun SampleCoachResponseBox(
  label: String,
  response: String,
  nextAdjustment: String,
  listenSessionKey: String,
  listenTestTag: String,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.testTag(testTag),
    shape = MaterialTheme.shapes.medium,
    color = NavyDeep,
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CoachKeithAvatar(sizeDp = 30)
          Column {
            Text(
              text = label.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = BurntOrange,
            )
            Text(
              text = "Simulated locally for prototype demo",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.72f),
            )
          }
        }
        CoachAudioListenButton(
          sessionKey = listenSessionKey,
          textToSpeak = "$response. Try the next rep: $nextAdjustment",
          testTag = listenTestTag,
        )
      }
      Text(
        text = response,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
      )
    }
  }
}
