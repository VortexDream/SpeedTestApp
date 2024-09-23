package com.vortex.android.speedtestapp.ui.test

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.vortex.android.speedtestapp.PreferencesRepository
import com.vortex.android.speedtestapp.R
import com.vortex.android.speedtestapp.firebase.BaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@SuppressLint("DefaultLocale")
//Вьюмодель первого экрана
@HiltViewModel
class TestViewModel @Inject constructor(
    private val settings: PreferencesRepository,
    private val storageRef: FirebaseStorage
) : ViewModel() {

    var downloadUrl = ""
    var uploadUrl = "http://speedtest.tele2.net/upload.php"

    //Создаем канал для прослушивания событий
    private val eventsChannel = Channel<AllEvents>()
    val allEventsFlow = eventsChannel.receiveAsFlow()

    //Была ли нажата кнопка (был ли начат тест хоть раз)
    var buttonWasPushed : Boolean = false

    //Скорости подключения
    val instDownloadSpeed = MutableStateFlow<String>("")

    val averageDownloadSpeed = MutableStateFlow<String>("")

    val instUploadSpeed = MutableStateFlow<String>("")

    val averageUploadSpeed = MutableStateFlow<String>("")

    //Идёт ли тест
    val isTestOngoing = MutableStateFlow<Boolean>(false)

    //Настройки загрузки
    private var _downloadPref : Boolean? = null
    val downloadPref : Boolean?
        get() = _downloadPref

    private var _uploadPref : Boolean? = null
    val uploadPref : Boolean?
        get() = _uploadPref

    //Подписываемся на обновление настроек
    init {
        viewModelScope.launch {
            settings.measuringDownload.collect { download ->
                _downloadPref = download
            }
        }

        viewModelScope.launch {
            settings.measuringUpload.collect { upload ->
                _uploadPref = upload
            }
        }
        storageRef.reference.child("Download.zip").downloadUrl.addOnSuccessListener {
            downloadUrl = it.toString()
        }
    }

    //Начало теста скорости загрузки
    fun startDownloadTest() {
        //Тест идёт
        isTestOngoing.value = true
        //Работаем с сетью на IO потоке
        viewModelScope.launch(Dispatchers.IO) {

            var flagConnectionPending = false
            //Создаём объект
            val speedTestSocket = SpeedTestSocket()

            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
                //Событие, которое срабатывает при успешном завершении теста
                override fun onCompletion(report: SpeedTestReport) {
                    //Выводим среднюю скорость на экран
                    if (instDownloadSpeed.value == "" || instDownloadSpeed.value == "0,00") {
                        viewModelScope.launch {
                            eventsChannel.send(AllEvents.Success(R.string.error_connection))
                        }
                    } else {
                        averageDownloadSpeed.value = String.format("%.2f", report.transferRateBit.toDouble()/(1024*1024))
                        viewModelScope.launch {
                            eventsChannel.send(AllEvents.Success(R.string.success_download))
                        }
                    }
                    //Проверяем настройки, если отключен тест, отдачи, то выходим
                    if (uploadPref != null && uploadPref!!) {
                        startUploadTest()
                    } else {
                        isTestOngoing.value = false
                    }
                }

                //Событие ошибки
                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    viewModelScope.launch {
                        //Отправляем событие ошибки в канал
                        eventsChannel.send(AllEvents.Error(0))
                    }
                    isTestOngoing.value = false
                    Log.d("download_tag","CONNECTION ERROR: $errorMessage")
                }

                //События, срабатываюшие с определенными интервалами в процессе теста.
                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    //Выводим мгновенную скорость
                    instDownloadSpeed.value = String.format("%.2f", report.transferRateBit.toDouble()/(1024*1024))
                    if (instDownloadSpeed.value == "0,00" && !flagConnectionPending) {
                        flagConnectionPending = true
                        viewModelScope.launch { //Обрабатываем проблемы с соединением и закрываем задачу при надобности
                            eventsChannel.send(AllEvents.Message(R.string.message_connection_pending_download))
                            delay(3000)
                            if (instDownloadSpeed.value == "0,00") {
                                isTestOngoing.value = false
                                flagConnectionPending = false
                                speedTestSocket.forceStopTask()
                            }
                        }
                    }
                }
            })
            //Старт самого теста, выставляем интервал срабатывания onProgress на 200 мс
            //Предел по продолжительности - 15 сек
            speedTestSocket.startFixedDownload(downloadUrl, 15000, 100)
        }
    }

    //Аналогично startDownloadTest()
    fun startUploadTest() {
        isTestOngoing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val speedTestSocket = SpeedTestSocket()
            var flagConnectionPending = false

            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport) {
                    averageUploadSpeed.value = String.format("%.2f", report.transferRateBit.toDouble()/(1024*1024))
                    if (instUploadSpeed.value == "" || instUploadSpeed.value == "0,00") {
                        viewModelScope.launch {
                            eventsChannel.send(AllEvents.Success(R.string.error_connection))
                        }
                    } else {
                        viewModelScope.launch {
                            eventsChannel.send(AllEvents.Success(R.string.success_upload))
                        }
                    }
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
                    Log.d("refresh", report.transferRateBit.toString())
                    //Выводим сообщение об установке соединения, чтобы пользователь не подумал, что зависло
                    if (instUploadSpeed.value == "0,00" && !flagConnectionPending) {
                        flagConnectionPending = true
                        viewModelScope.launch { //Обрабатываем проблемы с соединением и закрываем задачу при надобности
                            delay(2000)
                            eventsChannel.send(AllEvents.Message(R.string.message_connection_pending))
                            delay(20000)
                            if (instUploadSpeed.value == "0,00") {
                                isTestOngoing.value = false
                                flagConnectionPending = false
                                speedTestSocket.forceStopTask()
                            }
                        }
                    }
                }
            })
            //Единственное отличие - определяем размер файла, загружаемого на сервер
            speedTestSocket.startUpload(uploadUrl, 100000000, 100)
        }
    }

    //Класс с видами событий
    sealed class AllEvents {
        //Viewmodel не должна работать со строками
        data class Message(@StringRes val messageRes : Int) : AllEvents()
        data class Success(@StringRes val successRes : Int): AllEvents()
        data class Error(@StringRes val errorRes : Int) : AllEvents()
    }
}