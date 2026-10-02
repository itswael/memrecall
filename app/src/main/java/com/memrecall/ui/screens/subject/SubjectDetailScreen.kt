package com.memrecall.ui.screens.subject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.memrecall.domain.model.CardType
import com.memrecall.domain.model.FlashCard
import com.memrecall.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    navController: NavController,
    viewModel: SubjectDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddCardMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.subject?.name ?: "Subject") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        state.subject?.let {
                            navController.navigate(Screen.EditSubject.createRoute(it.id))
                        }
                    }) {
                        Icon(Icons.Outlined.Edit, "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                ExtendedFloatingActionButton(
                    onClick = { showAddCardMenu = true },
                    icon = { Icon(Icons.Filled.Add, null) },
                    text = { Text("Add Card") },
                )
                DropdownMenu(expanded = showAddCardMenu, onDismissRequest = { showAddCardMenu = false }) {
                    CardType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName()) },
                            leadingIcon = { Icon(type.icon(), null) },
                            onClick = {
                                showAddCardMenu = false
                                state.subject?.let { s ->
                                    navController.navigate(Screen.AddCard.createRoute(s.id, type.name))
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.subject?.let { subject ->
                item {
                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatCard("Total", subject.totalCards.toString(), Modifier.weight(1f))
                        StatCard("Mastered", subject.masteredCards.toString(), Modifier.weight(1f))
                        StatCard("Streak", "${subject.currentStreak}d", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    // Study button
                    Button(
                        onClick = {
                            navController.navigate(
                                Screen.StudySession.createRoute(subject.id.toString(), "REVISION")
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Start Revision Session")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Cards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            if (state.cards.isEmpty() && !state.isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Outlined.Style, null, modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))
                        Text("No cards yet — tap + to add your first card",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }

            items(state.cards, key = { it.id }) { card ->
                CardListItem(
                    card = card,
                    onEdit = { navController.navigate(Screen.EditCard.createRoute(card.id)) },
                    onDelete = { viewModel.deleteCard(card) },
                    onPin = { viewModel.togglePin(card) },
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CardListItem(
    card: FlashCard,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (card.isPinned) Text("📌", style = MaterialTheme.typography.labelSmall)
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            card.cardType.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    DifficultyBadge(card.difficulty.name)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    card.cardPreview(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                )
                if (card.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        card.tags.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, null)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (card.isPinned) "Unpin" else "Pin") },
                        leadingIcon = { Icon(if (card.isPinned) Icons.Outlined.PushPin else Icons.Outlined.PushPin, null) },
                        onClick = { showMenu = false; onPin() }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: String) {
    val color = when (difficulty) {
        "EASY" -> com.memrecall.ui.theme.GreenSuccess
        "HARD" -> com.memrecall.ui.theme.RedError
        else -> com.memrecall.ui.theme.OrangeWarning
    }
    Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.15f)) {
        Text(
            difficulty,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

private fun FlashCard.cardPreview(): String = when (val c = content) {
    is com.memrecall.domain.model.CardContent.GeneralCard -> c.front
    is com.memrecall.domain.model.CardContent.TheoryCard -> c.concept
    is com.memrecall.domain.model.CardContent.DsaCard -> c.problemTitle.ifBlank { c.problemStatement.take(100) }
    is com.memrecall.domain.model.CardContent.SystemDesignCard -> "Design: ${c.systemName}"
}

private fun CardType.displayName() = when (this) {
    CardType.GENERAL -> "General Card"
    CardType.THEORY -> "Theory / Concept"
    CardType.DSA -> "DSA Problem"
    CardType.SYSTEM_DESIGN -> "System Design"
}

private fun CardType.icon() = when (this) {
    CardType.GENERAL -> Icons.Outlined.Style
    CardType.THEORY -> Icons.Outlined.MenuBook
    CardType.DSA -> Icons.Outlined.Code
    CardType.SYSTEM_DESIGN -> Icons.Outlined.AccountTree
}
