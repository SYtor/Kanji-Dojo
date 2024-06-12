package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker

import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.LetterDeckPickerScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.data.AllImportCategories


class LetterDeckPickerViewModel(
    private val analyticsManager: AnalyticsManager
) : LetterDeckPickerScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loaded(AllImportCategories))

    override fun reportScreenShown() {
        analyticsManager.setScreen("import")
    }

}