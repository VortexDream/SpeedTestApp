package com.vortex.android.speedtestapp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

//Класс репозитория с настройками для взаимодействия хранилища с данными настроек с остальными частями приложения
class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    //Ключи для записи в датастор
    private val PREF_THEME = stringPreferencesKey("theme")
    private val PREF_MEASURING_DOWNLOAD = booleanPreferencesKey("measuringDownload")
    private val PREF_MEASURING_UPLOAD = booleanPreferencesKey("measuringUpload")

    //Флоу, отвечающее за извлечение темы
    val theme: Flow<String> = dataStore.data.map {
        it[PREF_THEME] ?: "System"
    }.distinctUntilChanged()

    //Запись текущей темы
    suspend fun setTheme(theme: String) {
        dataStore.edit {
            it[PREF_THEME] = theme
        }
    }

    //Флоу, отвечающее за настройку измерения скорости загрузки
    val measuringDownload: Flow<Boolean> = dataStore.data.map {
        it[PREF_MEASURING_DOWNLOAD] ?: true
    }.distinctUntilChanged()

    suspend fun setMeasuringDownload(download: Boolean) {
        dataStore.edit {
            it[PREF_MEASURING_DOWNLOAD] = download
        }
    }

    //Флоу, отвечающее за настройку измерения скорости отдачи
    val measuringUpload: Flow<Boolean> = dataStore.data.map {
        it[PREF_MEASURING_UPLOAD] ?: true
    }.distinctUntilChanged()

    suspend fun setMeasuringUpload(upload: Boolean) {
        dataStore.edit {
            it[PREF_MEASURING_UPLOAD] = upload
        }
    }
}