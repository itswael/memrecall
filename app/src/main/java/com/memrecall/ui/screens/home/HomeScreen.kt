package com.memrecall.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.memrecall.domain.model.Subject
import com.memrecall.ui.components.SubjectCard
import com.memrecall.ui.components.StatChip
import com.memrecall.ui.navigation.Screen
import com.memrecall.ui.theme.GreenSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MemRecall", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Your revision companion", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Stats.route) }) {
                        Icon(Icons.Outlined.BarChart, contentDescription = "Statistics")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddSubject.route) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New Subject") },
                containerColor = MaterialTheme.colorScheme.primary,
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Summary banner
            item {
                QuickStudyBanner(
                    dueCards = state.totalDueCards,
                    studyMinutes = state.totalStudyMinutesToday,
                    onStudyAll = {
                        val ids = state.subjects.joinToString(",") { it.id.toString() }
                        navController.navigate(Screen.StudySession.createRoute(ids, "REVISION"))
                    }
                )
            }

            item {
                Text(
                    "Subjects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (state.subjects.isEmpty()) {
                item { EmptySubjectsHint() }
            } else {
                items(state.subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        onClick = { navController.navigate(Screen.SubjectDetail.createRoute(subject.id)) },
                        onStudy = {
                            navController.navigate(Screen.StudySession.createRoute(subject.id.toString(), "REVISION"))
                        },
                        onEdit = { navController.navigate(Screen.EditSubject.createRoute(subject.id)) },
                        onDelete = { showDeleteDialog = subject },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    showDeleteDialog?.let { subject ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete ${subject.name}?") },
            text = { Text("All cards in this subject will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteSubject(subject); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun QuickStudyBanner(
    dueCards: Int,
    studyMinutes: Int,
    onStudyAll: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    if (dueCards > 0) "$dueCards cards due for review" else "All caught up!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip(icon = Icons.Outlined.Schedule, label = "${studyMinutes}min today")
                    if (dueCards > 0) {
                        StatChip(icon = Icons.Outlined.Notifications, label = "$dueCards due")
                    }
                }
                if (dueCards > 0) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onStudyAll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Study All Due")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySubjectsHint() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Outlined.LibraryBooks,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Text(
            "No subjects yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Tap + to create your first subject\nand start building flashcards",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
