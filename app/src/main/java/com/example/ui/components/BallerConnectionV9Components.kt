package com.example.ui.components

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.model.FirstLookAndSinceWeek1State
import com.example.data.model.GameTransferNote
import com.example.data.model.RoughWeekChoice
import com.example.data.model.StuckPrivateNote
import com.example.data.model.TeachBackEntry
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeTint
import com.example.ui.theme.NavyDeep

/**
 * Post-onboarding Today card incorporating the signature Baller Connection (v9) features:
 * 1. Coach Keith's "First Look" ("What Keith saw in you") + "From your session on Tue at Storm"
 * 2. Weekly Rhythm & Rough-Week Grace ("A light week", "A reset", "Talk to my coach" — zero guilt)
 * 3. "✋ I'm Stuck" Private Note to Coach Keith (3 gentle optional prompts, 1-business-day reply)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirstLookSessionAndPaceCard(
  firstLookState: FirstLookAndSinceWeek1State,
  roughWeekChoice: RoughWeekChoice,
  stuckNote: StuckPrivateNote,
  onSelectRoughWeekChoice: (RoughWeekChoice) -> Unit,
  onSubmitStuckNote: (whatHappened: String, whatWouldHelp: String, howFeeling: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showStuckForm by rememberSaveable { mutableStateOf(false) }
  var showPaceOptions by rememberSaveable { mutableStateOf(false) }
  var happenedText by rememberSaveable(stuckNote.whatHappened) { mutableStateOf(stuckNote.whatHappened) }
  var wantText by rememberSaveable(stuckNote.whatWouldHelp) { mutableStateOf(stuckNote.whatWouldHelp) }
  var feelingText by rememberSaveable(stuckNote.howFeeling) { mutableStateOf(stuckNote.howFeeling) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("first_look_session_pace_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // 1. In-Person Storm Session & Coach Keith First Look
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Visibility,
          contentDescription = null,
          tint = BurntOrange,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = firstLookState.inPersonSessionLine.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "A coach who sees your reps between sessions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Surface(
        shape = MaterialTheme.shapes.medium,
        color = NavyDeep,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = firstLookState.firstLookSourceLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "\"${firstLookState.firstLookSaw}\"",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = Color.White,
          )
          HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
          Text(
            text = "Session Note: ${firstLookState.inPersonSessionNote}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
          )
          Text(
            text = "Court Cue: ${firstLookState.inPersonSessionCue}",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // 2. Action Bar: "✋ I'm Stuck" Private Note + "Rough Week / Adjust Rhythm"
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = { showStuckForm = !showStuckForm },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("im_stuck_toggle_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (showStuckForm) NavyDeep else BurntOrange,
            contentColor = Color.White,
          ),
        ) {
          Icon(
            imageVector = Icons.Filled.FrontHand,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (stuckNote.isSent) "I'm Stuck Note Sent ✓" else "I'm Stuck (Private Note to Coach)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
          )
        }

        OutlinedButton(
          onClick = { showPaceOptions = !showPaceOptions },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("rough_week_toggle_button"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, NavyDeep),
        ) {
          Icon(
            imageVector = Icons.Outlined.Tune,
            contentDescription = null,
            tint = NavyDeep,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (roughWeekChoice == RoughWeekChoice.NONE) {
              "Rough Week? 3 Doors"
            } else {
              "Pace: ${roughWeekChoice.label}"
            },
            style = MaterialTheme.typography.labelMedium,
            color = NavyDeep,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // Expandable Rough Week / Pace Grace Doors
      if (showPaceOptions) {
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rough_week_options_box"),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = "A missed or tough week is fine — no streaks to lose, nothing to make up:",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
            RoughWeekChoice.entries.forEach { option ->
              val selected = option == roughWeekChoice
              Surface(
                onClick = { onSelectRoughWeekChoice(option) },
                modifier = Modifier
                  .fillMaxWidth()
                  .defaultMinSize(minHeight = 48.dp)
                  .testTag("rough_week_choice_${option.key}"),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) NavyDeep else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (selected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    tint = if (selected) BurntOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = option.label,
                      style = MaterialTheme.typography.labelLarge,
                      color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = option.note,
                      style = MaterialTheme.typography.bodySmall,
                      color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Expandable "I'm Stuck" Private Note to Coach Keith
      if (showStuckForm) {
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.primaryContainer,
          border = BorderStroke(1.5.dp, BurntOrange),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("im_stuck_private_sheet"),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = BurntOrange,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "PRIVATE NOTE TO COACH KEITH · STUCK OR FRUSTRATED?",
                style = MaterialTheme.typography.labelSmall,
                color = BurntOrange,
                fontWeight = FontWeight.Bold,
              )
            }
            Text(
              text = stuckNote.slaPromise,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            OutlinedTextField(
              value = happenedText,
              onValueChange = { happenedText = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("stuck_happened_input"),
              label = { Text("1. What happened? (A game, a drill, a tough day)") },
            )
            OutlinedTextField(
              value = wantText,
              onValueChange = { wantText = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("stuck_want_input"),
              label = { Text("2. What would help right now? (Different drill, lighter week, talk)") },
            )
            OutlinedTextField(
              value = feelingText,
              onValueChange = { feelingText = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("stuck_feeling_input"),
              label = { Text("3. How are you feeling about it? (One word is fine)") },
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Answer any one. A few words is enough.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Button(
                onClick = {
                  onSubmitStuckNote(happenedText, wantText, feelingText)
                  showStuckForm = false
                },
                enabled = happenedText.isNotBlank() || wantText.isNotBlank() || feelingText.isNotBlank(),
                modifier = Modifier
                  .defaultMinSize(minHeight = 48.dp)
                  .testTag("stuck_send_private_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = BurntOrange,
                  contentColor = Color.White,
                ),
              ) {
                Text(
                  text = "Send Privately",
                  style = MaterialTheme.typography.labelMedium,
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

/**
 * Mirrors Baller Connection (v9) Teach-Back ("In your words"):
 * Replaces passive tap-to-confirm with:
 * - "What will you do differently?"
 * - "What will you notice if it works?"
 * - Coach Keith marks "Got it ✓" or "Let's clarify ↺".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeachBackInYourWordsCard(
  teachBackEntry: TeachBackEntry,
  isCoachMode: Boolean,
  onSubmitTeachBack: (whatWillDoDifferently: String, whatWillNotice: String) -> Unit,
  onCoachMarkTeachBack: (mark: String, clarifyNote: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var changeInput by rememberSaveable(teachBackEntry.whatWillDoDifferently) {
    mutableStateOf(teachBackEntry.whatWillDoDifferently)
  }
  var noticeInput by rememberSaveable(teachBackEntry.whatWillNotice) {
    mutableStateOf(teachBackEntry.whatWillNotice)
  }
  var coachNoteInput by rememberSaveable(teachBackEntry.coachClarifyNote) {
    mutableStateOf(teachBackEntry.coachClarifyNote)
  }

  val examples = remember {
    listOf(
      "I'll keep my eyes on the rim until the ball is in my hands.",
      "I'll hold the weak-side corner for 1.5s before lifting to the wing.",
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("teach_back_in_your_words_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.RecordVoiceOver,
            contentDescription = null,
            tint = BurntOrange,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "TEACH-BACK · SAY THE ADJUSTMENT IN YOUR OWN WORDS",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
        }
        Surface(
          shape = RoundedCornerShape(50),
          color = when (teachBackEntry.coachMark) {
            "got-it" -> NavyDeep
            "clarify" -> BurntOrange
            else -> MaterialTheme.colorScheme.surfaceVariant
          },
        ) {
          Text(
            text = when (teachBackEntry.coachMark) {
              "got-it" -> "✓ Coach Keith: Got it"
              "clarify" -> "↺ Coach Keith: Let's clarify"
              else -> "Waiting for Coach Keith"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (teachBackEntry.coachMark == "unmarked") MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 3.dp)
              .testTag("teach_back_status_badge"),
          )
        }
      }

      Text(
        text = "A sentence is plenty. Coach Keith reads it and marks 'Got it' or clarifies. Skipping is fine — your next rep opens either way.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        examples.forEachIndexed { idx, ex ->
          Surface(
            onClick = { changeInput = ex },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier
              .defaultMinSize(minHeight = 40.dp)
              .testTag("teach_back_example_$idx"),
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "Example: \"$ex\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }

      OutlinedTextField(
        value = changeInput,
        onValueChange = { changeInput = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("teach_back_change_input"),
        label = { Text("What will you do differently on your next rep?") },
      )

      OutlinedTextField(
        value = noticeInput,
        onValueChange = { noticeInput = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("teach_back_notice_input"),
        label = { Text("What will you notice if it works? (Optional)") },
      )

      Button(
        onClick = { onSubmitTeachBack(changeInput, noticeInput) },
        enabled = changeInput.trim().length >= 4,
        modifier = Modifier
          .defaultMinSize(minHeight = 48.dp)
          .testTag("teach_back_send_button"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BurntOrange,
          contentColor = Color.White,
        ),
      ) {
        Text(
          text = "Send Teach-Back to Coach Keith",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
      }

      if (teachBackEntry.coachClarifyNote.isNotBlank()) {
        Surface(
          shape = MaterialTheme.shapes.small,
          color = NavyDeep,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "Coach Keith: \"${teachBackEntry.coachClarifyNote}\"",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.padding(10.dp),
          )
        }
      }

      if (isCoachMode || BuildConfig.DEBUG) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        Text(
          text = "COACH KEITH TEACH-BACK MARK",
          style = MaterialTheme.typography.labelSmall,
          color = BurntOrange,
          fontWeight = FontWeight.Bold,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = { onCoachMarkTeachBack("got-it", coachNoteInput) },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("coach_mark_teachback_got_it"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, NavyDeep),
          ) {
            Text("Mark: Got it ✓", color = NavyDeep)
          }
          OutlinedButton(
            onClick = { onCoachMarkTeachBack("clarify", coachNoteInput) },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .testTag("coach_mark_teachback_clarify"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BurntOrange),
          ) {
            Text("Mark: Let's clarify ↺", color = BurntOrange)
          }
        }
      }
    }
  }
}

/**
 * Mirrors Baller Connection (v9) Post-Game Transfer Log ("What carried over?")
 * and the 4-Week Cycle Review ("What's changed since Week 1").
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameTransferAndSinceWeek1Card(
  gameNotes: List<GameTransferNote>,
  firstLookState: FirstLookAndSinceWeek1State,
  isCoachMode: Boolean,
  onLogGameNote: (opponent: String, carriedOver: String, didntYet: String, feelLabel: String) -> Unit,
  onCoachMarkGame: (gameId: String, coachMark: String, coachNote: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showLogForm by rememberSaveable { mutableStateOf(false) }
  var opponentInput by rememberSaveable { mutableStateOf("") }
  var carriedInput by rememberSaveable { mutableStateOf("") }
  var missedInput by rememberSaveable { mutableStateOf("") }
  var selectedFeel by rememberSaveable { mutableStateOf("Good") }
  val feelOptions = remember { listOf("Rough", "Hard", "Okay", "Good", "Locked in") }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("game_transfer_since_week1_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.SportsBasketball,
          contentDescription = null,
          tint = BurntOrange,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "AFTER A GAME OR SCRIMMAGE · WHAT CARRIED OVER?",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Practice builds it. Games show it. Coach Keith marks what carried over (never a fake %).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      gameNotes.forEach { game ->
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("game_note_item_${game.id}"),
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
                text = "${game.dateLabel} · Felt: ${game.feelLabel}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
              Surface(
                shape = RoundedCornerShape(50),
                color = when (game.coachMark) {
                  "carried" -> NavyDeep
                  "partly" -> BurntOrange
                  else -> MaterialTheme.colorScheme.surface
                },
              ) {
                Text(
                  text = when (game.coachMark) {
                    "carried" -> "Coach Mark: Carried Over ✓"
                    "partly" -> "Coach Mark: Partly"
                    "not-yet" -> "Coach Mark: Not Yet"
                    else -> "Waiting for Coach Keith"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  color = if (game.coachMark in listOf("carried", "partly")) Color.White else NavyDeep,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
            Text(
              text = "Carried over: ${game.carriedOver}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            if (game.didntYet.isNotBlank()) {
              Text(
                text = "Didn't yet: ${game.didntYet}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            if (game.coachNote.isNotBlank()) {
              Text(
                text = "Coach Keith: \"${game.coachNote}\"",
                style = MaterialTheme.typography.bodySmall,
                color = NavyDeep,
                fontWeight = FontWeight.Medium,
              )
            }
            if (game.isTaggedWin) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.EmojiEvents,
                  contentDescription = null,
                  tint = BurntOrange,
                  modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "★ Added to 'What Coach Keith Noticed' Wins Wall",
                  style = MaterialTheme.typography.labelSmall,
                  color = BurntOrange,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
            if (isCoachMode && game.coachMark == "unread") {
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                listOf("carried" to "Carried Over", "partly" to "Partly", "not-yet" to "Not Yet").forEach { (markKey, label) ->
                  OutlinedButton(
                    onClick = { onCoachMarkGame(game.id, markKey, "") },
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                  ) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                  }
                }
              }
            }
          }
        }
      }

      OutlinedButton(
        onClick = { showLogForm = !showLogForm },
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 48.dp)
          .testTag("toggle_game_note_form_button"),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BurntOrange),
      ) {
        Text(
          text = if (showLogForm) "Hide Post-Game Note Form" else "+ Log Post-Game / Scrimmage Transfer Note",
          style = MaterialTheme.typography.labelLarge,
          color = BurntOrange,
          fontWeight = FontWeight.Bold,
        )
      }

      if (showLogForm) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("game_note_form_container"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = opponentInput,
            onValueChange = { opponentInput = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("game_opponent_input"),
            label = { Text("Opponent or Scrimmage (Optional)") },
          )
          OutlinedTextField(
            value = carriedInput,
            onValueChange = { carriedInput = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("game_carried_input"),
            label = { Text("What from practice showed up? ('Nothing yet' is honest)") },
          )
          OutlinedTextField(
            value = missedInput,
            onValueChange = { missedInput = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("game_missed_input"),
            label = { Text("What didn't show up yet? (Optional)") },
          )
          Text(
            text = "How did it feel? (Only you and Coach Keith see this):",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            feelOptions.forEach { feel ->
              val active = selectedFeel == feel
              Surface(
                onClick = { selectedFeel = feel },
                shape = RoundedCornerShape(10.dp),
                color = if (active) NavyDeep else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .defaultMinSize(minHeight = 44.dp)
                  .testTag("game_feel_${feel.lowercase().replace(" ", "_")}"),
              ) {
                Box(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = feel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }
          Button(
            onClick = {
              onLogGameNote(opponentInput, carriedInput, missedInput, selectedFeel)
              opponentInput = ""
              carriedInput = ""
              missedInput = ""
              showLogForm = false
            },
            enabled = carriedInput.trim().length >= 3,
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 48.dp)
              .testTag("save_game_note_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = BurntOrange,
              contentColor = Color.White,
            ),
          ) {
            Text("Save Game Note for Coach Keith", fontWeight = FontWeight.Bold)
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

      // 4-Week Cycle Review: "What's Changed Since Week 1"
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.CompareArrows,
          contentDescription = null,
          tint = NavyDeep,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "WHAT'S CHANGED SINCE WEEK 1 (4-WEEK RE-CHECK)",
          style = MaterialTheme.typography.labelSmall,
          color = NavyDeep,
          fontWeight = FontWeight.Bold,
        )
      }

      Surface(
        shape = MaterialTheme.shapes.medium,
        color = NavyDeep,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("since_week1_comparison_box"),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "THEN (${firstLookState.week1ClipTitle}):",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = firstLookState.week1ClipNote,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "NOW (${firstLookState.nowClipTitle}):",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = firstLookState.nowClipNote,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
          )
          HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
          Text(
            text = firstLookState.coachCycleComparisonNote,
            style = MaterialTheme.typography.bodySmall,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Medium,
          )
        }
      }
    }
  }
}

/**
 * Mirrors Baller Connection (v9) Parent View (`Parent.tsx` & `shared/v9.ts`):
 * - One-sentence Sunday summary ("What this means this week" — no per-rep notification spam)
 * - 3 Do / Don't Ride-Home Tips for Parents
 * - 4-Part Family Roles Agreement (The player, The parent, The coach, Everyone)
 */
