package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetPrioritizedWordReadingUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabPracticeFlashcardDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabPracticeQueueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabPracticeReadingDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabPracticeSummaryItemUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.DefaultGetVocabPracticeWritingDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetPrioritizedWordReadingUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeFlashcardDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeQueueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeReadingDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeSummaryItemUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeWritingDataUseCase

val vocabPracticeScreenModule = module {


    factory<GetPrioritizedWordReadingUseCase> { DefaultGetPrioritizedWordReadingUseCase() }

    factory<GetVocabPracticeFlashcardDataUseCase> {
        DefaultGetVocabPracticeFlashcardDataUseCase(
            appDataRepository = get(),
            getPrioritizedWordReadingUseCase = get()
        )
    }

    factory<GetVocabPracticeQueueDataUseCase> { DefaultGetVocabPracticeQueueDataUseCase() }

    factory<GetVocabPracticeSummaryItemUseCase> { DefaultGetVocabPracticeSummaryItemUseCase() }

    factory<GetVocabPracticeReadingDataUseCase> {
        DefaultGetVocabPracticeReadingDataUseCase(
            appDataRepository = get(),
            getPrioritizedWordReadingUseCase = get()
        )
    }

    factory<GetVocabPracticeWritingDataUseCase> {
        DefaultGetVocabPracticeWritingDataUseCase(
            appDataRepository = get(),
            getPrioritizedWordReadingUseCase = get()
        )
    }


    factory<VocabPracticeQueue> {
        DefaultVocabPracticeQueue(
            coroutineScope = it.component1(),
            timeUtils = get(),
            srsItemRepository = get(),
            srsScheduler = get(),
            getFlashcardReviewStateUseCase = get(),
            getReadingReviewStateUseCase = get(),
            getWritingReviewStateUseCase = get(),
            getSummaryItemUseCase = get(),
            reviewHistoryRepository = get(),
            analyticsManager = get()
        )
    }

    multiplatformViewModel<VocabPracticeScreenContract.ViewModel> {
        VocabPracticeViewModel(
            viewModelScope = it.component1(),
            practicePreferences = get(),
            getQueueDataUseCase = get(),
            practiceQueue = get { it },
            analyticsManager = get()
        )
    }
}