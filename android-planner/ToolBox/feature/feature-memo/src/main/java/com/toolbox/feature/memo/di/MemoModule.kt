package com.toolbox.feature.memo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.database.dao.MemoDao
import com.toolbox.feature.memo.data.MemoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MemoModule {

    @Provides
    @Singleton
    fun provideMemoRepository(memoDao: MemoDao): MemoRepository {
        return MemoRepository(memoDao)
    }
}
