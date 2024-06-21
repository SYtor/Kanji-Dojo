package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.core.stroke_evaluator.AltKanjiStrokeEvaluator
import ua.syt0r.kanji.core.stroke_evaluator.DefaultKanjiStrokeEvaluator
import ua.syt0r.kanji.core.stroke_evaluator.KanjiStrokeEvaluator
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.tts.KanaTtsManager
import ua.syt0r.kanji.core.user_data.practice.CharacterReviewOutcome
import ua.syt0r.kanji.core.user_data.practice.CharacterWritingReviewResult
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterInputState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DefaultCharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.MultipleStrokeInputContentState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeCharacterReviewResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavingResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ReviewAction
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ReviewSummary
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ReviewUserAction.Next
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ReviewUserAction.Repeat
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ReviewUserAction.StudyNext
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract.ScreenState
import kotlin.math.max


class WritingPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val loadDataUseCase: WritingPracticeScreenContract.LoadPracticeData,
    private val practiceRepository: LetterPracticeRepository,
    private val userPreferencesRepository: PracticeUserPreferencesRepository,
    private val analyticsManager: AnalyticsManager,
    private val timeUtils: TimeUtils,
    private val kanaTtsManager: KanaTtsManager
) : WritingPracticeScreenContract.ViewModel {

    private var practiceId: Long? = null
    private lateinit var screenConfiguration: WritingScreenConfiguration

    private lateinit var radicalsHighlight: MutableState<Boolean>
    private lateinit var kanaAutoPlay: MutableState<Boolean>

    private lateinit var reviewManager: WritingCharacterReviewManager
    private lateinit var reviewDataState: MutableStateFlow<WritingReviewState>
    private lateinit var characterWriterState: CharacterWriterState

    private val mistakesMap = mutableMapOf<String, Int>()

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)
    private lateinit var kanjiStrokeEvaluator: KanjiStrokeEvaluator

    override fun init(configuration: MainDestination.Practice.Writing) {
        if (practiceId != null) return

        practiceId = configuration.practiceId

        viewModelScope.launch {
            state.value = ScreenState.Configuring(
                characters = configuration.characterList,
                noTranslationsLayout = userPreferencesRepository.noTranslationLayout.get(),
                leftHandedMode = userPreferencesRepository.leftHandMode.get(),
                kanaRomaji = userPreferencesRepository
                    .writingRomajiInsteadOfKanaWords.get(),
                inputMode = userPreferencesRepository.writingInputMethod.get().toInputMode(),
                altStrokeEvaluatorEnabled = userPreferencesRepository.altStrokeEvaluator.get(),
            )
        }
    }

    override fun onPracticeConfigured(configuration: WritingScreenConfiguration) {
        state.value = ScreenState.Loading
        screenConfiguration = configuration

        viewModelScope.launch {

            userPreferencesRepository.apply {
                noTranslationLayout.set(configuration.noTranslationsLayout)
                leftHandMode.set(configuration.leftHandedMode)
                writingRomajiInsteadOfKanaWords.set(configuration.useRomajiForKanaWords)
                writingInputMethod.set(configuration.inputMode.inputMethod)
                altStrokeEvaluator.set(configuration.altStrokeEvaluatorEnabled)
            }

            radicalsHighlight = mutableStateOf(
                value = userPreferencesRepository.highlightRadicals.get()
            )

            kanaAutoPlay = mutableStateOf(
                value = userPreferencesRepository.kanaAutoPlay.get()
            )

            kanjiStrokeEvaluator = if (configuration.altStrokeEvaluatorEnabled)
                AltKanjiStrokeEvaluator()
            else
                DefaultKanjiStrokeEvaluator()

            val queueItems = loadDataUseCase.load(configuration, viewModelScope)
            reviewManager = WritingCharacterReviewManager(
                reviewItems = queueItems,
                coroutineScope = viewModelScope,
                timeUtils = timeUtils,
                onCompletedCallback = { loadSavingState() }
            )

            reviewManager.currentItem
                .onEach { it.applyToState() }
                .launchIn(this)
        }
    }

    override fun savePractice(result: PracticeSavingResult) {
        state.value = ScreenState.Loading
        viewModelScope.launch {
            userPreferencesRepository.writingToleratedMistakes.set(result.toleratedMistakesCount)

            val reviewSummary = reviewManager.getSummary()

            val characterReviewList = mistakesMap.map { (character, mistakes) ->
                val characterReviewSummary = reviewSummary.characterSummaries.getValue(character)
                CharacterWritingReviewResult(
                    character = character,
                    practiceId = practiceId!!,
                    mistakes = mistakes,
                    reviewDuration = characterReviewSummary.reviewDuration,
                    outcome = result.outcomes.getValue(character),
                    isStudy = characterReviewSummary.details.isStudy
                )
            }

            practiceRepository.saveWritingReviews(
                practiceTime = reviewSummary.startTime,
                reviewResultList = characterReviewList
            )

            val totalStrokes = reviewSummary.characterSummaries.values
                .fold(0) { a, b -> a + b.details.strokesCount }
            val totalMistakes = characterReviewList.sumOf { it.mistakes }

            state.value = ScreenState.Saved(
                practiceDuration = reviewSummary.totalReviewTime,
                accuracy = max(totalStrokes - totalMistakes, 0) / totalStrokes.toFloat() * 100,
                repeatCharacters = result.outcomes.filter { it.value == CharacterReviewOutcome.Fail }.keys.toList(),
                goodCharacters = result.outcomes.filter { it.value == CharacterReviewOutcome.Success }.keys.toList()
            )

            reportReviewResult(reviewSummary)
        }
    }

    override fun loadNextCharacter(userAction: ReviewUserAction) {
        val character = characterWriterState.character

        val mistakes = when (val inputState = characterWriterState.inputState) {
            is CharacterInputState.MultipleStroke -> inputState
                .let { it.contentState.value as? MultipleStrokeInputContentState.Processed }
                ?.mistakes

            is CharacterInputState.SingleStroke -> inputState
                .takeIf { it.drawnStrokesCount.value == characterWriterState.strokes.size }
                ?.totalMistakes
                ?.value
        } ?: return // Avoid handling quick multiple clicks on submit button during animation

        mistakesMap[character] = mistakesMap.getOrDefault(character, 0) + mistakes

        val action = when (userAction) {
            Next -> ReviewAction.Next()
            StudyNext -> ReviewAction.RepeatNow(WritingCharacterReviewHistory.Review)
            Repeat -> ReviewAction.RepeatLater(WritingCharacterReviewHistory.Repeat)
        }
        viewModelScope.launch { reviewManager.next(action) }
    }

    override fun toggleRadicalsHighlight() {
        val updatedValue = radicalsHighlight.value.not()
        radicalsHighlight.value = updatedValue
        viewModelScope.launch { userPreferencesRepository.highlightRadicals.set(updatedValue) }
    }

    override fun toggleAutoPlay() {
        val newValue = !kanaAutoPlay.value
        kanaAutoPlay.value = newValue
        viewModelScope.launch { userPreferencesRepository.kanaAutoPlay.set(newValue) }
    }

    override fun speakKana(reading: KanaReading) {
        viewModelScope.launch { kanaTtsManager.speak(reading) }
    }

    override fun reportScreenShown(configuration: MainDestination.Practice.Writing) {
        analyticsManager.setScreen("writing_practice")
        analyticsManager.sendEvent("writing_practice_configuration") {
            put("list_size", configuration.characterList.size)
        }
    }

    private suspend fun WritingCharacterReviewData.applyToState() {
        if (!details.isCompleted) {
            state.value = ScreenState.Loading
        }

        val finalDetails = details.await()

        val isStudyMode = history.last() == WritingCharacterReviewHistory.Study

        val writerConfiguration = when {
            screenConfiguration.inputMode == WritingPracticeInputMode.Character && !isStudyMode -> {
                CharacterWriterConfiguration.CharacterInput
            }

            else -> {
                CharacterWriterConfiguration.StrokeInput(isStudyMode)
            }
        }

        characterWriterState = DefaultCharacterWriterState(
            coroutineScope = viewModelScope,
            strokeEvaluator = kanjiStrokeEvaluator,
            character = finalDetails.character,
            strokes = finalDetails.strokes,
            configuration = writerConfiguration
        )

        val reviewData = WritingReviewState(
            practiceProgress = reviewManager.getProgress(),
            characterDetails = finalDetails,
            writerState = characterWriterState
        )

        if (!::reviewDataState.isInitialized) {
            reviewDataState = MutableStateFlow(reviewData)
        } else {
            reviewDataState.value = reviewData
        }

        val currentState = state.value
        if (currentState !is ScreenState.Review) {
            state.value = ScreenState.Review(
                layoutConfiguration = WritingScreenLayoutConfiguration(
                    noTranslationsLayout = screenConfiguration.noTranslationsLayout,
                    radicalsHighlight = radicalsHighlight,
                    kanaAutoPlay = kanaAutoPlay,
                    leftHandedMode = screenConfiguration.leftHandedMode
                ),
                reviewState = reviewDataState
            )
        }

        if (kanaAutoPlay.value && finalDetails is WritingReviewCharacterDetails.KanaReviewDetails) {
            delay(200)
            kanaTtsManager.speak(finalDetails.reading)
        }

    }

    private fun loadSavingState() {
        viewModelScope.launch {
            val reviewResults = mistakesMap.map { (character, mistakes) ->
                PracticeCharacterReviewResult(
                    character = character,
                    mistakes = mistakes
                )
            }
            state.value = ScreenState.Saving(
                reviewResultList = reviewResults,
                toleratedMistakesCount = userPreferencesRepository.writingToleratedMistakes.get()
            )
        }
    }

    private fun reportReviewResult(
        reviewSummary: ReviewSummary<WritingReviewCharacterSummaryDetails>
    ) {
        analyticsManager.sendEvent("writing_practice_summary") {
            put("practice_size", reviewSummary.characterSummaries.size)
            put("total_mistakes", mistakesMap.values.sum())
            put("review_duration_sec", reviewSummary.totalReviewTime.inWholeSeconds)
        }
        reviewSummary.characterSummaries.forEach { (character, _) ->
            analyticsManager.sendEvent("char_reviewed") {
                put("char", character)
                put("mistakes", mistakesMap.getValue(character))
            }
        }
    }

}
