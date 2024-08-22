package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case

import kotlinx.coroutines.CoroutineScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DefaultCharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeInputMode

interface GetLetterPracticeReviewStateUseCase {
    operator fun invoke(queueState: LetterPracticeQueueState.Review): LetterPracticeReviewState
}

class DefaultGetLetterPracticeReviewStateUseCase(
    private val characterWriterCoroutineScope: CoroutineScope
) : GetLetterPracticeReviewStateUseCase {

    override fun invoke(queueState: LetterPracticeQueueState.Review): LetterPracticeReviewState {
        return when (val descriptor = queueState.descriptor) {
            is LetterPracticeQueueItemDescriptor.Writing -> {
                val data = queueState.data as LetterPracticeItemData.WritingData
                LetterPracticeReviewState.Writing(
                    layout = descriptor.layoutConfiguration,
                    itemData = data,
                    answers = queueState.answers,
                    studyWriterState = when (
                        descriptor.shouldStudy && queueState.currentItemRepeat == 0
                    ) {
                        true -> DefaultCharacterWriterState(
                            coroutineScope = characterWriterCoroutineScope,
                            strokeEvaluator = descriptor.evaluator,
                            character = descriptor.character,
                            strokes = data.strokes,
                            configuration = CharacterWriterConfiguration.StrokeInput(isStudyMode = true)
                        )

                        false -> null
                    },
                    reviewWriterState = DefaultCharacterWriterState(
                        coroutineScope = characterWriterCoroutineScope,
                        strokeEvaluator = descriptor.evaluator,
                        character = descriptor.character,
                        strokes = data.strokes,
                        configuration = when (descriptor.inputMode) {
                            WritingPracticeInputMode.Stroke -> {
                                CharacterWriterConfiguration.StrokeInput(isStudyMode = false)
                            }

                            WritingPracticeInputMode.Character -> CharacterWriterConfiguration.CharacterInput
                        }
                    )
                )
            }
        }
    }
}