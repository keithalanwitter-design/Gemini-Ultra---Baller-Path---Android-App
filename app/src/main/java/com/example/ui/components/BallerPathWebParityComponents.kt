package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.CoachCheckInMeetingType
import com.example.data.model.CoachCheckInState
import com.example.data.model.GameCarryoverEntry
import com.example.data.model.TeachBackEntry
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeDark
import com.example.ui.theme.BurntOrangeTint
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.SuccessGreen

/**
 * 1. Compact Coaching Loop Card on Today.
 * Matches ballerpath.com/preview: introduces the Starting Point first, then shows
 * Focus -> Practice -> Written Reflection -> Coach Adjustment.
 * Explicitly notes that film review is closed at launch.
 * Contains no action button so Today remains focused on one clear next action.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachingLoopSummaryCard(
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("baller_path_loop_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.Loop,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "HOW BALLER PATH WORKS · GRADES 4–8",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        FictionalDemoBadge(text = "Sample Pilot Walkthrough")
      }

      Text(
        text = "1. Starting Point First",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Coach Keith starts by learning where your player is right now, then guides one weekly loop:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("coaching_loop_steps_ pill_row"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
      ) {
        Text(
          text = "Focus  →  Practice  →  Written Reflection  →  Coach Adjustment",
          style = MaterialTheme.typography.labelMedium,
          color = NavyDeep,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
      }

      Text(
        text = "Player video submissions and film review are closed at launch.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/**
 * 2. Check-Ins Card on Plan (`CoachKeithCheckInConsultationCard`).
 * Uses the exact meeting types and eligibility from ballerpath.com/book:
 * - Prospective Family Call: 30 minutes
 * - First-Week Check-In: Planned for 20 minutes (30-minute calendar reservation) · Included once after activation
 * - Development Check-In: Planned for 20 minutes (30-minute calendar reservation) · Regular check-ins depend on program
 * No film-review agenda options. Notes saved in the offline app stay on this phone only.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachKeithCheckInConsultationCard(
  checkInState: CoachCheckInState,
  onSelectMeetingType: (CoachCheckInMeetingType) -> Unit,
  onToggleAgendaTopic: (String) -> Unit,
  onSaveLocalCallNote: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var noteDraft by rememberSaveable(checkInState.localNoteForCall) {
    mutableStateOf(checkInState.localNoteForCall)
  }

  val approvedAgendaOptions = listOf(
    "Confirm this week's spacing focus",
    "Questions on the 'Try the next rep' adjustment",
    "Discuss what showed up in games",
    "Plan next week's practice rhythm",
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("coach_checkin_consultation_card"),
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "CHECK-INS WITH COACH KEITH",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        FictionalDemoBadge(text = "Saved on This Phone Only")
      }

      Text(
        text = "Meeting Types & Eligibility",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "First-week call is included once after activation. Regular development check-ins depend on your program.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CoachCheckInMeetingType.entries.forEach { meetingType ->
          val isSelected = checkInState.selectedMeetingType == meetingType
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onSelectMeetingType(meetingType) },
              )
              .semantics(mergeDescendants = true) {
                contentDescription = "${meetingType.title}, ${meetingType.durationText}. ${meetingType.eligibilityText}"
                role = Role.RadioButton
              }
              .testTag("checkin_type_${meetingType.id}"),
            shape = MaterialTheme.shapes.medium,
            color = if (isSelected) BurntOrangeTint.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = meetingType.title,
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = meetingType.durationText,
                  style = MaterialTheme.typography.labelSmall,
                  color = BurntOrangeDark,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              Text(
                text = meetingType.eligibilityText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

      Text(
        text = "Optional Call Scratchpad (Stays on This Phone)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Any note saved in this offline app stays on this phone and does not reach Coach Keith.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        approvedAgendaOptions.forEachIndexed { idx, topic ->
          val selected = topic in checkInState.selectedAgendaTopics
          FilterChip(
            selected = selected,
            onClick = { onToggleAgendaTopic(topic) },
            modifier = Modifier.testTag("checkin_agenda_chip_$idx"),
            label = {
              Text(
                text = topic,
                style = MaterialTheme.typography.labelSmall,
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = BurntOrangeTint,
              selectedLabelColor = BurntOrangeDark,
            ),
          )
        }
      }

      OutlinedTextField(
        value = noteDraft,
        onValueChange = { noteDraft = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("checkin_local_note_input"),
        label = { Text("Questions to remember for your call (saved on this phone)") },
        minLines = 2,
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BurntOrange,
          focusedLabelColor = BurntOrange,
          cursorColor = BurntOrange,
        ),
      )

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = { onSaveLocalCallNote(noteDraft) },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("save_checkin_prep_button"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, NavyDeep),
        ) {
          Text(
            text = if (checkInState.isLocalNoteSaved) "Saved on This Phone" else "Save Note on Phone",
            style = MaterialTheme.typography.labelLarge,
            color = NavyDeep,
            fontWeight = FontWeight.SemiBold,
          )
        }

        Button(
          onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ballerpath.com/book")).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
          },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
              contentDescription = "Open Baller Path booking page at https://ballerpath.com/book"
            }
            .testTag("open_booking_page_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BurntOrange,
            contentColor = Color.White,
          ),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Book on ballerpath.com/book",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

/**
 * 3. "In Your Words" Card (`InYourWordsTeachBackCard`) after feedback.
 * Gives the player a way to restate the coaching adjustment in their own words before the next rep.
 * Clearly labeled as saved on this phone only (no coach-mark controls or false delivery claims).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InYourWordsTeachBackCard(
  teachBackEntry: TeachBackEntry,
  onSaveTeachBack: (whatWillDoDifferently: String, whatWillNotice: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var doDifferentlyDraft by rememberSaveable(teachBackEntry.whatWillDoDifferently) {
    mutableStateOf(teachBackEntry.whatWillDoDifferently)
  }
  var noticeDraft by rememberSaveable(teachBackEntry.whatWillNotice) {
    mutableStateOf(teachBackEntry.whatWillNotice)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("teach_back_in_your_words_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
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
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.EditNote,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "IN YOUR WORDS · AFTER FEEDBACK",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        FictionalDemoBadge(text = "Saved on This Phone Only")
      }

      Text(
        text = "Put the Adjustment in Your Own Words",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "After reading feedback, write how you'll use the adjustment on your next court session. This note stays on this phone.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      OutlinedTextField(
        value = doDifferentlyDraft,
        onValueChange = { doDifferentlyDraft = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("teach_back_do_differently_input"),
        label = { Text("What will you do differently on the next rep?") },
        minLines = 2,
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BurntOrange,
          focusedLabelColor = BurntOrange,
          cursorColor = BurntOrange,
        ),
      )

      OutlinedTextField(
        value = noticeDraft,
        onValueChange = { noticeDraft = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("teach_back_notice_input"),
        label = { Text("What will you look for on the floor?") },
        minLines = 2,
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BurntOrange,
          focusedLabelColor = BurntOrange,
          cursorColor = BurntOrange,
        ),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (teachBackEntry.isSavedLocally) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.testTag("teach_back_saved_badge"),
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = SuccessGreen,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = "Saved on this phone",
              style = MaterialTheme.typography.labelSmall,
              color = SuccessGreen,
              fontWeight = FontWeight.Bold,
            )
          }
        } else {
          Spacer(modifier = Modifier.width(4.dp))
        }

        Button(
          onClick = {
            if (doDifferentlyDraft.isNotBlank() || noticeDraft.isNotBlank()) {
              onSaveTeachBack(doDifferentlyDraft.trim(), noticeDraft.trim())
            }
          },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("teach_back_save_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = NavyDeep,
            contentColor = Color.White,
          ),
        ) {
          Text(
            text = "Save In My Words",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

/**
 * 4. Short "What Showed Up in the Game?" Reflection Card (`GameCarryoverReflectionCard`).
 * Gives the player a quick way to record whether the weekly focus carried over to live play.
 * Clearly labeled with sample data and local-only storage on this phone.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameCarryoverReflectionCard(
  entries: List<GameCarryoverEntry>,
  onAddEntry: (opponent: String, whatShowedUp: String, nextFocusInGames: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var opponentDraft by rememberSaveable { mutableStateOf("") }
  var showedUpDraft by rememberSaveable { mutableStateOf("") }
  var nextGameDraft by rememberSaveable { mutableStateOf("") }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("game_transfer_bridge_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
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
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.SportsBasketball,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "GAME CARRYOVER · ON-DEVICE LOG",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        FictionalDemoBadge(text = "Saved on This Phone Only")
      }

      Text(
        text = "What Showed Up in the Game?",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "Log a quick note after a game or scrimmage to track whether your weekly focus carried over. Notes stay on this phone.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      OutlinedTextField(
        value = opponentDraft,
        onValueChange = { opponentDraft = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("game_transfer_opponent_input"),
        label = { Text("Game or Scrimmage (e.g., Saturday League)") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BurntOrange,
          focusedLabelColor = BurntOrange,
          cursorColor = BurntOrange,
        ),
      )

      OutlinedTextField(
        value = showedUpDraft,
        onValueChange = { showedUpDraft = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("game_transfer_carried_input"),
        label = { Text("What showed up from practice?") },
        minLines = 2,
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BurntOrange,
          focusedLabelColor = BurntOrange,
          cursorColor = BurntOrange,
        ),
      )

      OutlinedTextField(
        value = nextGameDraft,
        onValueChange = { nextGameDraft = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("game_transfer_next_input"),
        label = { Text("What do you want to notice earlier next game?") },
        minLines = 1,
        maxLines = 2,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BurntOrange,
          focusedLabelColor = BurntOrange,
          cursorColor = BurntOrange,
        ),
      )

      Button(
        onClick = {
          if (showedUpDraft.isNotBlank()) {
            onAddEntry(
              opponentDraft.trim().ifBlank { "Weekend Game" },
              showedUpDraft.trim(),
              nextGameDraft.trim(),
            )
            opponentDraft = ""
            showedUpDraft = ""
            nextGameDraft = ""
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 48.dp)
          .testTag("game_transfer_save_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BurntOrange,
          contentColor = Color.White,
        ),
      ) {
        Text(
          text = "Save Game Note on Phone",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }

      if (entries.isNotEmpty()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          entries.take(3).forEach { item ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("game_transfer_item_${item.id}"),
              shape = MaterialTheme.shapes.medium,
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = "${item.dateLabel} · ${item.opponent}",
                    style = MaterialTheme.typography.labelMedium,
                    color = NavyDeep,
                    fontWeight = FontWeight.Bold,
                  )
                  FictionalDemoBadge(
                    text = if (item.isSampleEntry) "Sample Entry" else "Saved on Phone",
                  )
                }
                Text(
                  text = item.whatShowedUp,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                if (item.nextFocusInGames.isNotBlank()) {
                  Text(
                    text = "Next game: ${item.nextFocusInGames}",
                    style = MaterialTheme.typography.labelSmall,
                    color = BurntOrangeDark,
                    fontWeight = FontWeight.Medium,
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

/**
 * 5. Website Handoff Card on Account (`WebsiteHandoffLinksCard`).
 * Replaces the Account audit card with short direct links for:
 * - Family Access (`https://ballerpath.com/register`)
 * - Book a Check-In (`https://ballerpath.com/book`)
 * - Privacy (`https://ballerpath.com/privacy`)
 * States clearly that two business days is the introduction review window and Coach Keith activates access separately.
 */
