package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import ua.syt0r.kanji.core.srs.fsrs.DefaultFsrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.Fsrs45
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
import ua.syt0r.kanji.core.srs.use_case.DefaultGetLetterDeckSrsProgressUseCase
import ua.syt0r.kanji.core.srs.use_case.DefaultGetLetterSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.DefaultGetSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.GetLetterDeckSrsProgressUseCase
import ua.syt0r.kanji.core.srs.use_case.GetLetterSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.GetSrsStatusUseCase

fun Module.applySrsDefinitions() {

    single<DailyLimitManager> {
        DefaultDailyLimitManager(
            userPreferencesRepository = get()
        )
    }

    single<LetterSrsManager> {
        DefaultLetterSrsManager(
            dailyLimitManager = get(),
            practiceRepository = get(),
            srsItemRepository = get(),
            reviewHistoryRepository = get(),
            getSrsStatusUseCase = get(),
            getLetterSrsStatusUseCase = get(),
            getDeckSrsProgressUseCase = get(),
            timeUtils = get()
        )
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
            srsItemRepository = get(),
            getSrsStatusUseCase = get()
        )
    }

    single<SrsItemRepository> {
        DefaultSrsItemRepository(fsrsItemRepository = get())
    }

    factory<SrsScheduler> { DefaultSrsScheduler(fsrsScheduler = get()) }
    factory<FsrsScheduler> { DefaultFsrsScheduler(Fsrs45()) }

    single<VocabSrsManager> {
        DefaultVocabSrsManager(
            practiceRepository = get(),
            srsItemRepository = get(),
            getSrsStatusUseCase = get(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }

}