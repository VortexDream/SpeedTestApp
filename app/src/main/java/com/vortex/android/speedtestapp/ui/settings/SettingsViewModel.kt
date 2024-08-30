package com.vortex.android.speedtestapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vortex.android.speedtestapp.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//Вьюмодель второго экрана
@HiltViewModel
class SettingsViewModel @Inject constructor(
    //Инжект репозитория настроек
    private val settings: PreferencesRepository
) : ViewModel() {

    //Флоу с настройками
    val themeFlow = MutableStateFlow<String?>(null)
    val downloadFlow = MutableStateFlow<Boolean?>(null)
    val uploadFlow = MutableStateFlow<Boolean?>(null)

    //Подписываемся на обновление настроек
    init {
        viewModelScope.launch {
            settings.theme.collect { theme ->
                themeFlow.value = theme
            }
        }

        viewModelScope.launch {
            settings.measuringDownload.collect { download ->
                downloadFlow.value = download
            }
        }

        viewModelScope.launch {
            settings.measuringUpload.collect { upload ->
                uploadFlow.value = upload
            }
        }

    }

    //Функции записи настроек
    fun setTheme(theme: String) {
        viewModelScope.launch {
            settings.setTheme(theme)
        }
    }

    fun toggleDownload(download: Boolean) {
        viewModelScope.launch {
            settings.setMeasuringDownload(download)
        }
    }

    fun toggleUpload(upload: Boolean) {
        viewModelScope.launch {
            settings.setMeasuringUpload(upload)
        }
    }
}
