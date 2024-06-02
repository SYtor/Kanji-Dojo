package ua.syt0r.kanji.presentation.screen.main.screen.reading_practice

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.tts.KanaTtsManager
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.core.user_data.practice.CharacterReadingReviewResult
import ua.syt0r.kanji.core.user_data.practice.CharacterReviewOutcome
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeCharacterReviewResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavingResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ReviewAction
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeContract.ScreenState
import kotlin.math.max

class ReadingPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val loadCharactersDataUseCase: ReadingPracticeContract.LoadCharactersDataUseCase,
    private val userPreferencesRepository: PracticeUserPreferencesRepository,
    private val practiceRepository: LetterPracticeRepository,
    private val kanaTtsManager: KanaTtsManager,
    private val analyticsManager: AnalyticsManager,
    private val timeUtils: TimeUtils
) : ReadingPracticeContract.ViewModel {

    private var practiceId: Long? = null
    private lateinit var screenConfiguration: ReadingScreenConfiguration

    private lateinit var reviewManager: ReadingCharacterReviewManager
    private lateinit var reviewData: MutableStateFlow<ReadingReviewData>

    private lateinit var showAnswer: MutableState<Boolean>
    private lateinit var kanaVoiceAutoPlay: MutableState<Boolean>

    override val state: MutableState<ScreenState> = mutableStateOf(ScreenState.Loading)

    override fun initialize(configuration: MainDestination.Practice.Reading) {
        if (practiceId != null) return
        practiceId = configuration.practiceId
        state.value = ScreenState.Loading

        viewModelScope.launch {
            state.value = ScreenState.Configuration(
                characters = configuration.characterList,
                kanaRomaji = userPreferencesRepository.readingRomajiFuriganaForKanaWords.get()
            )
        }
    }

    override fun onConfigured(configuration: ReadingScreenConfiguration) {
        state.value = ScreenState.Loading
        screenConfiguration = configuration

        viewModelScope.launch {
            userPreferencesRepository.apply {
                readingRomajiFuriganaForKanaWords.set(configuration.kanaRomaji)
            }

            val items = configuration.characters.map { character ->
                ReadingCharacterReviewData(
                    character = character,
                    details = async(
                        context = Dispatchers.IO,
                        start = CoroutineStart.LAZY
                    ) {
                        loadCharactersDataUseCase.load(character)
                    },
                    history = emptyList()
                )
            }

            kanaVoiceAutoPlay = mutableStateOf(userPreferencesRepository.kanaAutoPlay.get())

            reviewManager = ReadingCharacterReviewManager(
                reviewItems = items,
                coroutineScope = viewModelScope,
                timeUtils = timeUtils,
                onCompletedCallback = { loadSavingState() }
            )

            reviewManager.currentItem
                .onEach { it.applyToState() }
                .launchIn(this)
        }
    }

    override fun select(option: ReadingPracticeSelectedOption) {
        when (option) {
            ReadingPracticeSelectedOption.RevealAnswer -> {
                showAnswer.value = true
                val characterData = reviewData.value.characterData
                if (characterData is ReadingReviewCharacterData.Kana && kanaVoiceAutoPlay.value) {
                    speakKana(characterData.reading)
                }
            }

            ReadingPracticeSelectedOption.Repeat -> {
                viewModelScope.launch {
                    reviewManager.next(
                        ReviewAction.RepeatLater(ReadingCharacterReviewHistory.Repeat)
                    )
                }
            }

            ReadingPracticeSelectedOption.Good -> {
                viewModelScope.launch {
                    reviewManager.next(ReviewAction.Next())
                }
            }
        }

    }

    override fun toggleKanaAutoPlay() {
        val newValue = !kanaVoiceAutoPlay.value
        kanaVoiceAutoPlay.value = newValue
        viewModelScope.launch { userPreferencesRepository.kanaAutoPlay.set(newValue) }
    }

    override fun speakKana(reading: KanaReading) {
        viewModelScope.launch {
            kanaTtsManager.speak(reading)
        }
    }

    override fun savePractice(result: PracticeSavingResult) {
        val currentState = state.value as ScreenState.Saving
        state.value = ScreenState.Loading
        viewModelScope.launch {
            savePracticeInternal(result)

            val totalCharacter = screenConfiguration.characters.size
            val totalMistakes = currentState.reviewResultList.asSequence()
                .map { it.mistakes }
                .reduce { acc, mistakes -> acc.plus(mistakes) }

            val reviewSummary = reviewManager.getSummary()

            state.value = ScreenState.Saved(
                practiceDuration = reviewSummary.totalReviewTime,
                accuracy = max(totalCharacter - totalMistakes, 0) / totalCharacter.toFloat() * 100,
                repeatCharacters = result.outcomes
                    .filter { it.value == CharacterReviewOutcome.Fail }
                    .keys
                    .toList(),
                goodCharacters = result.outcomes
                    .filter { it.value == CharacterReviewOutcome.Success }
                    .keys
                    .toList()
            )
        }
    }

    override fun reportScreenShown(configuration: MainDestination.Practice.Reading) {
        analyticsManager.setScreen("reading_practice")
    }

    private suspend fun ReadingCharacterReviewData.applyToState() {
        if (!details.isCompleted) {
            state.value = ScreenState.Loading
        }

        showAnswer = mutableStateOf(false)
        val readingReviewData = ReadingReviewData(
            progress = reviewManager.getProgress(),
            characterData = details.await(),
            showAnswer = showAnswer,
            kanaVoiceAutoPlay = kanaVoiceAutoPlay
        )

        if (::reviewData.isInitialized) {
            reviewData.value = readingReviewData
        } else {
            reviewData = MutableStateFlow(readingReviewData)
        }

        val currentState = state.value
        if (currentState !is ScreenState.Review) {
            state.value = ScreenState.Review(reviewData)
        }
    }

    private fun loadSavingState() {
        state.value = ScreenState.Loading
        viewModelScope.launch {
            state.value = withContext(Dispatchers.IO) {
                val reviewSummary = reviewManager.getSummary()
                ScreenState.Saving(
                    toleratedMistakesCount = userPreferencesRepository.readingToleratedMistakes.get(),
                    reviewResultList = reviewSummary.characterSummaries.map {
                        PracticeCharacterReviewResult(
                            character = it.key,
                            mistakes = it.value.details.repeats
                        )
                    }
                )
            }
        }
    }

    private suspend fun savePracticeInternal(
        result: PracticeSavingResult
    ) {
        userPreferencesRepository.readingToleratedMistakes.set(result.toleratedMistakesCount)
        val summary = reviewManager.getSummary()
        practiceRepository.saveReadingReviews(
            practiceTime = summary.startTime,
            reviewResultList = result.outcomes.map { (character, outcome) ->
                val summaryCharacterData = summary.characterSummaries.getValue(character)
                CharacterReadingReviewResult(
                    character = character,
                    practiceId = practiceId!!,
                    mistakes = summaryCharacterData.details.repeats,
                    reviewDuration = summaryCharacterData.reviewDuration,
                    outcome = outcome
                )
            }
        )
    }

}