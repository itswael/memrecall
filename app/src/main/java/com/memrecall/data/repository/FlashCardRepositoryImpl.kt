package com.memrecall.data.repository

import com.memrecall.data.local.dao.FlashCardDao
import com.memrecall.data.local.dao.StudySessionDao
import com.memrecall.data.mapper.toDomain
import com.memrecall.data.mapper.toEntity
import com.memrecall.domain.model.FlashCard
import com.memrecall.domain.model.StudySession
import com.memrecall.domain.repository.FlashCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FlashCardRepositoryImpl @Inject constructor(
    private val cardDao: FlashCardDao,
    private val sessionDao: StudySessionDao,
) : FlashCardRepository {

    override fun getCardsBySubject(subjectId: Long) =
        cardDao.getCardsBySubject(subjectId).map { it.map { e -> e.toDomain() } }

    override fun getDueCards(subjectId: Long) =
        cardDao.getDueCards(subjectId).map { it.map { e -> e.toDomain() } }

    override fun getAllDueCards() =
        cardDao.getAllDueCards().map { it.map { e -> e.toDomain() } }

    override suspend fun getCardsForSession(subjectIds: List<Long>): List<FlashCard> =
        cardDao.getCardsForSession(subjectIds).map { it.toDomain() }

    override suspend fun getRandomCards(subjectId: Long, limit: Int) =
        cardDao.getRandomCards(subjectId, limit).map { it.toDomain() }

    override suspend fun getRandomCardsAll(limit: Int) =
        cardDao.getRandomCardsAll(limit).map { it.toDomain() }

    override suspend fun getCardById(id: Long) = cardDao.getCardById(id)?.toDomain()

    override suspend fun createCard(card: FlashCard): Long = cardDao.insertCard(card.toEntity())

    override suspend fun importCards(cards: List<FlashCard>) =
        cardDao.insertCards(cards.map { it.toEntity() })

    override suspend fun updateCard(card: FlashCard) = cardDao.updateCard(card.toEntity())

    override suspend fun deleteCard(card: FlashCard) = cardDao.deleteCard(card.toEntity())

    override suspend fun archiveCard(id: Long) = cardDao.archiveCard(id)

    override suspend fun setPinned(id: Long, pinned: Boolean) = cardDao.setPinned(id, pinned)

    override suspend fun getDueCardCount() = cardDao.getDueCardCount()

    override fun searchByTag(tag: String) =
        cardDao.searchByTag(tag).map { it.map { e -> e.toDomain() } }

    override suspend fun getTagsForSubject(subjectId: Long) = cardDao.getTagsForSubject(subjectId)

    override suspend fun saveSession(session: StudySession) =
        sessionDao.insertSession(session.toEntity())

    override suspend fun updateSession(session: StudySession) =
        sessionDao.updateSession(session.toEntity())

    override fun getRecentSessions(limit: Int) =
        sessionDao.getRecentSessions(limit).map { it.map { e -> e.toDomain() } }

    override fun getSessionsForSubject(subjectId: Long) =
        sessionDao.getSessionsForSubject(subjectId).map { it.map { e -> e.toDomain() } }

    override suspend fun getTotalStudyMinutes(since: Long): Int =
        (sessionDao.getTotalStudyTimeSince(since) ?: 0) / 60
}
