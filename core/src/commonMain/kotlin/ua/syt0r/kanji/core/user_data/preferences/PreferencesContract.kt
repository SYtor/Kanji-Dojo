package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty


interface PreferencesContract {

    interface AppPreferences {

        val refreshToken: SuspendedProperty<String?>
        val idToken: SuspendedProperty<String?>
        val userInfo: SuspendedProperty<PreferencesUserInfo?>

        val localDataId: SuspendedProperty<String>
        val localDataTimestamp: SuspendedProperty<Instant?>
        val lastSyncedDataInfo: SuspendedProperty<PreferencesSyncDataInfo?>

        val analyticsEnabled: SuspendedProperty<Boolean>

        val practiceType: SuspendedProperty<PreferencesLetterPracticeType>
        val filterNew: SuspendedProperty<Boolean>
        val filterDue: SuspendedProperty<Boolean>
        val filterDone: SuspendedProperty<Boolean>
        val sortOption: SuspendedProperty<PreferencesLetterSortOption>

        val isSortDescending: SuspendedProperty<Boolean>

        val practicePreviewLayout: SuspendedProperty<PreferencesDeckDetailsLetterLayout>

        val kanaGroupsEnabled: SuspendedProperty<Boolean>

        val theme: SuspendedProperty<PreferencesTheme>

        val dailyLimitEnabled: SuspendedProperty<Boolean>
        val dailyLimitConfigurationJson: SuspendedProperty<String>

        val reminderEnabled: SuspendedProperty<Boolean>
        val reminderTime: SuspendedProperty<LocalTime>

        val defaultHomeTab: SuspendedProperty<PreferencesDefaultHomeTab>

        val lastAppVersionWhenChangesDialogShown: SuspendedProperty<String>
        val tutorialSeen: SuspendedProperty<Boolean>
        val generalDashboardLetterPracticeType: SuspendedProperty<PreferencesLetterPracticeType>
        val generalDashboardVocabPracticeType: SuspendedProperty<PreferencesVocabPracticeType>

        val letterDashboardPracticeType: SuspendedProperty<PreferencesLetterPracticeType>
        val letterDashboardSortByTime: SuspendedProperty<Boolean>

        val vocabDashboardPracticeType: SuspendedProperty<PreferencesVocabPracticeType>
        val vocabDashboardSortByTime: SuspendedProperty<Boolean>

    }

    interface PracticePreferences {

        val noTranslationLayout: SuspendedProperty<Boolean>
        val leftHandMode: SuspendedProperty<Boolean>
        val altStrokeEvaluator: SuspendedProperty<Boolean>

        val highlightRadicals: SuspendedProperty<Boolean>
        val kanaAutoPlay: SuspendedProperty<Boolean>

        val writingInputMethod: SuspendedProperty<PreferencesLetterPracticeWritingInputMode>
        val writingRomajiInsteadOfKanaWords: SuspendedProperty<Boolean>

        val readingRomajiFuriganaForKanaWords: SuspendedProperty<Boolean>

        val vocabReadingPriority: SuspendedProperty<PreferencesVocabReadingPriority>
        val vocabFlashcardMeaningInFront: SuspendedProperty<Boolean>
        val vocabReadingPickerShowMeaning: SuspendedProperty<Boolean>

    }

}

enum class PreferencesLetterPracticeType { Writing, Reading }
enum class PreferencesLetterSortOption { AddOrder, Frequency, Name, ReviewTime }
enum class PreferencesDeckDetailsLetterLayout { Character, Groups }
enum class PreferencesTheme { System, Light, Dark }
enum class PreferencesLetterPracticeWritingInputMode { Stroke, Character }
enum class PreferencesVocabPracticeType { Flashcard, ReadingPicker, Writing }
enum class PreferencesVocabReadingPriority { Default, Kanji, Kana }
enum class PreferencesDefaultHomeTab { GeneralDashboard, Letters, Vocab }

@Serializable
data class PreferencesSyncDataInfo(
    val dataId: String,
    val dataVersion: Long,
    val dataTimestamp: Long?
)


@Serializable
data class PreferencesUserInfo(
    val email: String,
    val subscriptionEnabled: Boolean,
    val subscriptionDue: Long?
)
