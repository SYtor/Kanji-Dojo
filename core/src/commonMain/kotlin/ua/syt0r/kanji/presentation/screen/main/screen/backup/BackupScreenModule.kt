package ua.syt0r.kanji.presentation.screen.main.screen.backup

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val backupScreenModule = module {

    multiplatformViewModel<BackupContract.ViewModel> {
        BackupViewModel(
            viewModelScope = it.component1(),
            backupManager = get(),
            analyticsManager = get()
        )
    }

}