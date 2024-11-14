package ua.syt0r.kanji.core.auth

import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.qualifier
import org.koin.core.scope.Scope


private val AuthHttpClientQualifier = qualifier("auth_http_client")

fun Module.addAuthDefinitions() {

    single<AuthManager> {
        DefaultAuthManager(
            appPreferences = get(),
            unauthorisedClient = get()
        )
    }

    single<HttpClient>(AuthHttpClientQualifier) { get<AuthManager>().httpClient }

}

fun Scope.authHttpClient(): HttpClient {
    return get<HttpClient>(AuthHttpClientQualifier)
}
