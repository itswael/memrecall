package com.memrecall.data.local.dao

import androidx.room.*
import com.memrecall.data.local.entity.FlashCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashCardDao {

    @Query("SELECT * FROM flashcards WHERE subjectId = :subjectId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getCardsBySubject(subjectId: Long): Flow<List<FlashCardEntity>>

    @Query("SELECT * FROM flashcards WHERE subjectId = :subjectId AND isArchived = 0 AND nextReviewAt <= :now ORDER BY nextReviewAt ASC")
    fun getDueCards(subjectId: Long, now: Long = System.currentTimeMillis()): Flow<List<FlashCardEntity>>

    @Query("SELECT * FROM flashcards WHERE isArchived = 0 AND nextReviewAt <= :now ORDER BY nextReviewAt ASC")
    fun getAllDueCards(now: Long = System.currentTimeMillis()): Flow<List<FlashCardEntity>>

    @Query("SELECT * FROM flashcards WHERE subjectId = :subjectId AND isArchived = 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomCards(subjectId: Long, limit: Int): List<FlashCardEntity>

    @Query("SELECT * FROM flashcards WHERE isArchived = 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomCardsAll(limit: Int): List<FlashCardEntity>

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getCardById(id: Long): FlashCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashCardEntity>)

    @Update
    suspend fun updateCard(card: FlashCardEntity)

    @Delete
    suspend fun deleteCard(card: FlashCardEntity)

    @Query("DELETE FROM flashcards WHERE subjectId = :subjectId")
    suspend fun deleteAllCardsForSubject(subjectId: Long)

    @Query("UPDATE flashcards SET isArchived = 1 WHERE id = :id")
    suspend fun archiveCard(id: Long)

    @Query("UPDATE flashcards SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("SELECT COUNT(*) FROM flashcards WHERE subjectId = :subjectId AND isArchived = 0")
    suspend fun getCardCount(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE isArchived = 0 AND nextReviewAt <= :now")
    suspend fun getDueCardCount(now: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM flashcards WHERE subjectId IN (:subjectIds) AND isArchived = 0 ORDER BY isPinned DESC, nextReviewAt ASC")
    suspend fun getCardsForSession(subjectIds: List<Long>): List<FlashCardEntity>

    @Query("SELECT * FROM flashcards WHERE (tags LIKE '%' || :tag || '%' OR companyTags LIKE '%' || :tag || '%') AND isArchived = 0")
    fun searchByTag(tag: String): Flow<List<FlashCardEntity>>

    @Query("SELECT DISTINCT tags FROM flashcards WHERE subjectId = :subjectId AND tags != ''")
    suspend fun getTagsForSubject(subjectId: Long): List<String>

    @Query("SELECT DISTINCT companyTags FROM flashcards WHERE companyTags != ''")
    suspend fun getAllCompanyTags(): List<String>
}
