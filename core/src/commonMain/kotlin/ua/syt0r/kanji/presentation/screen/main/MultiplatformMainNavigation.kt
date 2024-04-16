package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder


interface MultiplatformMainNavigationState : MainNavigationState {
    val currentDestination: State<MainDestination>
    val stateHolder: SaveableStateHolder
}

@Composable
fun MultiplatformMainNavigation(
    state: MainNavigationState
) {
    state as MultiplatformMainNavigationState

    val destination = state.currentDestination.value

    state.stateHolder.SaveableStateProvider(destination.toString()) {
        destination.Draw(state)
    }

}

@Composable
fun rememberMultiplatformMainNavigationState(): MainNavigationState {
    val stack = remember { mutableStateOf<List<MainDestination>>(listOf(MainDestination.Home)) }
    val currentDestinationState = remember { derivedStateOf { stack.value.last() } }

    val stateHolder = rememberSaveableStateHolder()

    return remember {
        object : MultiplatformMainNavigationState {

            override val currentDestination = currentDestinationState
            override val stateHolder: SaveableStateHolder = stateHolder

            override fun navigateBack() {
                val lastItem = stack.value.last()
                stack.value = stack.value.dropLast(1)
                stateHolder.removeState(lastItem.toString())
            }

            override fun popUpToHome() {
                val itemsToRemove = stack.value.drop(1)
                stack.value = stack.value.take(1)
                itemsToRemove.forEach { stateHolder.removeState(it) }
            }

            override fun navigate(destination: MainDestination) {
                stack.value = stack.value.plus(destination)
            }

        }
    }
}