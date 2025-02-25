package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case.LettersDashboardLoadDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case.LettersDashboardUpdateSortUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case.MergeLettersDecksUseCase

val lettersDashboardScreenModule = module {

    factory<LettersDashboardScreenContract.LoadDataUseCase> {
        LettersDashboardLoadDataUseCase(
            srsManager = get(),
            timeUtils = get()
        )
    }

    factory<LettersDashboardScreenContract.MergeDecksUseCase> {
        MergeLettersDecksUseCase(repository = get())
    }

    factory<LettersDashboardScreenContract.UpdateSortUseCase> {
        LettersDashboardUpdateSortUseCase(
            appPreferences = get(),
            practiceRepository = get()
        )
    }

    multiplatformViewModel<LettersDashboardScreenContract.ViewModel> {
        LettersDashboardViewModel(
            viewModelScope = it.component1(),
            loadDataUseCase = get(),
            mergeDecksUseCase = get(),
            updateSortUseCase = get(),
            appPreferences = get(),
            analyticsManager = get()
        )
    }

}