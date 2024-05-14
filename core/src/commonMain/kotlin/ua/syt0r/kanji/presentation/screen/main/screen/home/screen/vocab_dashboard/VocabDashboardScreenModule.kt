package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import org.koin.dsl.module

val vocabDashboardScreenModule = module {

    factory<VocabDashboardScreenContract.ViewModel> {
        VocabDashboardViewModel(
            viewModelScope = it.component1(),
            appDataRepository = get(),
            analyticsManager = get()
        )
    }

}