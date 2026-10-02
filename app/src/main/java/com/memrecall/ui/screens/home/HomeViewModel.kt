package com.memrecall.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memrecall.domain.model.Subject
import com.memrecall.domain.repository.FlashCardRepository
import com.memrecall.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class HomeUiState(
    val subjects: List<Subject> = emptyList(),
    val totalDueCards: Int = 0,
    val totalStudyMinutesToday: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val subjectRepo: SubjectRepository,
    private val cardRepo: FlashCardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSubjects()
        loadStats()
    }

    private fun observeSubjects() {
        subjectRepo.getAllSubjects()
            .onEach { subjects ->
                _uiState.update { it.copy(subjects = subjects, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadStats() {
        viewModelScope.launch {
            val dueCount = cardRepo.getDueCardCount()
            val todayStart = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
            val studyMin = cardRepo.getTotalStudyMinutes(todayStart)
            _uiState.update { it.copy(totalDueCards = dueCount, totalStudyMinutesToday = studyMin) }
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            subjectRepo.deleteSubject(subject)
        }
    }

    fun refreshStats() = loadStats()
}
