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
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelectorState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.MutableVocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.SelectedReadingAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.toScreenType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeQueueDataUseCase

class VocabPracticeViewModel(
    private val viewModelScope: CoroutineScope,
    private val userPreferencesRepository: PracticeUserPreferencesRepository,
    private val getQueueDataUseCase: GetVocabPracticeQueueDataUseCase,
    private val practiceQueue: VocabPracticeQueue,
    private val analyticsManager: AnalyticsManager
) : VocabPracticeScreenContract.ViewModel {

    private lateinit var configuration: VocabPracticeScreenConfiguration

    private lateinit var _reviewState: MutableState<VocabPracticeQueueState.Review>
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)

    override val state: StateFlow<ScreenState>
        get() = _state

    override fun initialize(configuration: VocabPracticeScreenConfiguration) {
        if (this::configuration.isInitialized) return
        this.configuration = configuration

        viewModelScope.launch {
            _state.value = ScreenState.Configuration(
                practiceType = configuration.practiceType,
                itemsSelectorState = PracticeConfigurationItemsSelectorState(
                    itemToDeckIdMap = configuration.wordIdToDeckIdMap.toList(),
                    shuffle = true
                ),
                shuffle = mutableStateOf(true),
                readingPriority = mutableStateOf(
                    userPreferencesRepository.vocabReadingPriority.get().toScreenType()
                ),
                flashcard = VocabPracticeConfiguration.Flashcard(
                    translationInFront = mutableStateOf(
                        userPreferencesRepository.vocabFlashcardMeaningInFront.get()
                    )
                ),
                readingPicker = VocabPracticeConfiguration.ReadingPicker(
                    showMeaning = mutableStateOf(
                        userPreferencesRepository.vocabReadingPickerShowMeaning.get()
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
                vocabReadingPriority.set(configurationState.readingPriority.value.repoType)
                vocabReadingPickerShowMeaning.set(configurationState.readingPicker.showMeaning.value)
                vocabFlashcardMeaningInFront.set(configurationState.flashcard.translationInFront.value)
            }

            val data = getQueueDataUseCase(
                configuration = configuration,
                state = configurationState
            )

            practiceQueue.initialize(items = data)

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

    override fun next(answer: PracticeAnswer) {
        viewModelScope.launch { practiceQueue.submitAnswer(answer) }
    }

    override fun finishPractice() {
        practiceQueue.immediateFinish()
    }

    private fun applyToScreenState(queueState: VocabPracticeQueueState) {
        when (queueState) {
            VocabPracticeQueueState.Loading -> {
                _state.value = ScreenState.Loading
            }

            is VocabPracticeQueueState.Review -> {
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

            is VocabPracticeQueueState.Summary -> {
                _state.value = ScreenState.Summary(
                    practiceDuration = queueState.duration,
                    results = queueState.items
                )
            }
        }
    }

    private fun VocabPracticeQueueState.Review.toPracticeReviewState(): VocabPracticeReviewState {
        return VocabPracticeReviewState(
            progress = progress,
            reviewState = state.asImmutable,
            answers = answers
        )
    }

}