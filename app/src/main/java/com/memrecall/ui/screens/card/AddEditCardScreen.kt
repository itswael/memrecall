package com.memrecall.ui.screens.card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    navController: NavController,
    cardId: Long?,
    viewModel: AddEditCardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cardId == null) "New ${state.cardType.name} Card" else "Edit Card") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (state.cardType) {
                com.memrecall.domain.model.CardType.GENERAL -> GeneralCardForm(state, viewModel)
                com.memrecall.domain.model.CardType.THEORY -> TheoryCardForm(state, viewModel)
                com.memrecall.domain.model.CardType.DSA -> DsaCardForm(state, viewModel)
                com.memrecall.domain.model.CardType.SYSTEM_DESIGN -> SystemDesignCardForm(state, viewModel)
            }

            HorizontalDivider()

            OutlinedTextField(
                value = state.tags,
                onValueChange = { viewModel.update { copy(tags = it) } },
                label = { Text("Tags (comma-separated)") },
                placeholder = { Text("e.g. arrays, sorting, binary-search") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun GeneralCardForm(state: AddEditCardState, vm: AddEditCardViewModel) {
    LabeledField("Front (Question)") {
        OutlinedTextField(
            value = state.front,
            onValueChange = { vm.update { copy(front = it) } },
            placeholder = { Text("Enter your question or concept") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
    }
    LabeledField("Back (Answer)") {
        OutlinedTextField(
            value = state.back,
            onValueChange = { vm.update { copy(back = it) } },
            placeholder = { Text("Enter the answer or explanation") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
    OutlinedTextField(
        value = state.hint,
        onValueChange = { vm.update { copy(hint = it) } },
        label = { Text("Hint (optional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun TheoryCardForm(state: AddEditCardState, vm: AddEditCardViewModel) {
    LabeledField("Concept / Term") {
        OutlinedTextField(
            value = state.concept,
            onValueChange = { vm.update { copy(concept = it) } },
            placeholder = { Text("e.g. CAP Theorem, Deadlock, Polymorphism") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
    LabeledField("Explanation") {
        OutlinedTextField(
            value = state.explanation,
            onValueChange = { vm.update { copy(explanation = it) } },
            placeholder = { Text("Detailed explanation...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
    }
    LabeledField("Key Points (one per line)") {
        OutlinedTextField(
            value = state.keyPoints,
            onValueChange = { vm.update { copy(keyPoints = it) } },
            placeholder = { Text("• Point 1\n• Point 2") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
    OutlinedTextField(
        value = state.mnemonic,
        onValueChange = { vm.update { copy(mnemonic = it) } },
        label = { Text("Mnemonic / Memory tip") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    OutlinedTextField(
        value = state.example,
        onValueChange = { vm.update { copy(example = it) } },
        label = { Text("Example") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
}

@Composable
private fun DsaCardForm(state: AddEditCardState, vm: AddEditCardViewModel) {
    OutlinedTextField(
        value = state.problemTitle,
        onValueChange = { vm.update { copy(problemTitle = it) } },
        label = { Text("Problem Title") },
        placeholder = { Text("e.g. Two Sum, LRU Cache") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    LabeledField("Problem Statement") {
        OutlinedTextField(
            value = state.problemStatement,
            onValueChange = { vm.update { copy(problemStatement = it) } },
            placeholder = { Text("Full problem description...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
    OutlinedTextField(
        value = state.constraints,
        onValueChange = { vm.update { copy(constraints = it) } },
        label = { Text("Constraints") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
    LabeledField("Naive Approach") {
        OutlinedTextField(
            value = state.naiveApproach,
            onValueChange = { vm.update { copy(naiveApproach = it) } },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.naiveTimeComplexity,
            onValueChange = { vm.update { copy(naiveTimeComplexity = it) } },
            label = { Text("Time (naive)") },
            placeholder = { Text("O(n²)") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.naiveSpaceComplexity,
            onValueChange = { vm.update { copy(naiveSpaceComplexity = it) } },
            label = { Text("Space (naive)") },
            placeholder = { Text("O(1)") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
    }
    LabeledField("Optimized Approach") {
        OutlinedTextField(
            value = state.optimizedApproach,
            onValueChange = { vm.update { copy(optimizedApproach = it) } },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.optimizedTimeComplexity,
            onValueChange = { vm.update { copy(optimizedTimeComplexity = it) } },
            label = { Text("Time (optimized)") },
            placeholder = { Text("O(n)") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.optimizedSpaceComplexity,
            onValueChange = { vm.update { copy(optimizedSpaceComplexity = it) } },
            label = { Text("Space (optimized)") },
            placeholder = { Text("O(n)") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
    }
    OutlinedTextField(
        value = state.keyInsight,
        onValueChange = { vm.update { copy(keyInsight = it) } },
        label = { Text("Key Insight / Trick") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
    OutlinedTextField(
        value = state.pseudocode,
        onValueChange = { vm.update { copy(pseudocode = it) } },
        label = { Text("Pseudocode (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
    )
    OutlinedTextField(
        value = state.followUps,
        onValueChange = { vm.update { copy(followUps = it) } },
        label = { Text("Follow-up Questions (one per line)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
    OutlinedTextField(
        value = state.similarProblems,
        onValueChange = { vm.update { copy(similarProblems = it) } },
        label = { Text("Similar Problems") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    OutlinedTextField(
        value = state.leetcodeLink,
        onValueChange = { vm.update { copy(leetcodeLink = it) } },
        label = { Text("LeetCode / Problem Link") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    OutlinedTextField(
        value = state.companyTags,
        onValueChange = { vm.update { copy(companyTags = it) } },
        label = { Text("Company Tags (Google, Meta, ...)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun SystemDesignCardForm(state: AddEditCardState, vm: AddEditCardViewModel) {
    OutlinedTextField(
        value = state.systemName,
        onValueChange = { vm.update { copy(systemName = it) } },
        label = { Text("System / Component Name") },
        placeholder = { Text("e.g. URL Shortener, Twitter Feed") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    LabeledField("Requirements") {
        OutlinedTextField(
            value = state.requirements,
            onValueChange = { vm.update { copy(requirements = it) } },
            placeholder = { Text("Functional + Non-functional requirements...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
    LabeledField("High-Level Design") {
        OutlinedTextField(
            value = state.highLevelDesign,
            onValueChange = { vm.update { copy(highLevelDesign = it) } },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
    }
    LabeledField("Trade-offs") {
        OutlinedTextField(
            value = state.tradeoffs,
            onValueChange = { vm.update { copy(tradeoffs = it) } },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
    }
    OutlinedTextField(
        value = state.scalingNotes,
        onValueChange = { vm.update { copy(scalingNotes = it) } },
        label = { Text("Scaling Notes") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
    OutlinedTextField(
        value = state.bottlenecks,
        onValueChange = { vm.update { copy(bottlenecks = it) } },
        label = { Text("Bottlenecks / Pain Points") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}
