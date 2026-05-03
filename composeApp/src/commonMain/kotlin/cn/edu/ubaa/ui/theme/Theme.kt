package cn.edu.ubaa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF006B5F),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFC7F1E8),
        onPrimaryContainer = Color(0xFF05201C),
        secondary = Color(0xFF405A7A),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD8E5F8),
        onSecondaryContainer = Color(0xFF0D1D32),
        tertiary = Color(0xFF8A5A00),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDDA6),
        onTertiaryContainer = Color(0xFF2B1700),
        background = Color(0xFFF7F9FB),
        onBackground = Color(0xFF18201F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF18201F),
        surfaceVariant = Color(0xFFE8EEEF),
        onSurfaceVariant = Color(0xFF44504F),
        outline = Color(0xFF758180),
        error = Color(0xFFBA1A1A),
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF7BD7C7),
        onPrimary = Color(0xFF003731),
        primaryContainer = Color(0xFF005047),
        onPrimaryContainer = Color(0xFFC7F1E8),
        secondary = Color(0xFFB8C8E0),
        onSecondary = Color(0xFF102A49),
        secondaryContainer = Color(0xFF29425F),
        onSecondaryContainer = Color(0xFFD8E5F8),
        tertiary = Color(0xFFFFC86D),
        onTertiary = Color(0xFF492900),
        tertiaryContainer = Color(0xFF684000),
        onTertiaryContainer = Color(0xFFFFDDA6),
        background = Color(0xFF101414),
        onBackground = Color(0xFFE0E3E2),
        surface = Color(0xFF171B1B),
        onSurface = Color(0xFFE0E3E2),
        surfaceVariant = Color(0xFF3F4948),
        onSurfaceVariant = Color(0xFFBFC9C7),
        outline = Color(0xFF899391),
        error = Color(0xFFFFB4AB),
    )

private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(8.dp),
        extraLarge = RoundedCornerShape(8.dp),
    )

@Composable
fun UBAATheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val colorScheme =
      when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
      }

  MaterialTheme(colorScheme = colorScheme, typography = getAppTypography(), shapes = AppShapes) {
    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) { content() }
  }
}
