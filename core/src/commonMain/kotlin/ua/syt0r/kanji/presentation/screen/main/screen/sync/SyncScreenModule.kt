package ua.syt0r.kanji.presentation.screen.main.screen.sync

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val syncScreenModule = module {

    multiplatformViewModel<SyncScreenContract.ViewModel> {
        SyncScreenViewModel(
            coroutineScope = it.component1(),
            accountManager = get(),
            syncManager = get(),
            appPreferences = get(),
            getLocalSyncDataInfoUseCase = get()
        )
    }

}