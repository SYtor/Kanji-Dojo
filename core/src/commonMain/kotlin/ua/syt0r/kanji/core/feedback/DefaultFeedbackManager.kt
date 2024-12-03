package ua.syt0r.kanji.core.feedback

import ua.syt0r.kanji.core.FeedbackApiData
import ua.syt0r.kanji.core.NetworkApi

class DefaultFeedbackManager(
    private val userDataProvider: FeedbackUserDataProvider,
    private val networkApi: NetworkApi
) : FeedbackManager {

    override suspend fun sendFeedback(data: FeedbackRequestData): Result<Unit> {
        return networkApi.postFeedback(
            FeedbackApiData(
                topic = data.topic,
                message = data.message,
                userData = userDataProvider.provide()
            )
        )
    }
}
