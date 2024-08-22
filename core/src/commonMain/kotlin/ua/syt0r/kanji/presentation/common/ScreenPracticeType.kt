package ua.syt0r.kanji.presentation.common

import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeType
import ua.syt0r.kanji.core.user_data.preferences.PreferencesVocabPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum

interface ScreenPracticeType : DisplayableEnum

enum class ScreenLetterPracticeType(
    val dataType: LetterPracticeType,
    val preferencesType: PreferencesLetterPracticeType,
    override val titleResolver: StringResolveScope<String>,
) : ScreenPracticeType {

    Writing(
        dataType = LetterPracticeType.Writing,
        preferencesType = PreferencesLetterPracticeType.Writing,
        titleResolver = { lettersDashboard.itemWritingTitle }
    ),
    Reading(
        dataType = LetterPracticeType.Reading,
        preferencesType = PreferencesLetterPracticeType.Reading,
        titleResolver = { lettersDashboard.itemReadingTitle }
    );

    companion object {
        fun from(practiceType: LetterPracticeType): ScreenLetterPracticeType {
            return values().first { it.dataType == practiceType }
        }

        fun from(practiceType: PreferencesLetterPracticeType): ScreenLetterPracticeType {
            return values().first { it.preferencesType == practiceType }
        }
    }

}

enum class ScreenVocabPracticeType(
    val dataType: VocabPracticeType,
    val preferencesType: PreferencesVocabPracticeType,
    override val titleResolver: StringResolveScope<String>
) : ScreenPracticeType, DisplayableEnum {

    Flashcard(
        dataType = VocabPracticeType.Flashcard,
        preferencesType = PreferencesVocabPracticeType.Flashcard,
        titleResolver = { vocabPractice.practiceTypeFlashcard }
    ),
    ReadingPicker(
        dataType = VocabPracticeType.ReadingPicker,
        preferencesType = PreferencesVocabPracticeType.ReadingPicker,
        titleResolver = { vocabPractice.practiceTypeReadingPicker }
    ),
    Writing(
        dataType = VocabPracticeType.Writing,
        preferencesType = PreferencesVocabPracticeType.Writing,
        titleResolver = { vocabPractice.practiceTypeWriting }
    );

    companion object {

        fun from(practiceType: VocabPracticeType): ScreenVocabPracticeType {
            return values().first { it.dataType == practiceType }
        }

        fun from(practiceType: PreferencesVocabPracticeType): ScreenVocabPracticeType {
            return values().first { it.preferencesType == practiceType }
        }

    }

}