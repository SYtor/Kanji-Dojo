package ua.syt0r.kanji.core.srs

import org.koin.core.module.Module
import ua.syt0r.kanji.core.srs.fsrs.DefaultFsrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.Fsrs45
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
import ua.syt0r.kanji.core.srs.use_case.DefaultGetLetterDeckSrsProgressUseCase
import ua.syt0r.kanji.core.srs.use_case.DefaultGetLetterSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.DefaultGetSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.DefaultNotifySrsPreferencesChangedUseCase
import ua.syt0r.kanji.core.srs.use_case.GetLetterDeckSrsProgressUseCase
import ua.syt0r.kanji.core.srs.use_case.GetLetterSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.GetSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.NotifySrsPreferencesChangedUseCase

fun Module.applySrsDefinitions() {

    single<LetterSrsManager> {
        DefaultLetterSrsManager(
            userPreferencesRepository = get(),
            practiceRepository = get(),
            studyProgressCache = get(),
            getDeckSrsProgressUseCase = get(),
            getLetterSrsStatusUseCase = get(),
            timeUtils = get()
        )
    }

    single<CharacterStudyProgressCache> {
        DefaultCharacterStudyProgressCache(
            letterPracticeRepository = get()
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

    factory<GetSrsStatusUseCase> {
        DefaultGetSrsStatusUseCase(timeUtils = get())
    }

    factory<GetLetterSrsStatusUseCase> {
        DefaultGetLetterSrsStatusUseCase(
            characterStudyProgressCache = get(),
            getSrsStatusUseCase = get()
        )
    }

    single<SrsItemRepository> {
        DefaultSrsItemRepository(fsrsItemRepository = get())
    }

    factory<SrsScheduler> { DefaultSrsScheduler(fsrsScheduler = get()) }
    factory<FsrsScheduler> { DefaultFsrsScheduler(Fsrs45()) }

}