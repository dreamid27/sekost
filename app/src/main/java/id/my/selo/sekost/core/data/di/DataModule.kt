package id.my.selo.sekost.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.my.selo.sekost.core.data.database.SekostDatabase
import id.my.selo.sekost.core.data.datastore.DataStoreSettingsRepository
import id.my.selo.sekost.core.data.repository.IssueRepository
import id.my.selo.sekost.core.data.repository.OfflineIssueRepository
import id.my.selo.sekost.core.data.repository.OfflinePaymentRepository
import id.my.selo.sekost.core.data.repository.OfflinePropertyRepository
import id.my.selo.sekost.core.data.repository.OfflineRoomRepository
import id.my.selo.sekost.core.data.repository.OfflineTenantRepository
import id.my.selo.sekost.core.data.repository.PaymentRepository
import id.my.selo.sekost.core.data.repository.PropertyRepository
import id.my.selo.sekost.core.data.repository.RoomRepository
import id.my.selo.sekost.core.data.repository.RoomTransactionRunner
import id.my.selo.sekost.core.data.repository.SettingsRepository
import id.my.selo.sekost.core.data.repository.TenantRepository
import id.my.selo.sekost.core.data.repository.TransactionRunner
import id.my.selo.sekost.core.domain.SystemTimeProvider
import id.my.selo.sekost.core.domain.TimeProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): SekostDatabase =
        Room.databaseBuilder(context, SekostDatabase::class.java, SekostDatabase.NAME).build()

    @Provides fun propertyDao(db: SekostDatabase) = db.propertyDao()

    @Provides fun roomDao(db: SekostDatabase) = db.roomDao()

    @Provides fun tenantDao(db: SekostDatabase) = db.tenantDao()

    @Provides fun paymentDao(db: SekostDatabase) = db.paymentDao()

    @Provides fun issueDao(db: SekostDatabase) = db.issueDao()

    @Provides @Singleton
    fun preferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }

    @Provides fun timeProvider(): TimeProvider = SystemTimeProvider
}

@Module
@InstallIn(SingletonComponent::class)
internal interface RepositoryModule {
    @Binds fun properties(impl: OfflinePropertyRepository): PropertyRepository

    @Binds fun rooms(impl: OfflineRoomRepository): RoomRepository

    @Binds fun tenants(impl: OfflineTenantRepository): TenantRepository

    @Binds fun payments(impl: OfflinePaymentRepository): PaymentRepository

    @Binds fun issues(impl: OfflineIssueRepository): IssueRepository

    @Binds fun transactions(impl: RoomTransactionRunner): TransactionRunner

    @Binds @Singleton
    fun settings(impl: DataStoreSettingsRepository): SettingsRepository
}
