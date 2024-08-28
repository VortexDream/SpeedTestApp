package com.vortex.android.speedtestapp.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

//@HiltViewModel
class SettingsViewModel : ViewModel() {

    private val _text = MutableStateFlow<String>("This is Settings Fragment")
    val text : StateFlow<String>
        get() = _text.asStateFlow()
}