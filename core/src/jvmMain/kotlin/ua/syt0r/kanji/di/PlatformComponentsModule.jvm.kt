package ua.syt0r.kanji.di

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProvider
import ua.syt0r.kanji.core.app_data.AppDataDatabaseProviderJvm
import ua.syt0r.kanji.core.backup.JvmPlatformFileHandler
import ua.syt0r.kanji.core.backup.PlatformFileHandler
import ua.syt0r.kanji.core.logger.LoggerConfiguration
import ua.syt0r.kanji.core.suspended_property.JvmSuspendedPropertyProvider
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyProvider
import ua.syt0r.kanji.core.tts.JavaKanaTtsManager
import ua.syt0r.kanji.core.tts.KanaTtsManager
import ua.syt0r.kanji.core.tts.Neural2BKanaVoiceData
import ua.syt0r.kanji.core.user_data.JvmUserDataDatabaseManager
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.presentation.screen.main.screen.credits.GetCreditLibrariesUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.credits.JvmGetCreditLibrariesUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.JvmSettingsScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import java.util.prefs.Preferences


actual val platformComponentsModule: Module = module {

    factory { LoggerConfiguration(true) }

    factory<KanaTtsManager> { JavaKanaTtsManager(Neural2BKanaVoiceData) }

    single<AppDataDatabaseProvider> {
        AppDataDatabaseProviderJvm()
    }

    val userDataPreferencesQualifier = named("user_preferences")

    single<Preferences>(qualifier = userDataPreferencesQualifier) {
        Preferences.userRoot().node("user_preferences")
    }

    factory<SuspendedPropertyProvider> {
        JvmSuspendedPropertyProvider(preferences = get(userDataPreferencesQualifier))
    }

    single<UserDataDatabaseManager> {
        JvmUserDataDatabaseManager()
    }

    factory<PlatformFileHandler> {
        JvmPlatformFileHandler()
    }

    single<SettingsScreenContract.Content> { JvmSettingsScreenContent }

    factory<GetCreditLibrariesUseCase> { JvmGetCreditLibrariesUseCase }

}