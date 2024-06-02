package ua.syt0r.kanji.core.user_data

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import ua.syt0r.kanji.core.user_data.practice.db.BaseUserDataDatabaseManager
import java.io.File

class AndroidUserDataDatabaseManager(
    private val context: Context,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : BaseUserDataDatabaseManager(coroutineScope) {

    companion object {
        private const val DEFAULT_DB_NAME = "user_data"
    }

    override suspend fun createDatabaseConnection(): DatabaseConnection {
        val driver = AndroidSqliteDriver(
            schema = UserDataDatabase.Schema,
            context = context,
            name = DEFAULT_DB_NAME,
            callback = AndroidSqliteDriver.Callback(
                UserDataDatabase.Schema,
                *getMigrationCallbacks()
            )
        )
        return DatabaseConnection(
            sqlDriver = driver,
            database = UserDataDatabase(driver)
        )
    }

    override fun getDatabaseFile(): File {
        return context.getDatabasePath(DEFAULT_DB_NAME)!!
    }

}