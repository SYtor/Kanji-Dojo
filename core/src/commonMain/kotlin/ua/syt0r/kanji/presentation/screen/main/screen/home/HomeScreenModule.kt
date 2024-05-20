package ua.syt0r.kanji.presentation.screen.main.screen.home

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val homeScreenModule = module {

    multiplatformViewModel<HomeScreenContract.ViewModel> { parametersHolder ->
        HomeViewModel(parametersHolder.component1())
    }

}