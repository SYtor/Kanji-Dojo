package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.DefaultGetVocabDeckWordsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.DefaultGetVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.GetVocabDeckWordsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.GetVocabDecksUseCase

val vocabDashboardScreenModule = module {

    multiplatformViewModel<VocabDashboardScreenContract.ViewModel> {
        VocabDashboardViewModel(
            viewModelScope = it.component1(),
            getVocabDecksUseCase = get(),
            getVocabDeckWordsUseCase = get(),
            analyticsManager = get()
        )
    }

    factory<GetVocabDecksUseCase> { DefaultGetVocabDecksUseCase(get()) }

    factory<GetVocabDeckWordsUseCase> { DefaultGetVocabDeckWordsUseCase(get()) }

}