package com.memrecall.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long = -1, // -1 = mixed session
    val subjectName: String = "Mixed",
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long = 0,
    val totalCards: Int = 0,
    val correctCards: Int = 0,
    val skippedCards: Int = 0,
    val durationSeconds: Int = 0,
    // REVISION, LEARNING, QUICK
    val sessionMode: String = "REVISION",
)
