package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [ReflectionEntity::class, PracticeDraftStateEntity::class],
  version = 2,
  exportSchema = false,
)
abstract class BallerPathDatabase : RoomDatabase() {
  abstract fun reflectionDao(): ReflectionDao
  abstract fun practiceDraftDao(): PracticeDraftDao

  companion object {
    @Volatile
    private var instance: BallerPathDatabase? = null

    fun getInstance(context: Context): BallerPathDatabase {
      return instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          BallerPathDatabase::class.java,
          "baller_path_demo.db",
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()
          .also { instance = it }
      }
    }
  }
}
