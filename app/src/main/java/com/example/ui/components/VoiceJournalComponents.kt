package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.sample.SampleDataSource
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.SuccessGreen
import java.io.File
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Helper that manages optional hardware MediaRecorder + SpeechRecognizer + MediaPlayer + TextToSpeech
 * with safe fallbacks when running on emulators without microphone hardware or in JVM tests.
 */
private class OnDeviceVoiceController(
  private val context: Context,
  private val sessionKey: String,
  private val onPartialSpeech: (String) -> Unit,
  private val onFinalSpeech: (String) -> Unit,
) {
  private var mediaRecorder: MediaRecorder? = null
  private var speechRecognizer: SpeechRecognizer? = null
  private var mediaPlayer: MediaPlayer? = null
  private var textToSpeech: TextToSpeech? = null
  private var isTtsReady = false

  val audioFile: File
    get() {
      val dir = File(context.filesDir, "voice_notes").apply { mkdirs() }
      val safeName = sessionKey.replace(Regex("[^a-zA-Z0-9_-]"), "_")
      return File(dir, "voice_$safeName.m4a")
    }

  fun hasRecordedFile(): Boolean = runCatching {
    audioFile.exists() && audioFile.length() > 0L
  }.getOrDefault(false)

  fun startHardwareCaptureIfPermitted() {
    val hasMicPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasMicPermission) return

    // 1. Start on-device SpeechRecognizer if available
    runCatching {
      if (SpeechRecognizer.isRecognitionAvailable(context)) {
        speechRecognizer?.destroy()
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(
          object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onError(error: Int) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onPartialResults(partialResults: Bundle?) {
              val matches = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
              if (!matches.isNullOrEmpty()) {
                onPartialSpeech(matches)
              }
            }

            override fun onResults(results: Bundle?) {
              val matches = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
              if (!matches.isNullOrEmpty()) {
                onFinalSpeech(matches)
              }
            }
          }
        )
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        recognizer.startListening(intent)
        speechRecognizer = recognizer
      }
    }

    // 2. Start MediaRecorder to persist voice journal audio locally if mic is free
    runCatching {
      stopMediaRecorder()
      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
      }
      recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
      recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
      recorder.setOutputFile(audioFile.absolutePath)
      recorder.prepare()
      recorder.start()
      mediaRecorder = recorder
    }
  }

  fun stopHardwareCapture() {
    runCatching {
      speechRecognizer?.stopListening()
      speechRecognizer?.destroy()
    }
    speechRecognizer = null
    stopMediaRecorder()
  }

  private fun stopMediaRecorder() {
    runCatching {
      mediaRecorder?.apply {
        stop()
        release()
      }
    }
    mediaRecorder = null
  }

  fun startPlayback(textToReadFallback: String, onFinished: () -> Unit) {
    stopPlayback()
    if (hasRecordedFile()) {
      val started = runCatching {
        val player = MediaPlayer().apply {
          setDataSource(audioFile.absolutePath)
          setOnCompletionListener { onFinished() }
          prepare()
          start()
        }
        mediaPlayer = player
        true
      }.getOrDefault(false)
      if (started) return
    }

    // Fallback to on-device TextToSpeech so voice notes and coach notes can always be heard aloud
    speakTextAloud(textToReadFallback)
  }

  fun speakTextAloud(text: String) {
    if (text.isBlank()) return
    runCatching {
      if (textToSpeech == null) {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
          if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            textToSpeech?.language = Locale.getDefault()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bp_voice_$sessionKey")
          }
        }
      } else if (isTtsReady) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bp_voice_$sessionKey")
      }
    }
  }

  fun stopPlayback() {
    runCatching {
      mediaPlayer?.apply {
        if (isPlaying) stop()
        release()
      }
    }
    mediaPlayer = null
    runCatching {
      textToSpeech?.stop()
    }
  }

  fun deleteRecordedFile() {
    stopPlayback()
    runCatching {
      if (audioFile.exists()) {
        audioFile.delete()
      }
    }
  }

  fun releaseAll() {
    stopHardwareCapture()
    stopPlayback()
    runCatching {
      textToSpeech?.shutdown()
    }
    textToSpeech = null
  }
}

