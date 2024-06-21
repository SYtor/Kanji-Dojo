package ua.syt0r.kanji.presentation.screen.main.screen.sponsor

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

interface SponsorScreenContract {

    interface Content {

        @Composable
        operator fun invoke(state: MainNavigationState)

    }

}