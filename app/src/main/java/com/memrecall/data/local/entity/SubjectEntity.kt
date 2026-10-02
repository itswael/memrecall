package com.memrecall.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#6366F1",
    val iconName: String = "school",
    val totalCards: Int = 0,
    val masteredCards: Int = 0,
    val notificationEnabled: Boolean = false,
    // cron-style: e.g. "09:00", empty = disabled
    val notificationTimeStart: String = "09:00",
    val notificationTimeEnd: String = "21:00",
    // FIXED, RANDOM, SMART
    val notificationMode: String = "SMART",
    val notificationIntervalMinutes: Int = 120,
    val studyOrderMode: String = "SMART", // SEQUENTIAL, RANDOM, SMART, DUE_FIRST
    val createdAt: Long = System.currentTimeMillis(),
    val lastStudiedAt: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
)
