package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.use_case

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.BuildKonfig
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.mergeSharedFlows
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.VocabSrsManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
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
    private val vocabSrsManager: VocabSrsManager,
    private val preferencesRepository: UserPreferencesRepository,
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
            showAppVersionChangeHint = mutableStateOf(
                value = BuildKonfig.versionName != preferencesRepository.lastAppVersionWhenChangesDialogShown.get()
            ),
            showTutorialHint = mutableStateOf(
                value = !preferencesRepository.tutorialSeen.get()
            ),
            letterDecksData = getLetterDecksData(),
            vocabDecksInfo = getVocabDecksData()
        )
    }

    private suspend fun getLetterDecksData(): LetterDecksData {
        val data = letterSrsManager.getUpdatedDecksData()
        if (data.decks.isEmpty()) return LetterDecksData.NoDecks

        val practiceType = preferencesRepository.generalDashboardLetterPracticeType.get()
        return LetterDecksData.Data(
            studyType = mutableStateOf(LetterDeckStudyType.from(practiceType)),
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
                        due.addAll(it.due)
                    }

                LetterDecksStudyProgress(
                    new = new.take(data.dailyProgress.newLeft).toSet(),
                    due = due.take(data.dailyProgress.dueLeft).toSet()
                )
            }
        )
    }

    private suspend fun getVocabDecksData(): VocabDecksData {
        val data = vocabSrsManager.getUpdatedDecksData()
        if (data.decks.isEmpty()) return VocabDecksData.NoDecks

        val practiceType = preferencesRepository.generalDashboardVocabPracticeType.get()
        return VocabDecksData.Data(
            studyType = mutableStateOf(VocabPracticeType.from(practiceType)),
            studyProgressMap = VocabPracticeType.values().associateWith { studyType ->
                val due = mutableSetOf<Long>()

                data.decks
                    .map { it.summaries.getValue(studyType) }
                    .forEach { due.addAll(it.due) }

                VocabDecksStudyProgress(
                    due = due
                )
            }
        )
    }

}