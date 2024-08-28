package ua.syt0r.kanji.core.backup

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class BackupRestoreObservable : BackupRestoreCompletionNotifier, BackupRestoreEventsProvider {

    private val _onRestore = MutableSharedFlow<Unit>()
    override val onRestoreEventsFlow: SharedFlow<Unit> = _onRestore

    override suspend fun notify() {
        _onRestore.emit(Unit)
    }

}