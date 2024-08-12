package ua.syt0r.kanji.core.suspended_property

import kotlinx.serialization.json.JsonObject

interface SuspendedPropertiesBackupManager {
    suspend fun exportModifiedProperties(): JsonObject
    suspend fun importProperties(jsonObject: JsonObject)
}

class DefaultSuspendedPropertiesBackupManager(
    private val repositories: List<SuspendedPropertyRepository>
) : SuspendedPropertiesBackupManager {

    override suspend fun exportModifiedProperties(): JsonObject {
        return repositories.flatMap { it.backupProperties }
            .filter { it.isModified() }
            .associate { it.key to it.backup() }
            .let { JsonObject(it) }
    }

    override suspend fun importProperties(jsonObject: JsonObject) {
        val importedPropertiesMap = jsonObject.entries.associate { it.key to it.value }
        repositories.flatMap { it.backupProperties }.forEach { property ->
            val value = importedPropertiesMap[property.key]
            if (value != null) property.restore(value)
        }
    }

}