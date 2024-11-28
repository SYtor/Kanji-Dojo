package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.suspended_property.BooleanSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.EnumSuspendedPropertyType.Companion.enumSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.LocalDateSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.LocalTimeSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.LongSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.StringSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyCreatorScope
import java.util.UUID

class AppPreferences(
    creatorScope: SuspendedPropertyCreatorScope
) : PreferencesContract.AppPreferences,
    SuspendedPropertyCreatorScope by creatorScope {

    override val refreshToken: SuspendedProperty<String?> = createNullableProperty(
        type = StringSuspendedPropertyType,
        key = "refresh_token",
        initialValue = { null },
        enableBackup = false
    )

    override val idToken: SuspendedProperty<String?> = createNullableProperty(
        type = StringSuspendedPropertyType,
        key = "id_token",
        initialValue = { null },
        enableBackup = false
    )

    override val userEmail: SuspendedProperty<String?> = createNullableProperty(
        type = StringSuspendedPropertyType,
        key = "user_email",
        initialValue = { null },
        enableBackup = false
    )

    override val subscriptionDue: SuspendedProperty<LocalDate?> = createNullableProperty(
        type = LocalDateSuspendedPropertyType,
        key = "subscription_due",
        initialValue = { null },
        enableBackup = false
    )

    override val syncEnabled: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "sync_enabled",
        initialValue = { false },
        enableBackup = false
    )

    override val localDataId: SuspendedProperty<String> = createProperty(
        type = StringSuspendedPropertyType,
        key = "local_data_id",
        initialValue = { UUID.randomUUID().toString() },
        saveInitialValue = true
    )

    override val localDataTimestamp: SuspendedProperty<Long?> = createNullableProperty(
        type = LongSuspendedPropertyType,
        key = "local_data_timestamp",
        initialValue = { null }
    )

    override val lastSyncedDataInfoJson: SuspendedProperty<String?> = createNullableProperty(
        type = StringSuspendedPropertyType,
        key = "last_synced_data_info_json",
        initialValue = { null }
    )

    override val analyticsEnabled: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "analytics_enabled",
        initialValue = { true },
        enableBackup = false
    )

    override val practiceType: SuspendedProperty<PreferencesLetterPracticeType> = createProperty(
        type = enumSuspendedPropertyType<PreferencesLetterPracticeType>(),
        key = "practice_type",
        initialValue = { PreferencesLetterPracticeType.Writing }
    )

    override val filterNew: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "filter_new",
        initialValue = { true }
    )

    override val filterDue: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "filter_due",
        initialValue = { true }
    )

    override val filterDone: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "filter_done",
        initialValue = { true }
    )

    override val sortOption: SuspendedProperty<PreferencesLetterSortOption> = createProperty(
        type = enumSuspendedPropertyType<PreferencesLetterSortOption>(),
        key = "sort_option",
        initialValue = { PreferencesLetterSortOption.AddOrder }
    )

    override val isSortDescending: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "is_desc",
        initialValue = { false }
    )

    override val practicePreviewLayout: SuspendedProperty<PreferencesDeckDetailsLetterLayout> =
        createProperty(
            type = enumSuspendedPropertyType<PreferencesDeckDetailsLetterLayout>(),
            key = "practice_preview_layout2",
            initialValue = { PreferencesDeckDetailsLetterLayout.Groups }
        )

    override val kanaGroupsEnabled: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "kana_groups_enabled",
        initialValue = { true }
    )

    override val theme: SuspendedProperty<PreferencesTheme> = createProperty(
        type = enumSuspendedPropertyType<PreferencesTheme>(),
        key = "theme",
        initialValue = { PreferencesTheme.System }
    )

    override val dailyLimitEnabled: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "daily_limit_enabled",
        initialValue = { true },
        affectSync = true
    )

    override val dailyLimitConfigurationJson: SuspendedProperty<String> = createProperty(
        type = StringSuspendedPropertyType,
        key = "daily_limit_configuration",
        initialValue = { "" },
        affectSync = true
    )

    override val reminderEnabled: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "reminder_enabled",
        initialValue = { false },
        enableBackup = false
    )

    override val reminderTime: SuspendedProperty<LocalTime> = createProperty(
        type = LocalTimeSuspendedPropertyType,
        key = "reminder_time",
        initialValue = { LocalTime(hour = 9, minute = 0) },
        affectSync = true
    )

    override val lastAppVersionWhenChangesDialogShown: SuspendedProperty<String> = createProperty(
        type = StringSuspendedPropertyType,
        key = "last_changes_dialog_version_shown",
        initialValue = { "" }
    )

    override val tutorialSeen: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "tutorial_seen",
        initialValue = { false }
    )

    override val generalDashboardLetterPracticeType: SuspendedProperty<PreferencesLetterPracticeType> =
        createProperty(
            type = enumSuspendedPropertyType<PreferencesLetterPracticeType>(),
            key = "home_letter_practice_type",
            initialValue = { PreferencesLetterPracticeType.Writing }
        )

    override val generalDashboardVocabPracticeType: SuspendedProperty<PreferencesVocabPracticeType> =
        createProperty(
            type = enumSuspendedPropertyType<PreferencesVocabPracticeType>(),
            key = "home_vocab_practice_type",
            initialValue = { PreferencesVocabPracticeType.Flashcard }
        )

    override val letterDashboardPracticeType: SuspendedProperty<PreferencesLetterPracticeType> =
        createProperty(
            type = enumSuspendedPropertyType<PreferencesLetterPracticeType>(),
            key = "letter_dashboard_practice_type",
            initialValue = { PreferencesLetterPracticeType.Writing }
        )

    override val letterDashboardSortByTime: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "letter_dashboard_sort_by_time",
        initialValue = { false }
    )

    override val vocabDashboardPracticeType: SuspendedProperty<PreferencesVocabPracticeType> =
        createProperty(
            type = enumSuspendedPropertyType<PreferencesVocabPracticeType>(),
            key = "vocab_dashboard_practice_type",
            initialValue = { PreferencesVocabPracticeType.Flashcard }
        )

    override val vocabDashboardSortByTime: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "vocab_dashboard_sort_by_time",
        initialValue = { false }
    )

}
