package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.suspended_property.DefaultSuspendedPropertyRepository
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyProvider
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyRepository
import ua.syt0r.kanji.core.suspended_property.createEnumProperty
import ua.syt0r.kanji.core.suspended_property.createLocalTimeProperty

class DefaultUserPreferencesRepository(
    suspendedPropertyProvider: SuspendedPropertyProvider
) : UserPreferencesRepository,
    SuspendedPropertyRepository by DefaultSuspendedPropertyRepository(suspendedPropertyProvider) {

    override val refreshToken: SuspendedProperty<String?> = registerProperty(
        enableBackup = false
    ) {
        createNullableStringProperty(
            key = "refresh_token",
            initialValueProvider = { null }
        )
    }

    override val idToken: SuspendedProperty<String?> = registerProperty(
        enableBackup = false
    ) {
        createNullableStringProperty(
            key = "id_token",
            initialValueProvider = { null }
        )
    }

    override val syncEnabled: SuspendedProperty<Boolean> = registerProperty(
        enableBackup = false
    ) {
        createBooleanProperty(
            key = "sync_enabled",
            initialValueProvider = { false }
        )
    }

    override val lastDataModifyTimestamp: SuspendedProperty<Long?> = registerProperty {
        createNullableLongProperty(
            key = "last_modify_timestamp",
            initialValueProvider = { null }
        )
    }

    override val analyticsEnabled: SuspendedProperty<Boolean> = registerProperty(
        enableBackup = false
    ) {
        createBooleanProperty(
            key = "analytics_enabled",
            initialValueProvider = { true }
        )
    }

    override val practiceType: SuspendedProperty<PreferencesLetterPracticeType> = registerProperty {
        createEnumProperty(
            key = "practice_type",
            initialValueProvider = { PreferencesLetterPracticeType.Writing }
        )
    }

    override val filterNew: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "filter_new",
            initialValueProvider = { true }
        )
    }

    override val filterDue: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "filter_due",
            initialValueProvider = { true }
        )
    }

    override val filterDone: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "filter_done",
            initialValueProvider = { true }
        )
    }

    override val sortOption: SuspendedProperty<PreferencesLetterSortOption> = registerProperty {
        createEnumProperty(
            key = "sort_option",
            initialValueProvider = { PreferencesLetterSortOption.AddOrder }
        )
    }

    override val isSortDescending: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "is_desc",
            initialValueProvider = { false }
        )
    }

    override val practicePreviewLayout: SuspendedProperty<PreferencesDeckDetailsLetterLayout> =
        registerProperty {
            createEnumProperty(
                key = "practice_preview_layout2",
                initialValueProvider = { PreferencesDeckDetailsLetterLayout.Groups }
            )
        }

    override val kanaGroupsEnabled: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "kana_groups_enabled",
            initialValueProvider = { true }
        )
    }

    override val theme: SuspendedProperty<PreferencesTheme> = registerProperty {
        createEnumProperty(
            key = "theme",
            initialValueProvider = { PreferencesTheme.System }
        )
    }

    override val dailyLimitEnabled: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "daily_limit_enabled",
            initialValueProvider = { true }
        )
    }

    override val dailyLimitConfigurationJson: SuspendedProperty<String> = registerProperty {
        createStringProperty(
            key = "daily_limit_configuration",
            initialValueProvider = { "" }
        )
    }

    override val reminderEnabled: SuspendedProperty<Boolean> = registerProperty(
        enableBackup = false
    ) {
        createBooleanProperty(
            key = "reminder_enabled",
            initialValueProvider = { false }
        )
    }

    override val reminderTime: SuspendedProperty<LocalTime> = registerProperty(
        enableBackup = false
    ) {
        createLocalTimeProperty(
            key = "reminder_time",
            initialValueProvider = { LocalTime(hour = 9, minute = 0) }
        )
    }

    override val lastAppVersionWhenChangesDialogShown: SuspendedProperty<String> = registerProperty(
        enableBackup = false
    ) {
        createStringProperty(
            key = "last_changes_dialog_version_shown",
            initialValueProvider = { "" }
        )
    }

    override val tutorialSeen: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "tutorial_seen",
            initialValueProvider = { false }
        )
    }

    override val generalDashboardLetterPracticeType: SuspendedProperty<PreferencesLetterPracticeType> =
        registerProperty {
            createEnumProperty(
                key = "home_letter_practice_type",
                initialValueProvider = { PreferencesLetterPracticeType.Writing }
            )
        }

    override val generalDashboardVocabPracticeType: SuspendedProperty<PreferencesVocabPracticeType> =
        registerProperty {
            createEnumProperty(
                key = "home_vocab_practice_type",
                initialValueProvider = { PreferencesVocabPracticeType.Flashcard }
            )
        }

    override val letterDashboardPracticeType: SuspendedProperty<PreferencesLetterPracticeType> =
        registerProperty {
            createEnumProperty(
                key = "letter_dashboard_practice_type",
                initialValueProvider = { PreferencesLetterPracticeType.Writing }
            )
        }

    override val letterDashboardSortByTime: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "letter_dashboard_sort_by_time",
            initialValueProvider = { false }
        )
    }

    override val vocabDashboardPracticeType: SuspendedProperty<PreferencesVocabPracticeType> =
        registerProperty {
            createEnumProperty(
                key = "vocab_dashboard_practice_type",
                initialValueProvider = { PreferencesVocabPracticeType.Flashcard }
            )
        }

    override val vocabDashboardSortByTime: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "vocab_dashboard_sort_by_time",
            initialValueProvider = { false }
        )
    }

}
