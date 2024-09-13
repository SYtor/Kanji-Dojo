package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import ua.syt0r.kanji.core.srs.fsrs.DefaultFsrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.Fsrs5
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
import ua.syt0r.kanji.core.srs.use_case.DefaultGetSrsStatusUseCase
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
            timeUtils = get(),
            userPreferencesRepository = get(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }

    factory<GetSrsStatusUseCase> {
        DefaultGetSrsStatusUseCase(timeUtils = get())
    }

    single<SrsItemRepository> {
        DefaultSrsItemRepository(fsrsItemRepository = get())
    }

    factory<SrsScheduler> { DefaultSrsScheduler(fsrsScheduler = get()) }
    factory<FsrsScheduler> { DefaultFsrsScheduler(Fsrs5()) }

    single<VocabSrsManager> {
        DefaultVocabSrsManager(
            practiceRepository = get(),
            srsItemRepository = get(),
            dailyLimitManager = get(),
            timeUtils = get(),
            userPreferencesRepository = get(),
            reviewHistoryRepository = get(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }

}