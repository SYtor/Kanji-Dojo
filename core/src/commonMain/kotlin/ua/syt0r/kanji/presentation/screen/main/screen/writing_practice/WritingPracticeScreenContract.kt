package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.data.*

interface WritingPracticeScreenContract {

    companion object {
        const val WordsLimit = 100
    }

    interface ScreenContent {

        @Composable
        fun Draw(
            configuration: MainDestination.Practice.Writing,
            mainNavigationState: MainNavigationState,
        )

    }

    interface ViewModel {

        val state: State<ScreenState>

        fun init(practiceConfiguration: MainDestination.Practice.Writing)
        suspend fun submitUserDrawnPath(drawData: DrawData): DrawResult

        fun handleCorrectlyDrawnStroke()
        fun handleIncorrectlyDrawnStroke()

        fun loadNextCharacter(userAction: ReviewUserAction)

        fun toggleRadicalsHighlight()

        fun reportScreenShown(configuration: MainDestination.Practice.Writing)

    }

    sealed class ScreenState {

        object Loading : ScreenState()

        data class Review(
            val data: ReviewCharacterData,
            val isStudyMode: Boolean,
            val progress: PracticeProgress,
            val shouldHighlightRadicals: Boolean,
            val isNoTranslationLayout: Boolean,
            val drawnStrokesCount: Int = 0,
            val currentStrokeMistakes: Int = 0,
            val currentCharacterMistakes: Int = 0
        ) : ScreenState()

        sealed class Summary : ScreenState() {

            object Saving : Summary()

            data class Saved(
                val reviewResultList: List<ReviewResult>,
                val eligibleForInAppReview: Boolean
            ) : Summary()

        }

    }

    interface LoadWritingPracticeDataUseCase {
        suspend fun load(configuration: MainDestination.Practice.Writing): List<ReviewCharacterData>
    }

    interface IsEligibleForInAppReviewUseCase {
        suspend fun check(): Boolean
    }

}