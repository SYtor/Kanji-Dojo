package ua.syt0r.kanji.core.app_state

import ua.syt0r.kanji.core.user_data.practice.CharacterStudyProgress
import kotlin.time.Duration


data class LoadableData<T>(
    val isLoading: Boolean,
    val lastData: T?
)

data class AppState(
    val characterProgresses: Map<String, CombinedCharacterProgress>,
    val decks: List<DeckInfo>,
    val dailyGoalConfiguration: DailyGoalConfiguration,
    val dailyProgress: DailyProgress
)

enum class CharacterProgressStatus { New, Done, Review }

data class CombinedCharacterProgress(
    val writingStatus: CharacterProgressStatus,
    val writingProgress: CharacterStudyProgress?,
    val readingStatus: CharacterProgressStatus,
    val readingProgress: CharacterStudyProgress?
)

data class DeckInfo(
    val id: Long,
    val title: String,
    val position: Int,
    val characters: List<String>,
    val timeSinceLastReview: Duration?,
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
    val reviewed: Int
)

data class DeckStudyProgress(
    val all: List<String>,
    val done: List<String>,
    val review: List<String>,
    val new: List<String>
)