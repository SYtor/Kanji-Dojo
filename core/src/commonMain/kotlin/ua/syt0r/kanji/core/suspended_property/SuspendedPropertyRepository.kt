package ua.syt0r.kanji.core.suspended_property


interface SuspendedPropertyRepository {

    val backupProperties: List<SuspendedProperty<*>>

    fun <T> registerProperty(
        enableBackup: Boolean = true,
        block: SuspendedPropertyProvider.() -> SuspendedProperty<T>
    ): SuspendedProperty<T>

}

class DefaultSuspendedPropertyRepository(
    private val provider: SuspendedPropertyProvider
) : SuspendedPropertyRepository {

    private val _backupProperties = mutableSetOf<SuspendedProperty<*>>()
    override val backupProperties: List<SuspendedProperty<*>>
        get() = _backupProperties.toList()

    override fun <T> registerProperty(
        enableBackup: Boolean,
        block: SuspendedPropertyProvider.() -> SuspendedProperty<T>
    ): SuspendedProperty<T> {
        val property = provider.block()
        if (enableBackup) _backupProperties.add(property)
        return property
    }

}