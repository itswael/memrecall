package com.memrecall.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memrecall.domain.model.StudySession
import com.memrecall.domain.model.Subject
import com.memrecall.domain.repository.FlashCardRepository
import com.memrecall.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class StatsState(
    val totalCards: Int = 0,
    val studyMinutesToday: Int = 0,
    val totalSessions: Int = 0,
    val bestStreak: Int = 0,
    val recentSessions: List<StudySession> = emptyList(),
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val subjectRepo: SubjectRepository,
    private val cardRepo: FlashCardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        cardRepo.getRecentSessions(20)
            .onEach { sessions ->
                val todayStart = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
                val todayMin = cardRepo.getTotalStudyMinutes(todayStart)
                _state.update { it.copy(recentSessions = sessions, totalSessions = sessions.size, studyMinutesToday = todayMin) }
            }
            .launchIn(viewModelScope)

        subjectRepo.getAllSubjects()
            .onEach { subjects ->
                val best = subjects.maxOfOrNull { it.longestStreak } ?: 0
                val total = subjects.sumOf { it.totalCards }
                _state.update { it.copy(bestStreak = best, totalCards = total) }
            }
            .launchIn(viewModelScope)
    }
}
