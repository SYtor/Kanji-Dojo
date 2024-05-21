package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState

class VocabPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val reviewManager: VocabPracticeReviewManager,
    private val analyticsManager: AnalyticsManager
) : VocabPracticeScreenContract.ViewModel {

    private lateinit var expressions: List<Long>
    private lateinit var configuration: VocabPracticeConfiguration

    private lateinit var _reviewState: MutableStateFlow<VocabReviewManagingState.Review>
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)

    override val state: StateFlow<ScreenState>
        get() = _state

    override fun initialize(expressions: List<Long>) {
        if (::expressions.isInitialized) return
        this.expressions = expressions

        viewModelScope.launch {
            _state.value = ScreenState.Configuration(
                practiceType = VocabPracticeType.ReadingPicker,
                readingPriority = VocabPracticeReadingPriority.Default
            )
        }
    }

    override fun configure(configuration: VocabPracticeConfiguration) {
        _state.value = ScreenState.Loading
        this.configuration = configuration

        viewModelScope.launch {
            reviewManager.initialize(
                expressions = expressions
                    .map {
                        VocabQueueItemDescriptor(
                            id = it,
                            practiceType = configuration.practiceType,
                            priority = configuration.readingPriority
                        )
                    }
                    .shuffled()
            )

            reviewManager.state
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
        val isCorrectAnswer = _reviewState.value.isCorrectAnswer()
        viewModelScope.launch { reviewManager.completeCurrentReview(isCorrectAnswer) }
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
                    _reviewState = MutableStateFlow(this)
                } else {
                    _reviewState.value = this
                }

                if (_state.value !is ScreenState.Review) {
                    _state.value = ScreenState.Review(
                        showMeaning = configuration.showMeaning,
                        reviewState = _reviewState.map { it.asVocabReviewState }
                            .stateIn(
                                viewModelScope,
                                SharingStarted.Eagerly,
                                _reviewState.value.asVocabReviewState
                            )
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

}