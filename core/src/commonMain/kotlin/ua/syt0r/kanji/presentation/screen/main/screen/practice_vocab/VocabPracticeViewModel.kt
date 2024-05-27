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
import ua.syt0r.kanji.core.user_data.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState

class VocabPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val userPreferencesRepository: PracticeUserPreferencesRepository,
    private val practiceQueue: VocabPracticeQueue,
    private val analyticsManager: AnalyticsManager
) : VocabPracticeScreenContract.ViewModel {

    private lateinit var expressions: List<Long>
    private lateinit var configuration: VocabPracticeConfiguration

    private lateinit var _reviewState: MutableState<VocabReviewManagingState.Review>
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)

    override val state: StateFlow<ScreenState>
        get() = _state

    override fun initialize(expressions: List<Long>) {
        if (::expressions.isInitialized) return
        this.expressions = expressions

        viewModelScope.launch {
            _state.value = ScreenState.Configuration(
                practiceType = VocabPracticeType.ReadingPicker,
                shuffle = true,
                readingPriority = userPreferencesRepository.vocabReadingPriority.get()
                    .toScreenType(),
                showMeaning = userPreferencesRepository.vocabShowMeaning.get()
            )
        }
    }

    override fun configure(configuration: VocabPracticeConfiguration) {
        _state.value = ScreenState.Loading
        this.configuration = configuration

        viewModelScope.launch {
            userPreferencesRepository.apply {
                vocabReadingPriority.set(configuration.readingPriority.repoType)
                vocabShowMeaning.set(configuration.showMeaning)
            }

            val expressionDescriptors = expressions
                .map {
                    VocabQueueItemDescriptor(
                        id = it,
                        practiceType = configuration.practiceType,
                        priority = configuration.readingPriority
                    )
                }
                .run { if (configuration.shuffle) shuffled() else this }

            practiceQueue.initialize(expressions = expressionDescriptors)

            practiceQueue.state
                .onEach { it.applyToState() }
                .launchIn(viewModelScope)
        }
    }

    override fun submitAnswer(answer: String) {
        val currentState = _reviewState.value as VocabReviewManagingState.Review.Reading
        currentState.apply {
            displayReading.value = currentState.revealedReading
            selectedAnswer.value = SelectedReadingAnswer(answer, currentState.correctAnswer)
        }
    }

    override fun next() {
        val isCorrectAnswer = _reviewState.value.isCorrectAnswer() ?: return
        viewModelScope.launch { practiceQueue.completeCurrentReview(isCorrectAnswer) }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("expression_practice")
    }

    private fun VocabReviewManagingState.applyToState() {
        when (this) {
            VocabReviewManagingState.Loading -> {
                _state.value = ScreenState.Loading
            }

            is VocabReviewManagingState.Review -> {
                if (::_reviewState.isInitialized.not()) {
                    _reviewState = mutableStateOf(this)
                } else {
                    _reviewState.value = this
                }

                if (_state.value !is ScreenState.Review) {
                    _state.value = ScreenState.Review(
                        showMeaning = configuration.showMeaning,
                        practiceState = derivedStateOf {
                            _reviewState.value.toPracticeReviewState()
                        }
                    )
                }
            }

            is VocabReviewManagingState.Summary -> {
                _state.value = ScreenState.Summary(
                    practiceDuration = duration,
                    results = items
                )
            }
        }
    }

    private fun VocabReviewManagingState.Review.toPracticeReviewState(): VocabPracticeReviewState {
        val progress = practiceQueue.getProgress()
        return VocabPracticeReviewState(
            currentPositionInQueue = progress.current,
            totalItemsInQueue = progress.total,
            reviewState = asVocabReviewState
        )
    }

}