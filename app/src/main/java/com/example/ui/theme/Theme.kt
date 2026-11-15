package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val A2ALightColorScheme = lightColorScheme(
    primary = A2APrimary,
    onPrimary = Color.White,
    primaryContainer = A2APrimaryContainer,
    onPrimaryContainer = A2AOnPrimaryContainer,
    secondary = A2ASecondary,
    onSecondary = Color.White,
    secondaryContainer = A2ASecondaryContainer,
    onSecondaryContainer = A2AOnSecondaryContainer,
    tertiary = A2ATertiary,
    onTertiary = Color.White,
    tertiaryContainer = A2ATertiaryContainer,
    background = A2ABackground,
    onBackground = A2ATextPrimary,
    surface = A2ASurface,
    onSurface = A2ATextPrimary,
    surfaceVariant = A2ASurfaceVariant,
    onSurfaceVariant = A2ATextSecondary,
    outline = A2ABorder,
    error = A2ADanger,
    onError = Color.White,
    errorContainer = A2ADangerContainer,
    onErrorContainer = A2AOnDangerContainer
)

val A2AShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun A2ATheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = A2ALightColorScheme,
        typography = A2ATypography,
        shapes = A2AShapes,
        content = content
    )
}
