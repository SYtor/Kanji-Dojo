package ua.syt0r.kanji.core.srs

import org.koin.core.module.Module
import ua.syt0r.kanji.core.srs.use_case.DefaultGetLetterDeckSrsProgressUseCase
import ua.syt0r.kanji.core.srs.use_case.DefaultGetLetterSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.DefaultNotifySrsPreferencesChangedUseCase
import ua.syt0r.kanji.core.srs.use_case.GetLetterDeckSrsProgressUseCase
import ua.syt0r.kanji.core.srs.use_case.GetLetterSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.NotifySrsPreferencesChangedUseCase

fun Module.applySrsDefinitions() {

    single<LetterSrsManager> {
        DefaultLetterSrsManager(
            userPreferencesRepository = get(),
            practiceRepository = get(),
            getDeckSrsProgressUseCase = get(),
            getLetterSrsStatusUseCase = get(),
            timeUtils = get()
        )
    }

    factory<NotifySrsPreferencesChangedUseCase> {
        DefaultNotifySrsPreferencesChangedUseCase(manager = get())
    }

    factory<GetLetterDeckSrsProgressUseCase> {
        DefaultGetLetterDeckSrsProgressUseCase(
            repository = get(),
            getLetterSrsStatusUseCase = get()
        )
    }

    factory<GetLetterSrsStatusUseCase> {
        DefaultGetLetterSrsStatusUseCase(repository = get())
    }

}