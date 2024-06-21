package ua.syt0r.kanji.presentation.screen.sponsor

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenUI

object GooglePlaySponsorScreenContent : SponsorScreenContract.Content {

    @Composable
    override fun invoke(state: MainNavigationState) {
        SponsorScreenUI(
            onUpClick = { state.navigateBack() }
        ) {

        }
    }

}
