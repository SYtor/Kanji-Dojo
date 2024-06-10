package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.CreatePracticeGroupsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.DefaultCreatePracticeGroupsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.DefaultGetLetterDeckDetailsConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.DefaultGetLetterDeckDetailsVisibleDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.DefaultLetterDeckDetailsApplyFilterUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.DefaultLetterDeckDetailsApplySortUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.DefaultSubscribeOnLetterDeckDetailsDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.DefaultUpdateLetterDeckDetailsConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.GetLetterDeckDetailsConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.GetLetterDeckDetailsVisibleDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.LetterDeckDetailsApplyFilterUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.LetterDeckDetailsApplySortUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.SubscribeOnLetterDeckDetailsDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case.UpdateLetterDeckDetailsConfigurationUseCase

val letterDeckDetailsScreenModule = module {

    multiplatformViewModel<LetterDeckDetailsContract.ViewModel> {
        LetterDeckDetailsViewModel(
            viewModelScope = it.component1(),
            subscribeOnDataUseCase = get(),
            getConfigurationUseCase = get(),
            updateConfigurationUseCase = get(),
            getVisibleDataUseCase = get(),
            analyticsManager = get()
        )
    }

    factory<SubscribeOnLetterDeckDetailsDataUseCase> {
        DefaultSubscribeOnLetterDeckDetailsDataUseCase(
            letterSrsManager = get(),
            appDataRepository = get(),
            practiceRepository = get()
        )
    }

    factory<GetLetterDeckDetailsConfigurationUseCase> {
        DefaultGetLetterDeckDetailsConfigurationUseCase(
            repository = get()
        )
    }

    factory<UpdateLetterDeckDetailsConfigurationUseCase> {
        DefaultUpdateLetterDeckDetailsConfigurationUseCase(
            repository = get()
        )
    }

    factory<CreatePracticeGroupsUseCase> {
        DefaultCreatePracticeGroupsUseCase()
    }

    factory<LetterDeckDetailsApplyFilterUseCase> {
        DefaultLetterDeckDetailsApplyFilterUseCase()
    }

    factory<LetterDeckDetailsApplySortUseCase> {
        DefaultLetterDeckDetailsApplySortUseCase()
    }

    factory<GetLetterDeckDetailsVisibleDataUseCase> {
        DefaultGetLetterDeckDetailsVisibleDataUseCase(
            applyFilterUseCase = get(),
            applySortUseCase = get(),
            createGroupsUseCase = get()
        )
    }


}