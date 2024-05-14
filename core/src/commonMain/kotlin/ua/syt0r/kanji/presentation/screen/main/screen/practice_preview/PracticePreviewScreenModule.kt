package ua.syt0r.kanji.presentation.screen.main.screen.practice_preview

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.use_case.CreatePracticeGroupsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.use_case.PracticePreviewFetchItemsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.use_case.PracticePreviewFilterItemsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.use_case.PracticePreviewReloadStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.use_case.PracticePreviewSortItemsUseCase

val practicePreviewScreenModule = module {

    factory<PracticePreviewScreenContract.ReloadDataUseCase> {
        PracticePreviewReloadStateUseCase(
            userPreferencesRepository = get(),
            practiceRepository = get(),
            fetchItemsUseCase = get(),
            filterItemsUseCase = get(),
            sortItemsUseCase = get(),
            createGroupsUseCase = get()
        )
    }

    factory<PracticePreviewScreenContract.FetchItemsUseCase> {
        PracticePreviewFetchItemsUseCase(
            appStateManager = get(),
            appDataRepository = get(),
            practiceRepository = get()
        )
    }

    factory<PracticePreviewScreenContract.FilterItemsUseCase> {
        PracticePreviewFilterItemsUseCase()
    }

    factory<PracticePreviewScreenContract.SortItemsUseCase> {
        PracticePreviewSortItemsUseCase()
    }

    factory<PracticePreviewScreenContract.CreatePracticeGroupsUseCase> {
        CreatePracticeGroupsUseCase()
    }

    multiplatformViewModel<PracticePreviewScreenContract.ViewModel> {
        PracticePreviewViewModel(
            viewModelScope = it.component1(),
            reloadDataUseCase = get(),
            userPreferencesRepository = get(),
            filterItemsUseCase = get(),
            sortItemsUseCase = get(),
            createGroupsUseCase = get(),
            analyticsManager = get()
        )
    }

}