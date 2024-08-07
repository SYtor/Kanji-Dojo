package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.suspended_property.DefaultSuspendedPropertyRegistry
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyProvider
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyRegistry
import ua.syt0r.kanji.core.suspended_property.createEnumProperty
import ua.syt0r.kanji.core.suspended_property.createLocalTimeProperty

class DefaultUserPreferencesRepository(
    private val provider: SuspendedPropertyProvider
) : UserPreferencesRepository,
    SuspendedPropertyRegistry by DefaultSuspendedPropertyRegistry(provider) {

    companion object {

        private const val analyticsEnabledKey = "analytics_enabled"
        private const val practiceTypeKey = "practice_type"
        private const val filterNewKey = "filter_new"
        private const val filterDueKey = "filter_due"
        private const val filterDoneKey = "filter_done"
        private const val sortOptionKey = "sort_option"
        private const val isSortDescendingKey = "is_desc"
        private const val themeKey = "theme"
        private const val dailyLimitEnabledKey = "daily_limit_enabled"
        private const val dailyNewLimitKey = "daily_learn_limit"
        private const val dailyDueLimitKey = "daily_review_limit"
        private const val reminderEnabledKey = "reminder_enabled"
        private const val reminderTimeKey = "reminder_time"
        private const val lastVersionWhenChangesDialogShownKey = "last_changes_dialog_version_shown"
        private const val practicePreviewLayoutKey = "practice_preview_layout2"
        private const val kanaGroupsEnabledKey = "kana_groups_enabled"
        private const val dashboardSortByTimeKey = "dashboard_sort_by_time"

    }

    override val analyticsEnabled: SuspendedProperty<Boolean> = provider.run {
        createBooleanProperty(
            key = analyticsEnabledKey,
            initialValueProvider = { true }
        )
    }

    override val practiceType: SuspendedProperty<PracticeType> = registerProperty {
        createEnumProperty(
            key = practiceTypeKey,
            initialValueProvider = { PracticeType.Writing }
        )
    }

    override val filterNew: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = filterNewKey,
            initialValueProvider = { true }
        )
    }

    override val filterDue: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = filterDueKey,
            initialValueProvider = { true }
        )
    }

    override val filterDone: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = filterDoneKey,
            initialValueProvider = { true }
        )
    }

    override val sortOption: SuspendedProperty<SortOption> = registerProperty {
        createEnumProperty(
            key = sortOptionKey,
            initialValueProvider = { SortOption.AddOrder }
        )
    }

    override val isSortDescending: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = isSortDescendingKey,
            initialValueProvider = { false }
        )
    }

    override val practicePreviewLayout: SuspendedProperty<PracticePreviewLayout> =
        registerProperty {
            createEnumProperty(
                key = practicePreviewLayoutKey,
                initialValueProvider = { PracticePreviewLayout.Groups }
            )
        }

    override val kanaGroupsEnabled: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = kanaGroupsEnabledKey,
            initialValueProvider = { true }
        )
    }

    override val theme: SuspendedProperty<SupportedTheme> = registerProperty {
        createEnumProperty(
            key = themeKey,
            initialValueProvider = { SupportedTheme.System }
        )
    }

    override val dailyLimitEnabled: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = dailyLimitEnabledKey,
            initialValueProvider = { false }
        )
    }

    override val dailyNewLimit: SuspendedProperty<Int> = registerProperty {
        createIntProperty(
            key = dailyNewLimitKey,
            initialValueProvider = { 4 }
        )
    }

    override val dailyDueLimit: SuspendedProperty<Int> = registerProperty {
        createIntProperty(
            key = dailyDueLimitKey,
            initialValueProvider = { 60 }
        )
    }

    override val reminderEnabled: SuspendedProperty<Boolean> = provider.run {
        createBooleanProperty(
            key = reminderEnabledKey,
            initialValueProvider = { false }
        )
    }

    override val reminderTime: SuspendedProperty<LocalTime> = provider.run {
        createLocalTimeProperty(
            key = reminderTimeKey,
            initialValueProvider = { LocalTime(hour = 9, minute = 0) }
        )
    }

    override val lastAppVersionWhenChangesDialogShown: SuspendedProperty<String> = provider.run {
        createStringProperty(
            key = lastVersionWhenChangesDialogShownKey,
            initialValueProvider = { "" }
        )
    }

    override val dashboardSortByTime: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = dashboardSortByTimeKey,
            initialValueProvider = { false }
        )
    }

}