package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.suspended_property.DefaultSuspendedPropertyRepository
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyProvider
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyRepository
import ua.syt0r.kanji.core.suspended_property.createEnumProperty
import ua.syt0r.kanji.core.suspended_property.createLocalTimeProperty

class DefaultUserPreferencesRepository private constructor(
    suspendedPropertyRepository: SuspendedPropertyRepository
) : UserPreferencesRepository,
    SuspendedPropertyRepository by suspendedPropertyRepository {

    constructor(provider: SuspendedPropertyProvider) : this(
        suspendedPropertyRepository = DefaultSuspendedPropertyRepository(provider)
    )

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
            initialValueProvider = { false }
        )
    }

    override val dailyNewLimit: SuspendedProperty<Int> = registerProperty {
        createIntProperty(
            key = "daily_learn_limit",
            initialValueProvider = { 4 }
        )
    }

    override val dailyDueLimit: SuspendedProperty<Int> = registerProperty {
        createIntProperty(
            key = "daily_review_limit",
            initialValueProvider = { 60 }
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

    override val vocabDashboardVocabPracticeType: SuspendedProperty<PreferencesVocabPracticeType> =
        registerProperty {
            createEnumProperty(
                key = "vocab_dashboard_practice_type",
                initialValueProvider = { PreferencesVocabPracticeType.Flashcard }
            )
        }

    override val dashboardSortByTime: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "dashboard_sort_by_time",
            initialValueProvider = { false }
        )
    }

}
