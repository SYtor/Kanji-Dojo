package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import ua.syt0r.kanji.core.srs.CharacterProgressStatus
import ua.syt0r.kanji.core.srs.LetterSrsManager
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
        val progresses = letterSrsManager.getUpdatedData().characterProgresses

        return configuration.characters
            .map { character ->
                val writingStatus = progresses[character]?.writingStatus
                val shouldStudy: Boolean = when (configuration.hintMode) {
                    WritingPracticeHintMode.OnlyNew -> {
                        writingStatus == null || writingStatus == CharacterProgressStatus.New
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