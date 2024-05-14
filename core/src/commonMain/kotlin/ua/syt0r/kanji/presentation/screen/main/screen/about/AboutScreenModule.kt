package ua.syt0r.kanji.presentation.screen.main.screen.about

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val aboutScreenModule = module {

    multiplatformViewModel<AboutScreenContract.ViewModel> {
        AboutScreenViewModel(analyticsManager = get())
    }

}