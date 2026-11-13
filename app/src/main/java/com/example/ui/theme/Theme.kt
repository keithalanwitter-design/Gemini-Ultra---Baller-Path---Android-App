package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BallerPathLightScheme = lightColorScheme(
  primary = BurntOrange,
  onPrimary = Color.White,
  primaryContainer = BurntOrangeTint,
  onPrimaryContainer = BurntOrangeDark,
  secondary = NavyDeep,
  onSecondary = Color.White,
  secondaryContainer = NavyTint,
  onSecondaryContainer = NavyDeep,
  tertiary = SuccessGreen,
  onTertiary = Color.White,
  tertiaryContainer = SuccessGreenTint,
  onTertiaryContainer = SuccessGreen,
  background = CourtCream,
  onBackground = InkPrimary,
  surface = CardWhite,
  onSurface = InkPrimary,
  surfaceVariant = CourtWarmSurface,
  onSurfaceVariant = InkSecondary,
  outline = OutlineWarm,
  outlineVariant = OutlineWarm,
  error = ErrorRed,
  onError = Color.White,
  errorContainer = ErrorRedTint,
  onErrorContainer = ErrorRed,
)

private val BallerPathDarkScheme = darkColorScheme(
  primary = BurntOrange,
  onPrimary = Color.White,
  primaryContainer = BurntOrangeDark,
  onPrimaryContainer = Color.White,
  secondary = NavyElevated,
  onSecondary = Color.White,
  secondaryContainer = DarkNavyContainer,
  onSecondaryContainer = DarkInkPrimary,
  tertiary = Color(0xFF4ADE80),
  onTertiary = Color(0xFF042F17),
  tertiaryContainer = Color(0xFF12462B),
  onTertiaryContainer = Color(0xFFD1FAE5),
  background = DarkCourtBackground,
  onBackground = DarkInkPrimary,
  surface = DarkNavySurface,
  onSurface = DarkInkPrimary,
  surfaceVariant = DarkNavyContainer,
  onSurfaceVariant = DarkInkSecondary,
  outline = DarkOutline,
  outlineVariant = DarkOutline,
  error = Color(0xFFFCA5A5),
  onError = Color(0xFF450A0A),
  errorContainer = Color(0xFF7F1D1D),
  onErrorContainer = Color(0xFFFEE2E2),
)

val BallerPathShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun BallerPathTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) BallerPathDarkScheme else BallerPathLightScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = BallerPathTypography,
    shapes = BallerPathShapes,
    content = content,
  )
}
