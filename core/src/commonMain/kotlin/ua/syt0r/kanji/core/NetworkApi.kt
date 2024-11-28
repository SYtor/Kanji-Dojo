package ua.syt0r.kanji.core

import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ua.syt0r.kanji.core.sync.SyncDataInfo

interface NetworkApi {

    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun getBackupInfo(): Result<SyncDataInfo>
    suspend fun getBackup(): Result<ByteReadChannel>
    suspend fun updateBackup(info: SyncDataInfo, file: ChannelProvider): Result<Unit>

    suspend fun postFeedback(data: FeedbackApiData): Result<Unit>
    suspend fun postDonationPurchase(data: DonationPurchaseApiData): Result<Unit>

}

data class HttpResponseException(
    val statusCode: HttpStatusCode
) : Throwable()

data class UserInfo(
    val email: String,
    val subscription: Boolean,
    val subscriptionDue: LocalDate?
)

data class FeedbackApiData(
    val topic: String,
    val message: String,
    val userData: JsonObject
)

data class DonationPurchaseApiData(
    val email: String,
    val message: String,
    val purchasesJson: List<String>
)

class DefaultNetworkApi(
    private val networkClients: NetworkClients,
    private val jsonHandler: Json
) : NetworkApi {

    override suspend fun getUserInfo(): Result<UserInfo> {
        return runCatching {
            val response = networkClients.authenticatedClient.get(GET_USER_INFO_URL)
            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
            val json = response.bodyAsText()
            jsonHandler.decodeFromString(json)
        }
    }

    override suspend fun getBackupInfo(): Result<SyncDataInfo> {
        return runCatching {
            val response = networkClients.authenticatedClient.get(GET_BACKUP_INFO_URL)
            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
            val json = response.bodyAsText()
            jsonHandler.decodeFromString(json)
        }
    }

    override suspend fun getBackup(): Result<ByteReadChannel> {
        return runCatching {
            val response = networkClients.authenticatedClient.get(GET_BACKUP_URL)
            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
            response.bodyAsChannel()
        }
    }

    override suspend fun updateBackup(info: SyncDataInfo, file: ChannelProvider): Result<Unit> {
        return runCatching {
            val infoJson = jsonHandler.encodeToString(info)

            val response = networkClients.authenticatedClient.post(UPDATE_BACKUP_URL) {
                val partDataList = formData {
                    append("info", infoJson)
                    append("data", file, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"data.zip\"")
                    })
                }
                setBody(MultiPartFormDataContent(partDataList))
            }

            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
        }
    }

    override suspend fun postFeedback(data: FeedbackApiData): Result<Unit> {
        return runCatching {
            val requestBody = JsonObject(
                mapOf(
                    "topic" to JsonPrimitive(data.topic),
                    "text" to JsonPrimitive(data.message),
                    "user" to data.userData
                )
            )

            val response = networkClients.unauthenticatedClient.post(FEEDBACK_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }

            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
        }
    }

    override suspend fun postDonationPurchase(data: DonationPurchaseApiData): Result<Unit> {
        return runCatching {
            val requestBody = JsonObject(
                mapOf(
                    "email" to JsonPrimitive(data.email),
                    "message" to JsonPrimitive(data.message),
                    "paymentsJson" to JsonArray(
                        content = data.purchasesJson.map { JsonPrimitive(it) }
                    )
                )
            )

            val response = networkClients.unauthenticatedClient.post(SPONSOR_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }

            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
        }
    }

    private companion object {

        const val BASE = "http://localhost:8080"

        const val GET_USER_INFO_URL = "$BASE/getUserInfo"
        const val GET_BACKUP_INFO_URL = "$BASE/getBackupInfo"
        const val GET_BACKUP_URL = "$BASE/getBackup"
        const val UPDATE_BACKUP_URL = "$BASE/updateBackup"
        const val FEEDBACK_URL = "$BASE/feedback"
        const val SPONSOR_URL = "$BASE/sponsor"

    }

}
