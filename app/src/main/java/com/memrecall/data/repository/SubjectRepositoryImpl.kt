package com.memrecall.data.repository

import com.memrecall.data.local.dao.SubjectDao
import com.memrecall.data.mapper.toDomain
import com.memrecall.data.mapper.toEntity
import com.memrecall.domain.model.Subject
import com.memrecall.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao,
) : SubjectRepository {

    override fun getAllSubjects(): Flow<List<Subject>> =
        subjectDao.getAllSubjects().map { it.map { e -> e.toDomain() } }

    override suspend fun getSubjectById(id: Long): Subject? =
        subjectDao.getSubjectById(id)?.toDomain()

    override suspend fun createSubject(subject: Subject): Long =
        subjectDao.insertSubject(subject.toEntity())

    override suspend fun updateSubject(subject: Subject) =
        subjectDao.updateSubject(subject.toEntity())

    override suspend fun deleteSubject(subject: Subject) =
        subjectDao.deleteSubject(subject.toEntity())

    override suspend fun refreshCounts(subjectId: Long) {
        subjectDao.refreshCardCount(subjectId)
        subjectDao.refreshMasteredCount(subjectId)
    }
}
