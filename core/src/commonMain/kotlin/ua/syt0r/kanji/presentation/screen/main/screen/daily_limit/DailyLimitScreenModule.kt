package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val dailyLimitScreenModule = module {

    multiplatformViewModel<DailyLimitScreenContract.ViewModel> {
        DailyLimitScreenViewModel(
            viewModelScope = it.component1(),
            dailyLimitManager = get(),
            analyticsManager = get()
        )
    }

}