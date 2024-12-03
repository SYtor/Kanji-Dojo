package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.account.GooglePlayAccountScreenContract.ScreenState

object GooglePlayAccountScreenContent : AccountScreenContract.Content {

    @Composable
    override fun invoke(
        state: MainNavigationState,
        data: AccountScreenContract.ScreenData?
    ) {

        val viewModel = getMultiplatformViewModel<GooglePlayAccountScreenContract.ViewModel>()
        val urlHandler = rememberUrlHandler()

        LaunchedEffect(Unit) {
            if (data != null) viewModel.signIn(data)
        }

        GooglePlayAccountScreenUI(
            state = viewModel.state.collectAsState(),
            onUpClick = { state.navigateBack() },
            onSignInClick = { urlHandler.openInBrowser(AccountScreenContract.DEEP_LINK_AUTH_URL) },
            onSignOutClick = { viewModel.signOut() },
            refresh = { viewModel.refresh() }
        )

    }

}

interface GooglePlayAccountScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun signIn(data: AccountScreenContract.ScreenData)
        fun signOut()
        fun refresh()
    }

    interface ScreenState {
        object Loading : ScreenState
        object SignedOut : ScreenState
        data class SignedIn(
            val email: String,
            val subscriptionInfo: SubscriptionInfo
        ) : ScreenState
    }

}

@Composable
fun GooglePlayAccountScreenUI(
    state: State<ScreenState>,
    onUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    refresh: () -> Unit
) {

    AccountScreenContainer(
        state = state,
        onUpClick = onUpClick
    ) { screenState ->

        when (screenState) {
            ScreenState.SignedOut -> {
                AccountScreenSignedOut(
                    openLoginWebPage = onSignInClick
                )
            }

            ScreenState.Loading -> {
                AccountScreenLoading()
            }

            is ScreenState.SignedIn -> {
                AccountScreenSignedIn(
                    email = screenState.email,
                    subscriptionInfo = screenState.subscriptionInfo,
                    refresh = refresh,
                    signOut = onSignOutClick
                )
            }
        }

    }

}

class GooglePlayAccountScreenViewModel(
    coroutineScope: CoroutineScope,
    private val accountManager: AccountManager
) : GooglePlayAccountScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {

        accountManager.state
            .onEach {
                _state.value = when (it) {
                    AccountState.Loading -> ScreenState.Loading
                    AccountState.LoggedOut -> ScreenState.SignedOut
                    is AccountState.LoggedIn -> ScreenState.SignedIn(
                        email = it.email,
                        subscriptionInfo = it.subscriptionInfo
                    )

                    is AccountState.Error -> TODO()
                }
            }
            .launchIn(coroutineScope)

    }

    override fun signIn(data: AccountScreenContract.ScreenData) {
        accountManager.signIn(data.refreshToken, data.idToken)
    }

    override fun signOut() {
        accountManager.signOut()
    }

    override fun refresh() {
        accountManager.refreshUserData()
    }

}
