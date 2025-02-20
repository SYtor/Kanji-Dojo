package ua.syt0r.kanji.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.definition.Definition
import org.koin.core.module.Module

inline fun <reified T> Module.multiplatformViewModel(
    crossinline scope: Definition<T>
) = platformMultiplatformViewModel(scope)

expect inline fun <reified T> Module.platformMultiplatformViewModel(
    crossinline scope: Definition<T>
)

enum class LifecycleState { Visible, Hidden }

interface LifecycleAwareViewModel {
    val lifecycleState: MutableStateFlow<LifecycleState>
}

@Composable
inline fun <reified T> getMultiplatformViewModel(vararg args: Any): T {
    val viewModel: T = platformGetMultiplatformViewModel(args)

    if (viewModel is LifecycleAwareViewModel) {
        DisposableEffect(Unit) {
            viewModel.lifecycleState.value = LifecycleState.Visible
            onDispose {
                viewModel.lifecycleState.value = LifecycleState.Hidden
            }
        }
    }

    return viewModel
}

@Composable
expect inline fun <reified T> platformGetMultiplatformViewModel(args: Array<out Any>): T
