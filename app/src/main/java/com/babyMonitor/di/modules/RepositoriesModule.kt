package com.babyMonitor.di.modules

import android.content.Context
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.repositories.ClientTokenRepository
import com.babyMonitor.repositories.SleepStateRepository
import com.babyMonitor.repositories.TemperatureRepository
import com.babyMonitor.repositories.TemperatureThresholdsRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
object RepositoriesModule {

    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }

    @Provides
    fun provideClientTokenRepository(
        @ApplicationContext applicationContext: Context,
        database: FirebaseDatabase
    ): ClientTokenRepository {
        return ClientTokenRepository(applicationContext, database)
    }

    @Provides
    fun provideTemperatureThresholdsRepository(
        database: FirebaseDatabase
    ): TemperatureThresholdsRepository {
        return TemperatureThresholdsRepository(database)
    }

    @Provides
    fun provideSleepStateRepository(
        database: FirebaseDatabase
    ): SleepStateRepository {
        return SleepStateRepository(database.getReference(RTDatabasePaths.PATH_SLEEP_STATES))
    }

    @Provides
    fun provideTemperatureRepository(
        database: FirebaseDatabase
    ): TemperatureRepository {
        return TemperatureRepository(database.getReference(RTDatabasePaths.PATH_THERMOMETER_READINGS))
    }

}