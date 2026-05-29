package com.sthao.quickform.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    surfaceDim = DarkGray50
)

// Creating theme extension properties for custom, non-standard colors.
val ColorScheme.pickupForm: Color
    @Composable
    get() = if (isSystemInDarkTheme()) PickupForm.copy(alpha = 0.2f) else PickupForm

val ColorScheme.dropOffForm: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DropOffForm.copy(alpha = 0.2f) else DropOffForm

val ColorScheme.stationsForm: Color
    @Composable
    get() = if (isSystemInDarkTheme()) StationsForm.copy(alpha = 0.2f) else StationsForm


@Composable
fun QuickFormTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
