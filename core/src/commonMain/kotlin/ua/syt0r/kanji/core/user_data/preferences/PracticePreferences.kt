package ua.syt0r.kanji.core.user_data.preferences

import androidx.compose.ui.text.intl.Locale
import ua.syt0r.kanji.core.suspended_property.BooleanSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.EnumSuspendedPropertyType.Companion.enumSuspendedPropertyType
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyCreatorScope

class PracticePreferences(
    suspendedPropertyCreatorScope: SuspendedPropertyCreatorScope,
    private val isSystemLanguageJapanese: Boolean = Locale.current.language == "ja"
) : PreferencesContract.PracticePreferences,
    SuspendedPropertyCreatorScope by suspendedPropertyCreatorScope {

    override val noTranslationLayout: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "no_trans_layout_enabled",
        initialValue = { isSystemLanguageJapanese }
    )

    override val leftHandMode: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "left_handed_mode",
        initialValue = { false }
    )

    override val altStrokeEvaluator: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "use_alt_stroke_evaluator",
        initialValue = { false }
    )

    override val kanaAutoPlay: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "practice_kana_auto_play",
        initialValue = { true }
    )

    override val highlightRadicals: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "highlight_radicals",
        initialValue = { true }
    )

    override val writingInputMethod = createProperty(
        type = enumSuspendedPropertyType<PreferencesLetterPracticeWritingInputMode>(),
        key = "writing_input_method",
        initialValue = { PreferencesLetterPracticeWritingInputMode.Stroke }
    )

    override val writingRomajiInsteadOfKanaWords: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "writing_kana_words_romaji",
        initialValue = { true }
    )

    override val readingRomajiFuriganaForKanaWords: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "reading_kana_words_romaji",
        initialValue = { true }
    )

    override val vocabReadingPriority: SuspendedProperty<PreferencesVocabReadingPriority> =
        createProperty(
            type = enumSuspendedPropertyType<PreferencesVocabReadingPriority>(),
            key = "vocab_reading_priority",
            initialValue = { PreferencesVocabReadingPriority.Default }
        )

    override val vocabFlashcardMeaningInFront: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "vocab_flashcard_meaning_in_front",
        initialValue = { false }
    )

    override val vocabReadingPickerShowMeaning: SuspendedProperty<Boolean> = createProperty(
        type = BooleanSuspendedPropertyType,
        key = "vocab_show_meaning",
        initialValue = { true }
    )

}