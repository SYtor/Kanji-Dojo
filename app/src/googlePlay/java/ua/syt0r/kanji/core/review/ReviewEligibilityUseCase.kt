package ua.syt0r.kanji.core.review

import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository

class ReviewEligibilityUseCase(
    private val practiceRepository: LetterPracticeRepository
) : AppReviewContract.ReviewEligibilityUseCase {

    companion object {
        private const val RequiredCharacterReviewsCount = 30L
    }

    override suspend fun checkIsEligible(): Boolean {
        return practiceRepository.getTotalReviewsCount() >= RequiredCharacterReviewsCount
    }

}