package ua.syt0r.kanji.presentation.screen.main.screen.reading_practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeCharacterReviewResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavingResult
import kotlin.time.Duration

interface ReadingPracticeContract {

    companion object {
        const val DisplayWordsLimit = 5
    }

    interface Content {
        @Composable
        fun Draw(
            configuration: MainDestination.Practice.Reading,
            mainNavigationState: MainNavigationState,
            viewModel: ViewModel
        )
    }

    interface ViewModel {

        val state: State<ScreenState>

        fun initialize(configuration: MainDestination.Practice.Reading)
        fun onConfigured(configuration: ReadingScreenConfiguration)

        fun select(option: ReadingPracticeSelectedOption)
        fun toggleKanaAutoPlay()
        fun speakKana(reading: KanaReading)

        fun savePractice(result: PracticeSavingResult)

    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Configuration(
            val characters: List<String>,
            val kanaRomaji: Boolean
        ) : ScreenState

        data class Review(
            val data: StateFlow<ReadingReviewData>
        ) : ScreenState

        data class Saving(
            val toleratedMistakesCount: Int,
            val reviewResultList: List<PracticeCharacterReviewResult>
        ) : ScreenState

        data class Saved(
            val practiceDuration: Duration,
            val accuracy: Float,
            val repeatCharacters: List<String>,
            val goodCharacters: List<String>
        ) : ScreenState

    }

    interface LoadCharactersDataUseCase {
        suspend fun load(character: String): ReadingReviewCharacterData
    }

}
