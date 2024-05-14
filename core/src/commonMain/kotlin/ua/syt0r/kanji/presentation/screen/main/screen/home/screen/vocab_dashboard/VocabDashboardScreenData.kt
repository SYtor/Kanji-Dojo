package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import ua.syt0r.kanji.core.app_data.data.JapaneseWord

data class VocabPracticeSet(
    val title: String,
    val expressionIds: List<Long>
)

sealed interface VocabPracticePreviewState {
    object Loading : VocabPracticePreviewState
    data class Loaded(val words: List<JapaneseWord>) : VocabPracticePreviewState
}

val vocabSets = VocabPracticeSet(
    title = "Grammar Terms",
    expressionIds = listOf(
        1531570,
        1451380,
        1250430
    )
)
