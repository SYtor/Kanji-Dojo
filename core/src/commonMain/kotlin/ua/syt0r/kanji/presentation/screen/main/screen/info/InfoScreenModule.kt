package ua.syt0r.kanji.presentation.screen.main.screen.info

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.info.use_case.InfoLoadLetterStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.info.use_case.InfoLoadVocabStateUseCase

val infoScreenModule = module {

    factory<InfoScreenContract.LoadLetterStateUseCase> {
        InfoLoadLetterStateUseCase(
            appDataRepository = get(),
            characterClassifier = get(),
            analyticsManager = get()
        )
    }

    factory<InfoScreenContract.LoadVocabStateUseCase> {
        InfoLoadVocabStateUseCase(
            appDataRepository = get()
        )
    }

    multiplatformViewModel<InfoScreenContract.ViewModel> {
        InfoScreenViewModel(
            viewModelScope = it.component1(),
            screenData = it.component2(),
            loadLetterStateUseCase = get(),
            loadVocabStateUseCase = get(),
            analyticsManager = get()
        )
    }

}