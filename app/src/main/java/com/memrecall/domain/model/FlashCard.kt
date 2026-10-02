package com.memrecall.domain.model

enum class CardType { GENERAL, THEORY, DSA, SYSTEM_DESIGN }
enum class Difficulty { EASY, MEDIUM, HARD }
enum class StudyOrderMode { SEQUENTIAL, RANDOM, SMART, DUE_FIRST }
enum class NotificationMode { FIXED, RANDOM, SMART }

data class FlashCard(
    val id: Long = 0,
    val subjectId: Long,
    val cardType: CardType = CardType.GENERAL,
    val content: CardContent,
    val tags: List<String> = emptyList(),
    val companyTags: List<String> = emptyList(),
    // SM-2 fields
    val easinessFactor: Float = 2.5f,
    val interval: Int = 1,
    val repetitions: Int = 0,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val lastReviewAt: Long = 0,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val recallRate: Float get() = if (totalReviews == 0) 0f else correctReviews.toFloat() / totalReviews
    val isDue: Boolean get() = nextReviewAt <= System.currentTimeMillis()
}

data class Subject(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#6366F1",
    val iconName: String = "school",
    val totalCards: Int = 0,
    val masteredCards: Int = 0,
    val notificationEnabled: Boolean = false,
    val notificationTimeStart: String = "09:00",
    val notificationTimeEnd: String = "21:00",
    val notificationMode: NotificationMode = NotificationMode.SMART,
    val notificationIntervalMinutes: Int = 120,
    val studyOrderMode: StudyOrderMode = StudyOrderMode.SMART,
    val createdAt: Long = System.currentTimeMillis(),
    val lastStudiedAt: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
) {
    val progressPercent: Int get() = if (totalCards == 0) 0 else (masteredCards * 100) / totalCards
}

data class StudySession(
    val id: Long = 0,
    val subjectId: Long = -1,
    val subjectName: String = "Mixed",
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long = 0,
    val totalCards: Int = 0,
    val correctCards: Int = 0,
    val skippedCards: Int = 0,
    val durationSeconds: Int = 0,
    val sessionMode: SessionMode = SessionMode.REVISION,
)

enum class SessionMode { REVISION, LEARNING, QUICK }
