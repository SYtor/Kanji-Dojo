package ua.syt0r.kanji.core.srs

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.user_data.practice.CharacterStudyProgress

data class LetterSrsDecksData(
    val decks: List<LetterSrsDeckInfo>,
    val dailyGoalConfiguration: DailyGoalConfiguration,
    val dailyProgress: DailyProgress
)

enum class SrsItemStatus { New, Done, Review }

data class CharacterSrsData(
    val character: String,
    val status: SrsItemStatus,
    val expectedReviewDate: LocalDate?,
    val studyProgress: CharacterStudyProgress?
)

data class LetterSrsDeckInfo(
    val id: Long,
    val title: String,
    val position: Int,
    val characters: List<String>,
    val lastReviewTime: Instant?,
    val writingDetails: DeckStudyProgress,
    val readingDetails: DeckStudyProgress
)

data class DailyGoalConfiguration(
    val enabled: Boolean,
    val learnLimit: Int,
    val reviewLimit: Int
)

data class DailyProgress(
    val studied: Int,
    val reviewed: Int,
    val leftToStudy: Int,
    val leftToReview: Int
)

data class DeckStudyProgress(
    val all: List<CharacterSrsData>,
    val done: List<String>,
    val review: List<String>,
    val new: List<String>
)