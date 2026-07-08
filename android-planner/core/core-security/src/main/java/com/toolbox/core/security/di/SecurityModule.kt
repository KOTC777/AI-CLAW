package com.toolbox.core.security.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.toolbox.core.datastore.DataStoreManager
import com.toolbox.core.security.crypto.Argon2Kdf
import com.toolbox.core.security.crypto.CryptoEngine
import com.toolbox.core.security.crypto.SecurityManager
import com.toolbox.core.security.keystore.KeystoreManager
import com.toolbox.core.security.lock.LockManager
import com.toolbox.core.security.vault.VaultExporter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideKeystoreManager(): KeystoreManager = KeystoreManager()

    @Provides
    @Singleton
    fun provideArgon2Kdf(): Argon2Kdf = Argon2Kdf()

    @Provides
    @Singleton
    fun provideCryptoEngine(): CryptoEngine = CryptoEngine()

    @Provides
    @Singleton
    fun provideSecurityManager(
        keystoreManager: KeystoreManager,
        dataStoreManager: DataStoreManager,
        argon2Kdf: Argon2Kdf,
        cryptoEngine: CryptoEngine
    ): SecurityManager = SecurityManager(keystoreManager, dataStoreManager, argon2Kdf, cryptoEngine)

    @Provides
    @Singleton
    fun provideLockManager(argon2Kdf: Argon2Kdf): LockManager = LockManager(argon2Kdf)

    @Provides
    @Singleton
    fun provideVaultExporter(cryptoEngine: CryptoEngine): VaultExporter = VaultExporter(cryptoEngine)
}
