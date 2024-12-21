package ua.syt0r.kanji.di

import org.koin.core.module.Module
import ua.syt0r.kanji.core.coreModule
import ua.syt0r.kanji.presentation.screen.main.mainScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.about.aboutScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.backup.backupScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.daily_limit.dailyLimitScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.deckDetailsScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.deckEditScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.deckPickerScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.feedbackScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.homeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.commonDashboardComponentModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.generalDashboardScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.lettersDashboardScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.searchScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.settingsScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.statsScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.vocabDashboardScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.kanjiInfoScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.letterPracticeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.vocabPracticeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.sync.syncScreenModule

private val screenModules = listOf(
    mainScreenModule,
    homeScreenModule,
    commonDashboardComponentModule,
    generalDashboardScreenModule,
    lettersDashboardScreenModule,
    vocabDashboardScreenModule,
    statsScreenModule,
    searchScreenModule,
    settingsScreenModule,
    aboutScreenModule,
    deckPickerScreenModule,
    deckEditScreenModule,
    deckDetailsScreenModule,
    letterPracticeScreenModule,
    vocabPracticeScreenModule,
    kanjiInfoScreenModule,
    syncScreenModule,
    backupScreenModule,
    feedbackScreenModule,
    dailyLimitScreenModule
)

val appModules: List<Module> = screenModules + listOf(
    coreModule,
    platformComponentsModule
)