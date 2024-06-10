package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.use_case.MergePracticeSetsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.use_case.PracticeDashboardApplySortUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.use_case.PracticeDashboardLoadDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.use_case.PracticeDashboardUpdateSortUseCase

val practiceDashboardScreenModule = module {

    factory<PracticeDashboardScreenContract.LoadDataUseCase> {
        PracticeDashboardLoadDataUseCase(
            srsManager = get()
        )
    }

    factory<PracticeDashboardScreenContract.MergePracticeSetsUseCase> {
        MergePracticeSetsUseCase(repository = get())
    }

    factory<PracticeDashboardScreenContract.UpdateSortUseCase> {
        PracticeDashboardUpdateSortUseCase(
            userPreferencesRepository = get(),
            practiceRepository = get()
        )
    }

    factory<PracticeDashboardScreenContract.ApplySortUseCase> {
        PracticeDashboardApplySortUseCase()
    }

    multiplatformViewModel<PracticeDashboardScreenContract.ViewModel> {
        PracticeDashboardViewModel(
            viewModelScope = it.component1(),
            loadDataUseCase = get(),
            applySortUseCase = get(),
            mergePracticeSetsUseCase = get(),
            updateSortUseCase = get(),
            notifySrsPreferencesChangedUseCase = get(),
            userPreferencesRepository = get(),
            analyticsManager = get()
        )
    }

}