package com.sthao.quickform.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A single, static color scheme for the entire app.
private val AppColorScheme =
    lightColorScheme(
        primary = Blue,
        secondary = LightBlue,
        background = Grey,
        surface = White,
        onPrimary = White,
        onSecondary = Black,
        onBackground = Black,
        onSurface = Black,

        // These now correspond to the FAB colors.
        primaryContainer = FabColorSave,
        onPrimaryContainer = Color.White,
        secondaryContainer = FabColorView,
        onSecondaryContainer = Color.White,
        tertiaryContainer = FabColorNew,
        onTertiaryContainer = Color.Black,
        surfaceContainer = SavedForms,
        surfaceContainerHigh = FormsSelected,
        surfaceDim = DarkGray50
    )

// Creating theme extension properties for custom, non-standard colors.
val ColorScheme.pickupForm: Color
    @Composable
    get() = PickupForm

val ColorScheme.dropOffForm: Color
    @Composable
    get() = DropOffForm


@Composable
fun QuickFormTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content,
    )
}