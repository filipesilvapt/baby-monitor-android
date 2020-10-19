package com.babyMonitor.di.modules

import android.content.Context
import com.babyMonitor.repositories.ClientTokenRepository
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

}