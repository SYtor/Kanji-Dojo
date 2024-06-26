package ua.syt0r.kanji.presentation.screen.sponsor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import ua.syt0r.kanji.presentation.common.asActivity
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract

object GooglePlaySponsorScreenContent : SponsorScreenContract.Content {

    @Composable
    override fun invoke(
        state: MainNavigationState
    ) {

        val viewModel: GooglePlaySponsorScreenContract.ViewModel = getMultiplatformViewModel()
        val activity = LocalContext.current.asActivity()!!

        LaunchedEffect(Unit) {
            viewModel.reportScreenShown()
        }

        GooglePlaySponsorScreenUI(
            state = viewModel.state.collectAsState(),
            onUpClick = { state.navigateBack() },
            fillDetails = { viewModel.loadInputState() },
            startPurchase = { viewModel.startPurchase(activity) },
            retry = { viewModel.retry(activity) }
        )

    }

}
