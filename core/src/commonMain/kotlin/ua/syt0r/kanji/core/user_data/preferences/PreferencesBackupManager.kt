package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import ua.syt0r.kanji.core.suspended_property.InstantSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.StringSuspendedPropertyType
import ua.syt0r.kanji.core.time.TimeUtils
import java.util.UUID

interface PreferencesBackupManager {
    suspend fun exportPreferences(): JsonObject
    suspend fun importPreferences(jsonObject: JsonObject)
}

class DefaultPreferencesBackupManager(
    private val preferencesManager: PreferencesManager,
    private val backupPropertiesHolder: BackupPropertiesHolder,
    private val timeUtils: TimeUtils
) : PreferencesBackupManager {

    override suspend fun exportPreferences(): JsonObject {
        return backupPropertiesHolder.backupProperties
            .filter { it.isModified() }
            .mapNotNull { it.key to (it.backup() ?: return@mapNotNull null) }
            .toMap()
            .let { JsonObject(it) }
    }

    override suspend fun importPreferences(jsonObject: JsonObject) {
        val importedPropertiesMap = jsonObject.entries
            .associate { it.key to it.value }
            .toMutableMap()

        val dataId = importedPropertiesMap[LOCAL_DATA_ID_KEY]
            ?: StringSuspendedPropertyType.backup(UUID.randomUUID().toString())

        val dataTimeStamp = importedPropertiesMap[LOCAL_DATA_TIMESTAMP_KEY]
            ?: InstantSuspendedPropertyType.backup(timeUtils.now())

        importedPropertiesMap.remove(LOCAL_DATA_ID_KEY)
        importedPropertiesMap.remove(LOCAL_DATA_TIMESTAMP_KEY)

        val keyToPropertyMap = backupPropertiesHolder.backupProperties.associateBy { it.key }

        keyToPropertyMap.forEach { (_, property) ->
            val value = importedPropertiesMap[property.key]
            if (value != null) property.restore(value.jsonPrimitive)
        }
        preferencesManager.migrate()

        // Restoring data related values manually in the end to avoid overwriting them while
        // importing other properties
        keyToPropertyMap.getValue(LOCAL_DATA_ID_KEY).restore(dataId.jsonPrimitive)
        keyToPropertyMap.getValue(LOCAL_DATA_TIMESTAMP_KEY).restore(dataTimeStamp.jsonPrimitive)
    }

    companion object {
        private const val LOCAL_DATA_ID_KEY = "local_data_id"
        private const val LOCAL_DATA_TIMESTAMP_KEY = "local_data_timestamp"
    }

}