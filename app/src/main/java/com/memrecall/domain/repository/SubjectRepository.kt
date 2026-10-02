package com.memrecall.domain.repository

import com.memrecall.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getAllSubjects(): Flow<List<Subject>>
    suspend fun getSubjectById(id: Long): Subject?
    suspend fun createSubject(subject: Subject): Long
    suspend fun updateSubject(subject: Subject)
    suspend fun deleteSubject(subject: Subject)
    suspend fun refreshCounts(subjectId: Long)
}
