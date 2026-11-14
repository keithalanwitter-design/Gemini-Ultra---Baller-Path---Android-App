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
import androidx.compose.material.icons.outlined.Info
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
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeDark
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.SuccessGreen
import java.io.File
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Checks whether strictly on-device speech recognition is available on this device (Android 12+).
 * Never falls back to a cloud-backed SpeechRecognizer.
 */
private fun isOnDeviceSpeechAvailable(context: Context): Boolean = runCatching {
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
    SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
}.getOrDefault(false)

/**
 * Creates a strictly on-device SpeechRecognizer when supported.
 * Never calls SpeechRecognizer.createSpeechRecognizer(context).
 */
private fun createStrictOnDeviceRecognizer(context: Context): SpeechRecognizer? {
  if (!isOnDeviceSpeechAvailable(context)) return null
  return runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
    } else {
      null
    }
  }.getOrNull()
}

/**
 * Manages optional local voice note recording (MediaRecorder ONLY) and local playback
 * (MediaPlayer / TextToSpeech). Never runs MediaRecorder and SpeechRecognizer at the same time.
 */
private class OnDeviceAudioRecorderController(
  private val context: Context,
  private val sessionKey: String,
) {
  private var mediaRecorder: MediaRecorder? = null
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

  /**
   * Starts hardware MediaRecorder ONLY if RECORD_AUDIO permission is granted.
   * Returns true only if MediaRecorder actually started.
   */
  fun startMediaRecordingIfPermitted(): Boolean {
    val hasMicPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasMicPermission) return false

    stopMediaRecorder()
    return runCatching {
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
      true
    }.getOrElse {
      stopMediaRecorder()
      false
    }
  }

  fun stopMediaRecorder() {
    runCatching {
      mediaRecorder?.apply {
        stop()
        release()
      }
    }
    mediaRecorder = null
  }

  fun startPlayback(textToReadFallback: String, onFinished: () -> Unit): Boolean {
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
      if (started) return true
    }

    if (textToReadFallback.isNotBlank()) {
      speakTextAloud(textToReadFallback)
      return true
    }
    return false
  }

  private fun speakTextAloud(text: String) {
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
    stopMediaRecorder()
    stopPlayback()
    runCatching {
      textToSpeech?.shutdown()
    }
    textToSpeech = null
  }
}

