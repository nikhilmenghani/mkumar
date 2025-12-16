package com.mkumar.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mkumar.data.ThemePreference

// ---------- Brand palette (static) ----------
private object Brand {
    // Primary (brand blue)
    val Primary40 = Color(0xFF2563EB) // tone 40-ish
    val Primary90 = Color(0xFFDFE7FF) // container / fixed
    val Primary95 = Color(0xFFF2F5FF) // highest container
    val Primary20 = Color(0xFF0B2E73) // onPrimaryContainer (dark)
    val Primary10 = Color(0xFF001B3D) // deepest

    // Secondary (slate)
    val Secondary40 = Color(0xFF64748B)
    val Secondary90 = Color(0xFFDEE5EF)
    val Secondary95 = Color(0xFFF1F4F8)
    val Secondary20 = Color(0xFF233044)
    val Secondary10 = Color(0xFF121A28)

    // Tertiary (emerald)
    val Tertiary40 = Color(0xFF2CB67D)
    val Tertiary90 = Color(0xFFCBEFDB)
    val Tertiary95 = Color(0xFFEAF9F1)
    val Tertiary20 = Color(0xFF0F4A34)
    val Tertiary10 = Color(0xFF08261B)

    // Neutral / Neutral Variant for surfaces & variants
    val Neutral98 = Color(0xFFFAFAFC) // surfaceBright
    val Neutral95 = Color(0xFFF4F5F7)
    val Neutral99 = Color(0xFFFCFCFF)
    val Neutral10 = Color(0xFF121316) // surfaceDim (dark base)
    val Neutral6  = Color(0xFF0C0D10)

    val NeutralVariant90 = Color(0xFFE6E8ED) // surfaceVariant (light)
    val NeutralVariant30 = Color(0xFF464A52) // surfaceVariant (dark)

    // Outline / errors (Material defaults-ish)
    val Outline = Color(0xFF76777F)
    val OutlineVariantLight = Color(0xFFCACDD5)
    val OutlineVariantDark  = Color(0xFF3B3E45)

    val Error = Color(0xFFBA1A1A)
    val OnErrorLight = Color.White
    val OnErrorDark  = Color(0xFFFFDAD6)
    val ErrorContainerLight = Color(0xFFFFDAD6)
    val ErrorContainerDark  = Color(0xFF93000A)

    // Scrim
    val Scrim = Color(0xFF000000)
}

// ---------- Static Light ColorScheme (all fields) ----------
private val StaticLightColorScheme = lightColorScheme(
    primary = Brand.Primary40,
    onPrimary = Color.White,
    primaryContainer = Brand.Primary90,
    onPrimaryContainer = Brand.Primary10,
    inversePrimary = Color(0xFFB3C5FF), // tone ~80

    secondary = Brand.Secondary40,
    onSecondary = Color.White,
    secondaryContainer = Brand.Secondary90,
    onSecondaryContainer = Brand.Secondary10,

    tertiary = Brand.Tertiary40,
    onTertiary = Color.White,
    tertiaryContainer = Brand.Tertiary90,
    onTertiaryContainer = Brand.Tertiary10,

    background = Brand.Neutral99,
    onBackground = Color(0xFF111318),

    surface = Color.White,
    onSurface = Color(0xFF111318),

    surfaceVariant = Brand.NeutralVariant90,
    onSurfaceVariant = Color(0xFF3F4249),

    surfaceTint = Brand.Primary40,

    inverseSurface = Color(0xFF1E1F23),
    inverseOnSurface = Color(0xFFE2E2E6),

    error = Brand.Error,
    onError = Brand.OnErrorLight,
    errorContainer = Brand.ErrorContainerLight,
    onErrorContainer = Color(0xFF410002),

    outline = Brand.Outline,
    outlineVariant = Brand.OutlineVariantLight,
    scrim = Brand.Scrim,

    // Newer M3 tokens
    surfaceBright = Brand.Neutral98,
    surfaceDim = Brand.Neutral95,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Brand.Neutral98,
    surfaceContainer = Brand.Neutral95,
    surfaceContainerHigh = Color(0xFFEDEEF2),
    surfaceContainerHighest = Color(0xFFE4E6EB),

    // Fixed palettes (helpful for large surfaces & dynamic-like accents)
    primaryFixed = Brand.Primary95,
    primaryFixedDim = Brand.Primary90,
    onPrimaryFixed = Brand.Primary10,
    onPrimaryFixedVariant = Brand.Primary20,

    secondaryFixed = Brand.Secondary95,
    secondaryFixedDim = Brand.Secondary90,
    onSecondaryFixed = Brand.Secondary10,
    onSecondaryFixedVariant = Brand.Secondary20,

    tertiaryFixed = Brand.Tertiary95,
    tertiaryFixedDim = Brand.Tertiary90,
    onTertiaryFixed = Brand.Tertiary10,
    onTertiaryFixedVariant = Brand.Tertiary20,
)

