package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.MutableState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckStudyType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

sealed interface LetterDecksData {

    object NoDecks : LetterDecksData

    data class Data(
        val studyType: MutableState<LetterDeckStudyType>,
        val studyProgressMap: Map<LetterDeckStudyType, LetterDecksStudyProgress>
    ) : LetterDecksData

}

data class LetterDecksStudyProgress(
    val new: Set<String>,
    val due: Set<String>,
) {
    val combined: Set<String> = new + due
}

sealed interface VocabDecksData {

    object NoDecks : VocabDecksData

    data class Data(
        val studyType: MutableState<VocabPracticeType>,
        val studyProgressMap: Map<VocabPracticeType, VocabDecksStudyProgress>
    ) : VocabDecksData

}

data class VocabDecksStudyProgress(
    val due: Set<Long>,
)