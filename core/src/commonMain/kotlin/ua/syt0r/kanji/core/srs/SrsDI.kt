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
            appPreferences = get()
        )
    }

    single<LetterSrsManager> {
        DefaultLetterSrsManager(
            dailyLimitManager = get(),
            practiceRepository = get(),
            srsCardRepository = get(),
            reviewHistoryRepository = get(),
            timeUtils = get(),
            appPreferences = get(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }

    factory<GetSrsStatusUseCase> {
        DefaultGetSrsStatusUseCase(timeUtils = get())
    }

    single<SrsCardRepository> {
        DefaultSrsCardRepository(fsrsCardRepository = get())
    }

    factory<SrsScheduler> { DefaultSrsScheduler(fsrsScheduler = get()) }
    factory<FsrsScheduler> { DefaultFsrsScheduler(Fsrs5()) }

    single<VocabSrsManager> {
        DefaultVocabSrsManager(
            practiceRepository = get(),
            srsCardRepository = get(),
            dailyLimitManager = get(),
            timeUtils = get(),
            appPreferences = get(),
            reviewHistoryRepository = get(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }

}