@Composable
fun ParentSundayDigestAndRolesCard(
  firstLookState: FirstLookAndSinceWeek1State,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("parent_sunday_digest_roles_card"),
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
          imageVector = Icons.Outlined.FamilyRestroom,
          contentDescription = null,
          tint = BurntOrange,
          modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "PARENT VIEW · SUNDAY NOTE, RIDE-HOME TIPS & ROLES",
            style = MaterialTheme.typography.labelMedium,
            color = BurntOrange,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "One calm Sunday summary — no per-rep alert spam",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Surface(
        shape = MaterialTheme.shapes.medium,
        color = NavyDeep,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "COACH KEITH'S WEEKLY PARENT SENTENCE",
            style = MaterialTheme.typography.labelSmall,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = firstLookState.parentWeeklySentence,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
          )
        }
      }

      Text(
        text = "3 Parent Do / Don't Tips This Week:",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )
      ParentTipRow(
        badge = "DO",
        isDo = true,
        text = "Ask: \"What's your focus this week?\" and let Maya answer in her own words.",
      )
      ParentTipRow(
        badge = "DO",
        isDo = true,
        text = "If Maya wants to show you a clip, watch it together. Skip the critique.",
      )
      ParentTipRow(
        badge = "DON'T",
        isDo = false,
        text = "Skip \"Did you do your reps?\" and \"Why didn't that go in?\" on the ride home. Coach Keith handles the coaching.",
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

      Text(
        text = "Baller Path Roles Agreement:",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )
      RoleAgreementLine(
        who = "The player",
        line = "Sends a 30s clip on the agreed rhythm, writes one honest sentence, and says the adjustment back in their own words.",
      )
      RoleAgreementLine(
        who = "The parent",
        line = "Asks about the rep, not the result. Lets the player own clip day. Doesn't coach over Coach Keith.",
      )
      RoleAgreementLine(
        who = "Coach Keith",
        line = "Watches every clip, writes back inside the reply window (what went well, one adjustment, why, next rep), and sets one focus at a time.",
      )
      RoleAgreementLine(
        who = "Everyone",
        line = "A missed week is fine — say so and we reset. Private 'I'm stuck' notes go directly to Coach Keith.",
      )
    }
  }
}

@Composable
private fun ParentTipRow(
  badge: String,
  isDo: Boolean,
  text: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
  ) {
    Surface(
      shape = RoundedCornerShape(6.dp),
      color = if (isDo) NavyDeep else BurntOrange,
      modifier = Modifier.padding(top = 2.dp),
    ) {
      Text(
        text = badge,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun RoleAgreementLine(
  who: String,
  line: String,
) {
  Text(
    text = "• $who: $line",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}
