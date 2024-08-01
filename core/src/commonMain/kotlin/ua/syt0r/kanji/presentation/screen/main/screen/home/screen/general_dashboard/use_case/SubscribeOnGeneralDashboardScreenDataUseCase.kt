package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.use_case

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.mergeSharedFlows
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.VocabSrsManager
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckStudyType
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.LetterDecksData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.LetterDecksStudyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.VocabDecksData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.VocabDecksStudyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

interface SubscribeOnGeneralDashboardScreenDataUseCase {

    operator fun invoke(
        coroutineScope: CoroutineScope,
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<ScreenState.Loaded>>

}

class DefaultSubscribeOnGeneralDashboardScreenDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val vocabSrsManager: VocabSrsManager
) : SubscribeOnGeneralDashboardScreenDataUseCase {

    override fun invoke(
        coroutineScope: CoroutineScope,
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<ScreenState.Loaded>> = refreshableDataFlow(
        dataChangeFlow = mergeSharedFlows(
            coroutineScope,
            letterSrsManager.dataChangeFlow,
            vocabSrsManager.dataChangeFlow
        ),
        lifecycleState = lifecycleState,
        valueProvider = { getLoadedState() }
    )

    private suspend fun getLoadedState(): ScreenState.Loaded = withContext(Dispatchers.IO) {
        ScreenState.Loaded(
            showAppVersionChangeHint = mutableStateOf(true),
            showTutorialHint = mutableStateOf(true),
            letterDecksData = getLetterDecksData(),
            vocabDecksInfo = getVocabDecksData()
        )
    }

    private suspend fun getLetterDecksData(): LetterDecksData {
        val data = letterSrsManager.getUpdatedDecksData()
        if (data.decks.isEmpty()) return LetterDecksData.NoDecks

        return LetterDecksData.Data(
            studyType = mutableStateOf(LetterDeckStudyType.Writing),
            studyProgressMap = listOf(
                LetterDeckStudyType.Writing,
                LetterDeckStudyType.Reading
            ).associateWith { studyType ->

                val new = mutableSetOf<String>()
                val due = mutableSetOf<String>()

                data.decks
                    .map {
                        when (studyType) {
                            LetterDeckStudyType.Reading -> it.readingDetails
                            LetterDeckStudyType.Writing -> it.writingDetails
                        }
                    }
                    .forEach {
                        new.addAll(it.new)
                        due.addAll(it.review)
                    }

                LetterDecksStudyProgress(new, due)
            }
        )
    }

    private suspend fun getVocabDecksData(): VocabDecksData {
        val data = vocabSrsManager.getUpdatedDecksData()
        if (data.decks.isEmpty()) return VocabDecksData.NoDecks

        return VocabDecksData.Data(
            studyType = mutableStateOf(VocabPracticeType.Flashcard),
            studyProgressMap = VocabPracticeType.values().associateWith { studyType ->
                val due = mutableSetOf<Long>()

                data.decks
                    .map { it.summaries.getValue(VocabPracticeType.Writing) }
                    .forEach { due.addAll(it.due) }

                VocabDecksStudyProgress(
                    due = due
                )
            }
        )
    }

}