package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case.DefaultSubscribeOnStatsDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case.SubscribeOnStatsDataUseCase

val statsScreenModule = module {

    multiplatformViewModel<StatsScreenContract.ViewModel> {
        StatsViewModel(
            viewModelScope = it.component1(),
            subscribeOnStatsDataUseCase = get(),
            analyticsManager = get()
        )
    }

    factory<SubscribeOnStatsDataUseCase> {
        DefaultSubscribeOnStatsDataUseCase(
            letterPracticeRepository = get(),
            timeUtils = get()
        )
    }

}