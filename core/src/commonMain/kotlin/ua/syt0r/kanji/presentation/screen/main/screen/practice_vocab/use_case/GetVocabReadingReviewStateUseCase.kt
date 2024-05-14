package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import kotlinx.coroutines.flow.MutableStateFlow
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabReviewState

class MutableVocabReadingReviewState(
    val visibleVocab: FuriganaString,
    val hiddenVocab: FuriganaString,
    override val answers: List<String>,
    override val correctAnswer: String
) : VocabReviewState.Reading {
    override val vocab = MutableStateFlow<FuriganaString>(hiddenVocab)
    override val selectedAnswer = MutableStateFlow<String?>(null)
}

interface GetVocabReadingReviewStateUseCase {
    suspend operator fun invoke(id: Long): MutableVocabReadingReviewState
}

class DefaultGetVocabReadingReviewStateUseCase(
    private val appDataRepository: AppDataRepository
) : GetVocabReadingReviewStateUseCase {

    override suspend fun invoke(id: Long): MutableVocabReadingReviewState {
        val vocab = appDataRepository.getWordReadings(id).first()
        return MutableVocabReadingReviewState(
            visibleVocab = vocab,
            hiddenVocab = vocab,
            answers = vocab.compounds.map { it.annotation }.filterNotNull(),
            correctAnswer = "a"
        )
    }

}