package ua.syt0r.kanji.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.dsl.bind
import org.koin.dsl.module
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.analytics.PrintAnalyticsManager
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProvider
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.SqlDelightAppDataRepository
import ua.syt0r.kanji.core.backup.BackupManager
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
import ua.syt0r.kanji.core.suspended_property.DefaultSuspendedPropertiesBackupManager
import ua.syt0r.kanji.core.suspended_property.DefaultSuspendedPropertyRepository
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertiesBackupManager
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyRepository
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
import ua.syt0r.kanji.core.user_data.preferences.DefaultUserPreferencesRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository

val coreModule = module {

    applySrsDefinitions()

    single<AnalyticsManager> { PrintAnalyticsManager() }

    single<AppDataRepository> {
        val deferredDatabase = get<AppDataDatabaseProvider>().provideAsync()
        SqlDelightAppDataRepository(deferredDatabase)
    }

    single<LetterPracticeRepository> {
        SqlDelightLetterPracticeRepository(
            databaseManager = get()
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
            userDataDatabaseManager = get()
        )
    }

    single<ReviewHistoryRepository> {
        SqlDelightReviewHistoryRepository(
            userDataDatabaseManager = get()
        )
    }

    single<SuspendedPropertyRepository> {
        DefaultSuspendedPropertyRepository(
            provider = get()
        )
    }

    factory<SuspendedPropertiesBackupManager> {
        DefaultSuspendedPropertiesBackupManager(
            repositories = getAll<SuspendedPropertyRepository>()
        )
    }

    single<PracticeUserPreferencesRepository> {
        DefaultPracticeUserPreferencesRepository(
            provider = get()
        )
    } bind SuspendedPropertyRepository::class

    single<UserPreferencesRepository> {
        DefaultUserPreferencesRepository(
            provider = get()
        )
    } bind SuspendedPropertyRepository::class

    factory<BackupManager> {
        DefaultBackupManager(
            platformFileHandler = get(),
            userDataDatabaseManager = get(),
            suspendedPropertiesBackupManager = get(),
            themeManager = get()
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