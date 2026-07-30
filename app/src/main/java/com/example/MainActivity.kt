package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.ConnectXAppNavigation
import com.example.ui.theme.ConnectXTheme
import com.example.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkModePref by settingsViewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val useDarkMode = isDarkModePref || systemDark

            ConnectXTheme(darkTheme = useDarkMode) {
                ConnectXAppNavigation(settingsViewModel = settingsViewModel)
            }
        }
    }
}
