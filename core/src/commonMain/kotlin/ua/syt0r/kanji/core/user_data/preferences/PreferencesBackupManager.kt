package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

interface PreferencesBackupManager {
    suspend fun exportPreferences(): JsonObject
    suspend fun importPreferences(jsonObject: JsonObject)
}

class DefaultPreferencesBackupManager(
    private val preferencesManager: PreferencesManager,
    private val backupPropertiesHolder: BackupPropertiesHolder
) : PreferencesBackupManager {

    override suspend fun exportPreferences(): JsonObject {
        return backupPropertiesHolder.backupProperties
            .filter { it.isModified() }
            .mapNotNull { it.key to (it.backup() ?: return@mapNotNull null) }
            .toMap()
            .let { JsonObject(it) }
    }

    override suspend fun importPreferences(jsonObject: JsonObject) {
        preferencesManager.clear()
        val importedPropertiesMap = jsonObject.entries.associate { it.key to it.value }
        backupPropertiesHolder.backupProperties.forEach { property ->
            val value = importedPropertiesMap[property.key]
            if (value != null) property.restore(value.jsonPrimitive)
        }
        preferencesManager.migrate()
    }

}