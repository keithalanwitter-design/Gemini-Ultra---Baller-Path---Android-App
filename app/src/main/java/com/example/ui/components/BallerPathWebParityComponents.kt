package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Sync
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BallerPathLoopStage
import com.example.data.model.CoachCheckInState
import com.example.data.model.GradeBandPathway
import com.example.data.model.TrainingProgramTrack
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeTint
import com.example.ui.theme.NavyDeep

private const val BALLER_PATH_WEBSITE_URL = "https://ballerpath.com"

/**
 * Mirrors the signature 4-step coaching loop from ballerpath.com:
 * 1. Focus -> 2. Practice -> 3. Reflect -> 4. Adjust
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FourStageCoachingLoopCard(
  activeStage: BallerPathLoopStage,
  onSelectStageAction: (BallerPathLoopStage) -> Unit,
  modifier: Modifier = Modifier,
) {
  var inspectedStage by remember(activeStage) { mutableStateOf(activeStage) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("baller_path_four_stage_loop_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Loop,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "BALLERPATH.COM CORE LOOP · FOCUS ➔ PRACTICE ➔ REFLECT ➔ ADJUST",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        Surface(
          shape = RoundedCornerShape(50),
          color = NavyDeep,
        ) {
          Text(
            text = "Current Stage: ${activeStage.title}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 3.dp)
              .testTag("active_loop_stage_badge"),
          )
        }
      }

      Text(
        text = "Busy isn't the same as progress. Every week on ballerpath.com and in the app repeats this 4-step coaching cycle with Coach Keith:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        BallerPathLoopStage.entries.forEach { stage ->
          val isCurrent = stage == activeStage
          val isInspected = stage == inspectedStage
          Surface(
            onClick = { inspectedStage = stage },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("loop_stage_pill_${stage.name.lowercase()}"),
            shape = RoundedCornerShape(12.dp),
            color = when {
              isCurrent -> BurntOrange
              isInspected -> NavyDeep
              else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            },
            border = BorderStroke(
              width = 1.dp,
              color = if (isCurrent || isInspected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
            ),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .clip(CircleShape)
                  .background(
                    if (isCurrent || isInspected) Color.White.copy(alpha = 0.22f)
                    else MaterialTheme.colorScheme.surface
                  ),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = "${stage.stepNumber}",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isCurrent || isInspected) Color.White else NavyDeep,
                  fontWeight = FontWeight.Bold,
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = stage.title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isCurrent || isInspected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = NavyDeep,
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "STAGE ${inspectedStage.stepNumber}: ${inspectedStage.title.uppercase()} — ${inspectedStage.tagline}",
            style = MaterialTheme.typography.labelMedium,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = inspectedStage.webAndAppConnection,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
          )
          OutlinedButton(
            onClick = { onSelectStageAction(inspectedStage) },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("loop_stage_action_button"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BurntOrange),
          ) {
            Text(
              text = "Go to ${inspectedStage.title} Step in App",
              style = MaterialTheme.typography.labelMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}

/**
 * Streamlines Baller Path for a broader reach across Grades 4th–12th and connects
 * Remote Anywhere families + Storm Basketball Training (IBL / ABL / Skill Lab) players.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BroaderReachPathwayAndEvaluationCard(
  selectedGradeBand: GradeBandPathway,
  selectedProgramTrack: TrainingProgramTrack,
  onSelectGradeBand: (GradeBandPathway) -> Unit,
  onSelectProgramTrack: (TrainingProgramTrack) -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("broader_reach_pathway_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.School,
          contentDescription = null,
          tint = BurntOrange,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "STARTING-POINT EVALUATION & PATHWAY (GRADES 4TH–12TH)",
            style = MaterialTheme.typography.labelMedium,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Streamlined across ballerpath.com & Storm Basketball Training",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Text(
        text = "1. Player Grade Band (tailors tactical terminology & rep speed):",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        GradeBandPathway.entries.forEach { band ->
          val selected = band == selectedGradeBand
          Surface(
            onClick = { onSelectGradeBand(band) },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("pathway_grade_${band.id}"),
            shape = RoundedCornerShape(12.dp),
            color = if (selected) NavyDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, if (selected) BurntOrange else MaterialTheme.colorScheme.outline),
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
                text = band.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }

      Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "Grade Band Focus: ${selectedGradeBand.focusSummary}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.padding(10.dp),
        )
      }

      Text(
        text = "2. Program Track (Remote Anywhere vs. Storm Basketball IBL / ABL / Skill Lab):",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TrainingProgramTrack.entries.forEach { track ->
          val selected = track == selectedProgramTrack
          Surface(
            onClick = { onSelectProgramTrack(track) },
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 48.dp)
              .testTag("pathway_track_${track.id}"),
            shape = RoundedCornerShape(12.dp),
            color = if (selected) NavyDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, if (selected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = track.label,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) BurntOrangeTint else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = track.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Mirrors ballerpath.com's Google Meet Check-In & Consultation system:
 * - First-Week Activation Check-In (20 min)
 * - Ongoing Development Check-In (20 min)
 * - Prospective Family Intro Call (30 min)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachKeithCheckInConsultationCard(
  checkInState: CoachCheckInState,
  onSaveCheckInPrep: (checkInType: String, agendaTopics: List<String>, confidenceRating: Int, familyNote: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val checkInTypes = remember {
    listOf(
      "First-Week Activation Check-In (20 min · Google Meet)",
      "Development Check-In (20 min · Google Meet)",
      "Prospective Family Fit Call (30 min · Google Meet)",
    )
  }
  val availableTopics = remember {
    listOf(
      "Review 'Pass, Look, Decide' wing-angle film clip",
      "Confirm next week's spacing adjustment",
      "Game confidence & decision speed under pressure",
      "Connecting home reps with team / IBL / ABL games",
    )
  }

  var selectedType by rememberSaveable(checkInState.selectedCheckInType) {
    mutableStateOf(checkInState.selectedCheckInType)
  }
  var selectedTopics by rememberSaveable(checkInState.selectedAgendaTopics) {
    mutableStateOf(checkInState.selectedAgendaTopics)
  }
  var confidence by rememberSaveable(checkInState.confidenceRating) {
    mutableIntStateOf(checkInState.confidenceRating)
  }
  var familyNote by rememberSaveable(checkInState.familyQuestionNote) {
    mutableStateOf(checkInState.familyQuestionNote)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("coach_checkin_consultation_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, BurntOrange.copy(alpha = 0.8f)),
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
            imageVector = Icons.Filled.VideoCall,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "COACH KEITH LIVE CHECK-INS · GOOGLE MEET & BALLERPATH.COM",
              style = MaterialTheme.typography.labelMedium,
              color = BurntOrange,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Prepare your 20-min First-Week or Development Check-In agenda",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        if (checkInState.isCheckInPrepSaved) {
          Surface(
            shape = RoundedCornerShape(50),
            color = BurntOrange,
          ) {
            Text(
              text = "Agenda Ready",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 3.dp)
                .testTag("checkin_prep_saved_badge"),
            )
          }
        }
      }

      Text(
        text = "1. Select Check-In Session Type (from ballerpath.com):",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        checkInTypes.forEachIndexed { idx, type ->
          val isSelected = selectedType == type
          Surface(
            onClick = { selectedType = type },
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 48.dp)
              .testTag("checkin_type_option_$idx"),
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) NavyDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, if (isSelected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = if (isSelected) BurntOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = type,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            }
          }
        }
      }

      Text(
        text = "2. Court Confidence Check ($confidence / 5):",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        (1..5).forEach { level ->
          val active = confidence == level
          Surface(
            onClick = { confidence = level },
            modifier = Modifier
              .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
              .testTag("checkin_confidence_$level"),
            shape = RoundedCornerShape(10.dp),
            color = if (active) BurntOrange else MaterialTheme.colorScheme.surfaceVariant,
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "$level/5",
                style = MaterialTheme.typography.labelMedium,
                color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }

      Text(
        text = "3. Topics for Coach Keith on Google Meet:",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        availableTopics.forEachIndexed { idx, topic ->
          val chosen = topic in selectedTopics
          Surface(
            onClick = {
              selectedTopics = if (chosen && selectedTopics.size > 1) {
                selectedTopics - topic
              } else if (!chosen) {
                selectedTopics + topic
              } else {
                selectedTopics
              }
            },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("checkin_topic_chip_$idx"),
            shape = RoundedCornerShape(10.dp),
            color = if (chosen) NavyDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, if (chosen) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = if (chosen) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                contentDescription = null,
                tint = if (chosen) BurntOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = topic,
                style = MaterialTheme.typography.bodySmall,
                color = if (chosen) Color.White else MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }

      OutlinedTextField(
        value = familyNote,
        onValueChange = { familyNote = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("checkin_family_note_input"),
        label = { Text("Parent / Player Pre-Call Note for Coach Keith") },
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
            onSaveCheckInPrep(selectedType, selectedTopics, confidence, familyNote)
          },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("save_checkin_prep_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BurntOrange,
            contentColor = Color.White,
          ),
        ) {
          Text(
            text = "Save Check-In Agenda for Coach Keith",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }

        OutlinedButton(
          onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BALLER_PATH_WEBSITE_URL)).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
          },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
              contentDescription = "Open ballerpath.com website to manage membership and Google Meet check-ins"
            }
            .testTag("open_ballerpath_website_button"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, NavyDeep),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = NavyDeep,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Open ballerpath.com",
            style = MaterialTheme.typography.labelLarge,
            color = NavyDeep,
          )
        }
      }
    }
  }
}

/**
 * Displays the Website (ballerpath.com) <-> Mobile App Streamlined Concept Alignment Card
 * so families and Coach Keith see how the website membership and mobile app repeat the exact same pillars.
 */
