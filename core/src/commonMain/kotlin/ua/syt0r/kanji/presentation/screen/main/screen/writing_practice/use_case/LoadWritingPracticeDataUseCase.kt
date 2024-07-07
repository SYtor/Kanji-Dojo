package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.user_data.preferences.PracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingCharacterReviewData
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingCharacterReviewHistory
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingScreenConfiguration

class LoadWritingPracticeDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val loadCharacterDataUseCase: WritingPracticeScreenContract.LoadCharacterDataUseCase,
) : WritingPracticeScreenContract.LoadPracticeData {

    override suspend fun load(
        configuration: WritingScreenConfiguration,
        scope: CoroutineScope,
    ): List<WritingCharacterReviewData> {
        return configuration.characters
            .map { character ->
                val shouldStudy: Boolean = when (configuration.hintMode) {
                    WritingPracticeHintMode.OnlyNew -> {
                        letterSrsManager.getStatus(character, PracticeType.Writing).status ==
                                SrsItemStatus.New
                    }

                    WritingPracticeHintMode.All -> true
                    WritingPracticeHintMode.None -> false
                }
                val initialAction = when (shouldStudy) {
                    true -> WritingCharacterReviewHistory.Study
                    false -> WritingCharacterReviewHistory.Review
                }
                WritingCharacterReviewData(
                    character = character,
                    details = scope.async(
                        context = Dispatchers.IO,
                        start = CoroutineStart.LAZY
                    ) {
                        loadCharacterDataUseCase.load(character)
                    },
                    history = listOf(initialAction)
                )
            }
    }

}