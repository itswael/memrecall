package com.memrecall.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.memrecall.data.local.AppDatabase
import com.memrecall.data.mapper.toDomain
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StudyAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationManager: StudyNotificationManager
    @Inject lateinit var db: AppDatabase

    override fun onReceive(context: Context, intent: Intent) {
        val subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, -1L)
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: "Study"

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAll()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val dueCount = db.flashCardDao().getDueCardCount()
            if (subjectId != -1L) {
                notificationManager.showStudyReminder(subjectId, subjectName, dueCount)
                val subject = db.subjectDao().getSubjectById(subjectId)
                subject?.let { notificationManager.scheduleForSubject(it.toDomain()) }
            }
        }
    }

    private fun rescheduleAll() {
        CoroutineScope(Dispatchers.IO).launch {
            db.subjectDao().getSubjectsWithNotifications().forEach { entity ->
                notificationManager.scheduleForSubject(entity.toDomain())
            }
        }
    }

    companion object {
        const val EXTRA_SUBJECT_ID = "subject_id"
        const val EXTRA_SUBJECT_NAME = "subject_name"
    }
}
