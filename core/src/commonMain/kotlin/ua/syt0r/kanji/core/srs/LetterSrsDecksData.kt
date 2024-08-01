package ua.syt0r.kanji.core.srs

import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.user_data.practice.CharacterStudyProgress

data class LetterSrsDecksData(
    val decks: List<LetterSrsDeckInfo>,
    val dailyLimitConfiguration: DailyLimitConfiguration,
    val dailyProgress: LetterDailyProgress
)

data class LetterSrsDeckInfo(
    val id: Long,
    val title: String,
    val position: Int,
    val characters: List<String>,
    val writingDetails: DeckStudyProgress,
    val readingDetails: DeckStudyProgress
)

data class DailyLimitConfiguration(
    val enabled: Boolean,
    val newLimit: Int,
    val dueLimit: Int
)

data class LetterDailyProgress(
    val newReviewed: Int,
    val dueReviewed: Int,
    val newLeft: Int,
    val dueLeft: Int
)

data class DeckStudyProgress(
    val charactersData: Map<String, CharacterSrsData>,
    val all: List<String>,
    val done: List<String>,
    val due: List<String>,
    val new: List<String>
)

data class CharacterSrsData(
    val character: String,
    val status: SrsItemStatus,
    val expectedReviewDate: LocalDate?,
    val studyProgress: CharacterStudyProgress?
)
