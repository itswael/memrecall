package com.memrecall.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.memrecall.data.local.dao.FlashCardDao
import com.memrecall.data.local.dao.StudySessionDao
import com.memrecall.data.local.dao.SubjectDao
import com.memrecall.data.local.entity.FlashCardEntity
import com.memrecall.data.local.entity.StudySessionEntity
import com.memrecall.data.local.entity.SubjectEntity

@Database(
    entities = [SubjectEntity::class, FlashCardEntity::class, StudySessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun flashCardDao(): FlashCardDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        const val DATABASE_NAME = "memrecall.db"
    }
}
