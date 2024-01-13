package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice

import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.BaseCharacterReviewManager
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterReviewData
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.data.WritingReviewCharacterDetails
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.data.WritingReviewCharacterSummaryDetails

enum class WritingCharacterReviewHistory { Study, Review, Repeat }

typealias WritingCharacterReviewData = CharacterReviewData<WritingCharacterReviewHistory, WritingReviewCharacterDetails>

class WritingCharacterReviewManager(
    reviewItems: List<WritingCharacterReviewData>,
    timeUtils: TimeUtils,
    onCompletedCallback: () -> Unit
) : BaseCharacterReviewManager<WritingCharacterReviewHistory, WritingReviewCharacterDetails, WritingReviewCharacterSummaryDetails>(
    reviewItems,
    timeUtils,
    onCompletedCallback
) {

    override fun getCharacterSummary(
        history: List<WritingCharacterReviewHistory>,
        characterData: WritingReviewCharacterDetails
    ): WritingReviewCharacterSummaryDetails {
        return WritingReviewCharacterSummaryDetails(
            strokesCount = characterData.strokes.size,
            isStudy = history.first() == WritingCharacterReviewHistory.Study
        )
    }

    override fun isRepeat(
        data: WritingCharacterReviewData
    ): Boolean {
        return data.history.last() == WritingCharacterReviewHistory.Repeat
    }

}