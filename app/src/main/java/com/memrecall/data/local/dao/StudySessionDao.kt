package com.memrecall.data.local.dao

import androidx.room.*
import com.memrecall.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Insert
    suspend fun insertSession(session: StudySessionEntity): Long

    @Update
    suspend fun updateSession(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 20): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE subjectId = :subjectId ORDER BY startedAt DESC LIMIT :limit")
    fun getSessionsForSubject(subjectId: Long, limit: Int = 10): Flow<List<StudySessionEntity>>

    @Query("SELECT SUM(durationSeconds) FROM study_sessions WHERE startedAt >= :since")
    suspend fun getTotalStudyTimeSince(since: Long): Int?

    @Query("SELECT COUNT(DISTINCT DATE(startedAt/1000, 'unixepoch')) FROM study_sessions WHERE startedAt >= :since")
    suspend fun getActiveDaysSince(since: Long): Int

    // Returns one row per calendar day with total cards reviewed
    @Query("""
        SELECT DATE(startedAt/1000, 'unixepoch') as day, SUM(totalCards) as cards
        FROM study_sessions
        WHERE startedAt >= :since
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getDailyActivity(since: Long): List<DayActivity>
}

data class DayActivity(val day: String, val cards: Int)
