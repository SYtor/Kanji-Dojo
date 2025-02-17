package ua.syt0r.kanji.core.user_data.database

import org.koin.core.module.Module
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightFsrsCardRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightLetterPracticeRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.database.sqldelight.SqlDelightVocabPracticeRepository
import ua.syt0r.kanji.core.user_data.database.use_case.DefaultUpdateLocalDataTimestampUseCase
import ua.syt0r.kanji.core.user_data.database.use_case.UpdateLocalDataTimestampUseCase

fun Module.addUserDataDatabaseDefinitions() {

    single<LetterPracticeRepository> {
        SqlDelightLetterPracticeRepository(
            databaseManager = get()
        )
    }

    single<VocabPracticeRepository> {
        SqlDelightVocabPracticeRepository(
            databaseManager = get()
        )
    }

    factory<UpdateLocalDataTimestampUseCase> {
        DefaultUpdateLocalDataTimestampUseCase(
            appPreferences = get(),
            timeUtils = get()
        )
    }

    single<FsrsCardRepository> {
        SqlDelightFsrsCardRepository(
            userDataDatabaseManager = get()
        )
    }

    single<ReviewHistoryRepository> {
        SqlDelightReviewHistoryRepository(
            userDataDatabaseManager = get()
        )
    }

}