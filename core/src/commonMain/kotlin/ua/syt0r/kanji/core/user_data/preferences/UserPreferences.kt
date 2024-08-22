package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyRepository

interface UserPreferencesRepository : SuspendedPropertyRepository {

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
    val dailyNewLimit: SuspendedProperty<Int>
    val dailyDueLimit: SuspendedProperty<Int>

    val reminderEnabled: SuspendedProperty<Boolean>
    val reminderTime: SuspendedProperty<LocalTime>

    val lastAppVersionWhenChangesDialogShown: SuspendedProperty<String>
    val tutorialSeen: SuspendedProperty<Boolean>
    val generalDashboardLetterPracticeType: SuspendedProperty<PreferencesLetterPracticeType>
    val generalDashboardVocabPracticeType: SuspendedProperty<PreferencesVocabPracticeType>

    val vocabDashboardVocabPracticeType: SuspendedProperty<PreferencesVocabPracticeType>

    val dashboardSortByTime: SuspendedProperty<Boolean>

}

interface PracticeUserPreferencesRepository : SuspendedPropertyRepository {

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

enum class PreferencesLetterPracticeType { Writing, Reading }
enum class PreferencesLetterSortOption { AddOrder, Frequency, Name }
enum class PreferencesDeckDetailsLetterLayout { Character, Groups }
enum class PreferencesTheme { System, Light, Dark }
enum class PreferencesLetterPracticeWritingInputMode { Stroke, Character }
enum class PreferencesVocabPracticeType { Flashcard, ReadingPicker, Writing }
enum class PreferencesVocabReadingPriority { Default, Kanji, Kana }
