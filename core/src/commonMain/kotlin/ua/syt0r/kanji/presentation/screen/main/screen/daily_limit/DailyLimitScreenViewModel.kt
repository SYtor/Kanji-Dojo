package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.srs.PracticeLimit
import ua.syt0r.kanji.core.srs.DailyLimitConfiguration
import ua.syt0r.kanji.core.srs.DailyLimitManager
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.daily_limit.DailyLimitScreenContract.ScreenState

class DailyLimitScreenViewModel(
    private val viewModelScope: CoroutineScope,
    private val dailyLimitManager: DailyLimitManager,
    private val analyticsManager: AnalyticsManager
) : DailyLimitScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    private lateinit var configuration: DailyLimitConfiguration

    init {
        viewModelScope.launch {
            configuration = dailyLimitManager.getConfiguration()

            fun PracticeLimit.toScreenType(): LimitItem {
                return LimitItem(
                    new = LimitInputData(mutableStateOf(new.toString())),
                    due = LimitInputData(mutableStateOf(due.toString()))
                )
            }

            _state.value = ScreenState.Loaded(
                enabled = mutableStateOf(dailyLimitManager.isEnabled()),
                isLetterLimitCombined = mutableStateOf(configuration.isLetterLimitCombined),
                letterCombined = configuration.letterCombinedLimit.toScreenType(),
                letterSeparate = ScreenLetterPracticeType.values()
                    .associateWith {
                        configuration.letterSeparatedLimit.getValue(it.dataType).toScreenType()
                    },
                isVocabLimitCombined = mutableStateOf(configuration.isVocabLimitCombined),
                vocabCombined = configuration.vocabCombinedLimit.toScreenType(),
                vocabSeparate = ScreenVocabPracticeType.values()
                    .associateWith {
                        configuration.vocabSeparatedLimit.getValue(it.dataType).toScreenType()
                    }
            )
        }
    }

    override fun saveChanges() {
        val loadedState = _state.value as? ScreenState.Loaded ?: return
        _state.value = ScreenState.Saving

        fun LimitItem.toManagerType(): PracticeLimit {
            return PracticeLimit(
                new = new.validated.value!!,
                due = due.validated.value!!
            )
        }

        viewModelScope.launch {
            val configuration = loadedState.run {

                val isLetterLimitCombined: Boolean = isLetterLimitCombined.value
                val letterCombinedLimit: PracticeLimit
                val letterSeparatedLimit: Map<LetterPracticeType, PracticeLimit>

                when (isLetterLimitCombined) {
                    true -> {
                        letterCombinedLimit = letterCombined.toManagerType()
                        letterSeparatedLimit = configuration.letterSeparatedLimit
                    }

                    false -> {
                        letterCombinedLimit = configuration.letterCombinedLimit
                        letterSeparatedLimit = loadedState.letterSeparate
                            .map { (practiceType, limitItem) ->
                                practiceType.dataType to limitItem.toManagerType()
                            }
                            .toMap()
                    }
                }

                val isVocabLimitCombined: Boolean = isVocabLimitCombined.value
                val vocabCombinedLimit: PracticeLimit
                val vocabSeparatedLimit: Map<VocabPracticeType, PracticeLimit>

                when (isVocabLimitCombined) {
                    true -> {
                        vocabCombinedLimit = vocabCombined.toManagerType()
                        vocabSeparatedLimit = configuration.vocabSeparatedLimit
                    }

                    false -> {
                        vocabCombinedLimit = configuration.vocabCombinedLimit
                        vocabSeparatedLimit = loadedState.vocabSeparate
                            .map { (practiceType, limitItem) ->
                                practiceType.dataType to limitItem.toManagerType()
                            }
                            .toMap()
                    }
                }

                DailyLimitConfiguration(
                    isLetterLimitCombined = isLetterLimitCombined,
                    letterCombinedLimit = letterCombinedLimit,
                    letterSeparatedLimit = letterSeparatedLimit,
                    isVocabLimitCombined = isVocabLimitCombined,
                    vocabCombinedLimit = vocabCombinedLimit,
                    vocabSeparatedLimit = vocabSeparatedLimit
                )
            }
            dailyLimitManager.updateConfiguration(configuration)
            _state.value = ScreenState.Done
        }
    }

}