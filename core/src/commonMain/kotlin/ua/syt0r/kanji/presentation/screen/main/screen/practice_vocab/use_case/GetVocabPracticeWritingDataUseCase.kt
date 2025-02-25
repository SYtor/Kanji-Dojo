package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.VocabCardResolver
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.formattedVocabReading
import ua.syt0r.kanji.core.app_data.data.toFurigana
import ua.syt0r.kanji.core.app_data.data.withoutAnnotations
import ua.syt0r.kanji.core.stroke_evaluator.DefaultKanjiStrokeEvaluator
import ua.syt0r.kanji.core.toInfoScreenData
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.CharacterWriterData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor

interface GetVocabPracticeWritingDataUseCase {
    suspend operator fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.Writing
    ): VocabPracticeItemData.Writing
}

class DefaultGetVocabPracticeWritingDataUseCase(
    private val vocabCardResolver: VocabCardResolver,
    private val appDataRepository: AppDataRepository
) : GetVocabPracticeWritingDataUseCase {

    override suspend fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.Writing
    ): VocabPracticeItemData.Writing {
        val card = vocabCardResolver.resolveUserCard(descriptor.cardId)
        val strokeEvaluator = DefaultKanjiStrokeEvaluator()

        val letters: String
        val summaryReading: FuriganaString

        when {
            card.furigana != null -> {
                letters = card.furigana.withoutAnnotations()
                summaryReading = card.furigana
            }

            card.kanjiReading != null -> {
                letters = card.kanjiReading
                summaryReading = formattedVocabReading(
                    kanaReading = card.kanaReading,
                    kanjiReading = card.kanjiReading
                )
            }

            else -> {
                letters = card.kanaReading
                summaryReading = card.kanaReading.toFurigana()
            }
        }

        val writerData = letters
            .map { it.toString() }
            .map { character ->
                val strokes = appDataRepository.getStrokes(character)

                val characterWriterData = when {
                    strokes.isEmpty() -> null
                    else -> CharacterWriterData(
                        character = character,
                        strokeEvaluator = strokeEvaluator,
                        strokes = parseKanjiStrokes(strokes),
                        configuration = CharacterWriterConfiguration.CharacterInput
                    )
                }
                character to characterWriterData
            }

        return VocabPracticeItemData.Writing(
            meaning = card.glossary.joinToString(),
            summaryReading = summaryReading,
            writerData = writerData,
            vocabReference = card.toInfoScreenData()
        )
    }

}