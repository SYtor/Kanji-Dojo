package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.use_case.DefaultSubscribeOnGeneralDashboardScreenDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.use_case.SubscribeOnGeneralDashboardScreenDataUseCase

val generalDashboardScreenModule = module {

    factory<SubscribeOnGeneralDashboardScreenDataUseCase> {
        DefaultSubscribeOnGeneralDashboardScreenDataUseCase(
            letterSrsManager = get(),
            vocabSrsManager = get()
        )
    }

    multiplatformViewModel<GeneralDashboardScreenContract.ViewModel> {
        GeneralDashboardViewModel(
            viewModelScope = it.component1(),
            subscribeOnScreenDataUseCase = get(),
            analyticsManager = get()
        )
    }

}