@Composable
fun WebsiteAppStreamlinedAuditCard(
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("website_app_streamlined_audit_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Language,
          contentDescription = null,
          tint = BurntOrange,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "WEBSITE (BALLERPATH.COM) ↔ ANDROID APP PARITY",
            style = MaterialTheme.typography.labelMedium,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Repeatable core concepts shared across web membership & daily court tool",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

      WebAppConceptRow(
        pillar = "1. Focus ➔ Practice ➔ Reflect ➔ Adjust + Teach-Back ('In Your Words')",
        detail = "Coach Keith sets 1 focus from your session/clip; player completes guided reps, submits a 30s clip + 1 honest sentence, and says the adjustment back in their own words.",
      )
      WebAppConceptRow(
        pillar = "2. 'I'm Stuck' Private Note & Rough-Week 3 Doors (Zero Guilt)",
        detail = "Players have a private 'I'm Stuck' line to Coach Keith (1-business-day reply) and 3 rough-week doors (Light Week, Reset Focus, Talk to Coach) with nothing to make up.",
      )
      WebAppConceptRow(
        pillar = "3. Post-Game Transfer Log & 'What's Changed Since Week 1'",
        detail = "Logs what carried over into games (marked Carried Over / Partly / Not Yet by Coach Keith) and compares Week 1 vs. Week 4 clips side-by-side.",
      )
      WebAppConceptRow(
        pillar = "4. Parent Sunday Note, 3 Ride-Home Tips & Google Meet Check-Ins",
        detail = "Parents receive 1 calm weekly sentence + 3 Do/Don't ride-home tips (no per-rep spam), alongside 20m First-Week & Development Google Meet check-ins.",
      )
    }
  }
}

@Composable
private fun WebAppConceptRow(
  pillar: String,
  detail: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(
      imageVector = Icons.Outlined.Sync,
      contentDescription = null,
      tint = NavyDeep,
      modifier = Modifier
        .padding(top = 2.dp)
        .size(16.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(
        text = pillar,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = detail,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