/**
 * In-app pre-permission explanation shown BEFORE launching the system RECORD_AUDIO dialog.
 * Explains that the microphone is optional, only for the player's reflection or a voice note,
 * and recordings stay on this device.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MicPermissionRationaleCard(
  onContinueToSystemDialog: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
  cardTestTag: String = "mic_rationale_card",
  continueButtonTestTag: String = "mic_rationale_continue_button",
  cancelButtonTestTag: String = "mic_rationale_cancel_button",
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .semantics(mergeDescendants = true) {}
      .testTag(cardTestTag),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.secondaryContainer,
    border = BorderStroke(1.dp, BurntOrange),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = null,
          tint = BurntOrange,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = stringResource(R.string.mic_rationale_title),
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          fontWeight = FontWeight.Bold,
        )
      }
      Text(
        text = stringResource(R.string.mic_rationale_body),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
      )
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = onContinueToSystemDialog,
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(continueButtonTestTag),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BurntOrange,
            contentColor = Color.White,
          ),
        ) {
          Text(
            text = stringResource(R.string.mic_rationale_continue),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }
        OutlinedButton(
          onClick = onCancel,
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(cancelButtonTestTag),
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, NavyDeep),
        ) {
          Text(
            text = stringResource(R.string.mic_rationale_cancel),
            style = MaterialTheme.typography.labelLarge,
            color = NavyDeep,
          )
        }
      }
    }
  }
}

/**
 * Player Optional On-Device Voice Note Recorder Card used in PracticeFlowScreen (Reflection stage).
 * Uses ONLY MediaRecorder (never runs SpeechRecognizer simultaneously and never inserts canned starter text).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceRecorderStudioCard(
  sessionKey: String,
  modifier: Modifier = Modifier,
  cardTestTag: String = "player_voice_recorder_card",
  recordButtonTestTag: String = "player_voice_record_button",
  playButtonTestTag: String = "player_voice_play_button",
  clearButtonTestTag: String = "player_voice_clear_button",
  statusBadgeTestTag: String = "player_voice_status_badge",
) {
  val context = LocalContext.current
  val permissionDeniedText = stringResource(R.string.mic_permission_denied_message)
  val recorderUnavailableText = "Microphone recording isn't available on this device. Please type your reflection below."

  var isRecording by rememberSaveable(sessionKey) { mutableStateOf(false) }
  var recordingSeconds by rememberSaveable(sessionKey) { mutableIntStateOf(0) }
  var hasVoiceClip by rememberSaveable(sessionKey) { mutableStateOf(false) }
  var isPlayingBack by rememberSaveable(sessionKey) { mutableStateOf(false) }
  var showMicRationale by rememberSaveable(sessionKey) { mutableStateOf(false) }
  var statusMessage by rememberSaveable(sessionKey) { mutableStateOf<String?>(null) }

  val controller = remember(sessionKey) {
    OnDeviceAudioRecorderController(
      context = context,
      sessionKey = sessionKey,
    )
  }

  DisposableEffect(controller) {
    onDispose {
      controller.releaseAll()
    }
  }

  fun beginHardwareVoiceNoteRecording() {
    isPlayingBack = false
    controller.stopPlayback()
    recordingSeconds = 0
    val started = controller.startMediaRecordingIfPermitted()
    if (started) {
      isRecording = true
      statusMessage = null
    } else {
      isRecording = false
      statusMessage = recorderUnavailableText
    }
  }

  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    showMicRationale = false
    if (granted) {
      beginHardwareVoiceNoteRecording()
    } else {
      isRecording = false
      statusMessage = permissionDeniedText
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

  fun onRecordButtonClicked() {
    if (isRecording) {
      controller.stopMediaRecorder()
      isRecording = false
      hasVoiceClip = controller.hasRecordedFile()
      return
    }

    val hasMicPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED
    if (hasMicPermission) {
      beginHardwareVoiceNoteRecording()
    } else {
      isRecording = false
      showMicRationale = true
    }
  }

  val statusLabel = when {
    isRecording -> "Recording ${formatVoiceSeconds(recordingSeconds)}"
    isPlayingBack -> stringResource(R.string.coach_playing_note)
    hasVoiceClip -> "Audio Saved on Device"
    else -> "On-Device Audio"
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

      if (showMicRationale) {
        MicPermissionRationaleCard(
          onContinueToSystemDialog = {
            showMicRationale = false
            runCatching {
              micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }.onFailure {
              isRecording = false
              statusMessage = permissionDeniedText
            }
          },
          onCancel = {
            showMicRationale = false
            isRecording = false
          },
          cardTestTag = "${cardTestTag}_mic_rationale",
          continueButtonTestTag = "${cardTestTag}_mic_rationale_continue",
          cancelButtonTestTag = "${cardTestTag}_mic_rationale_cancel",
        )
      }

      if (statusMessage != null) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("${cardTestTag}_status_message"),
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
          Text(
            text = statusMessage.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }

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
                text = "Recording on-device voice note… (${formatVoiceSeconds(recordingSeconds)})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
              )
            }
            VoiceWaveformIndicator(activeTick = recordingSeconds)
          }
        }
      }

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
          onClick = ::onRecordButtonClicked,
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

        if (hasVoiceClip) {
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
                val started = controller.startPlayback(textToReadFallback = "") {
                  isPlayingBack = false
                }
                isPlayingBack = started
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

          OutlinedButton(
            onClick = {
              controller.deleteRecordedFile()
              hasVoiceClip = false
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
    }
  }
}

/**
 * Debug-only Collapsible Coach Keith Voice-to-Text & Custom Feedback Studio for Work + Feedback cards.
 * Uses strictly on-device speech recognition when available, never inserts canned Coach Keith paragraphs,
 * and never auto-fills empty fields on Save.
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
  val sttUnavailableMessage = stringResource(R.string.stt_unavailable_message)
  val sttFailedMessage = stringResource(R.string.stt_failed_message)
  val permissionDeniedMessage = stringResource(R.string.mic_permission_denied_message)

  var isExpanded by rememberSaveable(assignmentId) { mutableStateOf(false) }
  var coachResponseDraft by rememberSaveable(assignmentId, initialCoachResponse) {
    mutableStateOf(initialCoachResponse)
  }
  var nextRepDraft by rememberSaveable(assignmentId, initialNextAdjustment) {
    mutableStateOf(initialNextAdjustment)
  }
  var isDictatingCoachVoice by rememberSaveable(assignmentId) { mutableStateOf(false) }
  var coachDictationSeconds by rememberSaveable(assignmentId) { mutableIntStateOf(0) }
  var showMicRationale by rememberSaveable(assignmentId) { mutableStateOf(false) }
  var statusMessage by rememberSaveable(assignmentId) { mutableStateOf<String?>(null) }
  var validationError by rememberSaveable(assignmentId) { mutableStateOf<String?>(null) }
  var speechRecognizerRef by remember { mutableStateOf<SpeechRecognizer?>(null) }

  fun stopCoachRecognizer() {
    runCatching {
      speechRecognizerRef?.stopListening()
      speechRecognizerRef?.destroy()
    }
    speechRecognizerRef = null
    isDictatingCoachVoice = false
  }

  DisposableEffect(assignmentId) {
    onDispose {
      stopCoachRecognizer()
    }
  }

  fun startStrictOnDeviceCoachDictation() {
    if (!isOnDeviceSpeechAvailable(context)) {
      isDictatingCoachVoice = false
      statusMessage = sttUnavailableMessage
      return
    }
    val baseText = coachResponseDraft
    val started = runCatching {
      speechRecognizerRef?.destroy()
      val recognizer = createStrictOnDeviceRecognizer(context) ?: return@runCatching false
      recognizer.setRecognitionListener(
        object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) = Unit
          override fun onBeginningOfSpeech() = Unit
          override fun onRmsChanged(rmsdB: Float) = Unit
          override fun onBufferReceived(buffer: ByteArray?) = Unit
          override fun onEndOfSpeech() {
            isDictatingCoachVoice = false
          }

          override fun onError(error: Int) {
            isDictatingCoachVoice = false
            statusMessage = sttFailedMessage
          }

          override fun onResults(results: Bundle?) {
            val spoken = results
              ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
              ?.firstOrNull()
              ?.trim()
              .orEmpty()
            if (spoken.isNotEmpty()) {
              coachResponseDraft = if (baseText.isBlank()) {
                spoken
              } else {
                "${baseText.trimEnd().trimEnd('.')}. $spoken"
              }
              validationError = null
            } else {
              statusMessage = sttFailedMessage
            }
            isDictatingCoachVoice = false
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
              ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
              ?.firstOrNull()
              ?.trim()
              .orEmpty()
            if (partial.isNotEmpty()) {
              coachResponseDraft = if (baseText.isBlank()) {
                partial
              } else {
                "${baseText.trimEnd().trimEnd('.')}. $partial"
              }
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
      )
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      }
      recognizer.startListening(intent)
      speechRecognizerRef = recognizer
      true
    }.getOrDefault(false)

    if (started) {
      coachDictationSeconds = 0
      isDictatingCoachVoice = true
      statusMessage = null
    } else {
      isDictatingCoachVoice = false
      statusMessage = sttUnavailableMessage
    }
  }

  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    showMicRationale = false
    if (granted) {
      startStrictOnDeviceCoachDictation()
    } else {
      isDictatingCoachVoice = false
      statusMessage = permissionDeniedMessage
    }
  }

  LaunchedEffect(isDictatingCoachVoice) {
    if (isDictatingCoachVoice) {
      while (isDictatingCoachVoice) {
        delay(1_000L)
        coachDictationSeconds += 1
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
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            CoachKeithAvatar(sizeDp = 28)
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

          if (showMicRationale) {
            MicPermissionRationaleCard(
              onContinueToSystemDialog = {
                showMicRationale = false
                runCatching {
                  micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }.onFailure {
                  isDictatingCoachVoice = false
                  statusMessage = permissionDeniedMessage
                }
              },
              onCancel = {
                showMicRationale = false
                isDictatingCoachVoice = false
              },
              cardTestTag = "coach_mic_rationale_$assignmentId",
              continueButtonTestTag = "coach_mic_rationale_continue_$assignmentId",
              cancelButtonTestTag = "coach_mic_rationale_cancel_$assignmentId",
            )
          }

          if (statusMessage != null) {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("coach_voice_status_$assignmentId"),
              shape = MaterialTheme.shapes.small,
              color = MaterialTheme.colorScheme.surface,
            ) {
              Text(
                text = statusMessage.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              )
            }
          }

          if (isDictatingCoachVoice) {
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
                  .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "Listening on device… (${formatVoiceSeconds(coachDictationSeconds)})",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White,
                )
                VoiceWaveformIndicator(activeTick = coachDictationSeconds)
              }
            }
          }

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            val coachRecordLabel = if (isDictatingCoachVoice) {
              "Stop On-Device Dictation"
            } else {
              "Dictate Coach Note"
            }
            Button(
              onClick = {
                if (isDictatingCoachVoice) {
                  stopCoachRecognizer()
                } else {
                  val hasMicPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                  ) == PackageManager.PERMISSION_GRANTED
                  if (hasMicPerm) {
                    startStrictOnDeviceCoachDictation()
                  } else {
                    isDictatingCoachVoice = false
                    showMicRationale = true
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
                containerColor = if (isDictatingCoachVoice) NavyDeep else BurntOrange,
                contentColor = Color.White,
              ),
            ) {
              Icon(
                imageVector = if (isDictatingCoachVoice) Icons.Filled.Stop else Icons.Filled.Mic,
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
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

          OutlinedTextField(
            value = coachResponseDraft,
            onValueChange = {
              coachResponseDraft = it
              validationError = null
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("coach_response_input_$assignmentId"),
            label = { Text("Coach Keith's Feedback (Debug Only)") },
            minLines = 1,
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

          OutlinedTextField(
            value = nextRepDraft,
            onValueChange = {
              nextRepDraft = it
              validationError = null
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("coach_next_rep_input_$assignmentId"),
            label = { Text("Try the next rep (Debug Only)") },
            minLines = 1,
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BurntOrange,
              focusedLabelColor = BurntOrange,
              cursorColor = BurntOrange,
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = MaterialTheme.shapes.small,
          )

          if (validationError != null) {
            Text(
              text = validationError.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.testTag("coach_studio_validation_error_$assignmentId"),
            )
          }

          Button(
            onClick = {
              val finalResponse = coachResponseDraft.trim()
              val finalNextRep = nextRepDraft.trim()
              if (finalResponse.isEmpty() || finalNextRep.isEmpty()) {
                validationError = "Enter both Coach Keith's feedback and the Try the next rep note before saving."
              } else {
                validationError = null
                onSaveCoachFeedback(finalResponse, finalNextRep)
                isExpanded = false
              }
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
    OnDeviceAudioRecorderController(
      context = context,
      sessionKey = sessionKey,
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
 * Microphone IconButton integrated into the Reflection OutlinedTextField.
 * Uses strictly on-device SpeechRecognizer (createOnDeviceSpeechRecognizer) when available.
 * Never falls back to a cloud recognizer and never inserts canned starter text on failure/unavailability/denial.
 */