// ---------- Static Dark ColorScheme (all fields) ----------
private val StaticDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB3C5FF), // tone ~80
    onPrimary = Color(0xFF0C2B66),
    primaryContainer = Color(0xFF264688),
    onPrimaryContainer = Brand.Primary95,
    inversePrimary = Brand.Primary40,

    secondary = Color(0xFFBAC7D8),
    onSecondary = Color(0xFF223145),
    secondaryContainer = Color(0xFF3A4A61),
    onSecondaryContainer = Brand.Secondary95,

    tertiary = Color(0xFFAFE4C9),
    onTertiary = Color(0xFF113F2B),
    tertiaryContainer = Color(0xFF1F6446),
    onTertiaryContainer = Brand.Tertiary95,

    background = Brand.Neutral10,
    onBackground = Color(0xFFE0E2E7),

    surface = Brand.Neutral10,
    onSurface = Color(0xFFE0E2E7),

    surfaceVariant = Brand.NeutralVariant30,
    onSurfaceVariant = Color(0xFFC2C6CF),

    surfaceTint = Color(0xFFB3C5FF),

    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF1B1C20),

    error = Brand.Error,
    onError = Brand.OnErrorDark,
    errorContainer = Brand.ErrorContainerDark,
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF8D9099),
    outlineVariant = Brand.OutlineVariantDark,
    scrim = Brand.Scrim,

    surfaceBright = Color(0xFF26272B),
    surfaceDim = Brand.Neutral6,
    surfaceContainerLowest = Color(0xFF0A0B0D),
    surfaceContainerLow = Color(0xFF141518),
    surfaceContainer = Color(0xFF181A1D),
    surfaceContainerHigh = Color(0xFF1E2024),
    surfaceContainerHighest = Color(0xFF24262A),

    primaryFixed = Brand.Primary95,
    primaryFixedDim = Brand.Primary90,
    onPrimaryFixed = Brand.Primary10,
    onPrimaryFixedVariant = Brand.Primary20,

    secondaryFixed = Brand.Secondary95,
    secondaryFixedDim = Brand.Secondary90,
    onSecondaryFixed = Brand.Secondary10,
    onSecondaryFixedVariant = Brand.Secondary20,

    tertiaryFixed = Brand.Tertiary95,
    tertiaryFixedDim = Brand.Tertiary90,
    onTertiaryFixed = Brand.Tertiary10,
    onTertiaryFixedVariant = Brand.Tertiary20,
)

// utility preserved from your code
fun Color.applyOpacity(enabled: Boolean): Color =
    if (enabled) this else this.copy(alpha = 0.62f)

// ---------- Theme selection ----------
@Composable
fun NikTheme(content: @Composable () -> Unit) {

    val manager = LocalPreferencesManager.current
    val context = LocalContext.current

    val useDynamicColor = manager.displayPrefs.useDynamicColor

    val darkTheme: Boolean =
        if (manager.displayPrefs.theme == ThemePreference.SYSTEM.ordinal) {
            isSystemInDarkTheme()
        } else {
            manager.displayPrefs.theme == ThemePreference.DARK.ordinal
        }

    val colorScheme = when {
        useDynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> StaticDarkColorScheme
        else -> StaticLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun NikThemePreview(useDynamicColor: Boolean = false, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        useDynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StaticDarkColorScheme
        else -> StaticLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}