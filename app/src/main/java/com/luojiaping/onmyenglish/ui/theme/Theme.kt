package com.luojiaping.onmyenglish.ui.theme

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

private val LightColors = lightColorScheme(
    primary = Color(0xFF276B4D),
    onPrimary = Color.White,
    secondary = Color(0xFF665A35),
    tertiary = Color(0xFF8B3D48),
    background = Color(0xFFF8FAF7),
    surface = Color(0xFFF8FAF7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8ED3AD),
    onPrimary = Color(0xFF003824),
    secondary = Color(0xFFD2C38E),
    tertiary = Color(0xFFFFB2B9),
    background = Color(0xFF101411),
    surface = Color(0xFF101411),
)

@Composable
fun OnMyEnglishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
