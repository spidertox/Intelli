package com.krcast.sender.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun KRCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = remember {
        if (darkTheme) darkColorScheme(
            primary = Color(0xFF1F6FFF),
            secondary = Color(0xFF8E3DFF),
            tertiary = Color(0xFF00D9FF),
            background = Color(0xFF0A0A0F),
            surface = Color(0xFF16161F),
            surfaceVariant = Color(0xFF1E1E2E),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFF0A0A0F),
            onBackground = Color(0xFFFFFFFF),
            onSurface = Color(0xFFE8E8F0),
            onSurfaceVariant = Color(0xFF9494A8),
            outline = Color(0xFF2A2A3A),
            outlineVariant = Color(0xFF2A2A3A)
        ) else lightColorScheme(
            primary = Color(0xFF1F6FFF),
            secondary = Color(0xFF8E3DFF),
            tertiary = Color(0xFF00D9FF),
            background = Color(0xFFF5F5FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF0F0F5),
            onPrimary = Color(0xFF0A0A0F),
            onSecondary = Color(0xFF0A0A0F),
            onTertiary = Color(0xFF0A0A0F),
            onBackground = Color(0xFF0A0A0F),
            onSurface = Color(0xFF0A0A0F),
            onSurfaceVariant = Color(0xFF4A4A5A),
            outline = Color(0xFFD0D0DD),
            outlineVariant = Color(0xFFE0E0E8)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

private val Typography = androidx.compose.material3.Typography(
    headlineLarge = androidx.compose.material3.TextStyle(
        fontSize = 32.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    headlineMedium = androidx.compose.material3.TextStyle(
        fontSize = 28.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    titleLarge = androidx.compose.material3.TextStyle(
        fontSize = 18.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    bodyLarge = androidx.compose.material3.TextStyle(
        fontSize = 16.sp
    ),
    bodyMedium = androidx.compose.material3.TextStyle(
        fontSize = 14.sp
    ),
    labelLarge = androidx.compose.material3.TextStyle(
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    )
)

private val Shapes = androidx.compose.material3.Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)
