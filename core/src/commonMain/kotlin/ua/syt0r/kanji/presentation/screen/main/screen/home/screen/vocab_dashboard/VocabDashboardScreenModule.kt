package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val vocabDashboardScreenModule = module {

    multiplatformViewModel<VocabDashboardScreenContract.ViewModel> {
        VocabDashboardViewModel(
            viewModelScope = it.component1(),
            appDataRepository = get(),
            analyticsManager = get()
        )
    }

}