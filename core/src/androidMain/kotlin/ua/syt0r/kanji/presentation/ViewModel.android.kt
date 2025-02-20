package ua.syt0r.kanji.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

/***
 * Wraps screen's view model interface with android's view model to survive configuration changes
 * Allows to use viewModel's coroutine scope
 */
@Stable
class AndroidViewModelWrapper<T>(
    provider: (coroutineScope: CoroutineScope) -> T
) : ViewModel() {
    val viewModel = provider(viewModelScope)
}

actual inline fun <reified T> Module.platformMultiplatformViewModel(
    crossinline scope: Definition<T>
) {

    factory { scope(it) }

    viewModel<AndroidViewModelWrapper<T>>(
        qualifier = named<T>()
    ) { params ->
        AndroidViewModelWrapper { coroutineScope ->
            get { parametersOf(coroutineScope, *params.values.toTypedArray()) }
        }
    }

}

@Composable
actual inline fun <reified T> platformGetMultiplatformViewModel(args: Array<out Any>): T {
    return koinViewModel<AndroidViewModelWrapper<T>>(
        qualifier = named<T>(),
        parameters = { parametersOf(*args) }
    ).viewModel
}
