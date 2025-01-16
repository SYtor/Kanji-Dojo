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
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ua.syt0r.kanji.core.user_data.preferences.PreferencesSyncDataInfo

interface NetworkApi {

    suspend fun getUserInfo(): Result<ApiUserInfo>

    suspend fun getSyncDataInfo(): Result<ApiSyncDataInfo>
    suspend fun getSyncData(): Result<ByteReadChannel>
    suspend fun updateSyncData(info: ApiSyncDataInfo, file: ChannelProvider): Result<Unit>

    suspend fun postFeedback(data: FeedbackApiData): Result<Unit>
    suspend fun postDonationPurchase(data: DonationPurchaseApiData): Result<Unit>

}

data class HttpResponseException(
    val statusCode: HttpStatusCode
) : Throwable()

sealed interface ApiRequestIssue {

    data object NoConnection : ApiRequestIssue
    data object NotAuthenticated : ApiRequestIssue
    data object NoSubscription : ApiRequestIssue
    data class Other(val throwable: Throwable) : ApiRequestIssue

    companion object {
        fun classify(throwable: Throwable): ApiRequestIssue {
            return when (throwable) {
                is UnresolvedAddressException -> NoConnection
                is HttpResponseException -> when (throwable.statusCode) {
                    HttpStatusCode.Unauthorized -> NotAuthenticated
                    HttpStatusCode.PaymentRequired -> NoSubscription
                    else -> Other(throwable)
                }

                else -> {
                    Other(throwable)
                }
            }
        }
    }

}


@Serializable
data class ApiUserInfo(
    val email: String,
    val subscription: Boolean,
    val subscriptionDue: Long? = null
)

@Serializable
data class ApiSyncDataInfo(
    val dataId: String,
    val dataVersion: Long,
    val dataTimestamp: Long? = null
)

fun ApiSyncDataInfo.toPreferencesType() =
    PreferencesSyncDataInfo(dataId, dataVersion, dataTimestamp)

fun PreferencesSyncDataInfo.toApiType() = ApiSyncDataInfo(dataId, dataVersion, dataTimestamp)

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
    private val json: Json
) : NetworkApi {

    override suspend fun getUserInfo(): Result<ApiUserInfo> {
        return runCatching {
            val response = networkClients.authenticatedClient.get(GET_USER_INFO_URL)
            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
            val jsonValue = response.bodyAsText()
            json.decodeFromString(jsonValue)
        }
    }

    override suspend fun getSyncDataInfo(): Result<ApiSyncDataInfo> {
        return runCatching {
            val response = networkClients.authenticatedClient.get(GET_SYNC_INFO_URL)
            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
            val jsonValue = response.bodyAsText()
            json.decodeFromString<ApiSyncDataInfo>(jsonValue)
        }
    }

    override suspend fun getSyncData(): Result<ByteReadChannel> {
        return runCatching {
            val response = networkClients.authenticatedClient.get(GET_SYNC_URL)
            if (response.status != HttpStatusCode.OK) throw HttpResponseException(response.status)
            response.bodyAsChannel()
        }
    }

    override suspend fun updateSyncData(
        info: ApiSyncDataInfo,
        file: ChannelProvider
    ): Result<Unit> {
        return runCatching {
            val infoJson = json.encodeToString(info)

            val response = networkClients.authenticatedClient.post(UPDATE_SYNC_URL) {
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

        const val BASE = "https://kanji-dojo.com/api/v2"

        const val GET_USER_INFO_URL = "$BASE/user/info"
        const val GET_SYNC_INFO_URL = "$BASE/sync/info"
        const val GET_SYNC_URL = "$BASE/sync/get"
        const val UPDATE_SYNC_URL = "$BASE/sync/update"
        const val FEEDBACK_URL = "$BASE/feedback"
        const val SPONSOR_URL = "$BASE/sponsor"

    }

}
