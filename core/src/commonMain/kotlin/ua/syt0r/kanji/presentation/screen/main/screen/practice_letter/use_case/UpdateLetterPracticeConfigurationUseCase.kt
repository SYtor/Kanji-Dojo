package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case

import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeConfiguration

interface UpdateLetterPracticeConfigurationUseCase {
    suspend operator fun invoke(configuration: LetterPracticeConfiguration)
}

class DefaultUpdateLetterPracticeConfigurationUseCase(
    private val userPreferencesRepository: PracticeUserPreferencesRepository
) : UpdateLetterPracticeConfigurationUseCase {

    override suspend fun invoke(
        configuration: LetterPracticeConfiguration
    ) = userPreferencesRepository.run {
        when (configuration) {
            is LetterPracticeConfiguration.Writing -> {
                noTranslationLayout.set(configuration.noTranslationsLayout.value)
                leftHandMode.set(configuration.leftHandedMode.value)
                writingRomajiInsteadOfKanaWords.set(configuration.useRomajiForKanaWords.value)
                writingInputMethod.set(configuration.inputMode.value.repoType)
                altStrokeEvaluator.set(configuration.altStrokeEvaluatorEnabled.value)
            }

            is LetterPracticeConfiguration.Reading -> {
                readingRomajiFuriganaForKanaWords.set(configuration.useRomajiForKanaWords.value)
            }
        }
    }
}