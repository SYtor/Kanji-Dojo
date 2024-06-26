package ua.syt0r.kanji.presentation.screen.sponsor.use_case

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

interface GooglePlaySendSponsorResultsUseCase {

    suspend operator fun invoke(
        email: String,
        message: String,
        purchasesJson: List<String>
    ): Result<Unit>

}

class DefaultGooglePlaySendSponsorResultsUseCase(
    private val httpClient: HttpClient
) : GooglePlaySendSponsorResultsUseCase {

    override suspend fun invoke(
        email: String,
        message: String,
        purchasesJson: List<String>
    ): Result<Unit> {
        return runCatching {
            val requestBody = JsonObject(
                mapOf(
                    "email" to JsonPrimitive(email),
                    "message" to JsonPrimitive(message),
                    "paymentsJson" to JsonArray(purchasesJson.map { JsonPrimitive(it) })
                )
            )

            val response = httpClient.post(ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }

            if (!response.status.isSuccess())
                throw Throwable(response.status.description)
        }
    }

    companion object {
        private const val ENDPOINT = "https://kanji-dojo.com/api/v1/sponsor"
    }

}
