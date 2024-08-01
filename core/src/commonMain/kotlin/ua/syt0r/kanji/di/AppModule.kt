package ua.syt0r.kanji.di

import org.koin.core.module.Module
import ua.syt0r.kanji.core.coreModule
import ua.syt0r.kanji.presentation.screen.main.mainScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.about.aboutScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.backup.backupScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.letterDeckDetailsScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.deckEditScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.feedbackScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.homeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.commonDashboardComponentModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.generalDashboardScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.lettersDashboardScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.searchScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.statsScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.vocabDashboardScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.kanjiInfoScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.letterDeckPickerScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.vocabPracticeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.readingPracticeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.writingPracticeScreenModule

private val screenModules = listOf(
    mainScreenModule,
    homeScreenModule,
    commonDashboardComponentModule,
    generalDashboardScreenModule,
    lettersDashboardScreenModule,
    vocabDashboardScreenModule,
    statsScreenModule,
    searchScreenModule,
    aboutScreenModule,
    letterDeckPickerScreenModule,
    deckEditScreenModule,
    letterDeckDetailsScreenModule,
    writingPracticeScreenModule,
    readingPracticeScreenModule,
    vocabPracticeScreenModule,
    kanjiInfoScreenModule,
    backupScreenModule,
    feedbackScreenModule
)

val appModules: List<Module> = screenModules + listOf(
    coreModule,
    platformComponentsModule
)