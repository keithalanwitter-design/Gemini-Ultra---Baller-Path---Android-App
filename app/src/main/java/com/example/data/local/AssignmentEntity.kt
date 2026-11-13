package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CoachAssignment
import com.example.data.model.PracticeStep
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "assignments")
data class AssignmentEntity(
  @PrimaryKey val assignmentId: String,
  val sortOrder: Int,
  val drillId: String,
  val title: String,
  val dayTag: String,
  val focusHeadline: String,
  val totalMinutes: Int,
  val coachCue: String,
  val reflectionPrompt: String,
  val isGeneralWork: Boolean,
  val stepsJson: String,
  val starterCuesJson: String,
  val hasActiveSession: Boolean,
  val currentStepId: String,
  val isReflectionStage: Boolean,
  val remainingSeconds: Int,
  val hasStartedTimer: Boolean,
  val reflectionDraft: String,
  val updatedAtMillis: Long,
) {
  fun toDomain(): CoachAssignment {
    val steps = decodeSteps(stepsJson)
    return CoachAssignment(
      assignmentId = assignmentId,
      sortOrder = sortOrder,
      drillId = drillId,
      title = title,
      dayTag = dayTag,
      focusHeadline = focusHeadline,
      totalMinutes = totalMinutes,
      coachCue = coachCue,
      reflectionPrompt = reflectionPrompt,
      starterCues = decodeStrings(starterCuesJson),
      steps = steps,
      isGeneralWork = isGeneralWork,
      hasActiveSession = hasActiveSession,
      currentStepId = currentStepId,
      isReflectionStage = isReflectionStage,
      remainingSeconds = remainingSeconds,
      hasStartedTimer = hasStartedTimer,
      reflectionDraft = reflectionDraft,
      updatedAtMillis = updatedAtMillis,
    )
  }

  companion object {
    fun encodeSteps(steps: List<PracticeStep>): String {
      val array = JSONArray()
      steps.forEach { step ->
        array.put(
          JSONObject().apply {
            put("id", step.id)
            put("stepNumber", step.stepNumber)
            put("phaseLabel", step.phaseLabel)
            put("title", step.title)
            put("durationMinutes", step.durationMinutes)
            put("durationSeconds", step.durationSeconds)
            put("instruction", step.instruction)
            put("coachCue", step.coachCue)
            put("keyLookFor", step.keyLookFor)
          }
        )
      }
      return array.toString()
    }

    fun decodeSteps(json: String): List<PracticeStep> {
      if (json.isBlank()) return emptyList()
      val array = JSONArray(json)
      return buildList(array.length()) {
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          add(
            PracticeStep(
              id = obj.getString("id"),
              stepNumber = obj.getInt("stepNumber"),
              phaseLabel = obj.getString("phaseLabel"),
              title = obj.getString("title"),
              durationMinutes = obj.getInt("durationMinutes"),
              durationSeconds = obj.getInt("durationSeconds"),
              instruction = obj.getString("instruction"),
              coachCue = obj.getString("coachCue"),
              keyLookFor = obj.getString("keyLookFor"),
            )
          )
        }
      }
    }

    fun encodeStrings(items: List<String>): String {
      val array = JSONArray()
      items.forEach { array.put(it) }
      return array.toString()
    }

    fun decodeStrings(json: String): List<String> {
      if (json.isBlank()) return emptyList()
      val array = JSONArray(json)
      return buildList(array.length()) {
        for (i in 0 until array.length()) {
          add(array.getString(i))
        }
      }
    }
  }
}