@Composable
fun ReflectionSpeechToTextMicButton(
  sessionKey: String,
  promptText: String,
  currentText: String,
  onTranscribedText: (String) -> Unit,
  onListeningChanged: (Boolean) -> Unit = {},
  onRequestRationaleChanged: (Boolean) -> Unit = {},
  onStatusMessageChanged: (String?) -> Unit = {},
  permissionRequestTrigger: Int = 0,
  modifier: Modifier = Modifier,
  testTag: String = "reflection_mic_button",
) {
  val context = LocalContext.current
  val sttUnavailableMessage = stringResource(R.string.stt_unavailable_message)
  val sttFailedMessage = stringResource(R.string.stt_failed_message)
  val permissionDeniedMessage = stringResource(R.string.mic_permission_denied_message)

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

  fun startOnDeviceSpeechRecognizer(initialBaseText: String) {
    if (!isOnDeviceSpeechAvailable(context)) {
      stopRecognizer()
      onStatusMessageChanged(sttUnavailableMessage)
      return
    }

    val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
      putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
    }

    val startedLiveRecognizer = runCatching {
      speechRecognizerRef?.destroy()
      val recognizer = createStrictOnDeviceRecognizer(context) ?: return@runCatching false
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
            isListening = false
            onListeningChanged(false)
            onStatusMessageChanged(sttFailedMessage)
          }

          override fun onResults(results: Bundle?) {
            val spoken = results
              ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
              ?.firstOrNull()
              ?.trim()
              .orEmpty()
            if (spoken.isNotEmpty()) {
              onTranscribedText(mergeTranscript(initialBaseText, spoken))
              onStatusMessageChanged(null)
            } else {
              onStatusMessageChanged(sttFailedMessage)
            }
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

    if (startedLiveRecognizer) {
      isListening = true
      onListeningChanged(true)
      onStatusMessageChanged(null)
    } else {
      stopRecognizer()
      onStatusMessageChanged(sttUnavailableMessage)
    }
  }

  DisposableEffect(sessionKey) {
    onDispose {
      stopRecognizer()
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    onRequestRationaleChanged(false)
    if (granted) {
      baseTextBeforeListening = currentText
      startOnDeviceSpeechRecognizer(baseTextBeforeListening)
    } else {
      stopRecognizer()
      onStatusMessageChanged(permissionDeniedMessage)
    }
  }

  LaunchedEffect(permissionRequestTrigger) {
    if (permissionRequestTrigger > 0) {
      runCatching {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
      }.onFailure {
        stopRecognizer()
        onStatusMessageChanged(permissionDeniedMessage)
      }
    }
  }

  val micStartDesc = stringResource(R.string.cd_reflection_mic_start)
  val micStopDesc = stringResource(R.string.cd_reflection_mic_stop)

  Surface(
    onClick = {
      if (isListening) {
        stopRecognizer()
      } else {
        val hasMicPerm = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMicPerm) {
          baseTextBeforeListening = currentText
          startOnDeviceSpeechRecognizer(baseTextBeforeListening)
        } else {
          stopRecognizer()
          onStatusMessageChanged(null)
          onRequestRationaleChanged(true)
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
