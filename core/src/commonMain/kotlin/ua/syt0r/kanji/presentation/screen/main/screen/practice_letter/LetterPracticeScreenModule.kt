package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.DefaultGetLetterPracticeConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.DefaultGetLetterPracticeQueueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.DefaultGetLetterPracticeQueueItemDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.DefaultGetLetterPracticeReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.DefaultUpdateLetterPracticeConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeQueueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeQueueItemDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.UpdateLetterPracticeConfigurationUseCase

val letterPracticeScreenModule = module {

    factory<GetLetterPracticeConfigurationUseCase> {
        DefaultGetLetterPracticeConfigurationUseCase(
            repository = get()
        )
    }

    factory<UpdateLetterPracticeConfigurationUseCase> {
        DefaultUpdateLetterPracticeConfigurationUseCase(
            userPreferencesRepository = get()
        )
    }

    factory<GetLetterPracticeQueueDataUseCase> {
        DefaultGetLetterPracticeQueueDataUseCase(
            userPreferencesRepository = get(),
            letterSrsManager = get(),
            configurationUpdateScope = it.component1()
        )
    }

    factory<GetLetterPracticeQueueItemDataUseCase> {
        DefaultGetLetterPracticeQueueItemDataUseCase(
            appDataRepository = get(),
            userPreferencesRepository = get(),
            romajiConverter = get()
        )
    }

    factory<GetLetterPracticeReviewStateUseCase> {
        DefaultGetLetterPracticeReviewStateUseCase(
            characterWriterCoroutineScope = it.component1()
        )
    }

    factory<LetterPracticeQueue>(named<LetterPracticeScreenContract>()) {
        DefaultLetterPracticeQueue(
            coroutineScope = it.component1(),
            timeUtils = get(),
            srsItemRepository = get(),
            srsScheduler = get(),
            getQueueItemDataUseCase = get(),
            reviewHistoryRepository = get()
        )
    }

    multiplatformViewModel<LetterPracticeScreenContract.ViewModel> {
        LetterPracticeViewModel(
            viewModelScope = it.component1(),
            getConfigurationUseCase = get(),
            updateConfigurationUseCase = get(),
            getQueueDataUseCase = get { it },
            practiceQueue = get(named<LetterPracticeScreenContract>()) { it },
            getReviewStateUseCase = get { it },
            analyticsManager = get(),
            kanaTtsManager = get()
        )
    }

    single<LetterPracticeScreenContract.Content> { DefaultLetterPracticeScreenContent }

}