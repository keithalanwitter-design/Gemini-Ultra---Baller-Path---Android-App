package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayerProfile
import com.example.data.model.PracticeScopeSpec
import com.example.data.model.TacticalChatMessage
import com.example.data.model.VideoFilmSubmission
import com.example.data.model.WelcomeGoalChallengeState
import com.example.data.sample.SampleDataSource
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeTint
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.NavyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeGoalChallengeCard(
  playerFirstName: String,
  welcomeState: WelcomeGoalChallengeState,
  onSelectGoal: (String) -> Unit,
  onAcceptChallenge: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var customGoalText by rememberSaveable { mutableStateOf("") }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("welcome_goal_challenge_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(
      width = 1.5.dp,
      color = if (welcomeState.isChallengeAccepted) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    ),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Flag,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "DAILY TACTICAL CHECK-IN · COACH KEITH",
            style = MaterialTheme.typography.labelMedium,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        Surface(
          shape = RoundedCornerShape(50),
          color = if (welcomeState.isChallengeAccepted) BurntOrange else NavyDeep,
        ) {
          Text(
            text = if (welcomeState.isChallengeAccepted) "Challenge Locked In" else "Up for the Challenge?",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 4.dp)
              .testTag("welcome_challenge_status_badge"),
          )
        }
      }

      Text(
        text = "Welcome back, $playerFirstName — what is your tactical goal today? Are you up for the challenge?",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )

      Text(
        text = "Coach Keith's Challenge: ${welcomeState.challengeQuestion}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "ACTIVE COURT GOAL",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = welcomeState.selectedGoal,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag("active_tactical_goal_text"),
          )
        }
      }

      Text(
        text = "Choose today's court focus or write your own:",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SampleDataSource.tacticalGoalOptions.forEachIndexed { idx, option ->
          val selected = welcomeState.selectedGoal == option
          Surface(
            onClick = { onSelectGoal(option) },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("goal_option_chip_$idx"),
            shape = RoundedCornerShape(12.dp),
            color = if (selected) NavyDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(
              width = 1.dp,
              color = if (selected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            ),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                contentDescription = null,
                tint = if (selected) BurntOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = option,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = customGoalText,
          onValueChange = { customGoalText = it },
          modifier = Modifier
            .weight(1f)
            .testTag("welcome_goal_input"),
          label = { Text("Or type your own court goal") },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BurntOrange,
            focusedLabelColor = BurntOrange,
          ),
        )
        if (customGoalText.isNotBlank()) {
          OutlinedButton(
            onClick = {
              onSelectGoal(customGoalText)
              customGoalText = ""
            },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("set_custom_goal_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BurntOrange),
          ) {
            Text(
              text = "Set Goal",
              color = BurntOrange,
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }

      Button(
        onClick = onAcceptChallenge,
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 48.dp)
          .semantics {
            contentDescription = if (welcomeState.isChallengeAccepted) {
              "Challenge locked in for $playerFirstName"
            } else {
              "I'm up for the challenge, lock in tactical goal"
            }
          }
          .testTag("accept_challenge_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (welcomeState.isChallengeAccepted) NavyDeep else BurntOrange,
          contentColor = Color.White,
        ),
      ) {
        Icon(
          imageVector = Icons.Filled.EmojiEvents,
          contentDescription = null,
          tint = if (welcomeState.isChallengeAccepted) BurntOrange else Color.White,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (welcomeState.isChallengeAccepted) {
            "Challenge Locked In · Ready for Court Work"
          } else {
            "I'm Up for the Challenge — Lock It In"
          },
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveCongratulationsCard(
  welcomeState: WelcomeGoalChallengeState,
  onJumpToFilmAndMessages: () -> Unit,
  onStartPrimaryScope: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .semantics(mergeDescendants = true) {}
      .testTag("interactive_congratulations_card"),
    shape = MaterialTheme.shapes.large,
    color = NavyDeep,
    border = BorderStroke(1.5.dp, BurntOrange),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "CONNECTED FLOW · MILESTONE & NEXT STEP",
            style = MaterialTheme.typography.labelMedium,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Bold,
          )
        }
        Surface(
          shape = RoundedCornerShape(50),
          color = BurntOrange,
        ) {
          Text(
            text = "${welcomeState.completedMilestonesCount} Milestones",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
          )
        }
      }

      Text(
        text = welcomeState.latestCelebrationHeadline,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        fontWeight = FontWeight.Bold,
      )

      Text(
        text = welcomeState.latestNextSentencePrompt,
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.90f),
        modifier = Modifier.testTag("contextual_next_sentence_text"),
      )

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = onStartPrimaryScope,
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("congrats_action_start_scope"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, BurntOrange),
        ) {
          Icon(
            imageVector = Icons.Filled.PlayCircle,
            contentDescription = null,
            tint = BurntOrangeTint,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Open Active Scope",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
          )
        }

        OutlinedButton(
          onClick = onJumpToFilmAndMessages,
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("congrats_action_film_messages"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
        ) {
          Icon(
            imageVector = Icons.Filled.Videocam,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Film Critique & Coach Chat",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerTacticalProfileCard(
  player: PlayerProfile,
  welcomeState: WelcomeGoalChallengeState,
  completedScopesCount: Int,
  videoCount: Int,
  critiquedVideoCount: Int,
  messagesCount: Int,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("player_tactical_profile_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(listOf(NavyDeep, NavyPrimary))
              ),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = player.jerseyNumber,
              style = MaterialTheme.typography.titleMedium,
              color = BurntOrangeTint,
              fontWeight = FontWeight.Bold,
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "PLAYER TACTICAL PROFILE CARD",
              style = MaterialTheme.typography.labelSmall,
              color = BurntOrange,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = player.displayName,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "${player.positionAndGrade} · Coached by ${player.coachName}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ProfileMetricPill(
          label = "Scopes Active",
          value = "$completedScopesCount",
        )
        ProfileMetricPill(
          label = "Film Clips",
          value = "$videoCount ($critiquedVideoCount critiqued)",
        )
        ProfileMetricPill(
          label = "Coach Thread",
          value = "$messagesCount notes",
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "SCOUTING STRENGTHS & COURT TAGS",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          welcomeState.courtStrengths.forEach { tag ->
            Surface(
              shape = RoundedCornerShape(50),
              color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
              Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileMetricPill(
  label: String,
  value: String,
) {
  Surface(
    shape = MaterialTheme.shapes.small,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScopeOfWorkCard(
  scope: PracticeScopeSpec,
  onStartScope: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("scope_of_work_card_${scope.scopeId}"),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = scope.weekDayBadge,
          style = MaterialTheme.typography.labelSmall,
          color = BurntOrange,
          fontWeight = FontWeight.Bold,
        )
        Surface(
          shape = RoundedCornerShape(50),
          color = NavyDeep,
        ) {
          Text(
            text = "${scope.durationMinutes} min · ${scope.status}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
          )
        }
      }

      Text(
        text = "Scope of Work: ${scope.title}",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )

      ScopeDetailRow(
        icon = Icons.Outlined.Assignment,
        label = "Tactical Objective",
        value = scope.tacticalObjective,
      )
      ScopeDetailRow(
        icon = Icons.Outlined.Timer,
        label = "Rep & Phase Breakdown",
        value = scope.repBreakdown,
      )
      ScopeDetailRow(
        icon = Icons.Outlined.CameraAlt,
        label = "Required Film Angle for Coach Keith",
        value = scope.requiredFilmAngle,
      )
      ScopeDetailRow(
        icon = Icons.Outlined.TrackChanges,
        label = "Coach Keith Grading Standard",
        value = scope.successStandard,
      )

      if (onStartScope != null && scope.assignmentId != null) {
        Button(
          onClick = onStartScope,
          modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .testTag("start_scope_button_${scope.scopeId}"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BurntOrange,
            contentColor = Color.White,
          ),
        ) {
          Icon(
            imageVector = Icons.Filled.SportsBasketball,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Launch ${scope.title} Scope",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
private fun ScopeDetailRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = BurntOrange,
      modifier = Modifier
        .padding(top = 2.dp)
        .size(16.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoCritiqueUploadAndBreakdownSection(
  assignmentId: String,
  drillTitle: String,
  videoSubmissions: List<VideoFilmSubmission>,
  onSubmitVideo: (videoUri: String, cameraAngleTag: String, playerNote: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val cameraAngles = remember {
    listOf(
      "Wing / Corner Read (0:18)",
      "Top of Key Drive Read (0:22)",
      "Baseline Relocation Window (0:15)",
    )
  }
  var selectedAngle by rememberSaveable { mutableStateOf(cameraAngles.first()) }
  var playerVideoNote by rememberSaveable { mutableStateOf("") }
  var pickedUriLabel by rememberSaveable { mutableStateOf<String?>(null) }

  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      pickedUriLabel = uri.toString()
      onSubmitVideo(uri.toString(), selectedAngle, playerVideoNote)
      playerVideoNote = ""
    }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("video_upload_critique_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, BurntOrange.copy(alpha = 0.8f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Videocam,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "PLAYER FILM UPLOAD & COACH KEITH CRITIQUE",
              style = MaterialTheme.typography.labelMedium,
              color = BurntOrange,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Upload practice reps for timestamped tactical breakdown",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(50),
          color = NavyDeep,
        ) {
          Text(
            text = "${videoSubmissions.size} Clips",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = "1. Select Camera Angle for '$drillTitle':",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        cameraAngles.forEachIndexed { idx, angle ->
          val isSelected = selectedAngle == angle
          Surface(
            onClick = { selectedAngle = angle },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("video_angle_chip_$idx"),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) NavyDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, if (isSelected) BurntOrange else MaterialTheme.colorScheme.outline),
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = angle,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }

      OutlinedTextField(
        value = playerVideoNote,
        onValueChange = { playerVideoNote = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("video_player_note_input"),
        label = { Text("What should Coach Keith look for on this clip?") },
        placeholder = { Text("Example: Watch my footwork on rep 2 when the help defender steps up") },
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BurntOrange,
          focusedLabelColor = BurntOrange,
        ),
      )

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = {
            onSubmitVideo(
              pickedUriLabel ?: "content://com.ballerpath.app/court_clip_${System.currentTimeMillis()}.mp4",
              selectedAngle,
              playerVideoNote,
            )
            playerVideoNote = ""
          },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("attach_court_video_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BurntOrange,
            contentColor = Color.White,
          ),
        ) {
          Icon(
            imageVector = Icons.Filled.VideoFile,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Attach Court Film Clip",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }

        OutlinedButton(
          onClick = {
            videoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
          },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("pick_video_from_device_button"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, NavyDeep),
        ) {
          Icon(
            imageVector = Icons.Outlined.CameraAlt,
            contentDescription = null,
            tint = NavyDeep,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Choose Device Video",
            style = MaterialTheme.typography.labelLarge,
            color = NavyDeep,
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

      Text(
        text = "SUBMITTED PRACTICE FILM & COACH KEITH BREAKDOWNS",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
      )

      videoSubmissions.forEach { video ->
        VideoSubmissionBreakdownItem(video = video)
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoSubmissionBreakdownItem(
  video: VideoFilmSubmission,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("video_submission_item_${video.videoId}"),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    border = BorderStroke(
      width = 1.dp,
      color = if (video.isCritiqued) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    ),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Column {
          Text(
            text = "${video.drillTitle} · ${video.cameraAngleTag}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = video.durationLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(
          shape = RoundedCornerShape(50),
          color = if (video.isCritiqued) BurntOrange else NavyDeep,
        ) {
          Text(
            text = if (video.isCritiqued) "Critiqued by Coach Keith" else "Awaiting Coach Critique",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
          )
        }
      }

      Text(
        text = "Player Film Note: \"${video.playerNote}\"",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
      )

      if (video.isCritiqued) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.small,
          color = NavyDeep,
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              CoachKeithAvatar(sizeDp = 28)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "COACH KEITH TIMESTAMPED FILM BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                color = BurntOrangeTint,
                fontWeight = FontWeight.Bold,
              )
            }
            Text(
              text = video.coachCritique,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White,
            )
            if (video.timestampNotes.isNotBlank()) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.12f),
              ) {
                Text(
                  text = video.timestampNotes,
                  style = MaterialTheme.typography.labelSmall,
                  color = BurntOrangeTint,
                  modifier = Modifier.padding(8.dp),
                )
              }
            }
            if (video.nextRepAdjustment.isNotBlank()) {
              Text(
                text = "Try the next rep: ${video.nextRepAdjustment}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerCoachMessagingSection(
  messages: List<TacticalChatMessage>,
  activeScopeTitle: String,
  onSendPlayerMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var messageDraft by rememberSaveable { mutableStateOf("") }
  val quickPrompts = remember(activeScopeTitle) {
    listOf(
      "Coach Keith, on '$activeScopeTitle', should I hold the corner or lift when the driver stops?",
      "I just attached my wing-angle film clip—were my feet set early enough?",
      "Locked in my tactical challenge today! Ready for the next rep adjustment.",
    )
  }
  val dateFormatter = remember { SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("player_coach_messaging_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Forum,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "DIRECT PLAYER ↔ COACH KEITH MESSAGING",
              style = MaterialTheme.typography.labelMedium,
              color = BurntOrange,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Two-way tactical thread connected to your drills & film",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        messages.forEach { msg ->
          val isCoach = msg.isFromCoach
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("chat_message_${msg.messageId}"),
            shape = MaterialTheme.shapes.medium,
            color = if (isCoach) NavyDeep else MaterialTheme.colorScheme.secondaryContainer,
            border = if (isCoach) BorderStroke(1.dp, BurntOrange.copy(alpha = 0.6f)) else null,
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isCoach) {
                    CoachKeithAvatar(sizeDp = 24)
                    Spacer(modifier = Modifier.width(6.dp))
                  } else {
                    Icon(
                      imageVector = Icons.Filled.Person,
                      contentDescription = null,
                      tint = NavyDeep,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Text(
                    text = "${msg.senderName} · ${msg.relatedTitle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCoach) BurntOrangeTint else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                  )
                }
                Text(
                  text = dateFormatter.format(Date(msg.createdAtMillis)),
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isCoach) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                )
              }
              Text(
                text = msg.text,
                style = MaterialTheme.typography.bodySmall,
                color = if (isCoach) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
          }
        }
      }

      Text(
        text = "Tap a connected follow-up prompt or type a message to Coach Keith:",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        quickPrompts.forEachIndexed { index, prompt ->
          Surface(
            onClick = { messageDraft = prompt },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("message_quick_prompt_chip_$index"),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              contentAlignment = Alignment.CenterStart,
            ) {
              Text(
                text = prompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = messageDraft,
          onValueChange = { messageDraft = it },
          modifier = Modifier
            .weight(1f)
            .testTag("player_message_input"),
          label = { Text("Message Coach Keith") },
          placeholder = { Text("Ask about your spacing read or video clip...") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BurntOrange,
            focusedLabelColor = BurntOrange,
          ),
        )
        Button(
          onClick = {
            if (messageDraft.isNotBlank()) {
              onSendPlayerMessage(messageDraft)
              messageDraft = ""
            }
          },
          modifier = Modifier
            .defaultMinSize(minWidth = 54.dp, minHeight = 54.dp)
            .semantics {
              contentDescription = "Send tactical message to Coach Keith"
            }
            .testTag("send_player_message_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BurntOrange,
            contentColor = Color.White,
          ),
          contentPadding = PaddingValues(12.dp),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachKeithBackendPortalSection(
  isCoachPortalMode: Boolean,
  onToggleCoachPortalMode: (Boolean) -> Unit,
  videoSubmissions: List<VideoFilmSubmission>,
  onPublishScope: (title: String, objective: String, reps: String, filmAngle: String, standard: String) -> Unit,
  onSaveVideoCritique: (videoId: String, critique: String, timestamps: String, nextRep: String) -> Unit,
  onSendCoachMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var scopeTitle by rememberSaveable { mutableStateOf("") }
  var scopeObjective by rememberSaveable { mutableStateOf("") }
  var scopeReps by rememberSaveable { mutableStateOf("") }
  var scopeFilmAngle by rememberSaveable { mutableStateOf("Wing / 45° Slot Camera") }
  var scopeStandard by rememberSaveable { mutableStateOf("Eyes up on catch; read help defender's chest before cutting.") }

  val targetVideo = videoSubmissions.firstOrNull { !it.isCritiqued } ?: videoSubmissions.firstOrNull()
  var videoCritiqueText by rememberSaveable { mutableStateOf("") }
  var videoTimestampsText by rememberSaveable {
    mutableStateOf("[0:03] Chin up on the pass release • [0:08] Good 2-step slide to the corner window")
  }
  var videoNextRepText by rememberSaveable {
    mutableStateOf("Hold your corner stance for one full beat before lifting back to the wing.")
  }
  var coachMessageReply by rememberSaveable { mutableStateOf("") }
  var portalStatusBanner by rememberSaveable { mutableStateOf<String?>(null) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("role_switcher_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, if (isCoachPortalMode) BurntOrange else NavyDeep),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.VerifiedUser,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "PLAYER / PARENT SIDE ↔ COACH KEITH PORTAL",
              style = MaterialTheme.typography.labelMedium,
              color = BurntOrange,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = if (isCoachPortalMode) {
                "Coach Keith Backend Active: Publish scopes, critique film & reply"
              } else {
                "Currently viewing as Player / Family (Maya & Avery)"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = { onToggleCoachPortalMode(false) },
          modifier = Modifier
            .weight(1f)
            .defaultMinSize(minHeight = 48.dp)
            .testTag("switch_to_player_mode_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (!isCoachPortalMode) NavyDeep else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (!isCoachPortalMode) Color.White else MaterialTheme.colorScheme.onSurface,
          ),
        ) {
          Text(
            text = "Player / Family Side",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
          )
        }

        Button(
          onClick = { onToggleCoachPortalMode(true) },
          modifier = Modifier
            .weight(1f)
            .defaultMinSize(minHeight = 48.dp)
            .testTag("switch_to_coach_portal_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isCoachPortalMode) BurntOrange else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isCoachPortalMode) Color.White else MaterialTheme.colorScheme.onSurface,
          ),
        ) {
          Text(
            text = "Coach Keith Portal",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      if (isCoachPortalMode) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("coach_keith_portal_card"),
          shape = MaterialTheme.shapes.medium,
          color = NavyDeep,
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              CoachKeithAvatar(sizeDp = 36)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "COACH KEITH BACKEND WORKBENCH",
                  style = MaterialTheme.typography.labelMedium,
                  color = BurntOrangeTint,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "Assign Scopes of Work, publish timestamped film critiques, and message Maya & Avery.",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.85f),
                )
              }
            }

            if (portalStatusBanner != null) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = BurntOrange,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("coach_portal_status_banner"),
              ) {
                Text(
                  text = portalStatusBanner.orEmpty(),
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White,
                  modifier = Modifier.padding(10.dp),
                )
              }
            }

            // 1. Publish New Practice Scope of Work
            Surface(
              shape = MaterialTheme.shapes.small,
              color = Color.White,
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = "1. PUBLISH NEW PRACTICE SCOPE OF WORK",
                  style = MaterialTheme.typography.labelSmall,
                  color = BurntOrange,
                  fontWeight = FontWeight.Bold,
                )
                OutlinedTextField(
                  value = scopeTitle,
                  onValueChange = { scopeTitle = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("coach_publish_scope_title_input"),
                  label = { Text("Scope Title (e.g., Nail Help & Kick-Out Read)") },
                  singleLine = true,
                )
                OutlinedTextField(
                  value = scopeObjective,
                  onValueChange = { scopeObjective = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("coach_publish_scope_objective_input"),
                  label = { Text("Tactical Objective & Why It Matters") },
                )
                OutlinedTextField(
                  value = scopeReps,
                  onValueChange = { scopeReps = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("coach_publish_scope_reps_input"),
                  label = { Text("Rep Breakdown & Camera Angle") },
                )
                Button(
                  onClick = {
                    val finalTitle = scopeTitle.ifBlank { "Weak-Side Slot Relocation" }
                    val finalObj = scopeObjective.ifBlank {
                      "Read the nail help defender's hips and slide into an open passing lane."
                    }
                    onPublishScope(finalTitle, finalObj, scopeReps, scopeFilmAngle, scopeStandard)
                    scopeTitle = ""
                    scopeObjective = ""
                    scopeReps = ""
                    portalStatusBanner = "Published Scope of Work: '$finalTitle' to Maya's Plan!"
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("coach_publish_scope_button"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = BurntOrange,
                    contentColor = Color.White,
                  ),
                ) {
                  Text(
                    text = "Publish Scope of Work to Player Plan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }

            // 2. Critique Player Video Upload
            if (targetVideo != null) {
              Surface(
                shape = MaterialTheme.shapes.small,
                color = Color.White,
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = "2. CRITIQUE UPLOADED PLAYER FILM (${targetVideo.drillTitle})",
                    style = MaterialTheme.typography.labelSmall,
                    color = BurntOrange,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = "Clip: ${targetVideo.cameraAngleTag} · Note: \"${targetVideo.playerNote}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = NavyDeep,
                  )
                  OutlinedTextField(
                    value = videoCritiqueText,
                    onValueChange = { videoCritiqueText = it },
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("coach_video_critique_input"),
                    label = { Text("Coach Keith Tactical Film Assessment") },
                    placeholder = { Text("Great balance on the catch, Maya—your eyes scanned the paint immediately.") },
                  )
                  OutlinedTextField(
                    value = videoTimestampsText,
                    onValueChange = { videoTimestampsText = it },
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("coach_video_timestamps_input"),
                    label = { Text("Timestamped Notes ([0:03], [0:08])") },
                  )
                  OutlinedTextField(
                    value = videoNextRepText,
                    onValueChange = { videoNextRepText = it },
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("coach_video_next_rep_input"),
                    label = { Text("Try the Next Rep Adjustment") },
                  )
                  Button(
                    onClick = {
                      val finalCritique = videoCritiqueText.ifBlank {
                        "Coach Keith Film Breakdown: Your chin stayed up on the drive read and you slid cleanly into the passing window."
                      }
                      onSaveVideoCritique(
                        targetVideo.videoId,
                        finalCritique,
                        videoTimestampsText,
                        videoNextRepText,
                      )
                      videoCritiqueText = ""
                      portalStatusBanner = "Posted Coach Keith Film Critique for '${targetVideo.drillTitle}'!"
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .defaultMinSize(minHeight = 48.dp)
                      .testTag("coach_post_video_critique_button"),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = NavyDeep,
                      contentColor = Color.White,
                    ),
                  ) {
                    Text(
                      text = "Publish Timestamped Film Critique",
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }
              }
            }

            // 3. Direct Coach Keith Reply in Thread
            Surface(
              shape = MaterialTheme.shapes.small,
              color = Color.White,
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = "3. SEND COACH KEITH DIRECT MESSAGE",
                  style = MaterialTheme.typography.labelSmall,
                  color = BurntOrange,
                  fontWeight = FontWeight.Bold,
                )
                OutlinedTextField(
                  value = coachMessageReply,
                  onValueChange = { coachMessageReply = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("coach_reply_message_input"),
                  label = { Text("Coach Keith Reply to Maya & Avery") },
                )
                Button(
                  onClick = {
                    val reply = coachMessageReply.ifBlank {
                      "Great work locking in today's challenge, Maya! Keep your hips low on the corner relocation catch."
                    }
                    onSendCoachMessage(reply)
                    coachMessageReply = ""
                    portalStatusBanner = "Sent Coach Keith message to Maya's thread!"
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("coach_send_message_button"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = BurntOrange,
                    contentColor = Color.White,
                  ),
                ) {
                  Text(
                    text = "Post Coach Keith Reply",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
