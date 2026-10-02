package com.memrecall.ui.screens.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

val SUBJECT_COLORS = listOf(
    "#6366F1", "#8B5CF6", "#EC4899", "#EF4444",
    "#F59E0B", "#10B981", "#06B6D4", "#3B82F6",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubjectScreen(
    navController: NavController,
    subjectId: Long?,
    viewModel: AddEditSubjectViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (subjectId == null) "New Subject" else "Edit Subject") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::save,
                        enabled = state.name.isNotBlank(),
                    ) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Subject Name") },
                placeholder = { Text("e.g. DSA, System Design, OS") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )

            Text("Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SUBJECT_COLORS) { hex ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
                        .getOrElse { MaterialTheme.colorScheme.primary }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (state.colorHex == hex)
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                else Modifier
                            )
                            .clickable { viewModel.onColorChange(hex) }
                    )
                }
            }

            HorizontalDivider()

            Text("Notifications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Enable study reminders")
                Switch(
                    checked = state.notificationEnabled,
                    onCheckedChange = viewModel::onNotificationToggle,
                )
            }

            if (state.notificationEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.notificationTimeStart,
                        onValueChange = viewModel::onStartTimeChange,
                        label = { Text("From") },
                        placeholder = { Text("09:00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.notificationTimeEnd,
                        onValueChange = viewModel::onEndTimeChange,
                        label = { Text("Until") },
                        placeholder = { Text("21:00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }

                Text("Interval (minutes)", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = state.notificationIntervalMinutes.toFloat(),
                    onValueChange = { viewModel.onIntervalChange(it.toInt()) },
                    valueRange = 30f..480f,
                    steps = 14,
                )
                Text(
                    "Every ${state.notificationIntervalMinutes} min  (${state.notificationIntervalMinutes / 60}h ${state.notificationIntervalMinutes % 60}m)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider()

            Text("Study Order", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            listOf("SEQUENTIAL" to "Sequential", "RANDOM" to "Random", "SMART" to "Smart (SRS)", "DUE_FIRST" to "Due First").forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onStudyOrderChange(value) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = state.studyOrderMode == value,
                        onClick = { viewModel.onStudyOrderChange(value) },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(label)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
