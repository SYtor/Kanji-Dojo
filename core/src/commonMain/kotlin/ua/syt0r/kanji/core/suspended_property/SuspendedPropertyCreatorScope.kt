package ua.syt0r.kanji.core.suspended_property


interface SuspendedPropertyCreatorScope {

    fun <ExposedType, BackingType> createProperty(
        type: SuspendedPropertyType<ExposedType, BackingType>,
        key: String,
        enableBackup: Boolean = true,
        saveInitialValue: Boolean = false,
        affectSync: Boolean = false,
        initialValue: () -> ExposedType
    ): SuspendedProperty<ExposedType>


    fun <ExposedType, BackingType> createNullableProperty(
        type: SuspendedPropertyType<ExposedType, BackingType>,
        key: String,
        enableBackup: Boolean = true,
        saveInitialValue: Boolean = false,
        affectSync: Boolean = false,
        initialValue: () -> ExposedType?
    ): SuspendedProperty<ExposedType?>

}
