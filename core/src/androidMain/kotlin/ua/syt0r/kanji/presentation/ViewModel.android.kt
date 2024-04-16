package ua.syt0r.kanji.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.syt0r.kanji.presentation.screen.main.MainContract
import ua.syt0r.kanji.presentation.screen.main.screen.about.AboutScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.backup.BackupContract
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.HomeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_create.PracticeCreateScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_import.PracticeImportScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.PracticePreviewScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeContract
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract

/***
 * Wraps screen's view model interface with android's view model to survive configuration changes
 * Allows to use viewModel's coroutine scope
 */
@Stable
class AndroidViewModelWrapper<T>(
    provider: (coroutineScope: CoroutineScope) -> T
) : ViewModel() {
    val viewModel = provider(viewModelScope)
}


@Composable
actual inline fun <reified T> platformGetMultiplatformViewModel(): T {
    return getViewModel<AndroidViewModelWrapper<T>>(
        qualifier = named<T>()
    ).viewModel
}

inline fun <reified T> Module.androidMultiplatformViewModel() {
    viewModel<AndroidViewModelWrapper<T>>(
        qualifier = named<T>()
    ) {
        AndroidViewModelWrapper { coroutineScope -> get { parametersOf(coroutineScope) } }
    }
}

val androidViewModelModule = module {
    androidMultiplatformViewModel<MainContract.ViewModel>()
    androidMultiplatformViewModel<HomeScreenContract.ViewModel>()
    androidMultiplatformViewModel<PracticeDashboardScreenContract.ViewModel>()
    androidMultiplatformViewModel<StatsScreenContract.ViewModel>()
    androidMultiplatformViewModel<SearchScreenContract.ViewModel>()
    androidMultiplatformViewModel<AboutScreenContract.ViewModel>()
    androidMultiplatformViewModel<PracticeImportScreenContract.ViewModel>()
    androidMultiplatformViewModel<PracticeCreateScreenContract.ViewModel>()
    androidMultiplatformViewModel<PracticePreviewScreenContract.ViewModel>()
    androidMultiplatformViewModel<WritingPracticeScreenContract.ViewModel>()
    androidMultiplatformViewModel<ReadingPracticeContract.ViewModel>()
    androidMultiplatformViewModel<KanjiInfoScreenContract.ViewModel>()
    androidMultiplatformViewModel<BackupContract.ViewModel>()
    androidMultiplatformViewModel<FeedbackScreenContract.ViewModel>()
}