package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetPrioritizedWordReadingUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabFlashcardReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabPracticeQueueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabPracticeSummaryItemUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabReadingReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetPrioritizedWordReadingUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabFlashcardReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeQueueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeSummaryItemUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabReadingReviewStateUseCase

val vocabPracticeScreenModule = module {


    factory<GetPrioritizedWordReadingUseCase> { DefaultGetPrioritizedWordReadingUseCase() }

    factory<GetVocabFlashcardReviewStateUseCase> {
        DefaultGetVocabFlashcardReviewStateUseCase(
            appDataRepository = get(),
            getPrioritizedWordReadingUseCase = get()
        )
    }

    factory<GetVocabPracticeQueueDataUseCase> { DefaultGetVocabPracticeQueueDataUseCase() }

    factory<GetVocabPracticeSummaryItemUseCase> { DefaultGetVocabPracticeSummaryItemUseCase() }

    factory<GetVocabReadingReviewStateUseCase> {
        DefaultGetVocabReadingReviewStateUseCase(
            appDataRepository = get(),
            getPrioritizedWordReadingUseCase = get()
        )
    }
    factory<VocabPracticeQueue> {
        DefaultVocabPracticeQueue(
            coroutineScope = it.component1(),
            timeUtils = get(),
            getFlashcardReviewStateUseCase = get(),
            getReadingReviewStateUseCase = get(),
            getSummaryItemUseCase = get()
        )
    }
    multiplatformViewModel<VocabPracticeScreenContract.ViewModel> {
        VocabPracticeViewModel(
            viewModelScope = it.component1(),
            userPreferencesRepository = get(),
            getQueueDataUseCase = get(),
            practiceQueue = get { it },
            analyticsManager = get()
        )
    }
}