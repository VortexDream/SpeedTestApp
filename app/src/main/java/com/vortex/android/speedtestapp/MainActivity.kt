package com.vortex.android.speedtestapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.vortex.android.speedtestapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

//Единственная активити приложения. К ней прикрепляется фрагмент с помощью навигации на нижнем баре
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settings: PreferencesRepository //Репозиторий настроек

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)//ViewBinding
        setContentView(binding.root)
        applyTheme()//Применяется темная или светлая тема в зависимости от настроек приложения

        //Этот участок кода отвечает за логику BottomNavigationBar
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        //Если будет необходимо больше экранов, можно добавить их сюда
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_test, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        val bottomNavigationView = binding.navView
        bottomNavigationView.setupWithNavController(navController)
    }

    private fun applyTheme() {
        lifecycleScope.launch {
            //Подписываемся на обновление настроек, чтобы обновлять тему мгновенно без дополнительного вызова функции смены темы
            settings.theme.collect {
                when (it) {
                    "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    "System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }
}