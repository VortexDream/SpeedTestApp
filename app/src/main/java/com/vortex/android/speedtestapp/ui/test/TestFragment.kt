package com.vortex.android.speedtestapp.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.vortex.android.speedtestapp.R
import com.vortex.android.speedtestapp.databinding.FragmentTestBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

//Первый экран приложения, тест скорости происходит здесь
@AndroidEntryPoint
class TestFragment : Fragment() {

    //ViewBinding (также проверяем null safety)
    private var _binding: FragmentTestBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val testViewModel: TestViewModel by viewModels() //Объявляем вьюмодель
    //Ссылки на сервера, ссылка на сервер отдачи - заглушка, не работает.
    private val downloadUrl = "http://speedtest.tele2.net/100MB.zip"
    private val uploadUrl = "http://speedtest.karwos.net:8080/speedtest/upload.php"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Подписываемся на ивенты и подрубаем обновление интерфейса
        listenToChannels()
        UpdateUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Уничтожаем байндинг во избежание утечек памяти
        _binding = null
    }

    private fun UpdateUi() {
        //Четыре корутин снизу отвечают за подписки на обновление интерфейса - числа со значениями скоростей
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testViewModel.instUploadSpeed.collectLatest {
                    binding.instUploadSpeed.text = it
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testViewModel.instDownloadSpeed.collectLatest {
                    binding.instDownloadSpeed.text = it
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testViewModel.averageDownloadSpeed.collectLatest {
                    binding.averageDownloadSpeed.text = it
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testViewModel.averageUploadSpeed.collectLatest {
                    binding.averageUploadSpeed.text = it
                }
            }
        }
        //Выключаем кнопку начала теста, если тест уже идет
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testViewModel.isTestOngoing.collectLatest { isTestOngoing ->
                    binding.btnStartTest.apply {
                        isEnabled = !isTestOngoing
                        isClickable = !isTestOngoing
                    }
                }
            }
        }

        binding.apply {
            //На всякий случай проверяем, был ли хоть раз запущен тест, чтобы не вырубало интерфейс при переходе между фрагментами
            if (testViewModel.buttonWasPushed) {
                visibilityGroup.isVisible = true
            }
            //Отображаем адреса на экране
            downloadUrlTextview.text = "Download URL: $downloadUrl"
            uploadUrlTextview.text = "Upload URL: $uploadUrl"
            btnStartTest.setOnClickListener {
                //Выполняем проверку настроек, и в зависимости от значения, запускаем соответствующий этап теста
                if (testViewModel.downloadPref != null && testViewModel.downloadPref!!) {
                    testViewModel.buttonWasPushed = true
                    //Включаем видимость интерфейса только после начала теста
                    visibilityGroup.isVisible = true
                    testViewModel.run { //Обнуляем значения скоростей с началом нового теста
                        instUploadSpeed.value = ""
                        instDownloadSpeed.value = ""
                        averageUploadSpeed.value = ""
                        averageDownloadSpeed.value = ""
                    }
                    testViewModel.startDownloadTest() //Запускаем тест загрузки
                } else if (testViewModel.uploadPref != null && testViewModel.uploadPref!!) {
                    testViewModel.buttonWasPushed = true
                    visibilityGroup.isVisible = true
                    testViewModel.run {
                        instUploadSpeed.value = ""
                        instDownloadSpeed.value = ""
                        averageUploadSpeed.value = ""
                        averageDownloadSpeed.value = ""
                    }
                    testViewModel.startUploadTest() //Запускаем тест отдачи
                }
            }
        }
    }

    //Слушатель событий ошибки. При желании можно расширить на другие события и добавить информацию о видах ошибок внутрь сабклассов
    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            testViewModel.allEventsFlow.collect { event ->
                when(event){
                    is TestViewModel.AllEvents.Error -> {
                        //Выводим сообщение об ошибке тестирования
                        Snackbar
                            .make(binding.root, getString(R.string.error_empty_password), Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.btnStartTest)
                            .show()
                    }
                }
            }
        }
    }
}