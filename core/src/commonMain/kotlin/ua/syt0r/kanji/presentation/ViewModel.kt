package ua.syt0r.kanji.presentation

import androidx.compose.runtime.Composable
import org.koin.core.definition.Definition
import org.koin.core.module.Module

inline fun <reified T> Module.multiplatformViewModel(
    crossinline scope: Definition<T>
) = platformMultiplatformViewModel(scope)

expect inline fun <reified T> Module.platformMultiplatformViewModel(
    crossinline scope: Definition<T>
)

@Composable
inline fun <reified T> getMultiplatformViewModel(): T = platformGetMultiplatformViewModel()

@Composable
expect inline fun <reified T> platformGetMultiplatformViewModel(): T