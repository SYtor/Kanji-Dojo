package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.account.JvmAccountScreenContract.ScreenState

object JvmAccountScreenContent : AccountScreenContract.Content {

    @Composable
    override fun invoke(
        state: MainNavigationState,
        data: AccountScreenContract.ScreenData?
    ) {

        val viewModel = getMultiplatformViewModel<JvmAccountScreenContract.ViewModel>()
        val urlHandler = rememberUrlHandler()

        LaunchedEffect(Unit) {
            viewModel.state.filterIsInstance<ScreenState.WaitingForSignIn>()
                .onEach {
                    urlHandler.openInBrowser(
                        url = AccountScreenContract.serverAuthUrl(it.serverPort)
                    )
                }
                .collect()
        }

        AccountScreenUI(
            state = viewModel.state.collectAsState(),
            onUpClick = { state.navigateBack() },
            signIn = { viewModel.signIn() },
            signOut = { viewModel.signOut() }
        )
    }

}

@Composable
fun AccountScreenUI(
    state: State<ScreenState>,
    onUpClick: () -> Unit,
    signIn: () -> Unit,
    signOut: () -> Unit
) {

    AccountScreenContainer(
        onUpClick = onUpClick
    ) {

        when (val screenState = state.value) {
            ScreenState.SignedOut -> {
                AccountScreenLoggedOutState(
                    openLoginWebPage = signIn
                )
            }

            ScreenState.StartingSever,
            is ScreenState.WaitingForSignIn,
            ScreenState.LoadingUserData -> {
                CircularProgressIndicator()
            }

            is ScreenState.Loaded -> {
                Column {
                    Text("Logged in!")
                    TextButton(signOut) { Text("Sign out") }
                }
            }
        }

    }

}