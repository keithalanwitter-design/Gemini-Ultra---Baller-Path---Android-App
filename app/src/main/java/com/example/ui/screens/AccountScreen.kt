package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.SetupStage
import com.example.data.model.WorkflowScenarioId
import com.example.ui.auth.CloudSyncAuthBannerCard
import com.example.ui.components.CoachKeithAvatar
import com.example.ui.components.CoachKeithBackendPortalSection
import com.example.ui.components.FictionalDemoBadge
import com.example.ui.components.ParentSundayDigestAndRolesCard
import com.example.ui.components.PlayerTacticalProfileCard
import com.example.ui.components.PrivacyPolicyLinkCard
import com.example.ui.components.WebsiteAppStreamlinedAuditCard
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.NavyDeep
import com.example.ui.viewmodel.BallerPathUiState
import com.google.firebase.auth.FirebaseUser

@Composable
fun AccountScreen(
  uiState: BallerPathUiState,
  onSelectScenario: (WorkflowScenarioId) -> Unit,
  onSelectSetupState: (SetupStage, Boolean) -> Unit,
  onResetDemoData: () -> Unit,
  currentUser: FirebaseUser? = null,
  authErrorMessage: String? = null,
  onAuthError: (String?) -> Unit = {},
  onSignOutSuccess: () -> Unit = {},
  onToggleCoachPortalMode: (Boolean) -> Unit = {},
  onPublishScopeOfWork: (title: String, objective: String, reps: String, filmAngle: String, standard: String) -> Unit = { _, _, _, _, _ -> },
  onSaveCoachVideoCritique: (videoId: String, critique: String, timestamps: String, nextRep: String) -> Unit = { _, _, _, _ -> },
  onSendCoachMessage: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  val activeScenario = uiState.currentScenario

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
      Text(
        text = if (BuildConfig.DEBUG) "Account & Demo Controls" else "Account",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )

      // Official Baller Path Brand Lockup Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("account_brand_lockup_card"),
        shape = MaterialTheme.shapes.large,
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Image(
            painter = painterResource(id = R.drawable.logo_baller_path_stacked),
            contentDescription = stringResource(R.string.cd_baller_path_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(92.dp),
          )
          Text(
            text = "A clear next step. A coach in your corner.",
            style = MaterialTheme.typography.labelMedium,
            color = NavyDeep,
          )
        }
      }

      // Family Profile Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("simulated_family_card"),
        shape = MaterialTheme.shapes.large,
        color = NavyDeep,
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.FamilyRestroom,
              contentDescription = null,
              tint = BurntOrange,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = uiState.family.householdLabel,
              style = MaterialTheme.typography.labelMedium,
              color = BurntOrange,
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(BurntOrange),
              contentAlignment = Alignment.Center,
            ) {
              val initials = uiState.family.parentName
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifEmpty { "AP" }
              Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = uiState.family.parentName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
              )
              Text(
                text = "${uiState.family.relationshipLabel} · Fictional demo household",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
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
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${uiState.player.displayName} (${uiState.player.jerseyNumber})",
                  style = MaterialTheme.typography.titleSmall,
                  color = Color.White,
                )
                Text(
                  text = "${uiState.player.positionAndGrade} · Guided by ${uiState.player.coachName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.82f),
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              FictionalDemoBadge(
                text = "Fictional Player",
                onDarkBackground = true,
              )
            }
          }
        }
      }

      if (BuildConfig.DEBUG) {
        // Fictional Demo Scenario Switcher (includes "More work ready" in DEBUG only)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("scenario_switcher_card"),
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Science,
                contentDescription = null,
                tint = BurntOrange,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Demo Scenario Switcher",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Jump to any fictional state on Today and Work + Feedback.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            WorkflowScenarioId.entries.forEach { scenario ->
              val selected = uiState.setupState.setupStage == SetupStage.WORK_READY && scenario == activeScenario
              DemoChoiceRow(
                title = scenario.label,
                subtitle = "Today action: ${scenario.primaryActionLabel}",
                selected = selected,
                testTag = "scenario_${scenario.id}",
                onClick = { onSelectScenario(scenario) },
              )
            }
          }
        }

        // B2 Onboarding / Setup State Preview Switcher (DEBUG only)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("setup_state_switcher_card"),
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                tint = BurntOrange,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Setup State Preview",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Preview how Today and Plan behave before personal focus work is published.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            val stage = uiState.setupState.setupStage
            val hasGeneral = uiState.setupState.hasGeneralWorkPublished

            DemoChoiceRow(
              title = "Setup in progress",
              subtitle = "Family waiting for activation (within 2 business days)",
              selected = stage == SetupStage.SETUP_IN_PROGRESS,
              testTag = "setup_stage_in_progress",
              onClick = { onSelectSetupState(SetupStage.SETUP_IN_PROGRESS, false) },
            )
            DemoChoiceRow(
              title = "Plan being prepared (general work ready)",
              subtitle = "Active player, no personal focus yet; general drill published",
              selected = stage == SetupStage.PLAN_BEING_PREPARED && hasGeneral,
              testTag = "setup_stage_preparing_with_general",
              onClick = { onSelectSetupState(SetupStage.PLAN_BEING_PREPARED, true) },
            )
            DemoChoiceRow(
              title = "Plan being prepared (no general work)",
              subtitle = "Active player, waiting on Coach Keith to publish work",
              selected = stage == SetupStage.PLAN_BEING_PREPARED && !hasGeneral,
              testTag = "setup_stage_preparing_no_general",
              onClick = { onSelectSetupState(SetupStage.PLAN_BEING_PREPARED, false) },
            )
            DemoChoiceRow(
              title = "Work ready",
              subtitle = "Personal weekly assignments active",
              selected = stage == SetupStage.WORK_READY,
              testTag = "setup_stage_work_ready",
              onClick = { onSelectSetupState(SetupStage.WORK_READY, true) },
            )
          }
        }

        if (uiState.demoStatusMessage != null) {
          Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("account_demo_status_message"),
          ) {
            Text(
              text = uiState.demoStatusMessage,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(12.dp),
            )
          }
        }

        OutlinedButton(
          onClick = onResetDemoData,
          modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
              contentDescription = "Reset sample demo data to Practice ready"
            }
            .testTag("reset_demo_button"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, BurntOrange),
        ) {
          Icon(
            imageVector = Icons.Outlined.RestartAlt,
            contentDescription = null,
            tint = BurntOrange,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Reset Sample Demo Data",
            style = MaterialTheme.typography.labelLarge,
            color = BurntOrange,
          )
        }
      }

      // Assigned Coach Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          CoachKeithAvatar(sizeDp = 50)
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Assigned Coach: ${uiState.player.coachName}",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Responses revealed via the demo control are prewritten local sample responses for this offline pilot.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      if (BuildConfig.DEBUG) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = BurntOrange,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Prototype Scope",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Text(
              text = uiState.family.scopeNotice,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.Top) {
              Icon(
                imageVector = Icons.Outlined.Storage,
                contentDescription = null,
                tint = NavyDeep,
                modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Local Room storage keeps Maya's active step, unfinished drafts, and submitted reflections across app restarts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      PlayerTacticalProfileCard(
        player = uiState.player,
        welcomeState = uiState.welcomeGoalState,
        completedScopesCount = uiState.practiceScopes.size,
        videoCount = uiState.videoSubmissions.size,
        critiquedVideoCount = uiState.videoSubmissions.count { it.isCritiqued },
        messagesCount = uiState.tacticalMessages.size,
      )

      CloudSyncAuthBannerCard(
        currentUser = currentUser,
        isCoachMode = uiState.isCoachPortalMode,
        onToggleCoachMode = onToggleCoachPortalMode,
        onAuthSuccess = { onAuthError(null) },
        onSignedOut = onSignOutSuccess,
      )

      CoachKeithBackendPortalSection(
        isCoachPortalMode = uiState.isCoachPortalMode,
        onToggleCoachPortalMode = onToggleCoachPortalMode,
        videoSubmissions = uiState.videoSubmissions,
        onPublishScope = onPublishScopeOfWork,
        onSaveVideoCritique = onSaveCoachVideoCritique,
        onSendCoachMessage = onSendCoachMessage,
      )

      ParentSundayDigestAndRolesCard(
        firstLookState = uiState.welcomeGoalState.firstLookAndCycle,
      )

      WebsiteAppStreamlinedAuditCard()

      // Privacy & Parent Consent Card (always shown in pilot & Play builds)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("privacy_consent_card"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.VerifiedUser,
              contentDescription = null,
              tint = BurntOrange,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Privacy & Parent Permission",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
          Text(
            text = if (uiState.parentConsent.isAcknowledged) {
              "Parent or guardian confirmation saved on this device. Baller Path is for a parent or guardian of a player in grades 4–8, and Coach Keith reviews submitted work."
            } else {
              "Parent or guardian confirmation required before first use."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "Pilot storage: reflections, drafts, and optional voice notes remain local on this device with no login, no ad tracking, and no third-party data sharing.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          PrivacyPolicyLinkCard(
            linkTestTag = "account_privacy_policy_link",
            urlTestTag = "account_privacy_policy_url",
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun DemoChoiceRow(
  title: String,
  subtitle: String,
  selected: Boolean,
  testTag: String,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .defaultMinSize(minHeight = 48.dp)
      .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    border = BorderStroke(
      width = if (selected) 1.5.dp else 1.dp,
      color = if (selected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
          contentDescription = null,
          tint = if (selected) BurntOrange else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
