package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.MutableVocabReadingReviewState

class VocabPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val reviewManager: VocabPracticeReviewManager,
    private val analyticsManager: AnalyticsManager
) : VocabPracticeScreenContract.ViewModel {

    private lateinit var expressions: List<Long>

    private lateinit var _reviewState: MutableStateFlow<VocabReviewState>
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)

    override val state: StateFlow<ScreenState>
        get() = _state

    override fun initialize(expressions: List<Long>) {
        if (::expressions.isInitialized) return
        this.expressions = expressions

        viewModelScope.launch {
            _state.value = ScreenState.Configuration(
                practiceType = VocabPracticeType.ReadingPicker
            )
        }
    }

    override fun configure(configuration: VocabPracticeConfiguration) {
        _state.value = ScreenState.Loading
        viewModelScope.launch {
            reviewManager.initialize(
                expressions = expressions.map {
                    VocabQueueItemDescriptor(it, configuration.practiceType)
                }
            )

            reviewManager.currentState
                .onEach { it.applyToState() }
                .launchIn(viewModelScope)
        }
    }

    override fun submitAnswer(answer: String) {
        val currentState = _reviewState.value as MutableVocabReadingReviewState
        currentState.vocab.value = currentState.visibleVocab
        currentState.selectedAnswer.value = answer
    }

    override fun next() {
        viewModelScope.launch { reviewManager.next() }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("expression_practice")
    }

    private fun VocabReviewState.applyToState() {
        when (this) {
            VocabReviewState.Loading -> {
                _state.value = ScreenState.Loading
            }

            is VocabReviewState.Reading -> {
                if (::_reviewState.isInitialized.not()) {
                    _reviewState = MutableStateFlow(this)
                } else {
                    _reviewState.value = this
                }

                if (_state.value !is ScreenState.Review) {
                    _state.value = ScreenState.Review(_reviewState)
                }
            }

            is VocabReviewState.Summary -> {
                _state.value = ScreenState.Summary(
                    practiceDuration = duration
                )
            }
        }
    }

}