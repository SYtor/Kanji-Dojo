package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case

import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelectorState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.toScreenType

interface GetLetterPracticeConfigurationUseCase {
    suspend operator fun invoke(
        configuration: LetterPracticeScreenConfiguration
    ): LetterPracticeConfiguration
}

class DefaultGetLetterPracticeConfigurationUseCase(
    private val repository: PracticeUserPreferencesRepository
) : GetLetterPracticeConfigurationUseCase {

    override suspend fun invoke(configuration: LetterPracticeScreenConfiguration): LetterPracticeConfiguration {
        return when (configuration.practiceType) {
            ScreenLetterPracticeType.Writing -> {
                LetterPracticeConfiguration.Writing(
                    selectorState = PracticeConfigurationItemsSelectorState(
                        itemToDeckIdMap = configuration.characterToDeckIdMap.toList(),
                        shuffle = true
                    ),
                    noTranslationsLayout = mutableStateOf(repository.noTranslationLayout.get()),
                    leftHandedMode = mutableStateOf(repository.leftHandMode.get()),
                    useRomajiForKanaWords = mutableStateOf(repository.writingRomajiInsteadOfKanaWords.get()),
                    inputMode = mutableStateOf(repository.writingInputMethod.get().toScreenType()),
                    hintMode = mutableStateOf(WritingPracticeHintMode.OnlyNew),
                    altStrokeEvaluatorEnabled = mutableStateOf(repository.altStrokeEvaluator.get())
                )
            }

            ScreenLetterPracticeType.Reading -> {
                LetterPracticeConfiguration.Reading(
                    selectorState = PracticeConfigurationItemsSelectorState(
                        itemToDeckIdMap = configuration.characterToDeckIdMap.toList(),
                        shuffle = true
                    ),
                    useRomajiForKanaWords = mutableStateOf(repository.readingRomajiFuriganaForKanaWords.get())
                )
            }
        }
    }

}