package ua.syt0r.kanji.core.user_data.preferences

import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyRegistry

interface UserPreferencesRepository : SuspendedPropertyRegistry {

    val analyticsEnabled: SuspendedProperty<Boolean>

    val practiceType: SuspendedProperty<PracticeType>
    val filterNew: SuspendedProperty<Boolean>
    val filterDue: SuspendedProperty<Boolean>
    val filterDone: SuspendedProperty<Boolean>
    val sortOption: SuspendedProperty<SortOption>

    val isSortDescending: SuspendedProperty<Boolean>

    val practicePreviewLayout: SuspendedProperty<PracticePreviewLayout>

    val kanaGroupsEnabled: SuspendedProperty<Boolean>

    val theme: SuspendedProperty<SupportedTheme>

    val dailyLimitEnabled: SuspendedProperty<Boolean>
    val dailyNewLimit: SuspendedProperty<Int>
    val dailyDueLimit: SuspendedProperty<Int>

    val reminderEnabled: SuspendedProperty<Boolean>
    val reminderTime: SuspendedProperty<LocalTime>

    val lastAppVersionWhenChangesDialogShown: SuspendedProperty<String>

    val dashboardSortByTime: SuspendedProperty<Boolean>

}

interface PracticeUserPreferencesRepository : SuspendedPropertyRegistry {

    val noTranslationLayout: SuspendedProperty<Boolean>
    val leftHandMode: SuspendedProperty<Boolean>
    val altStrokeEvaluator: SuspendedProperty<Boolean>

    val highlightRadicals: SuspendedProperty<Boolean>
    val kanaAutoPlay: SuspendedProperty<Boolean>

    val writingInputMethod: SuspendedProperty<WritingInputMethod>
    val writingRomajiInsteadOfKanaWords: SuspendedProperty<Boolean>
    val writingToleratedMistakes: SuspendedProperty<Int>

    val readingRomajiFuriganaForKanaWords: SuspendedProperty<Boolean>
    val readingToleratedMistakes: SuspendedProperty<Int>

    val vocabPracticeType: SuspendedProperty<PreferencesVocabPracticeType>
    val vocabReadingPriority: SuspendedProperty<VocabReadingPriority>
    val vocabFlashcardMeaningInFront: SuspendedProperty<Boolean>
    val vocabReadingPickerShowMeaning: SuspendedProperty<Boolean>

}

enum class PracticeType { Writing, Reading }
enum class SortOption { AddOrder, Frequency, Name }
enum class PracticePreviewLayout { Character, Groups }
enum class SupportedTheme { System, Light, Dark }
enum class WritingInputMethod { Stroke, Character }
enum class PreferencesVocabPracticeType { Flashcard, ReadingPicker, Writing }
enum class VocabReadingPriority { Default, Kanji, Kana }
