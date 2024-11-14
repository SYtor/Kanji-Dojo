package ua.syt0r.kanji.core.sync

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import ua.syt0r.kanji.core.auth.authHttpClient
import ua.syt0r.kanji.core.sync.use_case.DefaultGetRemoteBackupUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultRefreshSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultSubscribeOnSyncDataChangeUseCase
import ua.syt0r.kanji.core.sync.use_case.DefaultUploadBackupUseCase
import ua.syt0r.kanji.core.sync.use_case.GetRemoteBackupUseCase
import ua.syt0r.kanji.core.sync.use_case.RefreshSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.SubscribeOnSyncDataChangeUseCase
import ua.syt0r.kanji.core.sync.use_case.UploadBackupUseCase

fun Module.addSyncDefinitions() {

    single<SyncManager> {
        DefaultSyncManager(
            subscribeOnSyncDataChangeUseCase = get(),
            refreshSyncStateUseCase = get(),
            uploadBackupUseCase = get(),
            getRemoteBackupUseCase = get()
        )
    }

    factory<SubscribeOnSyncDataChangeUseCase> {
        DefaultSubscribeOnSyncDataChangeUseCase(
            syncPropertiesObservable = get(),
            letterPracticeRepository = get(),
            vocabPracticeRepository = get(),
            appPreferences = get()
        )
    }

    factory<RefreshSyncStateUseCase> {
        DefaultRefreshSyncStateUseCase(
            appPreferences = get(),
            httpClient = authHttpClient(),
            json = Json
        )
    }

    factory<UploadBackupUseCase> {
        DefaultUploadBackupUseCase(
            appPreferences = get(),
            httpClient = authHttpClient(),
            syncFileManager = get(),
            backupManager = get(),
            json = Json
        )
    }

    factory<GetRemoteBackupUseCase> {
        DefaultGetRemoteBackupUseCase(
            httpClient = authHttpClient()
        )
    }

}