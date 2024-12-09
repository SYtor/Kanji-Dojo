package ua.syt0r.kanji.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import org.koin.core.module.Module
import org.koin.dsl.module
import ua.syt0r.kanji.JvmMainBuildConfig
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProvider
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProviderJvm
import ua.syt0r.kanji.core.backup.JvmPlatformFileHandler
import ua.syt0r.kanji.core.backup.PlatformFileHandler
import ua.syt0r.kanji.core.getUserPreferencesFile
import ua.syt0r.kanji.core.logger.LoggerConfiguration
import ua.syt0r.kanji.core.sync.JvmSyncBackupFileManager
import ua.syt0r.kanji.core.sync.SyncBackupFileManager
import ua.syt0r.kanji.core.tts.JavaKanaTtsManager
import ua.syt0r.kanji.core.tts.KanaTtsManager
import ua.syt0r.kanji.core.tts.Neural2BKanaVoiceData
import ua.syt0r.kanji.core.user_data.JvmUserDataDatabaseManager
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.user_data.preferences.DefaultUserPreferencesMigrationManager
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.account.AccountScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.account.JvmAccountScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.account.JvmAccountScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.account.JvmAccountScreenViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.credits.GetCreditLibrariesUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.credits.JvmGetCreditLibrariesUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.JvmSettingsScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.JvmSponsorScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract


actual val platformComponentsModule: Module = module {

    factory { LoggerConfiguration(true) }

    factory<KanaTtsManager> {
        JavaKanaTtsManager(
            voiceData = Neural2BKanaVoiceData(JvmMainBuildConfig.kanaVoiceAssetName)
        )
    }

    single<AppDataDatabaseProvider> {
        AppDataDatabaseProviderJvm()
    }

    single<SyncBackupFileManager> {
        JvmSyncBackupFileManager()
    }

    single<DataStore<*>> {
        PreferenceDataStoreFactory.create(
            migrations = DefaultUserPreferencesMigrationManager.DefaultMigrations,
            produceFile = { getUserPreferencesFile() }
        )
    }

    single<UserDataDatabaseManager> {
        JvmUserDataDatabaseManager(
            updateLocalDataTimestampUseCase = get()
        )
    }

    factory<PlatformFileHandler> {
        JvmPlatformFileHandler()
    }

    single<SettingsScreenContract.Content> { JvmSettingsScreenContent }
    single<SponsorScreenContract.Content> { JvmSponsorScreenContent }
    single<AccountScreenContract.Content> { JvmAccountScreenContent }
    multiplatformViewModel<JvmAccountScreenContract.ViewModel> {
        JvmAccountScreenViewModel(
            coroutineScope = it.component1(),
            accountManager = get()
        )
    }

    factory<GetCreditLibrariesUseCase> { JvmGetCreditLibrariesUseCase }

}