package ua.syt0r.kanji

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.FdroidSponsorScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract
import ua.syt0r.kanji.presentation.screen.settings.FdroidSettingsScreenContent
import ua.syt0r.kanji.presentation.screen.settings.FdroidSettingsScreenContract
import ua.syt0r.kanji.presentation.screen.settings.FdroidSettingsViewModel

val flavorModule = module {

    single<SettingsScreenContract.Content> { FdroidSettingsScreenContent }

    multiplatformViewModel<FdroidSettingsScreenContract.ViewModel> {
        FdroidSettingsViewModel(
            viewModelScope = it.component1(),
            userPreferencesRepository = get(),
            reminderScheduler = get()
        )
    }

    single<SponsorScreenContract.Content> { FdroidSponsorScreenContent }

}