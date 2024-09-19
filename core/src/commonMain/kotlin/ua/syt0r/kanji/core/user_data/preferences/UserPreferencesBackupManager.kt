package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.serialization.json.JsonObject

interface UserPreferencesBackupManager {
    suspend fun exportPreferences(): JsonObject
    suspend fun importPreferences(jsonObject: JsonObject)
}

class DefaultUserPreferencesBackupManager(
    private val userPreferences: UserPreferencesManager,
    userPreferencesRepository: UserPreferencesRepository,
    practiceUserPreferencesRepository: PracticeUserPreferencesRepository
) : UserPreferencesBackupManager {

    private val supportedRepositories = listOf(
        userPreferencesRepository,
        practiceUserPreferencesRepository
    )

    override suspend fun exportPreferences(): JsonObject {
        return supportedRepositories.flatMap { it.backupProperties }
            .filter { it.isModified() }
            .associate { it.key to it.backup() }
            .let { JsonObject(it) }
    }

    override suspend fun importPreferences(jsonObject: JsonObject) {
        userPreferences.clear()
        val importedPropertiesMap = jsonObject.entries.associate { it.key to it.value }
        supportedRepositories.flatMap { it.backupProperties }.forEach { property ->
            val value = importedPropertiesMap[property.key]
            if (value != null) property.restore(value)
        }
        userPreferences.migrate()
    }

}