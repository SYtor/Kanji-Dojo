package ua.syt0r.kanji.core.user_data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ua.syt0r.kanji.core.getUserDataDirectory
import ua.syt0r.kanji.core.readUserVersion
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import java.io.File

class UserDataDatabaseManagerJvm(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : BaseUserDataDatabaseManager(coroutineScope) {

    companion object {
        private const val DEFAULT_DB_NAME = "user_data.sqlite"
    }

    override suspend fun createDatabaseConnection(): DatabaseConnection {
        val databaseFile = getDatabaseFile()
        databaseFile.mkdirs()
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
