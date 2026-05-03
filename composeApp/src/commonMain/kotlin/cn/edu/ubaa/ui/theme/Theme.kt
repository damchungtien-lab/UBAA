package cn.edu.ubaa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF0D5B63),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD7EFF2),
        onPrimaryContainer = Color(0xFF001F23),
        secondary = Color(0xFF233E53),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD8E7F4),
        onSecondaryContainer = Color(0xFF0B1F2C),
        tertiary = Color(0xFFB36A1C),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDDB5),
        onTertiaryContainer = Color(0xFF2D1700),
        background = Color(0xFFF2F5F6),
        onBackground = Color(0xFF162124),
        surface = Color(0xFFFCFEFE),
        onSurface = Color(0xFF162124),
        surfaceVariant = Color(0xFFE2EAEC),
        onSurfaceVariant = Color(0xFF44545A),
        outline = Color(0xFF76868D),
        error = Color(0xFFBA1A1A),
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF86D5DD),
        onPrimary = Color(0xFF00373D),
        primaryContainer = Color(0xFF024B53),
        onPrimaryContainer = Color(0xFFD7EFF2),
        secondary = Color(0xFFB9CBDB),
        onSecondary = Color(0xFF0E2738),
        secondaryContainer = Color(0xFF294255),
        onSecondaryContainer = Color(0xFFD8E7F4),
        tertiary = Color(0xFFFFC782),
        onTertiary = Color(0xFF4A2800),
        tertiaryContainer = Color(0xFF6A3F00),
        onTertiaryContainer = Color(0xFFFFDDB5),
        background = Color(0xFF0F1518),
        onBackground = Color(0xFFDCE4E6),
        surface = Color(0xFF161D20),
        onSurface = Color(0xFFDCE4E6),
        surfaceVariant = Color(0xFF39474D),
        onSurfaceVariant = Color(0xFFBECBD1),
        outline = Color(0xFF87969D),
        error = Color(0xFFFFB4AB),
    )

private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(18.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(36.dp),
    )

@Composable
fun UBAATheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = getAppTypography(), shapes = AppShapes) {
    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) { content() }
  }
}
