package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration

private const val DownloadsUrl = "https://kanji-dojo.com/"
private const val YoutubeChannelUrl = "https://youtube.com/@kanji-dojo"
private const val DiscordInviteUrl = "https://discord.gg/2Ny6h6pXTY"

@Composable
fun GeneralDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: GeneralDashboardScreenContract.ViewModel = getMultiplatformViewModel()
) {

    val urlHandler = rememberUrlHandler()

    GeneralDashboardScreenUI(
        state = viewModel.state.collectAsState(),
        navigateToDailyLimitConfiguration = {
            mainNavigationState.navigate(MainDestination.DailyLimit)
        },
        navigateToCreateLetterDeck = {
            val destination = MainDestination.DeckPicker(DeckPickerScreenConfiguration.Letters)
            mainNavigationState.navigate(destination)
        },
        navigateToCreateVocabDeck = {
            val destination = MainDestination.DeckPicker(DeckPickerScreenConfiguration.Vocab)
            mainNavigationState.navigate(destination)
        },
        navigateToLetterPractice = { mainNavigationState.navigate(it) },
        navigateToVocabPractice = { mainNavigationState.navigate(it) },
        downloadsClick = { urlHandler.openInBrowser(DownloadsUrl) },
        youtubeClick = { urlHandler.openInBrowser(YoutubeChannelUrl) },
        discordClick = { urlHandler.openInBrowser(DiscordInviteUrl) }
    )

}
