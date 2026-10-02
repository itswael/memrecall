package com.memrecall.ui.screens.study

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.memrecall.domain.model.CardContent
import com.memrecall.domain.model.CardType
import com.memrecall.domain.model.FlashCard
import com.memrecall.ui.navigation.Screen
import com.memrecall.ui.theme.GreenSuccess
import com.memrecall.ui.theme.RedError
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionScreen(
    navController: NavController,
    viewModel: StudyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isComplete) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.SessionResult.createRoute(state.sessionId)) {
                popUpTo(Screen.Home.route)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).padding(end = 16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit")
                    }
                },
                actions = {
                    Text(
                        "${state.currentIndex + 1}/${state.cards.size}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Score row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ScoreChip(label = "✓ ${state.correctCount}", color = GreenSuccess)
                ScoreChip(label = "✗ ${state.incorrectCount}", color = RedError)
                ScoreChip(label = "→ ${state.skippedCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(8.dp))

            state.currentCard?.let { card ->
                FlipCard(
                    card = card,
                    isFlipped = state.isFlipped,
                    onFlip = viewModel::flipCard,
                    onSwipeRight = { viewModel.rateCard(5) },
                    onSwipeLeft = { viewModel.rateCard(1) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = state.isFlipped,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            ) {
                RatingRow(onRate = viewModel::rateCard)
            }

            if (!state.isFlipped) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    OutlinedButton(onClick = viewModel::skipCard) {
                        Icon(Icons.Outlined.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Skip")
                    }
                    Button(onClick = viewModel::flipCard) {
                        Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reveal Answer")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (state.isFlipped) "Swipe right if you knew it, left if you didn't"
                else "Tap the card to flip",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FlipCard(
    card: FlashCard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = dragOffset, label = "drag")

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "flip",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragOffset > 150 -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwipeRight()
                            }
                            dragOffset < -150 -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwipeLeft()
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, delta -> dragOffset += delta }
                )
            }
            .graphicsLayer { translationX = animatedOffset },
        contentAlignment = Alignment.Center,
    ) {
        // Swipe hint overlay
        if (abs(animatedOffset) > 30) {
            val isRight = animatedOffset > 0
            Surface(
                modifier = Modifier
                    .align(if (isRight) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(16.dp),
                color = (if (isRight) GreenSuccess else RedError).copy(alpha = (abs(animatedOffset) / 200f).coerceIn(0f, 0.8f)),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    if (isRight) "✓ Got it!" else "✗ Missed",
                    modifier = Modifier.padding(12.dp, 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Card(
            onClick = onFlip,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { rotationY = rotation },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (rotation <= 90f) {
                    CardFrontContent(card)
                } else {
                    Box(Modifier.graphicsLayer { rotationY = 180f }) {
                        CardBackContent(card)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardFrontContent(card: FlashCard) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                card.cardType.name,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = when (val c = card.content) {
                is CardContent.GeneralCard -> c.front
                is CardContent.TheoryCard -> c.concept
                is CardContent.DsaCard -> c.problemTitle.ifBlank { c.problemStatement.take(200) }
                is CardContent.SystemDesignCard -> "Design: ${c.systemName}"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        if (card.tags.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            TagRow(card.tags)
        }
    }
}

@Composable
private fun CardBackContent(card: FlashCard) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (val c = card.content) {
            is CardContent.GeneralCard -> {
                Text(c.back, style = MaterialTheme.typography.bodyLarge)
                if (c.hint.isNotBlank()) {
                    HintBox(c.hint)
                }
            }
            is CardContent.TheoryCard -> {
                Text(c.explanation, style = MaterialTheme.typography.bodyLarge)
                if (c.keyPoints.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Key Points", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    c.keyPoints.forEach { point ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("•", color = MaterialTheme.colorScheme.primary)
                            Text(point, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (c.mnemonic.isNotBlank()) HintBox(c.mnemonic)
            }
            is CardContent.DsaCard -> DsaCardBack(c)
            is CardContent.SystemDesignCard -> SystemDesignCardBack(c)
        }
    }
}

@Composable
private fun DsaCardBack(c: CardContent.DsaCard) {
    if (c.naiveApproach.isNotBlank()) {
        SectionCard(
            title = "Naive Approach",
            content = c.naiveApproach,
            complexity = "${c.naiveComplexity.time} time · ${c.naiveComplexity.space} space",
            color = MaterialTheme.colorScheme.errorContainer,
        )
    }
    if (c.optimizedApproach.isNotBlank()) {
        SectionCard(
            title = "Optimized Approach",
            content = c.optimizedApproach,
            complexity = "${c.optimizedComplexity.time} time · ${c.optimizedComplexity.space} space",
            color = MaterialTheme.colorScheme.primaryContainer,
        )
    }
    if (c.keyInsight.isNotBlank()) {
        HintBox(c.keyInsight)
    }
    if (c.followUpQuestions.isNotEmpty()) {
        Text("Follow-ups", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        c.followUpQuestions.forEach { q ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("→", color = MaterialTheme.colorScheme.secondary)
                Text(q, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SystemDesignCardBack(c: CardContent.SystemDesignCard) {
    if (c.highLevelDesign.isNotBlank()) {
        Text("High-Level Design", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(c.highLevelDesign, style = MaterialTheme.typography.bodyMedium)
    }
    if (c.tradeoffs.isNotBlank()) {
        Spacer(Modifier.height(4.dp))
        Text("Trade-offs", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(c.tradeoffs, style = MaterialTheme.typography.bodyMedium)
    }
    if (c.scalingNotes.isNotBlank()) {
        HintBox(c.scalingNotes)
    }
}

@Composable
private fun SectionCard(title: String, content: String, complexity: String, color: Color) {
    Surface(shape = MaterialTheme.shapes.medium, color = color) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(content, style = MaterialTheme.typography.bodyMedium)
            if (complexity.isNotBlank() && complexity != " time ·  space") {
                Text(complexity, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HintBox(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Lightbulb, contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TagRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.take(4).forEach { tag ->
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun RatingRow(onRate: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("How well did you know it?", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RatingButton("Again", RedError, modifier = Modifier.weight(1f)) { onRate(1) }
            RatingButton("Hard", com.memrecall.ui.theme.OrangeWarning, modifier = Modifier.weight(1f)) { onRate(3) }
            RatingButton("Good", GreenSuccess.copy(alpha = 0.7f), modifier = Modifier.weight(1f)) { onRate(4) }
            RatingButton("Easy", GreenSuccess, modifier = Modifier.weight(1f)) { onRate(5) }
        }
    }
}

@Composable
private fun RatingButton(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ScoreChip(label: String, color: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.12f)) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium, color = color)
    }
}
