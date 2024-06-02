package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.use_case

import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeMergeRequestData

class MergePracticeSetsUseCase(
    private val repository: LetterPracticeRepository
) : PracticeDashboardScreenContract.MergePracticeSetsUseCase {

    override suspend fun merge(data: PracticeMergeRequestData) {
        repository.createPracticeAndMerge(data.title, data.practiceIdList)
    }

}