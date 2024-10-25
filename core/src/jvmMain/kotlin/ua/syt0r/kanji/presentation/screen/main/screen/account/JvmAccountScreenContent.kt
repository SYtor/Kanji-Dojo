package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

object JvmAccountScreenContent : AccountScreenContract.Content {

    @Composable
    override fun invoke(state: MainNavigationState) {
        val urlHandler = rememberUrlHandler()
        AccountScreenUI(
            onUpClick = { state.navigateBack() },
            openLoginWebPage = { urlHandler.openInBrowser("https://kanji-dojo.com/account") }
        )
    }

}

@Composable
fun AccountScreenUI(
    onUpClick: () -> Unit,
    openLoginWebPage: () -> Unit
) {

    AccountScreenContainer(
        onUpClick = onUpClick
    ) {

        AccountScreenLoggedOutState(
            openLoginWebPage = openLoginWebPage
        )

    }

}