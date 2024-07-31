package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.use_case.DefaultSortDeckDashboardItemsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.use_case.SortDeckDashboardItemsUseCase

val commonDashboardComponentModule = module {

    factory<SortDeckDashboardItemsUseCase> {
        DefaultSortDeckDashboardItemsUseCase()
    }

}