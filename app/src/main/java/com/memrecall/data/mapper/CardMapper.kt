package com.memrecall.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memrecall.data.local.entity.FlashCardEntity
import com.memrecall.data.local.entity.StudySessionEntity
import com.memrecall.data.local.entity.SubjectEntity
import com.memrecall.domain.model.*

private val gson = Gson()

fun SubjectEntity.toDomain() = Subject(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex,
    iconName = iconName,
    totalCards = totalCards,
    masteredCards = masteredCards,
    notificationEnabled = notificationEnabled,
    notificationTimeStart = notificationTimeStart,
    notificationTimeEnd = notificationTimeEnd,
    notificationMode = NotificationMode.valueOf(notificationMode),
    notificationIntervalMinutes = notificationIntervalMinutes,
    studyOrderMode = StudyOrderMode.valueOf(studyOrderMode),
    createdAt = createdAt,
    lastStudiedAt = lastStudiedAt,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
)

fun Subject.toEntity() = SubjectEntity(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex,
    iconName = iconName,
    totalCards = totalCards,
    masteredCards = masteredCards,
    notificationEnabled = notificationEnabled,
    notificationTimeStart = notificationTimeStart,
    notificationTimeEnd = notificationTimeEnd,
    notificationMode = notificationMode.name,
    notificationIntervalMinutes = notificationIntervalMinutes,
    studyOrderMode = studyOrderMode.name,
    createdAt = createdAt,
    lastStudiedAt = lastStudiedAt,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
)

fun FlashCardEntity.toDomain(): FlashCard {
    val content = deserializeContent(cardType, contentJson)
    return FlashCard(
        id = id,
        subjectId = subjectId,
        cardType = CardType.valueOf(cardType),
        content = content,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
        companyTags = if (companyTags.isBlank()) emptyList() else companyTags.split(",").map { it.trim() },
        easinessFactor = easinessFactor,
        interval = interval,
        repetitions = repetitions,
        nextReviewAt = nextReviewAt,
        lastReviewAt = lastReviewAt,
        totalReviews = totalReviews,
        correctReviews = correctReviews,
        difficulty = Difficulty.valueOf(difficulty),
        isPinned = isPinned,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun FlashCard.toEntity() = FlashCardEntity(
    id = id,
    subjectId = subjectId,
    cardType = cardType.name,
    contentJson = serializeContent(content),
    tags = tags.joinToString(","),
    companyTags = companyTags.joinToString(","),
    easinessFactor = easinessFactor,
    interval = interval,
    repetitions = repetitions,
    nextReviewAt = nextReviewAt,
    lastReviewAt = lastReviewAt,
    totalReviews = totalReviews,
    correctReviews = correctReviews,
    difficulty = difficulty.name,
    isPinned = isPinned,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis(),
)

private fun serializeContent(content: CardContent): String = gson.toJson(content)

private fun deserializeContent(type: String, json: String): CardContent = when (CardType.valueOf(type)) {
    CardType.GENERAL -> gson.fromJson(json, CardContent.GeneralCard::class.java)
        ?: CardContent.GeneralCard()
    CardType.THEORY -> gson.fromJson(json, CardContent.TheoryCard::class.java)
        ?: CardContent.TheoryCard()
    CardType.DSA -> gson.fromJson(json, CardContent.DsaCard::class.java)
        ?: CardContent.DsaCard()
    CardType.SYSTEM_DESIGN -> gson.fromJson(json, CardContent.SystemDesignCard::class.java)
        ?: CardContent.SystemDesignCard()
}

fun StudySessionEntity.toDomain() = StudySession(
    id = id, subjectId = subjectId, subjectName = subjectName,
    startedAt = startedAt, endedAt = endedAt, totalCards = totalCards,
    correctCards = correctCards, skippedCards = skippedCards,
    durationSeconds = durationSeconds, sessionMode = SessionMode.valueOf(sessionMode),
)

fun StudySession.toEntity() = StudySessionEntity(
    id = id, subjectId = subjectId, subjectName = subjectName,
    startedAt = startedAt, endedAt = endedAt, totalCards = totalCards,
    correctCards = correctCards, skippedCards = skippedCards,
    durationSeconds = durationSeconds, sessionMode = sessionMode.name,
)
