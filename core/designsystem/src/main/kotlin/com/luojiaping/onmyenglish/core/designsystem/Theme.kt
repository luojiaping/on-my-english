package com.luojiaping.onmyenglish.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object OmeSpacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val page = 20.dp
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF176B51),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA8F2D2),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF685D32),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1E29D),
    tertiary = Color(0xFF8D3B4B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9DE),
    background = Color(0xFFF8FAF7),
    surface = Color(0xFFF8FAF7),
    surfaceVariant = Color(0xFFDEE5DF),
    outline = Color(0xFF707972),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DD5B6),
    onPrimary = Color(0xFF003829),
    primaryContainer = Color(0xFF00513C),
    onPrimaryContainer = Color(0xFFA8F2D2),
    secondary = Color(0xFFD5C680),
    onSecondary = Color(0xFF383000),
    secondaryContainer = Color(0xFF504716),
    tertiary = Color(0xFFFFB2BD),
    onTertiary = Color(0xFF551D2A),
    tertiaryContainer = Color(0xFF713342),
    background = Color(0xFF101411),
    surface = Color(0xFF101411),
    surfaceVariant = Color(0xFF414943),
    outline = Color(0xFF8A938C),
)

private val OmeTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        titleMedium = titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
    )
}

@Composable
fun OnMyEnglishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = OmeTypography,
        content = content,
    )
}
