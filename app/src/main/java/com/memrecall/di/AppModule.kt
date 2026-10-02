package com.memrecall.di

import android.content.Context
import androidx.room.Room
import com.memrecall.data.local.AppDatabase
import com.memrecall.data.local.dao.FlashCardDao
import com.memrecall.data.local.dao.StudySessionDao
import com.memrecall.data.local.dao.SubjectDao
import com.memrecall.data.repository.FlashCardRepositoryImpl
import com.memrecall.data.repository.SubjectRepositoryImpl
import com.memrecall.domain.repository.FlashCardRepository
import com.memrecall.domain.repository.SubjectRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSubjectDao(db: AppDatabase): SubjectDao = db.subjectDao()

    @Provides
    fun provideFlashCardDao(db: AppDatabase): FlashCardDao = db.flashCardDao()

    @Provides
    fun provideStudySessionDao(db: AppDatabase): StudySessionDao = db.studySessionDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindFlashCardRepository(impl: FlashCardRepositoryImpl): FlashCardRepository
}
