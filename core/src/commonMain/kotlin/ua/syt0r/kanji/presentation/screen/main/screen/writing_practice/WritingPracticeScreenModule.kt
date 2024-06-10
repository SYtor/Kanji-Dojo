package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.use_case.LoadWritingPracticeCharacterDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.use_case.LoadWritingPracticeDataUseCase

val writingPracticeScreenModule = module {

    factory<WritingPracticeScreenContract.LoadPracticeData> {
        LoadWritingPracticeDataUseCase(
            letterSrsManager = get(),
            loadCharacterDataUseCase = get()
        )
    }

    factory<WritingPracticeScreenContract.LoadCharacterDataUseCase> {
        LoadWritingPracticeCharacterDataUseCase(
            appDataRepository = get(),
            userPreferencesRepository = get(),
            romajiConverter = get()
        )
    }

    multiplatformViewModel<WritingPracticeScreenContract.ViewModel> {
        WritingPracticeViewModel(
            viewModelScope = it.component1(),
            loadDataUseCase = get(),
            practiceRepository = get(),
            userPreferencesRepository = get(),
            analyticsManager = get(),
            timeUtils = get(),
            kanaTtsManager = get()
        )
    }

    single<WritingPracticeScreenContract.Content> { DefaultWritingPracticeScreenContent }

}