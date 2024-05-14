package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabReadingReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabReadingReviewStateUseCase

val vocabPracticeScreenModule = module {

    multiplatformViewModel<VocabPracticeScreenContract.ViewModel> {
        VocabPracticeViewModel(
            viewModelScope = it.component1(),
            reviewManager = get { it },
            analyticsManager = get()
        )
    }

    factory {
        VocabPracticeReviewManager(
            coroutineScope = it.component1(),
            timeUtils = get(),
            getVocabReadingReviewStateUseCase = get()
        )
    }

    factory<GetVocabReadingReviewStateUseCase> {
        DefaultGetVocabReadingReviewStateUseCase(
            appDataRepository = get()
        )
    }

}