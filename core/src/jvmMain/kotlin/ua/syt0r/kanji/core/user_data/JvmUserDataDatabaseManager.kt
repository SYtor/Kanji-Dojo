package ua.syt0r.kanji.core.user_data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import ua.syt0r.kanji.core.getUserDataDirectory
import ua.syt0r.kanji.core.readUserVersion
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import ua.syt0r.kanji.core.user_data.practice.db.BaseUserDataDatabaseManager
import java.io.File
import kotlin.coroutines.CoroutineContext

class JvmUserDataDatabaseManager(
    coroutineContext: CoroutineContext = Dispatchers.IO
) : BaseUserDataDatabaseManager(coroutineContext, coroutineContext) {

    companion object {
        private const val DEFAULT_DB_NAME = "user_data.sqlite"
    }

    override suspend fun createDatabaseConnection(): DatabaseConnection {
        val databaseFile = getDatabaseFile()
        databaseFile.parentFile.mkdirs()
        val jdbcPath = "jdbc:sqlite:${databaseFile.absolutePath}"
        val driver = JdbcSqliteDriver(jdbcPath)
        if (!databaseFile.exists()) {
            UserDataDatabase.Schema.create(driver)
        } else {
            UserDataDatabase.Schema.migrate(
                driver,
                driver.readUserVersion(),
                UserDataDatabase.Schema.version,
                *getMigrationCallbacks()
            )
        }
        return DatabaseConnection(
            sqlDriver = driver,
            database = UserDataDatabase(driver)
        )
    }

    override fun getDatabaseFile(): File {
        val userDataDirectory = getUserDataDirectory()
        return File(userDataDirectory, DEFAULT_DB_NAME)
    }

}
