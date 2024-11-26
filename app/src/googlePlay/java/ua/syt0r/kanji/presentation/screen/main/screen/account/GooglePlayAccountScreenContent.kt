package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
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
            onSignOutClick = {
                viewModel.signOut()
            }
        )

    }

}

interface GooglePlayAccountScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun signIn(data: AccountScreenContract.ScreenData)
        fun signOut()
    }

    interface ScreenState {
        object Loading : ScreenState
        object SignedOut : ScreenState
        object SignedIn : ScreenState
    }

}

@Composable
fun GooglePlayAccountScreenUI(
    state: State<ScreenState>,
    onUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {

    AccountScreenContainer(
        onUpClick = onUpClick
    ) {

        when (val screenState = state.value) {
            ScreenState.SignedOut -> {
                AccountScreenLoggedOutState(
                    openLoginWebPage = onSignInClick
                )
            }

            ScreenState.Loading -> {
                CircularProgressIndicator()
            }

            is ScreenState.SignedIn -> {
                Column {
                    Text("Logged in!")
                    TextButton(onSignOutClick) { Text("Sign out") }
                }
            }
        }

    }

}

class GooglePlayAccountScreenViewModel(
    private val coroutineScope: CoroutineScope,
    private val appPreferences: PreferencesContract.AppPreferences
) : GooglePlayAccountScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        coroutineScope.launch {

            val data = appPreferences.run {
                AccountScreenContract.ScreenData(
                    refreshToken = refreshToken.get() ?: return@run null,
                    idToken = idToken.get() ?: return@run null
                )
            }

            if (data != null)
                _state.value = ScreenState.SignedIn
            else
                _state.value = ScreenState.SignedOut

        }
    }

    override fun signIn(data: AccountScreenContract.ScreenData) {
        _state.value = ScreenState.Loading
        coroutineScope.launch {
            appPreferences.refreshToken.set(data.refreshToken)
            appPreferences.idToken.set(data.idToken)
            _state.value = ScreenState.SignedIn
        }
    }

    override fun signOut() {
        _state.value = ScreenState.SignedOut
    }

}
