package ua.syt0r.kanji.presentation.common.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

enum class Orientation {
    Portrait,
    Landscape
}

val LocalOrientation: ProvidableCompositionLocal<Orientation> = compositionLocalOf {
    Orientation.Portrait
}