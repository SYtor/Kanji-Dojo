package ua.syt0r.kanji.core.user_data

import androidx.compose.ui.text.intl.Locale
import ua.syt0r.kanji.core.suspended_property.DefaultSuspendedPropertyRegistry
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyProvider
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyRegistry

class DefaultPracticeUserPreferencesRepository(
    provider: SuspendedPropertyProvider,
    private val isSystemLanguageJapanese: Boolean = Locale.current.language == "ja"
) : PracticeUserPreferencesRepository,
    SuspendedPropertyRegistry by DefaultSuspendedPropertyRegistry(provider) {

    override val noTranslationLayout: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "no_trans_layout_enabled",
            initialValueProvider = { isSystemLanguageJapanese }
        )
    }

    override val leftHandMode: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "left_handed_mode",
            initialValueProvider = { false }
        )
    }

    override val altStrokeEvaluator: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "use_alt_stroke_evaluator",
            initialValueProvider = { false }
        )
    }

    override val kanaAutoPlay: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "practice_kana_auto_play",
            initialValueProvider = { true }
        )
    }

    override val highlightRadicals: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "highlight_radicals",
            initialValueProvider = { true }
        )
    }

    override val writingRomajiInsteadOfKanaWords: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "writing_kana_words_romaji",
            initialValueProvider = { true }
        )
    }
    override val writingToleratedMistakes: SuspendedProperty<Int> = registerProperty {
        createIntProperty(
            key = "writing_tolerated_mistakes",
            initialValueProvider = { 2 }
        )
    }

    override val readingRomajiFuriganaForKanaWords: SuspendedProperty<Boolean> = registerProperty {
        createBooleanProperty(
            key = "reading_kana_words_romaji",
            initialValueProvider = { true }
        )
    }

    override val readingToleratedMistakes: SuspendedProperty<Int> = registerProperty {
        createIntProperty(
            key = "reading_tolerated_mistakes",
            initialValueProvider = { 0 }
        )
    }

}