package com.example.tugasto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Light Mode ────────────────────────────────────────────────────────────────
// Fondo blanco-frio, azul TuGasto como primario, verde esmeralda como secundario
private val LightColorScheme = lightColorScheme(
    // Primario: azul TuGasto — confianza, finanzas
    primary             = TuGastoBlue,           // #2563EB
    onPrimary           = Color.White,
    primaryContainer    = TuGastoBlueExtraLight,  // #EFF6FF — badges info
    onPrimaryContainer  = Color(0xFF1E3A8A),      // azul oscuro sobre fondo claro

    // Secundario: verde — dinero, crecimiento
    secondary            = Color(0xFF059669),     // Emerald 600 — más sobrio que #22C55E
    onSecondary          = Color.White,
    secondaryContainer   = TuGastoGreenLight,    // #DCFCE7
    onSecondaryContainer = TuGastoGreenDark,     // #15803D

    // Terciario: violeta — acento para categorías
    tertiary             = Color(0xFF7C3AED),
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFEDE9FE),
    onTertiaryContainer  = Color(0xFF4C1D95),

    // Error
    error                = TuGastoRed,
    onError              = Color.White,
    errorContainer       = TuGastoRedLight,
    onErrorContainer     = Color(0xFF7F1D1D),

    // Fondos y superficies
    background           = Color(0xFFF8FAFC),    // gris-azulado muy suave — no tan plano como blanco puro
    onBackground         = TuGastoGray900,       // #111827
    surface              = Color.White,
    onSurface            = TuGastoGray900,
    surfaceVariant       = Color(0xFFF1F5F9),    // fondos de chips, inputs
    onSurfaceVariant     = Color(0xFF475569),    // texto secundario en cards

    // Bordes
    outline              = Color(0xFFCBD5E1),    // divisores visibles
    outlineVariant       = Color(0xFFE2E8F0),    // divisores sutiles

    // Inversos (snackbars, tooltips)
    inverseSurface       = Color(0xFF1E293B),
    inverseOnSurface     = Color(0xFFF1F5F9),
    inversePrimary       = TuGastoBlueLight,

    surfaceTint          = TuGastoBlue,
    scrim                = Color.Black
)

// ── Dark Mode ─────────────────────────────────────────────────────────────────
// Fondo navy profundo — no gris genérico; se siente premium y financiero
private val DarkColorScheme = darkColorScheme(
    // Primario: azul claro — legible sobre navy oscuro
    primary             = Color(0xFF60A5FA),     // Blue 400
    onPrimary           = Color(0xFF1E3A8A),
    primaryContainer    = Color(0xFF1D4ED8),     // azul medio para badges
    onPrimaryContainer  = Color(0xFFBFDBFE),     // texto azul pastel

    // Secundario: esmeralda brillante
    secondary            = Color(0xFF34D399),    // Emerald 400
    onSecondary          = Color(0xFF064E3B),
    secondaryContainer   = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFA7F3D0),

    // Terciario: violeta claro
    tertiary             = Color(0xFFA78BFA),    // Violet 400
    onTertiary           = Color(0xFF4C1D95),
    tertiaryContainer    = Color(0xFF5B21B6),
    onTertiaryContainer  = Color(0xFFEDE9FE),

    // Error
    error                = Color(0xFFF87171),    // Red 400 — más suave en oscuro
    onError              = Color(0xFF7F1D1D),
    errorContainer       = Color(0xFF7F1D1D),
    onErrorContainer     = Color(0xFFFECACA),

    // Fondos y superficies: navy profundo
    background           = TuGastoNavyDeep,      // #0B1120
    onBackground         = Color(0xFFE2E8F0),    // gris azulado claro
    surface              = TuGastoNavySurface,   // #131D30 — tarjetas
    onSurface            = Color(0xFFE2E8F0),
    surfaceVariant       = TuGastoNavyVariant,   // #1E2D4A — inputs, chips
    onSurfaceVariant     = Color(0xFF94A3B8),    // texto secundario (Slate 400)

    // Bordes
    outline              = TuGastoNavyOutline,   // #334155
    outlineVariant       = Color(0xFF1E293B),    // divisores muy sutiles

    // Inversos
    inverseSurface       = Color(0xFFE2E8F0),
    inverseOnSurface     = Color(0xFF0F172A),
    inversePrimary       = TuGastoBlue,

    surfaceTint          = Color(0xFF60A5FA),
    scrim                = Color.Black
)

@Composable
fun TuGastoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