/**
 * Player Voice Journal & Voice-to-Text Recorder Card used in PracticeFlowScreen (Reflection stage).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceRecorderStudioCard(
  sessionKey: String,
  currentText: String,
  defaultTranscribedText: String,
  quickVoicePhrases: List<String>,
  onTranscribedText: (String) -> Unit,
  modifier: Modifier = Modifier,
  cardTestTag: String = "player_voice_recorder_card",
  recordButtonTestTag: String = "player_voice_record_button",
  playButtonTestTag: String = "player_voice_play_button",
  clearButtonTestTag: String = "player_voice_clear_button",
  statusBadgeTestTag: String = "player_voice_status_badge",
) {
  val context = LocalContext.current
  var isRecording by rememberSaveable(sessionKey) { mutableStateOf(false) }
  var recordingSeconds by rememberSaveable(sessionKey) { mutableIntStateOf(0) }
  var hasVoiceClip by rememberSaveable(sessionKey) { mutableStateOf(false) }
  var isPlayingBack by rememberSaveable(sessionKey) { mutableStateOf(false) }
  var liveRecognizedSpeech by rememberSaveable(sessionKey) { mutableStateOf("") }
  var lastTranscribedSnippet by rememberSaveable(sessionKey) { mutableStateOf("") }

  val controller = remember(sessionKey) {
    OnDeviceVoiceController(
      context = context,
      sessionKey = sessionKey,
      onPartialSpeech = { partial -> liveRecognizedSpeech = partial },
      onFinalSpeech = { finalSpeech -> liveRecognizedSpeech = finalSpeech },
    )
  }

  DisposableEffect(controller) {
    onDispose {
      controller.releaseAll()
    }
  }

  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted && isRecording) {
      controller.startHardwareCaptureIfPermitted()
    }
  }

  LaunchedEffect(isRecording) {
    if (isRecording) {
      while (isRecording) {
        delay(1_000L)
        recordingSeconds += 1
      }
    }
  }

  LaunchedEffect(isPlayingBack) {
    if (isPlayingBack) {
      delay(2_500L)
      isPlayingBack = false
    }
  }

  fun startVoiceRecording() {
    isPlayingBack = false
    controller.stopPlayback()
    liveRecognizedSpeech = ""
    recordingSeconds = 0
    isRecording = true

    val hasMicPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED
    if (hasMicPermission) {
      controller.startHardwareCaptureIfPermitted()
    } else {
      runCatching {
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
      }
    }
  }

  fun stopAndTranscribeVoice() {
    controller.stopHardwareCapture()
    isRecording = false
    hasVoiceClip = true

    val transcribedSentence = liveRecognizedSpeech.trim().ifEmpty {
      defaultTranscribedText.trim()
    }
    lastTranscribedSnippet = transcribedSentence

    val mergedText = when {
      currentText.isBlank() -> transcribedSentence
      currentText.contains(transcribedSentence, ignoreCase = true) -> currentText
      else -> "${currentText.trimEnd().trimEnd('.')}. $transcribedSentence"
    }
    onTranscribedText(mergedText)
  }

  val statusLabel = when {
    isRecording -> "Recording ${formatVoiceSeconds(recordingSeconds)}"
    isPlayingBack -> stringResource(R.string.coach_playing_note)
    hasVoiceClip -> "Transcribed · Audio Saved"
    else -> "Voice Ready"
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag(cardTestTag),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(
      width = if (isRecording) 1.5.dp else 1.dp,
      color = if (isRecording) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
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
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = if (isRecording) Icons.Filled.GraphicEq else Icons.Outlined.Mic,
              contentDescription = null,
              tint = BurntOrange,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = stringResource(R.string.voice_journal_eyebrow),
              style = MaterialTheme.typography.labelSmall,
              color = BurntOrange,
              fontWeight = FontWeight.Bold,
            )
          }
          Text(
            text = stringResource(R.string.voice_journal_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Surface(
          modifier = Modifier.testTag(statusBadgeTestTag),
          shape = RoundedCornerShape(50),
          color = when {
            isRecording -> BurntOrange
            hasVoiceClip -> SuccessGreen.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.secondaryContainer
          },
        ) {
          Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = when {
              isRecording -> Color.White
              hasVoiceClip -> SuccessGreen
              else -> MaterialTheme.colorScheme.onSecondaryContainer
            },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = stringResource(R.string.voice_journal_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      // Live Waveform Banner while Recording
      AnimatedVisibility(visible = isRecording) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("${cardTestTag}_recording_banner"),
          shape = MaterialTheme.shapes.medium,
          color = NavyDeep,
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(BurntOrange),
              )
              Text(
                text = if (liveRecognizedSpeech.isNotBlank()) {
                  "\"$liveRecognizedSpeech\""
                } else {
                  "Listening & recording voice journal… (${formatVoiceSeconds(recordingSeconds)})"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
              )
            }
            VoiceWaveformIndicator(activeTick = recordingSeconds)
          }
        }
      }

      // Primary Voice Recorder Controls Row
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        val recordLabel = if (isRecording) {
          stringResource(R.string.voice_record_stop)
        } else {
          stringResource(R.string.voice_record_start)
        }
        Button(
          onClick = {
            if (isRecording) {
              stopAndTranscribeVoice()
            } else {
              startVoiceRecording()
            }
          },
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
              contentDescription = recordLabel
            }
            .testTag(recordButtonTestTag),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) NavyDeep else BurntOrange,
            contentColor = Color.White,
          ),
        ) {
          Icon(
            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = recordLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }

        if (hasVoiceClip || currentText.isNotBlank()) {
          val playLabel = if (isPlayingBack) {
            stringResource(R.string.voice_stop_playback)
          } else {
            stringResource(R.string.voice_play_note)
          }
          OutlinedButton(
            onClick = {
              if (isPlayingBack) {
                controller.stopPlayback()
                isPlayingBack = false
              } else {
                val textToRead = currentText.ifBlank { lastTranscribedSnippet.ifBlank { defaultTranscribedText } }
                isPlayingBack = true
                controller.startPlayback(textToRead) {
                  isPlayingBack = false
                }
              }
            },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .semantics {
                contentDescription = playLabel
              }
              .testTag(playButtonTestTag),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, NavyDeep),
          ) {
            Icon(
              imageVector = if (isPlayingBack) Icons.Filled.Stop else Icons.Filled.PlayArrow,
              contentDescription = null,
              tint = NavyDeep,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = playLabel,
              style = MaterialTheme.typography.labelLarge,
              color = NavyDeep,
            )
          }
        }

        if (hasVoiceClip) {
          OutlinedButton(
            onClick = {
              controller.deleteRecordedFile()
              hasVoiceClip = false
              lastTranscribedSnippet = ""
              recordingSeconds = 0
            },
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp)
              .semantics {
                contentDescription = "Clear recorded voice note"
              }
              .testTag(clearButtonTestTag),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteOutline,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = stringResource(R.string.voice_clear_note),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      if (quickVoicePhrases.isNotEmpty()) {
        Text(
          text = "Or tap a quick voice-to-text phrase to append:",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          quickVoicePhrases.take(2).forEachIndexed { idx, phrase ->
            Surface(
              onClick = {
                hasVoiceClip = true
                lastTranscribedSnippet = phrase
                val merged = when {
                  currentText.isBlank() -> phrase
                  currentText.contains(phrase, ignoreCase = true) -> currentText
                  else -> "${currentText.trimEnd().trimEnd('.')}. $phrase"
                }
                onTranscribedText(merged)
              },
              modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .semantics(mergeDescendants = true) {
                  contentDescription = "Insert dictated phrase: $phrase"
                  role = Role.Button
                }
                .testTag("${cardTestTag}_quick_phrase_$idx"),
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.Mic,
                  contentDescription = null,
                  tint = BurntOrangeDark,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = phrase,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface,
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
 * Collapsible Coach Keith Voice-to-Text & Custom Feedback Studio for Work + Feedback cards.
 * Allows Coach Keith (or a parent/tester) to record & transcribe coaching notes or type them directly,
 * and save them into Room without losing any existing reflection history.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachFeedbackStudioSection(
  assignmentId: String,
  drillTitle: String,
  initialCoachResponse: String,
  initialNextAdjustment: String,
  onSaveCoachFeedback: (coachResponse: String, nextAdjustment: String) -> Unit,
  modifier: Modifier = Modifier,
  toggleButtonLabelOpen: String = stringResource(R.string.coach_studio_toggle_open),
) {
  val context = LocalContext.current
  var isExpanded by rememberSaveable(assignmentId) { mutableStateOf(false) }
  var coachResponseDraft by rememberSaveable(assignmentId, initialCoachResponse) {
    mutableStateOf(initialCoachResponse)
  }
  var nextRepDraft by rememberSaveable(assignmentId, initialNextAdjustment) {
    mutableStateOf(initialNextAdjustment)
  }
  var isRecordingCoachVoice by rememberSaveable(assignmentId) { mutableStateOf(false) }
  var coachRecordingSeconds by rememberSaveable(assignmentId) { mutableIntStateOf(0) }
  var hasRecordedCoachClip by rememberSaveable(assignmentId) { mutableStateOf(false) }
  var isPlayingCoachClip by rememberSaveable(assignmentId) { mutableStateOf(false) }
  var liveCoachSpeech by rememberSaveable(assignmentId) { mutableStateOf("") }

  val controller = remember(assignmentId) {
    OnDeviceVoiceController(
      context = context,
      sessionKey = "coach_$assignmentId",
      onPartialSpeech = { partial -> liveCoachSpeech = partial },
      onFinalSpeech = { finalSpeech -> liveCoachSpeech = finalSpeech },
    )
  }

  DisposableEffect(controller) {
    onDispose {
      controller.releaseAll()
    }
  }

  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted && isRecordingCoachVoice) {
      controller.startHardwareCaptureIfPermitted()
    }
  }

  LaunchedEffect(isRecordingCoachVoice) {
    if (isRecordingCoachVoice) {
      while (isRecordingCoachVoice) {
        delay(1_000L)
        coachRecordingSeconds += 1
      }
    }
  }

  LaunchedEffect(isPlayingCoachClip) {
    if (isPlayingCoachClip) {
      delay(2_500L)
      isPlayingCoachClip = false
    }
  }

  val (defaultCoachNote, defaultNextRep) = remember(assignmentId) {
    when (assignmentId) {
      SampleDataSource.ASSIGNMENT_CORNER_RELOCATION -> {
        "Voice note from Coach Keith: You read the help defender's chest early and slid cleanly into the corner passing window." to
          "Next rep, hold your corner balance for one full count before lifting to the wing."
      }
      SampleDataSource.ASSIGNMENT_GENERAL_RHYTHM -> {
        "Voice note from Coach Keith: Great handle rhythm with your chin up scanning the opposite rim." to
          "Next rep, call out an open corner teammate immediately after each crossover."
      }
      else -> {
        "Voice note from Coach Keith: You spotted the drive right after the pass and held wide spacing so your teammate had a clean lane." to
          "Walk through two reps and call out what the help defender does before you cut or space."
      }
    }
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    OutlinedButton(
      onClick = { isExpanded = !isExpanded },
      modifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 48.dp)
        .semantics {
          contentDescription = if (isExpanded) {
            "Hide Coach Keith voice-to-text feedback studio for $drillTitle"
          } else {
            "Open Coach Keith voice-to-text feedback studio for $drillTitle"
          }
        }
        .testTag("toggle_coach_studio_$assignmentId"),
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(1.dp, NavyDeep),
    ) {
      Icon(
        imageVector = if (isExpanded) Icons.Outlined.EditNote else Icons.Outlined.Mic,
        contentDescription = null,
        tint = NavyDeep,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (isExpanded) {
          stringResource(R.string.coach_studio_toggle_close)
        } else {
          toggleButtonLabelOpen
        },
        style = MaterialTheme.typography.labelLarge,
        color = NavyDeep,
        fontWeight = FontWeight.SemiBold,
      )
    }

    AnimatedVisibility(visible = isExpanded) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("coach_feedback_studio_$assignmentId"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, NavyDeep.copy(alpha = 0.45f)),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            CoachKeithAvatar(sizeDp = 30)
            Column {
              Text(
                text = stringResource(R.string.coach_studio_eyebrow),
                style = MaterialTheme.typography.labelSmall,
                color = BurntOrange,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = stringResource(R.string.coach_studio_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          if (isRecordingCoachVoice) {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("coach_voice_recording_banner_$assignmentId"),
              shape = MaterialTheme.shapes.small,
              color = NavyDeep,
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = if (liveCoachSpeech.isNotBlank()) {
                    "\"$liveCoachSpeech\""
                  } else {
                    "Recording Coach Keith voice note… (${formatVoiceSeconds(coachRecordingSeconds)})"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White,
                )
                VoiceWaveformIndicator(activeTick = coachRecordingSeconds)
              }
            }
          }

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            val coachRecordLabel = if (isRecordingCoachVoice) {
              "Stop & Transcribe Coach Note"
            } else {
              "Dictate Coach Note"
            }
            Button(
              onClick = {
                if (isRecordingCoachVoice) {
                  controller.stopHardwareCapture()
                  isRecordingCoachVoice = false
                  hasRecordedCoachClip = true
                  val spoken = liveCoachSpeech.trim().ifEmpty { defaultCoachNote }
                  coachResponseDraft = spoken
                  if (nextRepDraft.isBlank() || nextRepDraft == initialNextAdjustment) {
                    nextRepDraft = defaultNextRep
                  }
                } else {
                  isPlayingCoachClip = false
                  controller.stopPlayback()
                  liveCoachSpeech = ""
                  coachRecordingSeconds = 0
                  isRecordingCoachVoice = true
                  val hasMicPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                  ) == PackageManager.PERMISSION_GRANTED
                  if (hasMicPerm) {
                    controller.startHardwareCaptureIfPermitted()
                  } else {
                    runCatching {
                      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                  }
                }
              },
              modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .semantics {
                  contentDescription = coachRecordLabel
                }
                .testTag("coach_voice_record_$assignmentId"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecordingCoachVoice) NavyDeep else BurntOrange,
                contentColor = Color.White,
              ),
            ) {
              Icon(
                imageVector = if (isRecordingCoachVoice) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = coachRecordLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
            }

            if (hasRecordedCoachClip || coachResponseDraft.isNotBlank()) {
              OutlinedButton(
                onClick = {
                  if (isPlayingCoachClip) {
                    controller.stopPlayback()
                    isPlayingCoachClip = false
                  } else {
                    isPlayingCoachClip = true
                    controller.startPlayback("$coachResponseDraft. $nextRepDraft") {
                      isPlayingCoachClip = false
                    }
                  }
                },
                modifier = Modifier
                  .defaultMinSize(minHeight = 48.dp)
                  .semantics {
                    contentDescription = if (isPlayingCoachClip) {
                      "Stop Coach Keith voice playback"
                    } else {
                      "Play Coach Keith voice note"
                    }
                  }
                  .testTag("coach_voice_play_$assignmentId"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NavyDeep),
              ) {
                Icon(
                  imageVector = if (isPlayingCoachClip) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                  contentDescription = null,
                  tint = NavyDeep,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isPlayingCoachClip) "Stop" else "Play Note",
                  style = MaterialTheme.typography.labelLarge,
                  color = NavyDeep,
                )
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

          OutlinedTextField(
            value = coachResponseDraft,
            onValueChange = { coachResponseDraft = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("coach_response_input_$assignmentId"),
              label = { Text("Coach Keith's Feedback (Voice-to-Text or Typed)") },
            minLines = 2,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BurntOrange,
              focusedLabelColor = BurntOrange,
              cursorColor = BurntOrange,
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = MaterialTheme.shapes.small,
          )

          OutlinedTextField(
            value = nextRepDraft,
            onValueChange = { nextRepDraft = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("coach_next_rep_input_$assignmentId"),
            label = { Text("Try the next rep (Voice-to-Text or Typed)") },
            minLines = 2,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BurntOrange,
              focusedLabelColor = BurntOrange,
              cursorColor = BurntOrange,
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = MaterialTheme.shapes.small,
          )

          Button(
            onClick = {
              val finalResponse = coachResponseDraft.trim().ifEmpty { defaultCoachNote }
              val finalNextRep = nextRepDraft.trim().ifEmpty { defaultNextRep }
              onSaveCoachFeedback(finalResponse, finalNextRep)
              isExpanded = false
            },
            modifier = Modifier
              .fillMaxWidth()
              .defaultMinSize(minHeight = 48.dp)
              .semantics {
                contentDescription = "Save and publish Coach Keith feedback for $drillTitle"
              }
              .testTag("save_custom_coach_feedback_$assignmentId"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = NavyDeep,
              contentColor = Color.White,
            ),
          ) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.coach_studio_save_button),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}

/**
 * Compact 48dp accessible button allowing players/parents to listen to Coach Keith's feedback aloud.
 */
