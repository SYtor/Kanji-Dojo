package ua.syt0r.kanji.core.app_data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase
import ua.syt0r.kanji.core.getUserDataDirectory
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.readUserVersion
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class AppDataDatabaseProviderJvm : AppDataDatabaseProvider {

    private val context = CoroutineScope(context = Dispatchers.IO)

    override fun provideAsync(): Deferred<AppDataDatabase> = context.async {
        val input = ClassLoader.getSystemResourceAsStream(AppDataDatabaseResourceName)!!
        val dataDirectory = getUserDataDirectory()
        dataDirectory.mkdirs()
        val dbFile = File(dataDirectory, "kanji_data.sqlite")
        withContext(Dispatchers.IO) {
            Files.copy(input, dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        val dbVersion = driver.readUserVersion()
        Logger.d("dbVersion[$dbVersion]")
        AppDataDatabase(driver)
    }

}