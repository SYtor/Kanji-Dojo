package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.use_case.*

val searchScreenModule = module {

    factory<SearchScreenContract.ProcessInputUseCase> {
        SearchScreenProcessInputUseCase(
            appDataRepository = get()
        )
    }

    factory<SearchScreenContract.LoadRadicalsUseCase> {
        SearchScreenLoadRadicalsUseCase(
            appDataRepository = get()
        )
    }

    factory<SearchScreenContract.SearchByRadicalsUseCase> {
        SearchScreenSearchByRadicalsUseCase(
            appDataRepository = get()
        )
    }

    factory<SearchScreenContract.UpdateEnabledRadicalsUseCase> {
        SearchScreenUpdateEnabledRadicalsUseCase(
            appDataRepository = get()
        )
    }

    factory<SearchScreenContract.LoadMoreWordsUseCase> {
        SearchScreenLoadMoreWordsUseCase(
            appDataRepository = get()
        )
    }

    multiplatformViewModel<SearchScreenContract.ViewModel> {
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