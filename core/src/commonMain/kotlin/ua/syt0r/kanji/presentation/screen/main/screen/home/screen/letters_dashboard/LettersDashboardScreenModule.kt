package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case.MergeLettersDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case.LettersDashboardApplySortUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case.LettersDashboardLoadDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case.LettersDashboardUpdateSortUseCase

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
            userPreferencesRepository = get(),
            practiceRepository = get()
        )
    }

    factory<LettersDashboardScreenContract.ApplySortUseCase> {
        LettersDashboardApplySortUseCase()
    }

    multiplatformViewModel<LettersDashboardScreenContract.ViewModel> {
        LettersDashboardViewModel(
            viewModelScope = it.component1(),
            loadDataUseCase = get(),
            applySortUseCase = get(),
            mergeDecksUseCase = get(),
            updateSortUseCase = get(),
            notifySrsPreferencesChangedUseCase = get(),
            userPreferencesRepository = get(),
            analyticsManager = get()
        )
    }

}