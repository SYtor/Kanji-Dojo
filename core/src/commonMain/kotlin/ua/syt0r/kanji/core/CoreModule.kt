package ua.syt0r.kanji.core

import kotlinx.serialization.json.Json
import org.koin.dsl.binds
import org.koin.dsl.module
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.analytics.PrintAnalyticsManager
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProvider
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.SqlDelightAppDataRepository
import ua.syt0r.kanji.core.backup.BackupManager
import ua.syt0r.kanji.core.backup.BackupRestoreCompletionNotifier
import ua.syt0r.kanji.core.backup.BackupRestoreEventsProvider
import ua.syt0r.kanji.core.backup.BackupRestoreObservable
import ua.syt0r.kanji.core.backup.DefaultBackupManager
import ua.syt0r.kanji.core.feedback.DefaultFeedbackManager
import ua.syt0r.kanji.core.feedback.DefaultFeedbackUserDataProvider
import ua.syt0r.kanji.core.feedback.FeedbackManager
import ua.syt0r.kanji.core.feedback.FeedbackUserDataProvider
import ua.syt0r.kanji.core.japanese.CharacterClassifier
import ua.syt0r.kanji.core.japanese.DefaultCharacterClassifier
import ua.syt0r.kanji.core.japanese.RomajiConverter
import ua.syt0r.kanji.core.japanese.WanakanaRomajiConverter
import ua.syt0r.kanji.core.srs.applySrsDefinitions
import ua.syt0r.kanji.core.sync.addSyncDefinitions
import ua.syt0r.kanji.core.theme_manager.ThemeManager
import ua.syt0r.kanji.core.time.DefaultTimeUtils
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.DefaultUpdateLocalDataTimestampUseCase
import ua.syt0r.kanji.core.user_data.practice.FsrsItemRepository
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.practice.SqlDelightFsrsItemRepository
import ua.syt0r.kanji.core.user_data.practice.SqlDelightLetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.SqlDelightReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.practice.SqlDelightVocabPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.UpdateLocalDataTimestampUseCase
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.BackupPropertiesHolder
import ua.syt0r.kanji.core.user_data.preferences.DataStorePreferencesManager
import ua.syt0r.kanji.core.user_data.preferences.DefaultPreferencesBackupManager
import ua.syt0r.kanji.core.user_data.preferences.DefaultUserPreferencesMigrationManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesBackupManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesMigrationManager

val coreModule = module {

    applySrsDefinitions()
    addNetworkClientsDefinitions()
    addSyncDefinitions()
    addAccountDefinitions()

    single<AnalyticsManager> { PrintAnalyticsManager() }

    single<AppDataRepository> {
        val deferredDatabase = get<AppDataDatabaseProvider>().provideAsync()
        SqlDelightAppDataRepository(deferredDatabase)
    }

    single<LetterPracticeRepository> {
        SqlDelightLetterPracticeRepository(
            databaseManager = get(),
            srsItemRepository = get()
        )
    }

    single<VocabPracticeRepository> {
        SqlDelightVocabPracticeRepository(
            databaseManager = get(),
            srsItemRepository = get()
        )
    }

    factory<UpdateLocalDataTimestampUseCase> {
        DefaultUpdateLocalDataTimestampUseCase(
            appPreferences = get(),
            timeUtils = get()
        )
    }

    single<FsrsItemRepository> {
        SqlDelightFsrsItemRepository(
            userDataDatabaseManager = get(),
            backupRestoreEventsProvider = get()
        )
    }

    single<ReviewHistoryRepository> {
        SqlDelightReviewHistoryRepository(
            userDataDatabaseManager = get()
        )
    }

    factory<PreferencesBackupManager> {
        DefaultPreferencesBackupManager(
            preferencesManager = get(),
            backupPropertiesHolder = get(),
            timeUtils = get()
        )
    }

    single<UserPreferencesMigrationManager> {
        DefaultUserPreferencesMigrationManager(
            dataStore = get()
        )
    }

    single {
        DataStorePreferencesManager(
            dataStore = get(),
            migrationManager = get(),
            timeUtils = get()
        )
    } binds arrayOf(
        PreferencesManager::class,
        BackupPropertiesHolder::class
    )

    single<PreferencesContract.AppPreferences> { get<PreferencesManager>().appPreferences }

    single<PreferencesContract.PracticePreferences> { get<PreferencesManager>().practicePreferences }

    single { BackupRestoreObservable() } binds arrayOf(
        BackupRestoreCompletionNotifier::class,
        BackupRestoreEventsProvider::class
    )

    factory<BackupManager> {
        DefaultBackupManager(
            platformFileHandler = get(),
            userDataDatabaseManager = get(),
            preferencesBackupManager = get(),
            themeManager = get(),
            restoreCompletionNotifier = get()
        )
    }

    factory<TimeUtils> { DefaultTimeUtils }

    single<ThemeManager> {
        ThemeManager(appPreferences = get())
    }

    single<CharacterClassifier> { DefaultCharacterClassifier(appDataRepository = get()) }

    factory<RomajiConverter> { WanakanaRomajiConverter() }

    single<Json> { Json.Default }

    single<NetworkApi> {
        DefaultNetworkApi(
            networkClients = get(),
            json = Json { ignoreUnknownKeys = true }
        )
    }

    factory<FeedbackManager> {
        DefaultFeedbackManager(
            networkApi = get(),
            userDataProvider = get()
        )
    }

    factory<FeedbackUserDataProvider> {
        DefaultFeedbackUserDataProvider()
    }

}