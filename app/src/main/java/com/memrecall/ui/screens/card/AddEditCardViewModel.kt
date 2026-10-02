package com.memrecall.ui.screens.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memrecall.domain.model.*
import com.memrecall.domain.repository.FlashCardRepository
import com.memrecall.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditCardState(
    val subjectId: Long = 0,
    val cardType: CardType = CardType.GENERAL,
    // General
    val front: String = "",
    val back: String = "",
    val hint: String = "",
    // Theory
    val concept: String = "",
    val explanation: String = "",
    val keyPoints: String = "",
    val mnemonic: String = "",
    val example: String = "",
    // DSA
    val problemTitle: String = "",
    val problemStatement: String = "",
    val constraints: String = "",
    val naiveApproach: String = "",
    val naiveTimeComplexity: String = "",
    val naiveSpaceComplexity: String = "",
    val optimizedApproach: String = "",
    val optimizedTimeComplexity: String = "",
    val optimizedSpaceComplexity: String = "",
    val keyInsight: String = "",
    val pseudocode: String = "",
    val followUps: String = "",
    val similarProblems: String = "",
    val leetcodeLink: String = "",
    val companyTags: String = "",
    // System Design
    val systemName: String = "",
    val requirements: String = "",
    val highLevelDesign: String = "",
    val tradeoffs: String = "",
    val scalingNotes: String = "",
    val bottlenecks: String = "",
    // Shared
    val tags: String = "",
    val saved: Boolean = false,
    val isLoading: Boolean = false,
)

@HiltViewModel
class AddEditCardViewModel @Inject constructor(
    private val cardRepo: FlashCardRepository,
    private val subjectRepo: SubjectRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editingCardId: Long? = savedStateHandle.get<Long>("cardId")?.takeIf { it > 0 }
    private val subjectIdArg: Long = savedStateHandle.get<Long>("subjectId") ?: 0
    private val cardTypeArg: String = savedStateHandle.get<String>("cardType") ?: "GENERAL"

    private val _state = MutableStateFlow(
        AddEditCardState(
            subjectId = subjectIdArg,
            cardType = runCatching { CardType.valueOf(cardTypeArg) }.getOrElse { CardType.GENERAL }
        )
    )
    val state: StateFlow<AddEditCardState> = _state.asStateFlow()

    init {
        editingCardId?.let { id ->
            viewModelScope.launch {
                cardRepo.getCardById(id)?.let { card ->
                    _state.update { s ->
                        when (val c = card.content) {
                            is CardContent.GeneralCard -> s.copy(
                                subjectId = card.subjectId, cardType = card.cardType,
                                front = c.front, back = c.back, hint = c.hint,
                                tags = card.tags.joinToString(", "),
                            )
                            is CardContent.TheoryCard -> s.copy(
                                subjectId = card.subjectId, cardType = card.cardType,
                                concept = c.concept, explanation = c.explanation,
                                keyPoints = c.keyPoints.joinToString("\n"),
                                mnemonic = c.mnemonic, example = c.example,
                                tags = card.tags.joinToString(", "),
                            )
                            is CardContent.DsaCard -> s.copy(
                                subjectId = card.subjectId, cardType = card.cardType,
                                problemTitle = c.problemTitle, problemStatement = c.problemStatement,
                                constraints = c.constraints,
                                naiveApproach = c.naiveApproach,
                                naiveTimeComplexity = c.naiveComplexity.time,
                                naiveSpaceComplexity = c.naiveComplexity.space,
                                optimizedApproach = c.optimizedApproach,
                                optimizedTimeComplexity = c.optimizedComplexity.time,
                                optimizedSpaceComplexity = c.optimizedComplexity.space,
                                keyInsight = c.keyInsight, pseudocode = c.pseudocode,
                                followUps = c.followUpQuestions.joinToString("\n"),
                                similarProblems = c.similarProblems.joinToString("\n"),
                                leetcodeLink = c.leetcodeLink,
                                tags = card.tags.joinToString(", "),
                                companyTags = card.companyTags.joinToString(", "),
                            )
                            is CardContent.SystemDesignCard -> s.copy(
                                subjectId = card.subjectId, cardType = card.cardType,
                                systemName = c.systemName, requirements = c.requirements,
                                highLevelDesign = c.highLevelDesign, tradeoffs = c.tradeoffs,
                                scalingNotes = c.scalingNotes, bottlenecks = c.bottlenecks,
                                tags = card.tags.joinToString(", "),
                            )
                        }
                    }
                }
            }
        }
    }

    fun update(block: AddEditCardState.() -> AddEditCardState) = _state.update { it.block() }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            val content: CardContent = when (s.cardType) {
                CardType.GENERAL -> CardContent.GeneralCard(s.front, s.back, s.hint)
                CardType.THEORY -> CardContent.TheoryCard(
                    s.concept, s.explanation,
                    s.keyPoints.lines().filter { it.isNotBlank() },
                    s.mnemonic, s.example,
                )
                CardType.DSA -> CardContent.DsaCard(
                    s.problemTitle, s.problemStatement, s.constraints,
                    s.naiveApproach, Complexity(s.naiveTimeComplexity, s.naiveSpaceComplexity),
                    s.optimizedApproach, Complexity(s.optimizedTimeComplexity, s.optimizedSpaceComplexity),
                    s.keyInsight, s.pseudocode,
                    s.followUps.lines().filter { it.isNotBlank() },
                    s.similarProblems.lines().filter { it.isNotBlank() },
                    s.leetcodeLink,
                )
                CardType.SYSTEM_DESIGN -> CardContent.SystemDesignCard(
                    s.systemName, s.requirements, s.highLevelDesign,
                    tradeoffs = s.tradeoffs, scalingNotes = s.scalingNotes, bottlenecks = s.bottlenecks,
                )
            }
            val tags = s.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val companyTags = s.companyTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val card = FlashCard(
                id = editingCardId ?: 0,
                subjectId = s.subjectId,
                cardType = s.cardType,
                content = content,
                tags = tags,
                companyTags = companyTags,
            )
            if (editingCardId != null) cardRepo.updateCard(card) else cardRepo.createCard(card)
            subjectRepo.refreshCounts(s.subjectId)
            _state.update { it.copy(saved = true) }
        }
    }
}
