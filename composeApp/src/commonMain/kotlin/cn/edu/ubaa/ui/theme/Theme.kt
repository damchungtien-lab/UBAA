package cn.edu.ubaa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val PrimaryBlue = Color(0xFF1A56DB)
private val PrimaryBlueDark = Color(0xFF3B82F6)
private val SecondaryTeal = Color(0xFF0EA5E9)
private val SecondaryTealDark = Color(0xFF38BDF8)
private val SurfaceLight = Color(0xFFF8FAFC)
private val SurfaceDark = Color(0xFF0F172A)
private val BackgroundLight = Color(0xFFFFFFFF)
private val BackgroundDark = Color(0xFF020617)
private val ErrorRed = Color(0xFFDC2626)
private val SuccessGreen = Color(0xFF16A34A)
private val WarningAmber = Color(0xFFF59E0B)

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryBlue,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDBEAFE),
        onPrimaryContainer = Color(0xFF1E3A5F),
        secondary = SecondaryTeal,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF0C4A6E),
        tertiary = Color(0xFF7C3AED),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFEDE9FE),
        onTertiaryContainer = Color(0xFF4C1D95),
        error = ErrorRed,
        onError = Color.White,
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF7F1D1D),
        background = BackgroundLight,
        onBackground = Color(0xFF0F172A),
        surface = SurfaceLight,
        onSurface = Color(0xFF1E293B),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFCBD5E1),
        outlineVariant = Color(0xFFE2E8F0),
        inverseSurface = Color(0xFF1E293B),
        inverseOnSurface = Color(0xFFF8FAFC),
        inversePrimary = Color(0xFF93C5FD),
        surfaceTint = PrimaryBlue,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryBlueDark,
        onPrimary = Color(0xFF1E3A5F),
        primaryContainer = Color(0xFF1E3A5F),
        onPrimaryContainer = Color(0xFFDBEAFE),
        secondary = SecondaryTealDark,
        onSecondary = Color(0xFF0C4A6E),
        secondaryContainer = Color(0xFF0C4A6E),
        onSecondaryContainer = Color(0xFFE0F2FE),
        tertiary = Color(0xFFA78BFA),
        onTertiary = Color(0xFF4C1D95),
        tertiaryContainer = Color(0xFF4C1D95),
        onTertiaryContainer = Color(0xFFEDE9FE),
        error = Color(0xFFFCA5A5),
        onError = Color(0xFF7F1D1D),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFEE2E2),
        background = BackgroundDark,
        onBackground = Color(0xFFF1F5F9),
        surface = SurfaceDark,
        onSurface = Color(0xFFCBD5E1),
        surfaceVariant = Color(0xFF1E293B),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = Color(0xFF475569),
        outlineVariant = Color(0xFF334155),
        inverseSurface = Color(0xFFF1F5F9),
        inverseOnSurface = Color(0xFF020617),
        inversePrimary = Color(0xFF1A56DB),
        surfaceTint = PrimaryBlueDark,
    )

@Composable
fun UBAATheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = getAppTypography()) {
    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) { content() }
  }
}
