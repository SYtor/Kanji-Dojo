package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val statsScreenModule = module {

    multiplatformViewModel<StatsScreenContract.ViewModel> {
        StatsViewModel(
            viewModelScope = it.component1(),
            appStateManager = get(),
            practiceRepository = get(),
            timeUtils = get(),
            analyticsManager = get()
        )
    }

}