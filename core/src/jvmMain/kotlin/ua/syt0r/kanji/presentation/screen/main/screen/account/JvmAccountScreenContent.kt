package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveStream
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.core.logger.Logger
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
            signOut = { viewModel.signOut() },
            refresh = { viewModel.refresh() }
        )
    }

}

interface JvmAccountScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun signIn()
        fun signOut()
        fun refresh()
    }

    sealed interface ScreenState {
        object SignedOut : ScreenState
        object StartingSever : ScreenState
        data class WaitingForSignIn(val serverPort: Int) : ScreenState
        object LoadingUserData : ScreenState

        data class Loaded(
            val email: String,
            val subscriptionInfo: SubscriptionInfo,
            val issue: ApiRequestIssue?
        ) : ScreenState

        data class Error(
            val issue: ApiRequestIssue
        ) : ScreenState

    }

}

@Composable
fun AccountScreenUI(
    state: State<ScreenState>,
    onUpClick: () -> Unit,
    signIn: () -> Unit,
    signOut: () -> Unit,
    refresh: () -> Unit
) {

    AccountScreenContainer(
        state = state,
        onUpClick = onUpClick
    ) { screenState ->

        when (screenState) {
            ScreenState.SignedOut -> {
                AccountScreenSignedOut(
                    startSignIn = signIn
                )
            }

            ScreenState.StartingSever,
            is ScreenState.WaitingForSignIn,
            ScreenState.LoadingUserData -> {
                AccountScreenLoading()
            }

            is ScreenState.Loaded -> {
                AccountScreenSignedIn(
                    email = screenState.email,
                    subscriptionInfo = screenState.subscriptionInfo,
                    issue = screenState.issue,
                    refresh = refresh,
                    signOut = signOut,
                    signIn = signIn
                )
            }

            is ScreenState.Error -> {
                AccountScreenError(
                    issue = screenState.issue,
                    startSignIn = signIn
                )
            }
        }

    }

}

class JvmAccountScreenViewModel(
    private val coroutineScope: CoroutineScope,
    private val accountManager: AccountManager,
    private val serverCleanupScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : JvmAccountScreenContract.ViewModel {

    @Serializable
    private data class ApiSignInData(
        val refreshToken: String,
        val idToken: String
    )

    private val _state = MutableStateFlow<ScreenState>(ScreenState.LoadingUserData)
    override val state: StateFlow<ScreenState> = _state

    init {

        accountManager.state
            .onEach {
                _state.value = when (it) {
                    AccountState.Loading -> ScreenState.LoadingUserData
                    AccountState.LoggedOut -> ScreenState.SignedOut
                    is AccountState.LoggedIn -> ScreenState.Loaded(
                        email = it.email,
                        subscriptionInfo = it.subscriptionInfo,
                        issue = it.issue
                    )

                    is AccountState.Error -> ScreenState.Error(it.issue)
                }
            }
            .launchIn(coroutineScope)

    }

    override fun signIn() {
        _state.value = ScreenState.StartingSever

        coroutineScope.launch {
            val signInDataResult = receiveSignInData(
                onServerStarted = { _state.value = ScreenState.WaitingForSignIn(it) }
            ).await()
            when {
                signInDataResult.isSuccess -> {
                    val data = signInDataResult.getOrThrow()
                    accountManager.signIn(data.refreshToken, data.idToken)
                }

                else -> {
                    _state.value = ScreenState.Error(
                        ApiRequestIssue.Other(signInDataResult.exceptionOrNull()!!)
                    )
                }
            }

        }
    }

    override fun signOut() {
        accountManager.signOut()
    }

    override fun refresh() {
        accountManager.refreshUserData()
    }

    private fun receiveSignInData(
        onServerStarted: (port: Int) -> Unit
    ): Deferred<Result<ApiSignInData>> {
        val completable = CompletableDeferred<Result<ApiSignInData>>()
        val server = embeddedServer(Netty, port = 0) {
            routing {
                post("/") {
                    runCatching {
                        val data = Json.decodeFromStream<ApiSignInData>(call.receiveStream())

                        call.response.header("Access-Control-Allow-Origin", "*")
                        call.respond(HttpStatusCode.OK)

                        completable.complete(Result.success(data))
                    }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest)
                        completable.complete(Result.failure(it))
                    }
                }
            }
        }

        val deferred = coroutineScope.async {
            Logger.d("Starting auth server")
            server.start()

            val port = server.engine.resolvedConnectors().first().port
            onServerStarted(port)

            completable.await()
        }

        deferred.invokeOnCompletion {
            serverCleanupScope.launch {
                Logger.d("Stopping auth server")
                server.stop()
            }
        }

        return deferred
    }

}