package com.memrecall.data.local.dao

import androidx.room.*
import com.memrecall.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("UPDATE subjects SET totalCards = (SELECT COUNT(*) FROM flashcards WHERE subjectId = :subjectId AND isArchived = 0) WHERE id = :subjectId")
    suspend fun refreshCardCount(subjectId: Long)

    @Query("UPDATE subjects SET masteredCards = (SELECT COUNT(*) FROM flashcards WHERE subjectId = :subjectId AND difficulty = 'EASY' AND repetitions >= 3) WHERE id = :subjectId")
    suspend fun refreshMasteredCount(subjectId: Long)

    @Query("UPDATE subjects SET lastStudiedAt = :time, currentStreak = :streak, longestStreak = MAX(longestStreak, :streak) WHERE id = :id")
    suspend fun updateStudyStats(id: Long, time: Long, streak: Int)

    @Query("SELECT * FROM subjects WHERE notificationEnabled = 1")
    suspend fun getSubjectsWithNotifications(): List<SubjectEntity>
}
