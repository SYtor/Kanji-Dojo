package ua.syt0r.kanji.presentation.screen.main.screen.account

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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.account.JvmAccountScreenContract.ScreenState

@Serializable
data class SignInData(
    val refreshToken: String,
    val idToken: String
)

class JvmAccountScreenViewModel(
    private val coroutineScope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val serverCleanupScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : JvmAccountScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.LoadingUserData)
    override val state: StateFlow<ScreenState> = _state

    init {

        coroutineScope.launch {

            val (refreshToken, idToken) = userPreferencesRepository.run {
                refreshToken.get() to idToken.get()
            }

            if (refreshToken != null && idToken != null) {
                _state.value = ScreenState.Loaded
            } else {
                _state.value = ScreenState.SignedOut
            }

        }

    }

    override fun signIn() {
        _state.value = ScreenState.StartingSever

        coroutineScope.launch {
            val signInDataResult = startSingInFlow().await()
            when {
                signInDataResult.isSuccess -> {
                    val data = signInDataResult.getOrThrow()
                    userPreferencesRepository.apply {
                        refreshToken.set(data.refreshToken)
                        idToken.set(data.refreshToken)
                    }
                    _state.value = ScreenState.LoadingUserData
                    _state.value = ScreenState.Loaded
                }

                else -> {
                    TODO()
                }
            }

        }
    }

    override fun signOut() {
        _state.value = ScreenState.LoadingUserData
        coroutineScope.launch {
            userPreferencesRepository.run {
                refreshToken.set(null)
                idToken.set(null)
            }
            _state.value = ScreenState.SignedOut
        }
    }

    private fun startSingInFlow(): Deferred<Result<SignInData>> {
        val completable = CompletableDeferred<Result<SignInData>>()
        val server = embeddedServer(Netty, port = 0) {
            routing {
                post("/") {
                    runCatching {
                        val data = Json.decodeFromStream<SignInData>(call.receiveStream())

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
            _state.value = ScreenState.WaitingForSignIn(port)

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