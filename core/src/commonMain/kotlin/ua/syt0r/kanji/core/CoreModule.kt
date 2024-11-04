package ua.syt0r.kanji.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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
import ua.syt0r.kanji.core.sync.DefaultSyncManager
import ua.syt0r.kanji.core.sync.SyncManager
import ua.syt0r.kanji.core.theme_manager.ThemeManager
import ua.syt0r.kanji.core.time.DefaultTimeUtils
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.FsrsItemRepository
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.practice.SqlDelightFsrsItemRepository
import ua.syt0r.kanji.core.user_data.practice.SqlDelightLetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.SqlDelightReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.practice.SqlDelightVocabPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.DefaultPracticeUserPreferencesRepository
import ua.syt0r.kanji.core.user_data.preferences.DefaultUserPreferencesBackupManager
import ua.syt0r.kanji.core.user_data.preferences.DefaultUserPreferencesRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesBackupManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository

val coreModule = module {

    applySrsDefinitions()

    single<SyncManager> {
        DefaultSyncManager(
            userPreferencesRepository = get(),
        )
    }

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

    factory<UserPreferencesBackupManager> {
        DefaultUserPreferencesBackupManager(
            userPreferences = get(),
            userPreferencesRepository = get(),
            practiceUserPreferencesRepository = get()
        )
    }

    single<PracticeUserPreferencesRepository> {
        DefaultPracticeUserPreferencesRepository(
            suspendedPropertyProvider = get<UserPreferencesManager>().suspendedPropertyProvider
        )
    }

    single<UserPreferencesRepository> {
        DefaultUserPreferencesRepository(
            suspendedPropertyProvider = get<UserPreferencesManager>().suspendedPropertyProvider
        )
    }

    single { BackupRestoreObservable() } binds arrayOf(
        BackupRestoreCompletionNotifier::class,
        BackupRestoreEventsProvider::class
    )

    factory<BackupManager> {
        DefaultBackupManager(
            platformFileHandler = get(),
            userDataDatabaseManager = get(),
            userPreferencesBackupManager = get(),
            themeManager = get(),
            restoreCompletionNotifier = get()
        )
    }

    factory<TimeUtils> { DefaultTimeUtils }

    single<ThemeManager> {
        ThemeManager(userPreferencesRepository = get())
    }

    single<CharacterClassifier> { DefaultCharacterClassifier(appDataRepository = get()) }

    factory<RomajiConverter> { WanakanaRomajiConverter() }

    single { HttpClient(CIO) }

    factory<FeedbackManager> {
        DefaultFeedbackManager(
            httpClient = get(),
            userDataProvider = get()
        )
    }

    factory<FeedbackUserDataProvider> {
        DefaultFeedbackUserDataProvider()
    }

}