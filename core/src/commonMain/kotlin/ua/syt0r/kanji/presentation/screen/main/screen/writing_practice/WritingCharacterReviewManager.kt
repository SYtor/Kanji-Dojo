package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice

import kotlinx.coroutines.CoroutineScope
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.BaseCharacterReviewManager
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterReviewData

enum class WritingCharacterReviewHistory { Study, Review, Repeat }

typealias WritingCharacterReviewData = CharacterReviewData<WritingCharacterReviewHistory, WritingReviewCharacterDetails>

class WritingCharacterReviewManager(
    reviewItems: List<WritingCharacterReviewData>,
    coroutineScope: CoroutineScope,
    timeUtils: TimeUtils,
    onCompletedCallback: () -> Unit
) : BaseCharacterReviewManager<WritingCharacterReviewHistory, WritingReviewCharacterDetails, WritingReviewCharacterSummaryDetails>(
    reviewItems,
    coroutineScope,
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