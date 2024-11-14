package ua.syt0r.kanji.core.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface AuthManager {
    val state: StateFlow<AuthState>
    val httpClient: HttpClient
}

sealed interface AuthState {
    object Loading : AuthState
    object SignedOut : AuthState
    object Expired : AuthState
    object SignedIn : AuthState
}

class DefaultAuthManager(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val unauthorisedClient: HttpClient,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
) : AuthManager {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    override val state: StateFlow<AuthState> = _state

    override val httpClient: HttpClient

    init {

        httpClient = HttpClient(CIO) {
            install(Auth) {
                bearer {
                    loadTokens(::loadInitialTokens)
                    refreshTokens(::tokenRefreshHandler)
                }
            }
        }

        coroutineScope.launch {
            _state.value = when {
                loadInitialTokens() == null -> AuthState.SignedOut
                else -> AuthState.SignedIn
            }
        }

    }

    private suspend fun loadInitialTokens(): BearerTokens? {
        val (refreshToken, idToken) = appPreferences.run {
            refreshToken.get() to idToken.get()
        }
        return if (refreshToken != null && idToken != null) {
            BearerTokens(accessToken = idToken, refreshToken = refreshToken)
        } else {
            null
        }
    }

    private suspend fun tokenRefreshHandler(params: RefreshTokensParams): BearerTokens? {
        Logger.d("Session expired, refreshing token")

        val refreshToken = appPreferences.refreshToken.get()
        if (refreshToken == null) {
            Logger.d("No refresh token found")
            return null
        }

        val newTokens = runCatching {
            val response = unauthorisedClient.post(NetworkApi.Url.REFRESH_AUTH_TOKEN) {
                val payload = buildJsonObject {
                    put("grant_type", "refresh_token")
                    put("refresh_token", refreshToken)
                }
                val payloadJson = Json.encodeToString(payload)
                Logger.d("payload[$payloadJson]")
                setBody(payloadJson)
            }

            Logger.d("response status[${response.status}]")
            val body = Json.decodeFromString<JsonObject>(response.bodyAsText())
            val idToken = body["id_token"]!!.jsonPrimitive.content
            Logger.d("Received new id token")

            appPreferences.idToken.set(idToken)
            BearerTokens(
                accessToken = idToken,
                refreshToken = refreshToken
            )
        }.getOrElse {
            Logger.d("Id Token refreshing error [$it]")
            _state.value = AuthState.Expired
            null
        }

        return newTokens
    }

}
