package com.memrecall.ui.screens.subject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memrecall.domain.model.NotificationMode
import com.memrecall.domain.model.StudyOrderMode
import com.memrecall.domain.model.Subject
import com.memrecall.domain.repository.SubjectRepository
import com.memrecall.notification.StudyNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditSubjectState(
    val name: String = "",
    val description: String = "",
    val colorHex: String = "#6366F1",
    val notificationEnabled: Boolean = false,
    val notificationTimeStart: String = "09:00",
    val notificationTimeEnd: String = "21:00",
    val notificationIntervalMinutes: Int = 120,
    val studyOrderMode: String = "SMART",
    val saved: Boolean = false,
)

@HiltViewModel
class AddEditSubjectViewModel @Inject constructor(
    private val subjectRepo: SubjectRepository,
    private val notifManager: StudyNotificationManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editingId: Long? = savedStateHandle.get<Long>("subjectId")?.takeIf { it > 0 }

    private val _state = MutableStateFlow(AddEditSubjectState())
    val state: StateFlow<AddEditSubjectState> = _state.asStateFlow()

    init {
        editingId?.let { id ->
            viewModelScope.launch {
                subjectRepo.getSubjectById(id)?.let { s ->
                    _state.update { _ ->
                        AddEditSubjectState(
                            name = s.name,
                            description = s.description,
                            colorHex = s.colorHex,
                            notificationEnabled = s.notificationEnabled,
                            notificationTimeStart = s.notificationTimeStart,
                            notificationTimeEnd = s.notificationTimeEnd,
                            notificationIntervalMinutes = s.notificationIntervalMinutes,
                            studyOrderMode = s.studyOrderMode.name,
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v) }
    fun onDescriptionChange(v: String) = _state.update { it.copy(description = v) }
    fun onColorChange(v: String) = _state.update { it.copy(colorHex = v) }
    fun onNotificationToggle(v: Boolean) = _state.update { it.copy(notificationEnabled = v) }
    fun onStartTimeChange(v: String) = _state.update { it.copy(notificationTimeStart = v) }
    fun onEndTimeChange(v: String) = _state.update { it.copy(notificationTimeEnd = v) }
    fun onIntervalChange(v: Int) = _state.update { it.copy(notificationIntervalMinutes = v) }
    fun onStudyOrderChange(v: String) = _state.update { it.copy(studyOrderMode = v) }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            val subject = Subject(
                id = editingId ?: 0,
                name = s.name,
                description = s.description,
                colorHex = s.colorHex,
                notificationEnabled = s.notificationEnabled,
                notificationTimeStart = s.notificationTimeStart,
                notificationTimeEnd = s.notificationTimeEnd,
                notificationMode = NotificationMode.SMART,
                notificationIntervalMinutes = s.notificationIntervalMinutes,
                studyOrderMode = runCatching { StudyOrderMode.valueOf(s.studyOrderMode) }
                    .getOrElse { StudyOrderMode.SMART },
            )
            if (editingId != null) {
                subjectRepo.updateSubject(subject)
            } else {
                subjectRepo.createSubject(subject)
            }
            // Re-schedule notification
            if (s.notificationEnabled) notifManager.scheduleForSubject(subject)
            else editingId?.let { notifManager.cancelForSubject(it) }

            _state.update { it.copy(saved = true) }
        }
    }
}
