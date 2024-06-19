package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.MutableVocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.SelectedReadingAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeReadingPriority
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.toScreenType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeQueueDataUseCase

class VocabPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val userPreferencesRepository: PracticeUserPreferencesRepository,
    private val getQueueDataUseCase: GetVocabPracticeQueueDataUseCase,
    private val practiceQueue: VocabPracticeQueue,
    private val analyticsManager: AnalyticsManager
) : VocabPracticeScreenContract.ViewModel {

    private lateinit var expressions: List<Long>

    private lateinit var _reviewState: MutableState<VocabReviewQueueState.Review>
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)

    override val state: StateFlow<ScreenState>
        get() = _state

    override fun initialize(words: List<Long>) {
        if (::expressions.isInitialized) return
        this.expressions = words

        viewModelScope.launch {
            _state.value = ScreenState.Configuration(
                practiceType = mutableStateOf(VocabPracticeType.ReadingPicker),
                shuffle = mutableStateOf(true),
                flashcard = VocabPracticeConfiguration.Flashcard(
                    readingPriority = mutableStateOf(VocabPracticeReadingPriority.Default),
                    translationInFront = mutableStateOf(false)
                ),
                readingPicker = VocabPracticeConfiguration.ReadingPicker(
                    readingPriority = mutableStateOf(
                        userPreferencesRepository.vocabReadingPriority.get().toScreenType()
                    ),
                    showMeaning = mutableStateOf(
                        userPreferencesRepository.vocabShowMeaning.get()
                    )
                )
            )
        }
    }

    override fun configure() {
        val configurationState = _state.value as? ScreenState.Configuration ?: return
        _state.value = ScreenState.Loading

        viewModelScope.launch {
            userPreferencesRepository.apply {
                val pickerState = configurationState.readingPicker
                vocabReadingPriority.set(pickerState.readingPriority.value.repoType)
                vocabShowMeaning.set(pickerState.showMeaning.value)
            }

            val data = getQueueDataUseCase(expressions, configurationState)
            practiceQueue.initialize(expressions = data)

            practiceQueue.state
                .onEach { applyToScreenState(it) }
                .launchIn(viewModelScope)
        }
    }

    override fun revealFlashcard() {
        val currentState = _reviewState.value.state as MutableVocabReviewState.Flashcard
        currentState.showAnswer.value = true
    }

    override fun submitReadingPickerAnswer(answer: String) {
        val currentState = _reviewState.value.state as MutableVocabReviewState.Reading
        currentState.apply {
            displayReading.value = currentState.revealedReading
            selectedAnswer.value = SelectedReadingAnswer(answer, currentState.correctAnswer)
        }
    }

    override fun next() {
        viewModelScope.launch { practiceQueue.completeCurrentReview() }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("expression_practice")
    }

    private fun applyToScreenState(queueState: VocabReviewQueueState) {
        when (queueState) {
            VocabReviewQueueState.Loading -> {
                _state.value = ScreenState.Loading
            }

            is VocabReviewQueueState.Review -> {
                if (::_reviewState.isInitialized.not()) {
                    _reviewState = mutableStateOf(queueState)
                } else {
                    _reviewState.value = queueState
                }

                if (_state.value !is ScreenState.Review) {
                    _state.value = ScreenState.Review(
                        state = derivedStateOf { _reviewState.value.toPracticeReviewState() }
                    )
                }
            }

            is VocabReviewQueueState.Summary -> {
                _state.value = ScreenState.Summary(
                    practiceDuration = queueState.duration,
                    results = queueState.items
                )
            }
        }
    }

    private fun VocabReviewQueueState.Review.toPracticeReviewState(): VocabPracticeReviewState {
        return VocabPracticeReviewState(
            currentPositionInQueue = progress.current,
            totalItemsInQueue = progress.total,
            reviewState = state.asImmutable
        )
    }

}