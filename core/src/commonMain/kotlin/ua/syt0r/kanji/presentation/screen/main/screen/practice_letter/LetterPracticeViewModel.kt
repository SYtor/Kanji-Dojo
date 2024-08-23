package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.core.tts.KanaTtsManager
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeConfigurationUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeQueueDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.UpdateLetterPracticeConfigurationUseCase


class LetterPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val getConfigurationUseCase: GetLetterPracticeConfigurationUseCase,
    private val updateConfigurationUseCase: UpdateLetterPracticeConfigurationUseCase,
    private val getQueueDataUseCase: GetLetterPracticeQueueDataUseCase,
    private val practiceQueue: LetterPracticeQueue,
    private val getReviewStateUseCase: GetLetterPracticeReviewStateUseCase,
    private val analyticsManager: AnalyticsManager,
    private val kanaTtsManager: KanaTtsManager
) : LetterPracticeScreenContract.ViewModel {

    private lateinit var configuration: LetterPracticeScreenConfiguration

    private val _state = mutableStateOf<ScreenState>(ScreenState.Loading)
    override val state: State<ScreenState> = _state

    override fun initialize(configuration: LetterPracticeScreenConfiguration) {
        if (this::configuration.isInitialized) return
        this.configuration = configuration

        viewModelScope.launch {
            _state.value = ScreenState.Configuring(
                configuration = getConfigurationUseCase(configuration)
            )
        }
    }

    override fun configure() {
        val configurationState = _state.value as ScreenState.Configuring
        _state.value = ScreenState.Loading

        viewModelScope.launch {

            updateConfigurationUseCase(configurationState.configuration)

            practiceQueue.initialize(
                items = getQueueDataUseCase(configurationState.configuration)
            )

            practiceQueue.state
                .onEach {
                    when (it) {
                        LetterPracticeQueueState.Loading -> {
                            _state.value = ScreenState.Loading
                        }

                        is LetterPracticeQueueState.Review -> {
                            val reviewState = it.toScreenState()
                            _state.value = reviewState

                            reviewState.kanaAutoReadFlow()
                                .onEach { speakKana(it) }
                                .launchIn(viewModelScope)
                        }

                        is LetterPracticeQueueState.Summary -> {
                            _state.value = it.toScreenState()
                        }
                    }
                }
                .launchIn(this)
        }
    }

    override fun submitAnswer(answer: PracticeAnswer) {
        viewModelScope.launch { practiceQueue.submitAnswer(answer) }
    }

    override fun speakKana(reading: KanaReading) {
        viewModelScope.launch { kanaTtsManager.speak(reading) }
    }

    override fun finishPractice() {
        practiceQueue.immediateFinish()
    }

    override fun reportPracticeSize(configuration: LetterPracticeScreenConfiguration) {
        analyticsManager.sendEvent("writing_practice_configuration") {
            put("list_size", configuration.characterToDeckIdMap.size)
        }
    }

    private fun LetterPracticeQueueState.Review.toScreenState(): ScreenState.Review {
        val reviewState = getReviewStateUseCase(this)
        if (reviewState is LetterPracticeReviewState.Writing) {
            snapshotFlow { reviewState.reviewWriterState.progress.value }
                .filterIsInstance<CharacterWritingProgress.Completed>()
                .take(1)
                .onEach {
                    totalMistakes.value += it.mistakes
                    currentReviewMistakes.value = it.mistakes
                }
                .launchIn(viewModelScope)
        }
        return ScreenState.Review(
            practiceProgress = progress,
            reviewState = reviewState
        )
    }

    private fun LetterPracticeQueueState.Summary.toScreenState(): ScreenState.Summary {
        val accuracy: Float? = items.filterIsInstance<LetterPracticeSummaryItem.Writing>()
            .takeIf { it.isNotEmpty() }
            ?.let {
                val totalStrokeCount = it.fold(0) { sum, item -> sum + item.strokeCount }
                val totalMistakeCount = it.fold(0) { sum, item -> sum + item.mistakes }
                val correctStrokes = (totalStrokeCount - totalMistakeCount)
                    .coerceAtLeast(0)
                correctStrokes.toFloat() * 100 / totalStrokeCount
            }
        return ScreenState.Summary(
            duration = duration,
            accuracy = accuracy,
            items = items
        )
    }

    private fun ScreenState.Review.kanaAutoReadFlow(): Flow<KanaReading> = callbackFlow {
        when {
            reviewState is LetterPracticeReviewState.Reading &&
                    reviewState.itemData is LetterPracticeItemData.KanaReadingData -> {

                snapshotFlow { reviewState.revealed.value }
                    .filter { it && reviewState.layout.kanaAutoPlay.value }
                    .take(1)
                    .onEach { send(reviewState.itemData.reading) }
                    .collect()

            }

            reviewState is LetterPracticeReviewState.Writing &&
                    reviewState.itemData is LetterPracticeItemData.KanaWritingData -> {

                // Plays when writer state is switched (study/review)
                snapshotFlow { reviewState.writerState.value }
                    .filter { reviewState.layout.kanaAutoPlay.value }
                    .onEach {
                        delay(200)
                        send(reviewState.itemData.reading)
                    }
                    .collect()

            }
        }
        awaitClose()
    }

}
