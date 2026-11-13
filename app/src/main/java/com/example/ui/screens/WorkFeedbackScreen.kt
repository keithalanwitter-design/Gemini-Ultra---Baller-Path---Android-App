package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.PracticeReflection
import com.example.data.model.WorkflowScenarioId
import com.example.ui.components.CoachKeithAvatar
import com.example.ui.components.FictionalDemoBadge
import com.example.ui.components.HeadlineVisualBanner
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
  onStartOrResumePractice: () -> Unit,
  onRevealSampleFeedback: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scenario = uiState.currentScenario
  val hasDraft = uiState.practice.reflectionDraft.isNotBlank() || uiState.practice.isReflectionStage

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier
        .widthIn(max = 720.dp)
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        Spacer(modifier = Modifier.height(10.dp))
        PlayerHeaderBar(
          playerName = uiState.player.displayName,
          jerseyAndPosition = "${uiState.player.position} · ${uiState.player.grade}",
          demoBadgeLabel = if (BuildConfig.DEBUG) scenario.label else "",
        )
      }

      item {
        Text(
          text = "Work + Feedback",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }

      // Unfinished draft notice (kept clearly distinct from submitted reflections)
      if (hasDraft) {
        item {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("work_unfinished_draft_banner"),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
            border = BorderStroke(1.dp, NavyDeep.copy(alpha = 0.35f)),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "UNFINISHED DRAFT · NOT SUBMITTED YET",
                style = MaterialTheme.typography.labelSmall,
                color = NavyDeep,
              )
              if (uiState.practice.reflectionDraft.isNotBlank()) {
                Text(
                  text = "\"${uiState.practice.reflectionDraft}\"",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
              Button(
                onClick = onStartOrResumePractice,
                modifier = Modifier
                  .fillMaxWidth()
                  .defaultMinSize(minHeight = 48.dp)
                  .testTag("work_finish_reflection_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = BurntOrange,
                  contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
              ) {
                Text(
                  text = "Finish reflection",
                  style = MaterialTheme.typography.labelLarge,
                )
              }
            }
          }
        }
      } else if (!uiState.hasSubmittedTodayReflection) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, BurntOrange.copy(alpha = 0.5f)),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "Today's Pass, Look, Decide reflection isn't submitted yet.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Button(
                onClick = onStartOrResumePractice,
                modifier = Modifier
                  .fillMaxWidth()
                  .defaultMinSize(minHeight = 48.dp)
                  .testTag("feedback_start_practice_button"),
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
                  text = if (scenario == WorkflowScenarioId.IN_PROGRESS) {
                    "Resume practice"
                  } else {
                    "Start practice"
                  },
                  style = MaterialTheme.typography.labelLarge,
                )
              }
            }
          }
        }
      }

      items(
        items = uiState.reflections,
        key = { it.id },
      ) { reflection ->
        ReflectionComparisonCard(
          reflection = reflection,
          playerName = uiState.player.displayName,
          reviewLabel = uiState.developmentPlan.reviewLabel,
          onRevealSampleFeedback = { onRevealSampleFeedback(reflection.id) },
        )
      }

      item {
        HeadlineVisualBanner(
          drawableRes = R.drawable.img_headline_notice_grow,
          contentDescription = stringResource(R.string.cd_headline_notice_grow),
          modifier = Modifier.testTag("work_headline_visual"),
        )
      }

      item {
        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReflectionComparisonCard(
  reflection: PracticeReflection,
  playerName: String,
  reviewLabel: String,
  onRevealSampleFeedback: () -> Unit,
) {
  val dateFormatter = SimpleDateFormat("MMM d · h:mm a", Locale.getDefault())
  val timestampLabel = dateFormatter.format(Date(reflection.createdAtMillis))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(if (reflection.isSampleSeed) "seed_reflection_card" else "submitted_reflection_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(
      width = if (reflection.isSampleSeed) 1.dp else 1.5.dp,
      color = if (reflection.isSampleSeed) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
      } else {
        BurntOrange
      },
    ),
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
            text = reflection.drillTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = if (reflection.isSampleSeed) {
              "${if (BuildConfig.DEBUG) reflection.readTag else "Day 1 Check-In"} · $timestampLabel"
            } else if (reflection.isCoachResponseRevealed) {
              if (BuildConfig.DEBUG) {
                "Sample feedback ready · $timestampLabel"
              } else {
                "Coach responded · $timestampLabel"
              }
            } else {
              "Submitted · $timestampLabel"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        FictionalDemoBadge(
          text = when {
            reflection.isSampleSeed -> if (BuildConfig.DEBUG) "Sample Seed" else "Completed"
            reflection.isCoachResponseRevealed -> "Feedback Ready"
            else -> "Awaiting Coach"
          },
          modifier = Modifier.align(Alignment.CenterVertically),
        )
      }

      if (!reflection.isCoachResponseRevealed) {
        // Submitted reflection + Awaiting Coach state + clearly labeled demo reveal button
        PlayerObservationBox(
          playerName = playerName,
          observation = reflection.laneObservation,
          modifier = Modifier.fillMaxWidth(),
        )

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("awaiting_coach_card"),
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
              text = "Your reflection was saved on this phone. Coach Keith will review it ${reviewLabel.lowercase()}.",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White,
            )
          }
        }

        if (BuildConfig.DEBUG) {
          // Clearly labeled Demo Control to reveal the prewritten sample response
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
                  .testTag("reveal_sample_feedback_button"),
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
        }
      } else {
        // Revealed state: reflection paired with Sample Coach Response and Next Adjustment
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
          val useSideBySide = maxWidth >= 540.dp
          if (useSideBySide) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              PlayerObservationBox(
                playerName = playerName,
                observation = reflection.laneObservation,
                modifier = Modifier.weight(1f),
              )
              SampleCoachResponseBox(
                label = reflection.sampleCoachLabel,
                response = reflection.sampleCoachResponse,
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
                observation = reflection.laneObservation,
                modifier = Modifier.fillMaxWidth(),
              )
              SampleCoachResponseBox(
                label = reflection.sampleCoachLabel,
                response = reflection.sampleCoachResponse,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        }

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("next_adjustment_box"),
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
                text = reflection.nextAdjustment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
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
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.testTag("sample_coach_response_box"),
    shape = MaterialTheme.shapes.medium,
    color = NavyDeep,
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CoachKeithAvatar(sizeDp = 30)
        Column {
          Text(
            text = if (BuildConfig.DEBUG) label.uppercase() else "COACH KEITH",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
          )
          if (BuildConfig.DEBUG) {
            Text(
              text = "Simulated locally for prototype demo",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.72f),
            )
          }
        }
      }
      Text(
        text = response,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
      )
    }
  }
}
