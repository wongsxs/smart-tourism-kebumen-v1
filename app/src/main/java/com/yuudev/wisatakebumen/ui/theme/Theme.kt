package com.yuudev.wisatakebumen.ui.theme

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

    primary = AppColors.Blue,
    onPrimary = Color.White,

    secondary = AppColors.Navy,
    onSecondary = Color.White,

    tertiary = AppColors.Blue,

    background = AppColors.Navy,
    onBackground = Color.White,

    surface = Color(0xFF1E293B),
    onSurface = Color.White,

    error = AppColors.Error,
    onError = Color.White,

    outline = AppColors.Border,

    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color.White,

    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(

    primary = AppColors.Blue,
    onPrimary = Color.White,

    secondary = AppColors.Navy,
    onSecondary = Color.White,

    tertiary = AppColors.Blue,

    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,

    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,

    error = AppColors.Error,
    onError = Color.White,

    outline = AppColors.Border,

    primaryContainer = AppColors.BlueContainer,
    onPrimaryContainer = AppColors.Blue,

    secondaryContainer = Color(0xFFE8EEF7),
    onSecondaryContainer = AppColors.Navy
)

@Composable
fun WisataKebumenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    // Sebaiknya false agar branding tetap konsisten
    dynamicColor: Boolean = false,

    content: @Composable () -> Unit
) {

    val colorScheme = when {

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
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