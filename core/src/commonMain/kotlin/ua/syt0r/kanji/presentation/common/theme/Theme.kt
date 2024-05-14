package ua.syt0r.kanji.presentation.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import ua.syt0r.kanji.core.theme_manager.LocalThemeManager
import ua.syt0r.kanji.presentation.common.resources.string.LocalStrings
import ua.syt0r.kanji.presentation.common.resources.string.getStrings
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.getOrientation

private val LightThemeColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
)

private val DarkThemeColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
)

class ExtraColorsScheme(
    val link: Color,
    val success: Color,
    val pending: Color,
    val outdated: Color
)

val LightExtraColorScheme = ExtraColorsScheme(
    lightThemeLinkColor,
    lightThemeSuccessColor,
    lightThemePendingColor,
    lightOutdatedColor
)

val DarkExtraColorScheme = ExtraColorsScheme(
    darkThemeLinkColor,
    darkThemeSuccessColor,
    darkThemePendingColor,
    darkOutdatedColor
)

val LocalExtraColors = compositionLocalOf { LightExtraColorScheme }

val MaterialTheme.extraColorScheme: ExtraColorsScheme
    @Composable
    get() = LocalExtraColors.current

@Composable
fun isDarkTheme(): Boolean {
    return if (LocalInspectionMode.current) isSystemInDarkTheme()
    else LocalThemeManager.current.isDarkTheme
}

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isDarkTheme(),
    content: @Composable () -> Unit
) {
    val (colors, extraColors) = if (!useDarkTheme) {
        LightThemeColors to LightExtraColorScheme
    } else {
        DarkThemeColors to DarkExtraColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = {
            CompositionLocalProvider(
                LocalExtraColors provides extraColors,
                LocalOrientation provides getOrientation(),
                LocalStrings provides getStrings(),
                LocalTextSelectionColors provides neutralTextSelectionColors()
            ) {
                content()
            }
        }
    )
}

@Composable
private fun neutralTextSelectionColors() = TextSelectionColors(
    handleColor = MaterialTheme.colorScheme.onSurface,
    backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
)

@Composable
fun ButtonDefaults.neutralButtonColors(): ButtonColors {
    return MaterialTheme.colorScheme.run {
        buttonColors(
            containerColor = surfaceVariant,
            contentColor = onSurfaceVariant
        )
    }
}

@Composable
fun ButtonDefaults.neutralTextButtonColors(): ButtonColors {
    return MaterialTheme.colorScheme.run {
        textButtonColors(
            contentColor = onSurfaceVariant
        )
    }
}


@Composable
fun TextFieldDefaults.neutralColors(): TextFieldColors = MaterialTheme.colorScheme.run {
    val labelColor = onSurface.copy(alpha = 0.4f)
    colors(
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        unfocusedLabelColor = labelColor,
        focusedLabelColor = labelColor,
        disabledLabelColor = labelColor,
        cursorColor = onSurface
    )
}