@Composable
fun WebsiteHandoffLinksCard(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  fun openWebsiteUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("account_website_handoff_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = "BALLERPATH.COM · FAMILY HANDOFF",
        style = MaterialTheme.typography.labelSmall,
        color = BurntOrange,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Website Links & Next Steps",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "Coach Keith reviews family introductions within 2 business days and activates access separately.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      WebsiteDirectLinkRow(
        title = "Family Access",
        subtitle = "https://ballerpath.com/register",
        contentDesc = "Open Family Access registration page at https://ballerpath.com/register",
        testTag = "account_family_access_link",
        onClick = { openWebsiteUrl("https://ballerpath.com/register") },
      )

      WebsiteDirectLinkRow(
        title = "Book a Check-In",
        subtitle = "https://ballerpath.com/book",
        contentDesc = "Open Book a Check-In page at https://ballerpath.com/book",
        testTag = "account_book_checkin_link",
        onClick = { openWebsiteUrl("https://ballerpath.com/book") },
      )

      WebsiteDirectLinkRow(
        title = "Privacy",
        subtitle = "https://ballerpath.com/privacy",
        contentDesc = "Open Privacy page at https://ballerpath.com/privacy",
        testTag = "account_handoff_privacy_link",
        onClick = { openWebsiteUrl("https://ballerpath.com/privacy") },
      )
    }
  }
}

@Composable
private fun WebsiteDirectLinkRow(
  title: String,
  subtitle: String,
  contentDesc: String,
  testTag: String,
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .defaultMinSize(minHeight = 48.dp)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(),
        onClick = onClick,
      )
      .semantics(mergeDescendants = true) {
        contentDescription = contentDesc
        role = Role.Button
      }
      .testTag(testTag),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = BurntOrangeDark,
          textDecoration = TextDecoration.Underline,
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
        contentDescription = null,
        tint = BurntOrangeDark,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}
