package com.memrecall.ui.screens.subject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memrecall.domain.model.FlashCard
import com.memrecall.domain.model.Subject
import com.memrecall.domain.repository.FlashCardRepository
import com.memrecall.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectDetailState(
    val subject: Subject? = null,
    val cards: List<FlashCard> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    private val subjectRepo: SubjectRepository,
    private val cardRepo: FlashCardRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val subjectId: Long = savedStateHandle["subjectId"] ?: -1L

    private val _state = MutableStateFlow(SubjectDetailState())
    val state: StateFlow<SubjectDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            subjectRepo.getSubjectById(subjectId)?.let { subject ->
                _state.update { it.copy(subject = subject) }
            }
        }
        cardRepo.getCardsBySubject(subjectId)
            .onEach { cards ->
                _state.update { it.copy(cards = cards.sortedByDescending { c -> c.isPinned }, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun deleteCard(card: FlashCard) {
        viewModelScope.launch {
            cardRepo.deleteCard(card)
            subjectId.let { id -> subjectRepo.refreshCounts(id) }
        }
    }

    fun togglePin(card: FlashCard) {
        viewModelScope.launch {
            cardRepo.setPinned(card.id, !card.isPinned)
        }
    }
}
