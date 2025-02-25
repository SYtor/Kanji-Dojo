package ua.syt0r.kanji.presentation.screen.main.screen.deck_details

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DeckDetailsApplyFilterUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DeckDetailsApplySortUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DeckDetailsCreateLetterGroupsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DeckDetailsGetConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DefaultDeckDetailsApplyFilterUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DefaultDeckDetailsApplySortUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DefaultDeckDetailsCreateLetterGroupsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DefaultDeckDetailsGetConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DefaultGetDeckDetailsVisibleDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DefaultSubscribeOnDeckDetailsDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DefaultSubscribeOnVocabDeckDetailsDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.DefaultUpdateDeckDetailsConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.GetDeckDetailsVisibleDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.SubscribeOnDeckDetailsDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.SubscribeOnVocabDeckDetailsDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case.UpdateDeckDetailsConfigurationUseCase

val deckDetailsScreenModule = module {

    multiplatformViewModel<DeckDetailsScreenContract.ViewModel> {
        DeckDetailsViewModel(
            viewModelScope = it.component1(),
            subscribeOnLettersDataUseCase = get(),
            subscribeOnVocabDataUseCase = get(),
            getConfigurationUseCase = get(),
            updateConfigurationUseCase = get(),
            getVisibleDataUseCase = get(),
            analyticsManager = get()
        )
    }

    factory<SubscribeOnDeckDetailsDataUseCase> {
        DefaultSubscribeOnDeckDetailsDataUseCase(
            letterSrsManager = get(),
            appDataRepository = get()
        )
    }

    factory<SubscribeOnVocabDeckDetailsDataUseCase> {
        DefaultSubscribeOnVocabDeckDetailsDataUseCase(
            vocabSrsManager = get(),
            vocabPracticeRepository = get(),
            appDataRepository = get()
        )
    }

    factory<DeckDetailsGetConfigurationUseCase> {
        DefaultDeckDetailsGetConfigurationUseCase(
            repository = get()
        )
    }

    factory<UpdateDeckDetailsConfigurationUseCase> {
        DefaultUpdateDeckDetailsConfigurationUseCase(
            appPreferences = get()
        )
    }

    factory<DeckDetailsCreateLetterGroupsUseCase> {
        DefaultDeckDetailsCreateLetterGroupsUseCase()
    }

    factory<DeckDetailsApplyFilterUseCase> {
        DefaultDeckDetailsApplyFilterUseCase()
    }

    factory<DeckDetailsApplySortUseCase> {
        DefaultDeckDetailsApplySortUseCase()
    }

    factory<GetDeckDetailsVisibleDataUseCase> {
        DefaultGetDeckDetailsVisibleDataUseCase(
            applyFilterUseCase = get(),
            applySortUseCase = get(),
            createGroupsUseCase = get()
        )
    }


}