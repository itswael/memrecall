package com.memrecall.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.memrecall.MemRecallApplication
import com.memrecall.MainActivity
import com.memrecall.R
import com.memrecall.domain.model.Subject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class StudyNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun scheduleForSubject(subject: Subject) {
        cancelForSubject(subject.id)
        if (!subject.notificationEnabled) return

        val (startH, startM) = subject.notificationTimeStart.split(":").map { it.toInt() }
        val (endH, endM) = subject.notificationTimeEnd.split(":").map { it.toInt() }

        val triggerTime = when (subject.notificationMode) {
            com.memrecall.domain.model.NotificationMode.FIXED -> nextAlarmFixed(startH, startM)
            com.memrecall.domain.model.NotificationMode.RANDOM ->
                nextAlarmRandom(startH, startM, endH, endM)
            com.memrecall.domain.model.NotificationMode.SMART ->
                nextAlarmSmart(startH, startM, endH, endM, subject.notificationIntervalMinutes)
        }

        val intent = Intent(context, StudyAlarmReceiver::class.java).apply {
            putExtra(StudyAlarmReceiver.EXTRA_SUBJECT_ID, subject.id)
            putExtra(StudyAlarmReceiver.EXTRA_SUBJECT_NAME, subject.name)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            subject.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pending)
    }

    fun cancelForSubject(subjectId: Long) {
        val intent = Intent(context, StudyAlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, subjectId.toInt(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pending?.let { alarmManager.cancel(it) }
    }

    fun showStudyReminder(subjectId: Long, subjectName: String, dueCount: Int) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_subject", subjectId)
        }
        val tapPending = PendingIntent.getActivity(
            context, subjectId.toInt(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MemRecallApplication.CHANNEL_STUDY_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to study $subjectName")
            .setContentText("$dueCount card${if (dueCount != 1) "s" else ""} due for review")
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(subjectId.toInt(), notification)
    }

    private fun nextAlarmFixed(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun nextAlarmRandom(startH: Int, startM: Int, endH: Int, endM: Int): Long {
        val startMs = (startH * 60 + startM) * 60_000L
        val endMs = (endH * 60 + endM) * 60_000L
        val todayBase = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        val randomOffset = Random.nextLong(startMs, endMs)
        val candidate = todayBase + randomOffset
        return if (candidate > System.currentTimeMillis()) candidate
        else todayBase + 86_400_000L + randomOffset // tomorrow
    }

    private fun nextAlarmSmart(startH: Int, startM: Int, endH: Int, endM: Int, intervalMin: Int): Long {
        val now = System.currentTimeMillis()
        val next = now + intervalMin * 60_000L
        // Clamp to study window
        val cal = Calendar.getInstance().apply { timeInMillis = next }
        val h = cal.get(Calendar.HOUR_OF_DAY)
        return if (h in startH..endH) next else nextAlarmFixed(startH, startM)
    }
}
