package ua.syt0r.kanji.presentation.screen.main.screen.practice_import

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val practiceImportScreenModule = module {

    multiplatformViewModel<PracticeImportScreenContract.ViewModel> {
        PracticeImportViewModel(analyticsManager = get())
    }

}