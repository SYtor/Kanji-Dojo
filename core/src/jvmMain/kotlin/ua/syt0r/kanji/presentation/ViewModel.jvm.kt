package ua.syt0r.kanji.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.compose.currentKoinScope
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.emptyParametersHolder
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope

@Composable
actual inline fun <reified T> platformGetMultiplatformViewModel(): T {
    /***
     * Using custom coroutine scope instead of remember one since it can leave composition when
     * navigating so view model will have canceled scope after returning to the screen
     */
    return saveableKoinInject<T> { parametersOf(CoroutineScope(Dispatchers.Unconfined)) }
}

actual inline fun <reified T> Module.platformMultiplatformViewModel(
    crossinline scope: Definition<T>
) {
    factory { scope(it) }
}

@Composable
inline fun <reified T> saveableKoinInject(
    qualifier: Qualifier? = null,
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): T {
    // This will always refer to the latest parameters
    val currentParameters by rememberUpdatedState(parameters)

    return rememberSaveable(qualifier, scope) {
        scope.get(qualifier) {
            currentParameters?.invoke() ?: emptyParametersHolder()
        }
    }
}
