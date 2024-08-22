package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import org.koin.androidx.compose.get
import ua.syt0r.kanji.core.review.AppReviewContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.DefaultLetterPracticeScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration

object GooglePlayLetterPracticeScreenContent : LetterPracticeScreenContract.Content {

    @Composable
    override fun invoke(
        configuration: LetterPracticeScreenConfiguration ,
        mainNavigationState: MainNavigationState,
        viewModel: LetterPracticeScreenContract.ViewModel
    ) {

        DefaultLetterPracticeScreenContent.invoke(
            configuration = configuration,
            mainNavigationState = mainNavigationState,
            viewModel = viewModel
        )

        ReviewHandler(state = viewModel.state)

    }

    @Composable
    private fun ReviewHandler(state: State<ScreenState>) {

        val canStartReview = remember {
            derivedStateOf { state.value is ScreenState.Summary }
        }

        if (canStartReview.value) {
            val reviewManager = get<AppReviewContract.ReviewManager>()
            reviewManager.AttemptReview()
        }

    }

}