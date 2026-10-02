package com.memrecall.domain.model

import com.google.gson.annotations.SerializedName

sealed class CardContent {

    data class GeneralCard(
        val front: String = "",
        val back: String = "",
        val hint: String = "",
    ) : CardContent()

    data class TheoryCard(
        val concept: String = "",
        val explanation: String = "",
        val keyPoints: List<String> = emptyList(),
        val mnemonic: String = "",
        val example: String = "",
    ) : CardContent()

    data class DsaCard(
        val problemTitle: String = "",
        val problemStatement: String = "",
        val constraints: String = "",
        val naiveApproach: String = "",
        val naiveComplexity: Complexity = Complexity(),
        val optimizedApproach: String = "",
        val optimizedComplexity: Complexity = Complexity(),
        val keyInsight: String = "",
        val pseudocode: String = "",
        val followUpQuestions: List<String> = emptyList(),
        val similarProblems: List<String> = emptyList(),
        val leetcodeLink: String = "",
    ) : CardContent()

    data class SystemDesignCard(
        val systemName: String = "",
        val requirements: String = "",
        val highLevelDesign: String = "",
        val components: List<DesignComponent> = emptyList(),
        val tradeoffs: String = "",
        val scalingNotes: String = "",
        val bottlenecks: String = "",
        val realWorldExamples: List<String> = emptyList(),
    ) : CardContent()
}

data class Complexity(
    val time: String = "",
    val space: String = "",
)

data class DesignComponent(
    val name: String = "",
    val role: String = "",
    val technology: String = "",
)
