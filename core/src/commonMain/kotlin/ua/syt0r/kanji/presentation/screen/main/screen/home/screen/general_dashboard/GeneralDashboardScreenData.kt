package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.MutableState
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType

sealed interface LetterDecksData {

    object NoDecks : LetterDecksData

    data class Data(
        val practiceType: MutableState<ScreenLetterPracticeType>,
        val studyProgressMap: Map<ScreenLetterPracticeType, LetterDecksStudyProgress>
    ) : LetterDecksData

}

data class LetterDecksStudyProgress(
    val newToDeckIdMap: Map<String, Long>,
    val dueToDeckIdMap: Map<String, Long>,
) {
    val combined: Map<String, Long> = newToDeckIdMap + dueToDeckIdMap
}

sealed interface VocabDecksData {

    object NoDecks : VocabDecksData

    data class Data(
        val practiceType: MutableState<ScreenVocabPracticeType>,
        val studyProgressMap: Map<ScreenVocabPracticeType, VocabDecksStudyProgress>
    ) : VocabDecksData

}

data class VocabDecksStudyProgress(
    val dueToDeckIdMap: Map<Long, Long>,
)