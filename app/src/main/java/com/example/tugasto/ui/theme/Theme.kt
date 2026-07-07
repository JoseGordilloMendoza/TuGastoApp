package com.example.tugasto.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TuGastoBlue,
    onPrimary = Color.White,
    secondary = TuGastoGreen,
    onSecondary = Color.White,
    background = TuGastoDarkSurface,
    onBackground = Color.White,
    surface = TuGastoDark,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155), // like TuGastoGray800
    onSurfaceVariant = TuGastoGray400,
    outline = TuGastoGray700,
    error = TuGastoRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TuGastoBlue,
    onPrimary = Color.White,
    secondary = TuGastoGreen,
    onSecondary = Color.White,
    background = TuGastoBg,
    onBackground = TuGastoGray900,
    surface = Color.White,
    onSurface = TuGastoGray900,
    surfaceVariant = TuGastoGray100,
    onSurfaceVariant = TuGastoGray500,
    outline = TuGastoGray200,
    error = TuGastoRed,
    onError = Color.White
)

@Composable
fun TuGastoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}