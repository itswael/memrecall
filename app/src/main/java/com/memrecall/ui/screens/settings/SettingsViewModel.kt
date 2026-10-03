package com.memrecall.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.memrecall.domain.model.*
import com.memrecall.domain.repository.FlashCardRepository
import com.memrecall.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val count: Int, val subjectName: String) : ImportState()
    data class Error(val message: String) : ImportState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val flashCardRepository: FlashCardRepository,
    private val subjectRepository: SubjectRepository,
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    fun importFromJson(uri: Uri, context: Context) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val json = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }
                    ?: throw IllegalStateException("Could not read file")

                val bundle = Gson().fromJson(json, ImportBundle::class.java)
                    ?: throw IllegalStateException("Invalid JSON format")

                if (bundle.cards.isNullOrEmpty()) {
                    throw IllegalStateException("No cards found in file")
                }

                val subjectName = bundle.subject?.takeIf { it.isNotBlank() } ?: "Imported Cards"
                val subjectId = subjectRepository.createSubject(
                    Subject(
                        name = subjectName,
                        description = bundle.description ?: "",
                        colorHex = "#6366F1",
                        iconName = "code",
                    )
                )

                val cards = bundle.cards.mapNotNull { it.toFlashCard(subjectId) }
                flashCardRepository.importCards(cards)
                subjectRepository.refreshCounts(subjectId)

                _importState.value = ImportState.Success(cards.size, subjectName)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Import failed")
            }
        }
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }
}

// ── JSON data classes ──────────────────────────────────────────────────────────

private data class ImportBundle(
    val version: String? = null,
    val subject: String? = null,
    val description: String? = null,
    val totalCards: Int? = null,
    val cards: List<ImportCard>? = null,
)

private data class ImportCard(
    val leetcodeNumber: Int? = null,
    val cardType: String? = null,
    val difficulty: String? = null,
    val category: String? = null,
    val content: ImportContent? = null,
) {
    fun toFlashCard(subjectId: Long): FlashCard? {
        val content = content ?: return null
        val type = when (cardType?.uppercase()) {
            "DSA" -> CardType.DSA
            "THEORY" -> CardType.THEORY
            "SYSTEM_DESIGN" -> CardType.SYSTEM_DESIGN
            else -> CardType.GENERAL
        }
        val diff = when (difficulty?.uppercase()) {
            "EASY" -> Difficulty.EASY
            "HARD" -> Difficulty.HARD
            else -> Difficulty.MEDIUM
        }
        val cardContent = when (type) {
            CardType.DSA -> CardContent.DsaCard(
                problemTitle = content.problemTitle ?: "",
                problemStatement = content.problemStatement ?: "",
                constraints = content.constraints ?: "",
                naiveApproach = content.naiveApproach ?: "",
                naiveComplexity = Complexity(
                    time = content.naiveComplexity?.time ?: "",
                    space = content.naiveComplexity?.space ?: "",
                ),
                optimizedApproach = content.optimizedApproach ?: "",
                optimizedComplexity = Complexity(
                    time = content.optimizedComplexity?.time ?: "",
                    space = content.optimizedComplexity?.space ?: "",
                ),
                keyInsight = content.keyInsight ?: "",
                pseudocode = content.pseudocode ?: "",
                followUpQuestions = content.followUpQuestions ?: emptyList(),
                similarProblems = content.similarProblems ?: emptyList(),
                leetcodeLink = content.leetcodeLink ?: "",
            )
            else -> CardContent.GeneralCard(
                front = content.front ?: content.problemTitle ?: "",
                back = content.back ?: content.explanation ?: "",
            )
        }
        val tags = buildList {
            category?.let { add(it) }
            leetcodeNumber?.let { add("lc-$it") }
        }
        return FlashCard(
            subjectId = subjectId,
            cardType = type,
            content = cardContent,
            difficulty = diff,
            tags = tags,
        )
    }
}

private data class ImportContent(
    // DSA fields
    val problemTitle: String? = null,
    val problemStatement: String? = null,
    val constraints: String? = null,
    val naiveApproach: String? = null,
    val naiveComplexity: ImportComplexity? = null,
    val optimizedApproach: String? = null,
    val optimizedComplexity: ImportComplexity? = null,
    val keyInsight: String? = null,
    val pseudocode: String? = null,
    val followUpQuestions: List<String>? = null,
    val similarProblems: List<String>? = null,
    val leetcodeLink: String? = null,
    // General/Theory fields
    val front: String? = null,
    val back: String? = null,
    val explanation: String? = null,
)

private data class ImportComplexity(
    val time: String? = null,
    val space: String? = null,
)
