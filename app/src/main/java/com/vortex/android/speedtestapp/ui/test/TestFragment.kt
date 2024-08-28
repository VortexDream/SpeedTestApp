package com.vortex.android.speedtestapp.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.vortex.android.speedtestapp.databinding.FragmentSettingsBinding
import com.vortex.android.speedtestapp.databinding.FragmentTestBinding
import com.vortex.android.speedtestapp.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

//@AndroidEntryPoint
class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val testViewModel: TestViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTestBinding.inflate(inflater, container, false)

        val textView: TextView = binding.textTest
        textView.text = testViewModel.text.value

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}