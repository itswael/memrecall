package com.memrecall.domain.repository

import com.memrecall.domain.model.FlashCard
import com.memrecall.domain.model.StudySession
import kotlinx.coroutines.flow.Flow

interface FlashCardRepository {
    fun getCardsBySubject(subjectId: Long): Flow<List<FlashCard>>
    fun getDueCards(subjectId: Long): Flow<List<FlashCard>>
    fun getAllDueCards(): Flow<List<FlashCard>>
    suspend fun getCardsForSession(subjectIds: List<Long>): List<FlashCard>
    suspend fun getRandomCards(subjectId: Long, limit: Int): List<FlashCard>
    suspend fun getRandomCardsAll(limit: Int): List<FlashCard>
    suspend fun getCardById(id: Long): FlashCard?
    suspend fun createCard(card: FlashCard): Long
    suspend fun importCards(cards: List<FlashCard>)
    suspend fun updateCard(card: FlashCard)
    suspend fun deleteCard(card: FlashCard)
    suspend fun archiveCard(id: Long)
    suspend fun setPinned(id: Long, pinned: Boolean)
    suspend fun getDueCardCount(): Int
    fun searchByTag(tag: String): Flow<List<FlashCard>>
    suspend fun getTagsForSubject(subjectId: Long): List<String>

    // Sessions
    suspend fun saveSession(session: StudySession): Long
    suspend fun updateSession(session: StudySession)
    fun getRecentSessions(limit: Int = 20): Flow<List<StudySession>>
    fun getSessionsForSubject(subjectId: Long): Flow<List<StudySession>>
    suspend fun getTotalStudyMinutes(since: Long): Int
}
