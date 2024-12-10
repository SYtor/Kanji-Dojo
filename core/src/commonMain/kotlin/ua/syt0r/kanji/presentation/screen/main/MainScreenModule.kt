package ua.syt0r.kanji.presentation.screen.main

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val mainScreenModule = module {

    multiplatformViewModel<MainContract.ViewModel> {
        MainScreenViewModel(
            viewModelScope = it.component1(),
            syncManager = get()
        )
    }

    single { DeepLinkHandler() }

}