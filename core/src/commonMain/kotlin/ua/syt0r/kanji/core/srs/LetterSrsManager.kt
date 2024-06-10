package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.mergeSharedFlows
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.CharacterStudyProgress
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeType
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository

interface LetterSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun notifyPreferencesChange()
    suspend fun getUpdatedData(): LetterSrsData
}

class DefaultLetterSrsManager(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val practiceRepository: LetterPracticeRepository,
    private val timeUtils: TimeUtils,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) : LetterSrsManager {

    companion object {
        private const val SRS_INTERVAL = 1.1f
    }

    private val preferencesChange = Channel<Unit>()

    override val dataChangeFlow: SharedFlow<Unit> = mergeSharedFlows(
        coroutineScope,
        practiceRepository.changesFlow,
        preferencesChange.consumeAsFlow()
    )

    override suspend fun notifyPreferencesChange() {
        preferencesChange.send(Unit)
    }

    override suspend fun getUpdatedData(): LetterSrsData {
        val dailyGoalConfiguration = DailyGoalConfiguration(
            enabled = userPreferencesRepository.dailyLimitEnabled.get(),
            learnLimit = userPreferencesRepository.dailyLearnLimit.get(),
            reviewLimit = userPreferencesRepository.dailyReviewLimit.get()
        )

        val now = timeUtils.now()
        val currentDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        val characterProgresses = practiceRepository.getStudyProgresses()

        val combinedCharacterProgressesMap = characterProgresses
            .groupBy { it.character }
            .toList()
            .associate { (character, progresses) ->
                val writingProgress = progresses.find {
                    it.practiceType == PracticeType.Writing
                }
                val readingProgress = progresses.find {
                    it.practiceType == PracticeType.Reading
                }

                character to CombinedCharacterProgress(
                    writingStatus = getStatus(currentDate, SRS_INTERVAL, writingProgress),
                    writingProgress = writingProgress,
                    readingStatus = getStatus(currentDate, SRS_INTERVAL, readingProgress),
                    readingProgress = readingProgress
                )
            }

        return LetterSrsData(
            characterProgresses = combinedCharacterProgressesMap,
            decks = getDecks(now, combinedCharacterProgressesMap),
            dailyGoalConfiguration = dailyGoalConfiguration,
            dailyProgress = getDailyProgress(now, characterProgresses)
        )
    }

    private fun getStatus(
        currentDate: LocalDate,
        srsInterval: Float,
        progress: CharacterStudyProgress?,
    ): CharacterProgressStatus {
        val expectedReviewDate = progress?.getExpectedReviewTime(srsInterval)
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.date
        return when {
            expectedReviewDate == null -> CharacterProgressStatus.New
            expectedReviewDate > currentDate -> CharacterProgressStatus.Done
            else -> CharacterProgressStatus.Review
        }
    }

    private suspend fun getDailyProgress(
        now: Instant,
        characterProgresses: List<CharacterStudyProgress>,
    ): DailyProgress {
        val timeZone = TimeZone.currentSystemDefault()
        val currentDate = now.toLocalDateTime(timeZone).date

        val charactersUpdatedToday = characterProgresses.filter {
            it.lastReviewTime.toLocalDateTime(timeZone).date == currentDate
        }

        val studiedToday = charactersUpdatedToday.filter {
            practiceRepository.getFirstReviewTime(it.character, it.practiceType)
                ?.toLocalDateTime(timeZone)
                ?.date == currentDate
        }

        return DailyProgress(
            studied = studiedToday.size,
            reviewed = charactersUpdatedToday.size - studiedToday.size
        )
    }

    private suspend fun getDecks(
        now: Instant,
        map: Map<String, CombinedCharacterProgress>,
    ): List<LetterSrsDeckInfo> {
        return practiceRepository.getAllPractices().map { practice ->
            val characters = practiceRepository.getKanjiForPractice(practice.id)
            val progresses = characters.associateWith {
                map[it] ?: CombinedCharacterProgress(
                    writingStatus = CharacterProgressStatus.New,
                    writingProgress = null,
                    readingStatus = CharacterProgressStatus.New,
                    readingProgress = null
                )
            }

            val lastWritingReviewTime = practiceRepository.getLastReviewTime(
                practiceId = practice.id,
                type = PracticeType.Writing
            )

            val lastReadingReviewTime = practiceRepository.getLastReviewTime(
                practiceId = practice.id,
                type = PracticeType.Reading
            )

            val reviewTime = listOfNotNull(lastWritingReviewTime, lastReadingReviewTime).maxOrNull()

            LetterSrsDeckInfo(
                id = practice.id,
                title = practice.name,
                position = practice.position,
                characters = characters,
                timeSinceLastReview = reviewTime?.let { reviewInstant -> now - reviewInstant },
                writingDetails = getDeckDetails(
                    deckProgresses = progresses,
                    practiceType = PracticeType.Writing
                ),
                readingDetails = getDeckDetails(
                    deckProgresses = progresses,
                    practiceType = PracticeType.Reading
                )
            )
        }
    }

    private fun getDeckDetails(
        deckProgresses: Map<String, CombinedCharacterProgress>,
        practiceType: PracticeType,
    ): DeckStudyProgress {
        val characterToStatus = deckProgresses.mapValues { (_, progress) ->
            progress.getStatus(practiceType)
        }

        val new = characterToStatus.filter { it.value == CharacterProgressStatus.New }.keys
        val due = characterToStatus.filter { it.value == CharacterProgressStatus.Review }.keys
        val done = characterToStatus.filter { it.value == CharacterProgressStatus.Done }.keys

        return DeckStudyProgress(
            all = characterToStatus.keys.toList(),
            done = done.toList(),
            review = due.toList(),
            new = new.toList()
        )
    }

    private fun CombinedCharacterProgress.getStatus(
        practiceType: PracticeType,
    ) = when (practiceType) {
        PracticeType.Writing -> writingStatus
        PracticeType.Reading -> readingStatus
    }

}