package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.CoachKeithAvatar
import com.example.ui.components.FictionalDemoBadge
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.NavyDeep
import com.example.ui.viewmodel.BallerPathUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PracticeFlowScreen(
  uiState: BallerPathUiState,
  onNavigateBack: () -> Unit,
  onNextStage: () -> Unit,
  onToggleTimer: () -> Unit,
  onResetTimer: () -> Unit,
  onReflectionChanged: (String) -> Unit,
  onStarterCueClicked: (String) -> Unit,
  onSubmitReflection: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler {
    onNavigateBack()
  }

  val practice = uiState.practice
  val assignment = uiState.activePracticeAssignment
  val steps = assignment.steps
  val totalStages = steps.size + 1
  val currentStageNumber = if (practice.isReflectionStage) totalStages else practice.currentStepIndex + 1
  val currentStep = steps[practice.currentStepIndex.coerceIn(0, steps.lastIndex)]
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = NavyDeep,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White,
        ),
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
              .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
              .testTag("practice_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.cd_back_button),
            )
          }
        },
        title = {
          Column {
            Text(
              text = assignment.title,
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("practice_drill_title"),
            )
            Text(
              text = if (practice.isReflectionStage) {
                "Step $currentStageNumber of $totalStages · Reflection"
              } else {
                "Step $currentStageNumber of $totalStages · ${currentStep.phaseLabel} (${currentStep.durationMinutes} min)"
              },
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
        actions = {
          FictionalDemoBadge(
            text = "${assignment.totalMinutes} MIN",
            onDarkBackground = true,
            modifier = Modifier.padding(end = 12.dp),
          )
        },
      )
    },
    bottomBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          contentAlignment = Alignment.Center,
        ) {
          if (practice.isReflectionStage) {
            Button(
              onClick = onSubmitReflection,
              enabled = !practice.isSubmitting,
              modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 54.dp)
                .testTag("submit_reflection_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BurntOrange,
                contentColor = Color.White,
              ),
            ) {
              Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = stringResource(R.string.submit_reflection),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
            }
          } else {
            val nextLabel = if (practice.currentStepIndex < steps.lastIndex) {
              val nextStep = steps[practice.currentStepIndex + 1]
              "Next: ${nextStep.title} (${nextStep.durationMinutes} min)"
            } else {
              "Continue to Reflection"
            }
            Button(
              onClick = onNextStage,
              modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 54.dp)
                .testTag("practice_next_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BurntOrange,
                contentColor = Color.White,
              ),
            ) {
              Text(
                text = nextLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier
          .widthIn(max = 620.dp)
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        LinearProgressIndicator(
          progress = { currentStageNumber.toFloat() / totalStages.toFloat() },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50)),
          color = BurntOrange,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        if (!practice.isReflectionStage) {
          val timerButtonLabel = when {
            practice.isTimerRunning -> "Pause"
            practice.hasStartedTimer && practice.remainingSeconds > 0 -> "Resume"
            else -> "Start"
          }
          val timerStatusText = when {
            practice.remainingSeconds == 0 -> "Timer done · Move on when ready"
            practice.isTimerRunning -> "Running · Optional timer"
            practice.hasStartedTimer -> "Paused · Tap Resume or Next"
            else -> "Optional timer · Move on anytime"
          }

          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = NavyDeep,
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "STEP ${currentStep.stepNumber} · ${currentStep.phaseLabel}",
                style = MaterialTheme.typography.labelMedium,
                color = BurntOrange,
              )

              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Column {
                  Text(
                    text = formatSeconds(practice.remainingSeconds),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    modifier = Modifier.testTag("practice_timer_text"),
                  )
                  Text(
                    text = timerStatusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.80f),
                    modifier = Modifier.testTag("practice_timer_status"),
                  )
                }

                Row(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Button(
                    onClick = onToggleTimer,
                    modifier = Modifier
                      .defaultMinSize(minWidth = 104.dp, minHeight = 48.dp)
                      .testTag("practice_timer_toggle"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (practice.isTimerRunning) Color.White.copy(alpha = 0.16f) else BurntOrange,
                      contentColor = Color.White,
                    ),
                    border = if (practice.isTimerRunning) BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)) else null,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                  ) {
                    Icon(
                      imageVector = if (practice.isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                      contentDescription = null,
                      modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = timerButtonLabel,
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      softWrap = false,
                    )
                  }

                  IconButton(
                    onClick = onResetTimer,
                    modifier = Modifier
                      .size(48.dp)
                      .testTag("practice_timer_reset"),
                    colors = IconButtonDefaults.iconButtonColors(
                      contentColor = Color.White.copy(alpha = 0.85f),
                    ),
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Refresh,
                      contentDescription = stringResource(R.string.cd_reset_timer),
                    )
                  }
                }
              }
            }
          }

          Image(
            painter = painterResource(id = R.drawable.img_court_lane_diagram),
            contentDescription = stringResource(R.string.cd_court_diagram),
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxWidth()
              .height(128.dp)
              .clip(MaterialTheme.shapes.medium),
          )

          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = currentStep.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = currentStep.instruction,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )

              Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.Top,
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = BurntOrange,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Key Look-For: ${currentStep.keyLookFor}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                }
              }
            }
          }

          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CoachKeithAvatar(sizeDp = 40)
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = currentStep.coachCue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        } else {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = NavyDeep,
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "POST-DRILL REFLECTION · ${assignment.title.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = BurntOrange,
              )
              Text(
                text = assignment.reflectionPrompt,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
              )
              Text(
                text = "Unfinished drafts save automatically on this phone. After you submit, your reflection moves to Awaiting coach in Work + Feedback.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
              )
            }
          }

          if (practice.reflectionDraft.isNotBlank()) {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("draft_saved_indicator"),
              shape = MaterialTheme.shapes.small,
              color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Outlined.Save,
                  contentDescription = null,
                  tint = NavyDeep,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Draft saved on this device",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
          }

          Text(
            text = "Tap a quick starter or write your own:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            assignment.starterCues.forEach { cue ->
              Surface(
                onClick = { onStarterCueClicked(cue) },
                modifier = Modifier
                  .defaultMinSize(minHeight = 48.dp)
                  .testTag("starter_cue_chip"),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Box(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                  contentAlignment = Alignment.CenterStart,
                ) {
                  Text(
                    text = cue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }

          OutlinedTextField(
            value = practice.reflectionDraft,
            onValueChange = onReflectionChanged,
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 140.dp)
              .testTag("reflection_input"),
            label = { Text("What you noticed & chose") },
            placeholder = {
              Text("Example: ${assignment.starterCues.firstOrNull().orEmpty()}")
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            minLines = 4,
            maxLines = 8,
            isError = practice.validationError != null,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BurntOrange,
              focusedLabelColor = BurntOrange,
              cursorColor = BurntOrange,
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = MaterialTheme.shapes.medium,
          )

          if (practice.validationError != null) {
            Text(
              text = practice.validationError,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.testTag("reflection_validation_error"),
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

private fun formatSeconds(totalSeconds: Int): String {
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return "%02d:%02d".format(minutes, seconds)
}
