package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.stroke_evaluator.DefaultKanjiStrokeEvaluator
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DefaultCharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.MutableVocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueItemDescriptor

interface GetVocabWritingReviewStateUseCase {
    suspend operator fun invoke(
        descriptor: VocabQueueItemDescriptor.Writing
    ): MutableVocabReviewState.Writing
}

class DefaultGetVocabWritingReviewStateUseCase(
    private val appDataRepository: AppDataRepository,
    private val getPrioritizedWordReadingUseCase: GetPrioritizedWordReadingUseCase
) : GetVocabWritingReviewStateUseCase {

    override suspend fun invoke(
        descriptor: VocabQueueItemDescriptor.Writing
    ): MutableVocabReviewState.Writing {
        val word = appDataRepository.getWord(descriptor.wordId)
        val reading = getPrioritizedWordReadingUseCase(word, descriptor.priority)

        val writerStates = reading.compounds.toTypedArray()
            .map { stringCompound -> stringCompound.text.map { it.toString() } }
            .flatten()
            .map {
                val strokes = appDataRepository.getStrokes(it)
                DefaultCharacterWriterState(
                    coroutineScope = CoroutineScope(context = Dispatchers.IO),
                    strokeEvaluator = DefaultKanjiStrokeEvaluator(),
                    character = it,
                    strokes = parseKanjiStrokes(strokes),
                    configuration = CharacterWriterConfiguration.CharacterInput
                )
            }

        return MutableVocabReviewState.Writing(
            word = word,
            summaryReading = reading,
            charactersData = writerStates
        )
    }

}