package ua.syt0r.kanji.core.sync

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import ua.syt0r.kanji.core.sync.use_case.ApplyRemoteSyncDataUseCase
import ua.syt0r.kanji.core.sync.use_case.CreateTrackingChangesSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultApplyRemoteSyncDataUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultCreateTrackingChangesSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultGetLocalSyncDataInfoUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultHandleSyncIntentUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultRefreshSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultUploadSyncDataUseCase
import ua.syt0r.kanji.core.sync.use_case.GetLocalSyncDataInfoUseCase
import ua.syt0r.kanji.core.sync.use_case.HandleSyncIntentUseCase
import ua.syt0r.kanji.core.sync.use_case.RefreshSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.UploadSyncDataUseCase

fun Module.addSyncDefinitions() {

    single<SyncManager> {
        DefaultSyncManager(
            accountManager = get(),
            handleSyncIntentUseCase = get()
        )
    }

    factory<GetLocalSyncDataInfoUseCase> {
        DefaultGetLocalSyncDataInfoUseCase(
            appPreferences = get()
        )
    }

    factory<CreateTrackingChangesSyncStateUseCase> {
        DefaultCreateTrackingChangesSyncStateUseCase(
            appPreferences = get(),
            getLocalSyncDataInfoUseCase = get()
        )
    }

    factory<HandleSyncIntentUseCase> {
        DefaultHandleSyncIntentUseCase(
            refreshSyncStateUseCase = get(),
            uploadSyncDataUseCase = get(),
            applyRemoteSyncDataUseCase = get(),
            createTrackingChangesSyncStateUseCase = get()
        )
    }

    factory<RefreshSyncStateUseCase> {
        DefaultRefreshSyncStateUseCase(
            appPreferences = get(),
            getLocalSyncDataInfoUseCase = get(),
            networkApi = get()
        )
    }

    factory<UploadSyncDataUseCase> {
        DefaultUploadSyncDataUseCase(
            appPreferences = get(),
            getLocalSyncDataInfoUseCase = get(),
            networkApi = get(),
            syncBackupFileManager = get(),
            backupManager = get(),
            json = Json
        )
    }

    factory<ApplyRemoteSyncDataUseCase> {
        DefaultApplyRemoteSyncDataUseCase(
            networkApi = get(),
            syncBackupFileManager = get(),
            backupManager = get(),
            appPreferences = get()
        )
    }

}