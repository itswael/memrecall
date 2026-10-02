package com.memrecall.domain.algorithm

import com.memrecall.domain.model.FlashCard
import java.util.concurrent.TimeUnit
import kotlin.math.max

// SM-2 spaced repetition algorithm
// Quality: 0-2 = forgot, 3-5 = remembered (3=hard, 4=good, 5=easy)
object SpacedRepetition {

    fun processReview(card: FlashCard, quality: Int): FlashCard {
        val q = quality.coerceIn(0, 5)
        val newRepetitions: Int
        val newInterval: Int
        val newEF: Float

        if (q < 3) {
            // Forgot — reset
            newRepetitions = 0
            newInterval = 1
            newEF = card.easinessFactor
        } else {
            newRepetitions = card.repetitions + 1
            newInterval = when (newRepetitions) {
                1 -> 1
                2 -> 6
                else -> (card.interval * card.easinessFactor).toInt()
            }
            newEF = (card.easinessFactor + 0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
                .coerceAtLeast(1.3f)
        }

        val nextReview = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(newInterval.toLong())
        val correct = q >= 3

        return card.copy(
            easinessFactor = newEF,
            interval = max(1, newInterval),
            repetitions = newRepetitions,
            nextReviewAt = nextReview,
            lastReviewAt = System.currentTimeMillis(),
            totalReviews = card.totalReviews + 1,
            correctReviews = card.correctReviews + if (correct) 1 else 0,
            difficulty = computeDifficulty(newEF, card.totalReviews + 1, card.correctReviews + if (correct) 1 else 0),
        )
    }

    private fun computeDifficulty(ef: Float, totalReviews: Int, correctReviews: Int) = when {
        ef >= 2.5f && totalReviews >= 3 && correctReviews.toFloat() / totalReviews >= 0.8f ->
            com.memrecall.domain.model.Difficulty.EASY
        ef < 1.7f || (totalReviews >= 3 && correctReviews.toFloat() / totalReviews < 0.5f) ->
            com.memrecall.domain.model.Difficulty.HARD
        else -> com.memrecall.domain.model.Difficulty.MEDIUM
    }

    // Build an ordered study queue from a list of cards
    fun buildStudyQueue(cards: List<FlashCard>, maxCards: Int = 30): List<FlashCard> {
        val now = System.currentTimeMillis()
        val due = cards.filter { it.nextReviewAt <= now }.sortedBy { it.nextReviewAt }
        val newCards = cards.filter { it.totalReviews == 0 }.shuffled()
        val upcoming = cards.filter { it.nextReviewAt > now && it.totalReviews > 0 }
            .sortedBy { it.nextReviewAt }
        return (due + newCards + upcoming).take(maxCards)
    }
}
