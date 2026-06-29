package com.sthao.quickform.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Custom color set for QuickForm-specific UI elements.
 */
@Immutable
data class QuickFormExtendedColors(
    val pickupForm: Color = Color.Unspecified,
    val dropOffForm: Color = Color.Unspecified,
    val stationsForm: Color = Color.Unspecified,
)

/**
 * CompositionLocal to provide custom colors throughout the hierarchy.
 */
val LocalQuickFormExtendedColors = staticCompositionLocalOf { QuickFormExtendedColors() }

/**
 * Accessor to retrieve the custom colors from the current theme.
 */
@Suppress("UnusedReceiverParameter")
val MaterialTheme.extendedColors: QuickFormExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalQuickFormExtendedColors.current

private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    secondary = LightBlue,
    tertiary = FabColorNew,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    secondary = LightBlue,
    tertiary = FabColorNew,
    background = Grey,
    surface = White,
    onPrimary = White,
    onSecondary = Black,
    onTertiary = Color.White,
    onBackground = Black,
    onSurface = Black,

    // These now correspond to the FAB colors.
    primaryContainer = FabColorSave,
    onPrimaryContainer = Color.White,
    secondaryContainer = FabColorView,
    onSecondaryContainer = Color.White,
    tertiaryContainer = FabColorNew,
    onTertiaryContainer = Color.White,
    surfaceContainer = SavedForms,
    surfaceContainerHigh = FormsSelected,
    surfaceDim = DarkGray50,
)

@Composable
fun QuickFormTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Determine custom colors based on the current theme mode.
    val extendedColors = QuickFormExtendedColors(
        pickupForm = if (darkTheme) PickupForm.copy(alpha = 0.2f) else PickupForm,
        dropOffForm = if (darkTheme) DropOffForm.copy(alpha = 0.2f) else DropOffForm,
        stationsForm = if (darkTheme) StationsForm.copy(alpha = 0.2f) else StationsForm,
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Note: window.statusBarColor is deprecated and ignored when enableEdgeToEdge is used.
            // We only need to control whether the status bar icons are light or dark.
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalQuickFormExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
