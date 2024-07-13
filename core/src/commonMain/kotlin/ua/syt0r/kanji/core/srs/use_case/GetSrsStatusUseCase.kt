package ua.syt0r.kanji.core.srs.use_case

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.core.time.TimeUtils

interface GetSrsStatusUseCase {
    operator fun invoke(expectedReviewTime: Instant?): SrsItemStatus
}

class DefaultGetSrsStatusUseCase(
    private val timeUtils: TimeUtils
) : GetSrsStatusUseCase {

    override fun invoke(expectedReviewTime: Instant?): SrsItemStatus {
        if (expectedReviewTime == null) return SrsItemStatus.New

        val timeZone = TimeZone.currentSystemDefault()
        val currentDate = timeUtils.now().toLocalDateTime(timeZone)
        val expectedReviewDate = expectedReviewTime.toLocalDateTime(timeZone)
        return when {
            expectedReviewDate > currentDate -> SrsItemStatus.Done
            else -> SrsItemStatus.Review
        }
    }

}