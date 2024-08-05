package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.DefaultMergeVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.DefaultSubscribeOnDashboardVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.DefaultUpdateVocabDecksOrderUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.MergeVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.SubscribeOnDashboardVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.UpdateVocabDecksOrderUseCase

val vocabDashboardScreenModule = module {

    multiplatformViewModel<VocabDashboardScreenContract.ViewModel> {
        VocabDashboardViewModel(
            viewModelScope = it.component1(),
            subscribeOnDashboardVocabDecksUseCase = get(),
            sortDecksUseCase = get(),
            mergeVocabDecksUseCase = get(),
            updateDecksOrderUseCase = get(),
            preferencesRepository = get(),
            analyticsManager = get()
        )
    }

    factory<SubscribeOnDashboardVocabDecksUseCase> {
        DefaultSubscribeOnDashboardVocabDecksUseCase(
            vocabSrsManager = get(),
            timeUtils = get()
        )
    }

    factory<MergeVocabDecksUseCase> {
        DefaultMergeVocabDecksUseCase(
            repository = get()
        )
    }

    factory<UpdateVocabDecksOrderUseCase> {
        DefaultUpdateVocabDecksOrderUseCase(
            userPreferencesRepository = get(),
            practiceRepository = get()
        )
    }

}