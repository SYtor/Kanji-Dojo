package ua.syt0r.kanji.core.sync

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import ua.syt0r.kanji.core.auth.authHttpClient
import ua.syt0r.kanji.core.sync.use_case.ApplyRemoteSyncDataUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultApplyRemoteSyncDataUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultGetLocalSyncDataInfoUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultRefreshSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultSubscribeOnSyncDataChangeUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultUploadSyncDataUseCase
import ua.syt0r.kanji.core.sync.use_case.GetLocalSyncDataInfoUseCase
import ua.syt0r.kanji.core.sync.use_case.RefreshSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.SubscribeOnSyncDataChangeUseCase
import ua.syt0r.kanji.core.sync.use_case.UploadSyncDataUseCase

fun Module.addSyncDefinitions() {

    single<SyncManager> {
        DefaultSyncManager(
            appPreferences = get(),
            subscribeOnSyncDataChangeUseCase = get(),
            refreshSyncStateUseCase = get(),
            uploadSyncDataUseCase = get(),
            applyRemoteSyncDataUseCase = get()
        )
    }

    factory<GetLocalSyncDataInfoUseCase> {
        DefaultGetLocalSyncDataInfoUseCase(
            appPreferences = get()
        )
    }

    factory<SubscribeOnSyncDataChangeUseCase> {
        DefaultSubscribeOnSyncDataChangeUseCase(
            appPreferences = get(),
            getLocalSyncDataInfoUseCase = get()
        )
    }

    factory<RefreshSyncStateUseCase> {
        DefaultRefreshSyncStateUseCase(
            appPreferences = get(),
            getLocalSyncDataInfoUseCase = get(),
            httpClient = authHttpClient(),
            json = Json
        )
    }

    factory<UploadSyncDataUseCase> {
        DefaultUploadSyncDataUseCase(
            appPreferences = get(),
            getLocalSyncDataInfoUseCase = get(),
            httpClient = authHttpClient(),
            syncBackupFileManager = get(),
            backupManager = get(),
            json = Json
        )
    }

    factory<ApplyRemoteSyncDataUseCase> {
        DefaultApplyRemoteSyncDataUseCase(
            httpClient = authHttpClient(),
            syncBackupFileManager = get(),
            backupManager = get(),
            appPreferences = get()
        )
    }

}