package ua.syt0r.kanji

import org.koin.core.module.Module
import ua.syt0r.kanji.core.coreModule
import ua.syt0r.kanji.presentation.screen.main.mainScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.about.aboutScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.backup.backupScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.feedbackScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.homeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.practiceDashboardScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.searchScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.statsScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.vocabDashboardScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.kanjiInfoScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.practice_create.practiceCreateScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.practice_import.practiceImportScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.practicePreviewScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.vocabPracticeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.readingPracticeScreenModule
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.writingPracticeScreenModule

private val screenModules = listOf(
    mainScreenModule,
    homeScreenModule,
    practiceDashboardScreenModule,
    vocabDashboardScreenModule,
    statsScreenModule,
    searchScreenModule,
    aboutScreenModule,
    practiceImportScreenModule,
    practiceCreateScreenModule,
    practicePreviewScreenModule,
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