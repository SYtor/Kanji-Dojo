package ua.syt0r.kanji.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.koin.core.module.Module
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface NetworkClients {

    val unauthenticatedClient: HttpClient

    val authenticatedClient: HttpClient
    fun invalidateTokens()

}

fun Module.addNetworkClientsDefinitions() {

    single<NetworkClients> {
        DefaultNetworkClients(
            appPreferences = get()
        )
    }

}

class DefaultNetworkClients(
    private val appPreferences: PreferencesContract.AppPreferences
) : NetworkClients {

    override val unauthenticatedClient: HttpClient = HttpClient(CIO)
    override val authenticatedClient: HttpClient = HttpClient(CIO) {
        install(Auth) {
            bearer {
                loadTokens(::loadInitialTokens)
                refreshTokens(::tokenRefreshHandler)
            }
        }
    }

    override fun invalidateTokens() {
        authenticatedClient.authProvider<BearerAuthProvider>()!!.clearToken()
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
            val response = unauthenticatedClient.post(TOKEN_REFRESH_URL) {
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
            null
        }

        return newTokens
    }

    private companion object {
        const val PROJECT_KEY = "AIzaSyCP9IzlOBkf9C6VHXBsD7xJr88R-ZOUKsA"
        const val TOKEN_REFRESH_URL = "https://securetoken.googleapis.com/v1/token?key=$PROJECT_KEY"
    }

}
