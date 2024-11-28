package ua.syt0r.kanji.presentation.screen.sponsor.use_case

import ua.syt0r.kanji.core.DonationPurchaseApiData
import ua.syt0r.kanji.core.NetworkApi

interface GooglePlaySendSponsorResultsUseCase {

    suspend operator fun invoke(
        email: String,
        message: String,
        purchasesJson: List<String>
    ): Result<Unit>

}

class DefaultGooglePlaySendSponsorResultsUseCase(
    private val networkApi: NetworkApi
) : GooglePlaySendSponsorResultsUseCase {

    override suspend fun invoke(
        email: String,
        message: String,
        purchasesJson: List<String>
    ): Result<Unit> {
        return networkApi.postDonationPurchase(
            data = DonationPurchaseApiData(
                email = email,
                message = message,
                purchasesJson = purchasesJson
            )
        )
    }

}
