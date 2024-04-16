package ua.syt0r.kanji.presentation

import androidx.compose.runtime.Composable

@Composable
inline fun <reified T> getMultiplatformViewModel(): T = platformGetMultiplatformViewModel()

@Composable
expect inline fun <reified T> platformGetMultiplatformViewModel(): T