package com.vortex.android.speedtestapp.ui.test

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.vortex.android.speedtestapp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

//@HiltViewModel
class TestViewModel : ViewModel() {

    private val downloadUrl = "http://speedtest.tele2.net/100MB.zip"
    private val uploadUrl = "http://speedtest.karwos.net:8080/speedtest/upload.php"

    private val eventsChannel = Channel<AllEvents>()
    val allEventsFlow = eventsChannel.receiveAsFlow()

    private val _text = MutableStateFlow<String>("This is Test Fragment")
    val text : StateFlow<String>
        get() = _text.asStateFlow()

    var buttonWasPushed : Boolean = false

    val instDownloadSpeed = MutableStateFlow<String>("")

    val averageDownloadSpeed = MutableStateFlow<String>("")

    val instUploadSpeed = MutableStateFlow<String>("")

    val averageUploadSpeed = MutableStateFlow<String>("")

    val isTestOngoing = MutableStateFlow<Boolean>(false)


    fun startDownloadTest() {
        isTestOngoing.value = true
        viewModelScope.launch(Dispatchers.IO) {

            val speedTestSocket = SpeedTestSocket()

            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport) {
                    averageDownloadSpeed.value = String.format("%.2f", report.transferRateBit.toDouble()/(1024*1024))
                    startUploadTest()
                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    viewModelScope.launch {
                        eventsChannel.send(AllEvents.Error(0))
                    }
                    isTestOngoing.value = false
                    Log.d("download_tag","CONNECTION ERROR: $errorMessage")
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    instDownloadSpeed.value = String.format("%.2f", report.transferRateBit.toDouble()/(1024*1024))
                }
            })
            speedTestSocket.startDownload(downloadUrl, 200)
        }
    }

    private fun startUploadTest() {
        viewModelScope.launch(Dispatchers.IO) {
            val speedTestSocket = SpeedTestSocket()

            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport) {
                    averageUploadSpeed.value = String.format("%.2f", report.transferRateBit.toDouble()/(1024*1024))
                    isTestOngoing.value = false
                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    viewModelScope.launch {
                        eventsChannel.send(AllEvents.Error(0))
                    }
                    isTestOngoing.value = false
                    Log.d("upload_tag","CONNECTION ERROR: $errorMessage, $speedTestError")
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    instUploadSpeed.value = String.format("%.2f", report.transferRateBit.toDouble()/(1024*1024))
                }
            })
            speedTestSocket.startUpload(uploadUrl, 1000000, 200)
        }
    }

    sealed class AllEvents {
        //Viewmodel не должна работать со строками
        //data class Message(@StringRes val messageRes : Int) : AllEvents()
        //data class ErrorCode(val code : Int): AllEvents()
        data class Error(@StringRes val errorRes : Int) : AllEvents()
    }
}