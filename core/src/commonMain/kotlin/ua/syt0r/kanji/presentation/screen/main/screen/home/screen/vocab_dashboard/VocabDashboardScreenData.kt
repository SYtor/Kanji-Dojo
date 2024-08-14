package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import ua.syt0r.kanji.core.app_data.data.JapaneseWord

sealed interface VocabPracticePreviewState {
    object Loading : VocabPracticePreviewState
    data class Loaded(val words: List<JapaneseWord>) : VocabPracticePreviewState
}
