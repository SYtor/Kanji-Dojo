package ua.syt0r.kanji.presentation.screen.main.screen.feedback

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val feedbackScreenModule = module {

    multiplatformViewModel<FeedbackScreenContract.ViewModel> {
        FeedbackViewModel(
            viewModelScope = it.component1(),
            feedbackManager = get()
        )
    }

}