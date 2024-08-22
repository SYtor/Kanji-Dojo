package ua.syt0r.kanji.core.review

import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository

class ReviewEligibilityUseCase(
    private val reviewHistoryRepository: ReviewHistoryRepository
) : AppReviewContract.ReviewEligibilityUseCase {

    companion object {
        private const val RequiredCharacterReviewsCount = 30L
    }

    override suspend fun checkIsEligible(): Boolean {
        return reviewHistoryRepository.getTotalReviewsCount() >= RequiredCharacterReviewsCount
    }

}