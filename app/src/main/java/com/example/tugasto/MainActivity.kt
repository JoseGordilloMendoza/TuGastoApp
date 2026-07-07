package com.example.tugasto

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tugasto.ui.navigation.AppNavigation
import com.example.tugasto.ui.screens.ProfileViewModel
import com.example.tugasto.ui.theme.TuGastoTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ProfileViewModel = hiltViewModel()
            val isDarkModePref by viewModel.isDarkMode.collectAsState()
            val isDarkTheme = isDarkModePref ?: isSystemInDarkTheme()
            val view = LocalView.current

            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view)
                        .isAppearanceLightStatusBars = !isDarkTheme
                }
            }

            TuGastoTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}
