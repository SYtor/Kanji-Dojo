package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.DefaultGetVocabDeckWordsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.DefaultSubscribeOnDashboardVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.GetVocabDeckWordsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.SubscribeOnDashboardVocabDecksUseCase

val vocabDashboardScreenModule = module {

    multiplatformViewModel<VocabDashboardScreenContract.ViewModel> {
        VocabDashboardViewModel(
            viewModelScope = it.component1(),
            subscribeOnDashboardVocabDecksUseCase = get(),
            getVocabDeckWordsUseCase = get(),
            preferencesRepository = get(),
            analyticsManager = get()
        )
    }

    factory<SubscribeOnDashboardVocabDecksUseCase> {
        DefaultSubscribeOnDashboardVocabDecksUseCase(
            repository = get(),
            srsItemRepository = get(),
            timeUtils = get()
        )
    }

    factory<GetVocabDeckWordsUseCase> {
        DefaultGetVocabDeckWordsUseCase(appDataRepository = get())
    }

}