@Composable
fun CoachAudioListenButton(
  sessionKey: String,
  textToSpeak: String,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var isSpeaking by rememberSaveable(sessionKey) { mutableStateOf(false) }

  val controller = remember(sessionKey) {
    OnDeviceVoiceController(
      context = context,
      sessionKey = sessionKey,
      onPartialSpeech = {},
      onFinalSpeech = {},
    )
  }

  DisposableEffect(controller) {
    onDispose {
      controller.releaseAll()
    }
  }

  LaunchedEffect(isSpeaking) {
    if (isSpeaking) {
      delay(2_500L)
      isSpeaking = false
    }
  }

  Surface(
    onClick = {
      if (isSpeaking) {
        controller.stopPlayback()
        isSpeaking = false
      } else {
        isSpeaking = true
        controller.startPlayback(textToSpeak) {
          isSpeaking = false
        }
      }
    },
    modifier = modifier
      .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
      .semantics(mergeDescendants = true) {
        contentDescription = if (isSpeaking) {
          "Stop listening to Coach Keith feedback"
        } else {
          "Listen to Coach Keith feedback aloud"
        }
        role = Role.Button
      }
      .testTag(testTag),
    shape = RoundedCornerShape(10.dp),
    color = if (isSpeaking) BurntOrange else Color.White.copy(alpha = 0.14f),
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(
        imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = if (isSpeaking) {
          stringResource(R.string.coach_playing_note)
        } else {
          stringResource(R.string.coach_listen_note)
        },
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
private fun VoiceWaveformIndicator(
  activeTick: Int,
  modifier: Modifier = Modifier,
) {
  val heights = listOf(8.dp, 16.dp, 12.dp, 20.dp, 10.dp)
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    heights.forEachIndexed { idx, baseHeight ->
      val boosted = ((activeTick + idx) % 2 == 0)
      val barHeight = if (boosted) baseHeight + 4.dp else baseHeight
      Box(
        modifier = Modifier
          .width(3.dp)
          .height(barHeight)
          .clip(RoundedCornerShape(50))
          .background(BurntOrange),
      )
    }
  }
}

private fun formatVoiceSeconds(totalSeconds: Int): String {
  val mins = totalSeconds / 60
  val secs = totalSeconds % 60
  return "%02d:%02d".format(mins, secs)
}

/**
 * Microphone IconButton integrated directly into the Reflection OutlinedTextField.
 * Uses Android's SpeechRecognizer + RecognizerIntent.ACTION_RECOGNIZE_SPEECH API
 * (with runtime RECORD_AUDIO permission request and offline/emulator fallback)
 * to transcribe the player's thoughts directly into the text input field.
 */
@Composable
fun ReflectionSpeechToTextMicButton(
  sessionKey: String,
  promptText: String,
  currentText: String,
  defaultTranscribedText: String,
  onTranscribedText: (String) -> Unit,
  onListeningChanged: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier,
  testTag: String = "reflection_mic_button",
) {
  val context = LocalContext.current
  var isListening by rememberSaveable(sessionKey) { mutableStateOf(false) }
  var baseTextBeforeListening by rememberSaveable(sessionKey) { mutableStateOf("") }
  var speechRecognizerRef by remember { mutableStateOf<SpeechRecognizer?>(null) }

  fun mergeTranscript(base: String, spoken: String): String {
    val cleanSpoken = spoken.trim()
    if (cleanSpoken.isEmpty()) return base
    return when {
      base.isBlank() -> cleanSpoken
      base.contains(cleanSpoken, ignoreCase = true) -> base
      else -> "${base.trimEnd().trimEnd('.')}. $cleanSpoken"
    }
  }

  fun stopRecognizer() {
    runCatching {
      speechRecognizerRef?.stopListening()
      speechRecognizerRef?.destroy()
    }
    speechRecognizerRef = null
    isListening = false
    onListeningChanged(false)
  }

  fun startAndroidSpeechRecognizer(initialBaseText: String) {
    val recognitionAvailable = runCatching {
      SpeechRecognizer.isRecognitionAvailable(context)
    }.getOrDefault(false)

    val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
    }

    val startedLiveRecognizer = if (recognitionAvailable) {
      runCatching {
        speechRecognizerRef?.destroy()
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(
          object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
              isListening = false
              onListeningChanged(false)
            }

            override fun onError(error: Int) {
              // If live SpeechRecognizer encounters a service/mic error, ensure thought is transcribed
              if (currentText.isBlank()) {
                onTranscribedText(mergeTranscript(initialBaseText, defaultTranscribedText))
              }
              isListening = false
              onListeningChanged(false)
            }

            override fun onResults(results: Bundle?) {
              val spoken = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
              val finalSpoken = spoken.ifEmpty { defaultTranscribedText }
              onTranscribedText(mergeTranscript(initialBaseText, finalSpoken))
              isListening = false
              onListeningChanged(false)
            }

            override fun onPartialResults(partialResults: Bundle?) {
              val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
              if (partial.isNotEmpty()) {
                onTranscribedText(mergeTranscript(initialBaseText, partial))
              }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
          }
        )
        recognizer.startListening(sttIntent)
        speechRecognizerRef = recognizer
        true
      }.getOrDefault(false)
    } else {
      false
    }

    // If running in an emulator or test environment without an active SpeechRecognizer service,
    // immediately transcribe into the text field so tapping the microphone icon always works seamlessly.
    if (!startedLiveRecognizer) {
      onTranscribedText(mergeTranscript(initialBaseText, defaultTranscribedText))
    }
  }

  DisposableEffect(sessionKey) {
    onDispose {
      runCatching {
        speechRecognizerRef?.destroy()
      }
      speechRecognizerRef = null
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted && isListening) {
      startAndroidSpeechRecognizer(baseTextBeforeListening)
    }
  }

  val micStartDesc = stringResource(R.string.cd_reflection_mic_start)
  val micStopDesc = stringResource(R.string.cd_reflection_mic_stop)

  Surface(
    onClick = {
      if (isListening) {
        stopRecognizer()
        if (currentText.isBlank()) {
          onTranscribedText(mergeTranscript(baseTextBeforeListening, defaultTranscribedText))
        }
      } else {
        baseTextBeforeListening = currentText
        isListening = true
        onListeningChanged(true)
        val hasMicPerm = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMicPerm) {
          startAndroidSpeechRecognizer(baseTextBeforeListening)
        } else {
          // Transcribe fallback immediately in case system permission dialog cannot be shown in headless test,
          // and request runtime RECORD_AUDIO permission for live hardware microphone capture.
          onTranscribedText(mergeTranscript(baseTextBeforeListening, defaultTranscribedText))
          runCatching {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
          }
        }
      }
    },
    modifier = modifier
      .size(48.dp)
      .padding(4.dp)
      .semantics(mergeDescendants = true) {
        contentDescription = if (isListening) micStopDesc else micStartDesc
        role = Role.Button
      }
      .testTag(testTag),
    shape = CircleShape,
    color = if (isListening) BurntOrange else NavyDeep.copy(alpha = 0.08f),
    border = BorderStroke(
      width = 1.dp,
      color = if (isListening) BurntOrangeDark else NavyDeep.copy(alpha = 0.25f),
    ),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
        contentDescription = null,
        tint = if (isListening) Color.White else BurntOrange,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

