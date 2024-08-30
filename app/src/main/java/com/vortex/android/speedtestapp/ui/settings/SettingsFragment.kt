package com.vortex.android.speedtestapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vortex.android.speedtestapp.R
import com.vortex.android.speedtestapp.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

//Второй экран - здесь распологаются настройки
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    //ViewBinding (также проверяем null safety)
    private var _binding: FragmentSettingsBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val settingsViewModel: SettingsViewModel by viewModels()//Объявляем вьюмодель


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        //Обновляем интерфейс
        UpdateUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Сохранение настроек при изменении
        binding.apply {
            themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val theme = when (checkedId) {
                    R.id.radioLight -> "Light"
                    R.id.radioDark -> "Dark"
                    R.id.radioSystem -> "System"
                    else -> "System"
                }
                settingsViewModel.setTheme(theme)
            }

            downloadCheckBox.setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.toggleDownload(isChecked)
            }

            uploadCheckBox.setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.toggleUpload(isChecked)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Уничтожаем байндинг во избежание утечек памяти
        _binding = null
    }

    //Подписываемся на изменение настроек для синхронизации данных на экране и в файле настроек
    private fun UpdateUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.themeFlow.collectLatest {
                    //Проверяем на нулл т.к. класс nullable
                    it?.let {
                        when (it) {
                            "Light" -> binding.radioLight.isChecked = true
                            "Dark" -> binding.radioDark.isChecked = true
                            "System" -> binding.radioSystem.isChecked = true
                        }
                        //Применяем тему
                        applyTheme(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.downloadFlow.collectLatest {
                    it?.let {
                        binding.downloadCheckBox.isChecked = it
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.uploadFlow.collectLatest {
                    it?.let {
                        binding.uploadCheckBox.isChecked = it
                    }
                }
            }
        }
    }

    //Вызываем системное изменение темы
    private fun applyTheme(theme: String) {
        when (theme) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}