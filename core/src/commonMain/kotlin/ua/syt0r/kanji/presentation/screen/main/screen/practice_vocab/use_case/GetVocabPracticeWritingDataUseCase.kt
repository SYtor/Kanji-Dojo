package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.stroke_evaluator.DefaultKanjiStrokeEvaluator
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.CharacterWriterData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueItemDescriptor

interface GetVocabPracticeWritingDataUseCase {
    suspend operator fun invoke(
        descriptor: VocabQueueItemDescriptor.Writing
    ): VocabPracticeItemData.Writing
}

class DefaultGetVocabPracticeWritingDataUseCase(
    private val appDataRepository: AppDataRepository,
    private val getPrioritizedWordReadingUseCase: GetPrioritizedWordReadingUseCase
) : GetVocabPracticeWritingDataUseCase {

    override suspend fun invoke(
        descriptor: VocabQueueItemDescriptor.Writing
    ): VocabPracticeItemData.Writing {
        val word = appDataRepository.getWord(descriptor.wordId)
        val reading = getPrioritizedWordReadingUseCase(word, descriptor.priority)

        val strokeEvaluator = DefaultKanjiStrokeEvaluator()

        val writerData = reading.compounds.toTypedArray()
            .map { stringCompound -> stringCompound.text.map { it.toString() } }
            .flatten()
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
            word = word,
            summaryReading = reading,
            writerData = writerData
        )
    }

}