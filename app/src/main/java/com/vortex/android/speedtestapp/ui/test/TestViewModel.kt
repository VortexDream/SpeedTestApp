package com.vortex.android.speedtestapp.ui.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

//@HiltViewModel
class TestViewModel : ViewModel() {

    private val _text = MutableStateFlow<String>("This is Test Fragment")
    val text : StateFlow<String>
        get() = _text.asStateFlow()
}