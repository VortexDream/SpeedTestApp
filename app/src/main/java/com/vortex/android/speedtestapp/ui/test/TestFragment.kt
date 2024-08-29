package com.vortex.android.speedtestapp.ui.test

import android.os.Bundle
import android.util.Log
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
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


//@AndroidEntryPoint
class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val testViewModel: TestViewModel by viewModels()
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
        listenToChannels()
        UpdateUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun UpdateUi() {
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
            if (testViewModel.buttonWasPushed) {
                visibilityGroup.isVisible = true
            }
            downloadUrlTextview.text = "Download URL: $downloadUrl"
            uploadUrlTextview.text = "Upload URL: $uploadUrl"
            btnStartTest.setOnClickListener {
                testViewModel.buttonWasPushed = true
                visibilityGroup.isVisible = true
                testViewModel.run {
                    instUploadSpeed.value = ""
                    instDownloadSpeed.value = ""
                    averageUploadSpeed.value = ""
                    averageDownloadSpeed.value = ""
                }
                testViewModel.startDownloadTest()
            }
        }
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            testViewModel.allEventsFlow.collect { event ->
                when(event){
                    is TestViewModel.AllEvents.Error -> {
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