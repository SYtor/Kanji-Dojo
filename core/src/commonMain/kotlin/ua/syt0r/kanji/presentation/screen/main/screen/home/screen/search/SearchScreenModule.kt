package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case.*

val searchScreenModule = module {

    factory<SearchScreenContract.ProcessInputUseCase> {
        SearchScreenProcessInputUseCase(
            kanjiDataRepository = get()
        )
    }

    factory<SearchScreenContract.LoadRadicalsUseCase> {
        SearchScreenLoadRadicalsUseCase(
            kanjiDataRepository = get()
        )
    }

    factory<SearchScreenContract.SearchByRadicalsUseCase> {
        SearchScreenSearchByRadicalsUseCase(
            kanjiDataRepository = get()
        )
    }

    factory<SearchScreenContract.UpdateEnabledRadicalsUseCase> {
        SearchScreenUpdateEnabledRadicalsUseCase(
            kanjiDataRepository = get()
        )
    }

    factory<SearchScreenContract.LoadMoreWordsUseCase> {
        SearchScreenLoadMoreWordsUseCase(
            kanjiDataRepository = get()
        )
    }

    factory<SearchScreenContract.ViewModel> {
        SearchViewModel(
            viewModelScope = it.component1(),
            processInputUseCase = get(),
            loadMoreWordsUseCase = get(),
            loadRadicalsUseCase = get(),
            searchByRadicalsUseCase = get(),
            updateEnabledRadicalsUseCase = get(),
            analyticsManager = get()
        )
    }

}