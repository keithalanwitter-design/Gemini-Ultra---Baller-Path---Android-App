package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.SetupStage
import com.example.data.sample.SampleDataSource

@Database(
  entities = [
    ReflectionEntity::class,
    PracticeDraftStateEntity::class,
    AssignmentEntity::class,
    SetupStateEntity::class,
  ],
  version = 3,
  exportSchema = false,
)
abstract class BallerPathDatabase : RoomDatabase() {

  abstract fun reflectionDao(): ReflectionDao
  abstract fun practiceDraftDao(): PracticeDraftDao
  abstract fun assignmentDao(): AssignmentDao
  abstract fun setupStateDao(): SetupStateDao

  companion object {
    @Volatile
    private var instance: BallerPathDatabase? = null

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Preserve existing reflections & coach feedback; tag them with assignmentId.
        db.execSQL(
          "ALTER TABLE practice_reflections ADD COLUMN assignmentId TEXT NOT NULL DEFAULT 'assign-pass-look-decide'"
        )
        db.execSQL(
          "UPDATE practice_reflections SET assignmentId = 'w2_d1_target_pass' WHERE drillId = 'w2_d1_target_pass'"
        )

        // 2. Create per-assignment table matching Room's exact schema.
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `assignments` (
            `assignmentId` TEXT NOT NULL,
            `sortOrder` INTEGER NOT NULL,
            `drillId` TEXT NOT NULL,
            `title` TEXT NOT NULL,
            `dayTag` TEXT NOT NULL,
            `focusHeadline` TEXT NOT NULL,
            `totalMinutes` INTEGER NOT NULL,
            `coachCue` TEXT NOT NULL,
            `reflectionPrompt` TEXT NOT NULL,
            `isGeneralWork` INTEGER NOT NULL,
            `stepsJson` TEXT NOT NULL,
            `starterCuesJson` TEXT NOT NULL,
            `hasActiveSession` INTEGER NOT NULL,
            `currentStepId` TEXT NOT NULL,
            `isReflectionStage` INTEGER NOT NULL,
            `remainingSeconds` INTEGER NOT NULL,
            `hasStartedTimer` INTEGER NOT NULL,
            `reflectionDraft` TEXT NOT NULL,
            `updatedAtMillis` INTEGER NOT NULL,
            PRIMARY KEY(`assignmentId`)
          )
          """.trimIndent()
        )

        // 3. Migrate any existing v2 draft/session from practice_workflow_state into Assignment 1.
        var migratedHasActive = false
        var migratedStepId = "pass"
        var migratedIsReflection = false
        var migratedRemaining = SampleDataSource.assignmentOne.steps.first().durationSeconds
        var migratedHasStarted = false
        var migratedDraft = ""
        var migratedUpdated = System.currentTimeMillis()

        db.query("SELECT * FROM practice_workflow_state WHERE id = 1").use { cursor ->
          if (cursor.moveToFirst()) {
            val activeIdx = cursor.getColumnIndex("hasActiveSession")
            val stepIdx = cursor.getColumnIndex("currentStepId")
            val reflIdx = cursor.getColumnIndex("isReflectionStage")
            val remIdx = cursor.getColumnIndex("remainingSeconds")
            val startedIdx = cursor.getColumnIndex("hasStartedTimer")
            val draftIdx = cursor.getColumnIndex("reflectionDraft")
            val updatedIdx = cursor.getColumnIndex("updatedAtMillis")

            if (activeIdx >= 0) migratedHasActive = cursor.getInt(activeIdx) != 0
            if (stepIdx >= 0) migratedStepId = cursor.getString(stepIdx)
            if (reflIdx >= 0) migratedIsReflection = cursor.getInt(reflIdx) != 0
            if (remIdx >= 0) migratedRemaining = cursor.getInt(remIdx)
            if (startedIdx >= 0) migratedHasStarted = cursor.getInt(startedIdx) != 0
            if (draftIdx >= 0) migratedDraft = cursor.getString(draftIdx)
            if (updatedIdx >= 0) migratedUpdated = cursor.getLong(updatedIdx)
          }
        }

        val now = System.currentTimeMillis()
        SampleDataSource.defaultAssignmentEntities(now).forEach { entity ->
          val cv = ContentValues().apply {
            put("assignmentId", entity.assignmentId)
            put("sortOrder", entity.sortOrder)
            put("drillId", entity.drillId)
            put("title", entity.title)
            put("dayTag", entity.dayTag)
            put("focusHeadline", entity.focusHeadline)
            put("totalMinutes", entity.totalMinutes)
            put("coachCue", entity.coachCue)
            put("reflectionPrompt", entity.reflectionPrompt)
            put("isGeneralWork", if (entity.isGeneralWork) 1 else 0)
            put("stepsJson", entity.stepsJson)
            put("starterCuesJson", entity.starterCuesJson)
            if (entity.assignmentId == SampleDataSource.ASSIGNMENT_PASS_LOOK_DECIDE) {
              put("hasActiveSession", if (migratedHasActive) 1 else 0)
              put("currentStepId", migratedStepId)
              put("isReflectionStage", if (migratedIsReflection) 1 else 0)
              put("remainingSeconds", migratedRemaining)
              put("hasStartedTimer", if (migratedHasStarted) 1 else 0)
              put("reflectionDraft", migratedDraft)
              put("updatedAtMillis", migratedUpdated)
            } else {
              put("hasActiveSession", if (entity.hasActiveSession) 1 else 0)
              put("currentStepId", entity.currentStepId)
              put("isReflectionStage", if (entity.isReflectionStage) 1 else 0)
              put("remainingSeconds", entity.remainingSeconds)
              put("hasStartedTimer", if (entity.hasStartedTimer) 1 else 0)
              put("reflectionDraft", entity.reflectionDraft)
              put("updatedAtMillis", entity.updatedAtMillis)
            }
          }
          db.insert("assignments", SQLiteDatabase.CONFLICT_IGNORE, cv)
        }

        // 4. Create setup_state table and seed default WORK_READY.
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `setup_state` (
            `id` INTEGER NOT NULL,
            `setupStage` TEXT NOT NULL,
            `hasGeneralWorkPublished` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
          """.trimIndent()
        )
        val setupCv = ContentValues().apply {
          put("id", 1)
          put("setupStage", SetupStage.WORK_READY.id)
          put("hasGeneralWorkPublished", 1)
        }
        db.insert("setup_state", SQLiteDatabase.CONFLICT_IGNORE, setupCv)
      }
    }

    fun getInstance(context: Context): BallerPathDatabase {
      return instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          BallerPathDatabase::class.java,
          "baller_path_demo.db",
        )
          .addMigrations(MIGRATION_2_3)
          .build()
          .also { instance = it }
      }
    }
  }
}
