package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.core.stroke_evaluator.AltKanjiStrokeEvaluator
import ua.syt0r.kanji.core.stroke_evaluator.DefaultKanjiStrokeEvaluator
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeLayoutConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeHintMode

interface GetLetterPracticeQueueDataUseCase {

    suspend operator fun invoke(
        configuration: LetterPracticeConfiguration
    ): List<LetterPracticeQueueItemDescriptor>

}

class DefaultGetLetterPracticeQueueDataUseCase(
    private val userPreferencesRepository: PracticeUserPreferencesRepository,
    private val letterSrsManager: LetterSrsManager,
    private val configurationUpdateScope: CoroutineScope
) : GetLetterPracticeQueueDataUseCase {

    override suspend fun invoke(
        configuration: LetterPracticeConfiguration
    ): List<LetterPracticeQueueItemDescriptor> {

        val kanaAutoPlay = mutableStateOf(
            value = userPreferencesRepository.kanaAutoPlay.get()
        )

        snapshotFlow { kanaAutoPlay.value }
            .onEach { userPreferencesRepository.kanaAutoPlay.set(it) }
            .launchIn(configurationUpdateScope)

        return when (configuration) {
            is LetterPracticeConfiguration.Writing -> {

                val radicalsHighlight = mutableStateOf(
                    value = userPreferencesRepository.highlightRadicals.get()
                )

                snapshotFlow { radicalsHighlight.value }
                    .onEach { userPreferencesRepository.highlightRadicals.set(it) }
                    .launchIn(configurationUpdateScope)

                val layout = LetterPracticeLayoutConfiguration.WritingLayoutConfiguration(
                    noTranslationsLayout = configuration.noTranslationsLayout.value,
                    radicalsHighlight = radicalsHighlight,
                    kanaAutoPlay = kanaAutoPlay,
                    leftHandedMode = configuration.leftHandedMode.value
                )

                val evaluator = when (configuration.altStrokeEvaluatorEnabled.value) {
                    true -> AltKanjiStrokeEvaluator()
                    false -> DefaultKanjiStrokeEvaluator()
                }

                configuration.selectorState.result.map { (character, deckId) ->
                    val shouldStudy: Boolean = when (configuration.hintMode.value) {
                        WritingPracticeHintMode.OnlyNew -> {
                            val srsData = letterSrsManager
                                .getLetterSrsData(character, LetterPracticeType.Writing)
                            srsData.status == SrsItemStatus.New
                        }

                        WritingPracticeHintMode.All -> true
                        WritingPracticeHintMode.None -> false
                    }
                    LetterPracticeQueueItemDescriptor.Writing(
                        character = character,
                        deckId = deckId,
                        romajiReading = configuration.useRomajiForKanaWords.value,
                        layoutConfiguration = layout,
                        inputMode = configuration.inputMode.value,
                        evaluator = evaluator,
                        shouldStudy = shouldStudy
                    )
                }
            }

            is LetterPracticeConfiguration.Reading -> {

                val layout = LetterPracticeLayoutConfiguration.ReadingLayoutConfiguration(
                    kanaAutoPlay = kanaAutoPlay
                )

                configuration.selectorState.result.map { (character, deckId) ->
                    LetterPracticeQueueItemDescriptor.Reading(
                        character = character,
                        deckId = deckId,
                        romajiReading = configuration.useRomajiForKanaWords.value,
                        layoutConfiguration = layout
                    )
                }

            }
        }
    }

}