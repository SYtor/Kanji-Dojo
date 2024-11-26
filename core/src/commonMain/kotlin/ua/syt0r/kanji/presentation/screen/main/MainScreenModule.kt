package ua.syt0r.kanji.presentation.screen.main

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.MainViewModel

val mainScreenModule = module {

    multiplatformViewModel<MainContract.ViewModel> {
        MainViewModel(
            viewModelScope = it.component1(),
            syncManager = get()
        )
    }

    single { DeepLinkHandler() }

}