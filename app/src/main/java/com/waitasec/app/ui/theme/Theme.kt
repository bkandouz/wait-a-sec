package com.waitasec.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WaitColorScheme = lightColorScheme(
    primary = SageMid,
    onPrimary = Cream,
    primaryContainer = SagePale,
    onPrimaryContainer = SageDeep,
    secondary = SoftCoral,
    onSecondary = Cream,
    background = Mist,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = SagePale.copy(alpha = 0.45f),
    onSurfaceVariant = SageMid,
    outline = SageSoft,
)

@Composable
fun WaitASecTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WaitColorScheme,
        typography = WaitTypography,
        content = content,
    )
}
