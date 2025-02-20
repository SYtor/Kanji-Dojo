package ua.syt0r.kanji.presentation.screen.main.screen.info

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.info.use_case.LetterInfoLoadDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.info.use_case.LetterInfoLoadVocabUseCase

val letterInfoScreenModule = module {

    factory<InfoScreenContract.LoadDataUseCase> {
        LetterInfoLoadDataUseCase(
            appDataRepository = get(),
            characterClassifier = get(),
            analyticsManager = get()
        )
    }

    factory<InfoScreenContract.LoadCharacterWordsUseCase> {
        LetterInfoLoadVocabUseCase(
            appDataRepository = get()
        )
    }

    multiplatformViewModel<InfoScreenContract.ViewModel> {
        InfoScreenViewModel(
            viewModelScope = it.component1(),
            screenData = it.component2(),
            loadDataUseCase = get(),
            analyticsManager = get()
        )
    }

}