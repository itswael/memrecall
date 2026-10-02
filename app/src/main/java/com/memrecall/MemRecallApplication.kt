package com.memrecall

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MemRecallApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val studyChannel = NotificationChannel(
                CHANNEL_STUDY_REMINDER,
                "Study Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to review your flashcards"
                enableVibration(true)
            }

            val streakChannel = NotificationChannel(
                CHANNEL_STREAK,
                "Streak Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about your study streak"
            }

            manager.createNotificationChannels(listOf(studyChannel, streakChannel))
        }
    }

    companion object {
        const val CHANNEL_STUDY_REMINDER = "study_reminder"
        const val CHANNEL_STREAK = "streak_alert"
    }
}
