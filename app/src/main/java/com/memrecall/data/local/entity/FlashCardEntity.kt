package com.memrecall.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class FlashCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,

    // GENERAL, THEORY, DSA, SYSTEM_DESIGN, CUSTOM
    val cardType: String = "GENERAL",

    // Core content — stored as JSON for flexibility per card type
    val contentJson: String = "{}",

    // Tags for filtering
    val tags: String = "", // comma-separated

    // Spaced repetition fields (SM-2)
    val easinessFactor: Float = 2.5f,
    val interval: Int = 1,       // days until next review
    val repetitions: Int = 0,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val lastReviewAt: Long = 0,

    // Recall tracking
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,

    // Difficulty: EASY, MEDIUM, HARD (auto-computed)
    val difficulty: String = "MEDIUM",

    // Priority boost (user can pin important cards)
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,

    // Company tags for DSA (comma-separated)
    val companyTags: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
