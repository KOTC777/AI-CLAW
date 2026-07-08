package com.toolbox.feature.password.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.database.dao.PasswordEntryDao
import com.toolbox.core.database.dao.PasswordGroupDao
import com.toolbox.core.security.crypto.SecurityManager
import com.toolbox.feature.password.data.PasswordRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PasswordModule {

    @Provides
    @Singleton
    fun providePasswordRepository(
        entryDao: PasswordEntryDao,
        groupDao: PasswordGroupDao,
        securityManager: SecurityManager
    ): PasswordRepository {
        return PasswordRepository(entryDao, groupDao, securityManager)
    }
}
