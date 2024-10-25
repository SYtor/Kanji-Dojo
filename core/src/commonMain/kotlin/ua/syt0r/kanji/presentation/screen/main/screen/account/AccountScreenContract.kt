package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

interface AccountScreenContract {

    interface Content {

        @Composable
        operator fun invoke(state: MainNavigationState)

    }

}