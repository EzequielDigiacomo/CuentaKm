package com.cuentakm.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealSoft,
    onPrimaryContainer = TealDark,
    secondary = InkSoft,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = SurfaceCream,
    onSurface = Ink,
    surfaceVariant = CreamAccent,
    onSurfaceVariant = InkSoft,
    outline = Line,
    outlineVariant = Line,
    error = Danger,
    onError = Color.White,
    errorContainer = StatusOverdueSoft,
    onErrorContainer = StatusOverdue,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD1C6),
    onPrimary = Color(0xFF00332F),
    primaryContainer = Color(0xFF0B524C),
    onPrimaryContainer = Color(0xFFD7EFEB),
    secondary = DarkInkSoft,
    onSecondary = DarkBg,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkInkSoft,
    outline = DarkLine,
    outlineVariant = DarkLine,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun CuentaKmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Sin dynamic color: la identidad de CuentaKm es fija en web y app.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CuentaKmTypography,
        content = content,
    )
}
