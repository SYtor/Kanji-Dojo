package ua.syt0r.kanji.presentation

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.compose.koinInject
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf

@Composable
actual inline fun <reified T> platformGetMultiplatformViewModel(): T {
    /***
     * Using custom coroutine scope instead of remember one since it can leave composition when
     * navigating so view model will have canceled scope after returning to the screen
     */
    return koinInject<T> { parametersOf(CoroutineScope(Dispatchers.Unconfined)) }
}

actual inline fun <reified T> Module.platformMultiplatformViewModel(
    crossinline scope: Definition<T>
) {
    factory { scope(it) }
}