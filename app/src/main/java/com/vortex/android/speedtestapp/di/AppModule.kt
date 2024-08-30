package com.vortex.android.speedtestapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.vortex.android.speedtestapp.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Файл инстансов даггер хилта
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //Предоставление экземпляра Датастор, хранящий наши данные о настройках
    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context) : DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("settings")
        }
    }

    //Предоставление экземпляра репозитория для взаимодействия SharedPreferences с остальными частями приложения
    @Singleton
    @Provides
    fun providePreferencesRepository(dataStore: DataStore<Preferences>) : PreferencesRepository {
        return PreferencesRepository(dataStore)
    }
}