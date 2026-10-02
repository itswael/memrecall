package com.memrecall.ui.screens.study

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memrecall.domain.algorithm.SpacedRepetition
import com.memrecall.domain.model.FlashCard
import com.memrecall.domain.model.SessionMode
import com.memrecall.domain.model.StudySession
import com.memrecall.domain.repository.FlashCardRepository
import com.memrecall.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudyUiState(
    val cards: List<FlashCard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val sessionId: Long = -1,
    val sessionMode: SessionMode = SessionMode.REVISION,
    val isComplete: Boolean = false,
    val isLoading: Boolean = true,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skippedCount: Int = 0,
    val startTimeMs: Long = System.currentTimeMillis(),
) {
    val currentCard: FlashCard? get() = cards.getOrNull(currentIndex)
    val progress: Float get() = if (cards.isEmpty()) 0f else currentIndex.toFloat() / cards.size
}

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val cardRepo: FlashCardRepository,
    private val subjectRepo: SubjectRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val subjectIdsArg: String = savedStateHandle["subjectIds"] ?: ""
    private val modeArg: String = savedStateHandle["mode"] ?: "REVISION"

    private val _state = MutableStateFlow(StudyUiState())
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            val mode = runCatching { SessionMode.valueOf(modeArg) }.getOrElse { SessionMode.REVISION }
            val subjectIds = subjectIdsArg.split(",").mapNotNull { it.toLongOrNull() }

            val cards = when (mode) {
                SessionMode.LEARNING -> cardRepo.getCardsForSession(subjectIds)
                SessionMode.QUICK -> cardRepo.getRandomCardsAll(20)
                SessionMode.REVISION -> {
                    val all = cardRepo.getCardsForSession(subjectIds)
                    SpacedRepetition.buildStudyQueue(all)
                }
            }

            val session = StudySession(
                subjectId = if (subjectIds.size == 1) subjectIds.first() else -1L,
                subjectName = if (subjectIds.size == 1) {
                    subjectRepo.getSubjectById(subjectIds.first())?.name ?: "Study"
                } else "Mixed",
                sessionMode = mode,
                startedAt = System.currentTimeMillis(),
            )
            val sessionId = cardRepo.saveSession(session)

            _state.update {
                it.copy(
                    cards = cards,
                    sessionMode = mode,
                    sessionId = sessionId,
                    isLoading = false,
                    startTimeMs = System.currentTimeMillis(),
                )
            }
        }
    }

    fun flipCard() {
        _state.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun rateCard(quality: Int) {
        val s = _state.value
        val card = s.currentCard ?: return
        viewModelScope.launch {
            val updated = SpacedRepetition.processReview(card, quality)
            cardRepo.updateCard(updated)
        }
        val correct = quality >= 3
        _state.update {
            it.copy(
                correctCount = it.correctCount + if (correct) 1 else 0,
                incorrectCount = it.incorrectCount + if (!correct) 1 else 0,
                isFlipped = false,
            )
        }
        advance()
    }

    fun skipCard() {
        _state.update { it.copy(skippedCount = it.skippedCount + 1, isFlipped = false) }
        advance()
    }

    private fun advance() {
        val s = _state.value
        val nextIndex = s.currentIndex + 1
        if (nextIndex >= s.cards.size) {
            finishSession()
        } else {
            _state.update { it.copy(currentIndex = nextIndex) }
        }
    }

    private fun finishSession() {
        val s = _state.value
        val durationSec = ((System.currentTimeMillis() - s.startTimeMs) / 1000).toInt()
        viewModelScope.launch {
            val session = StudySession(
                id = s.sessionId,
                totalCards = s.cards.size,
                correctCards = s.correctCount,
                skippedCards = s.skippedCount,
                durationSeconds = durationSec,
                endedAt = System.currentTimeMillis(),
            )
            cardRepo.updateSession(session)
        }
        _state.update { it.copy(isComplete = true) }
    }
}
