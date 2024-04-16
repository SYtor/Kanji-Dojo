package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.java.KoinJavaComponent.getKoin
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun WritingPracticeScreen(
    configuration: MainDestination.Practice.Writing,
    mainNavigationState: MainNavigationState,
    viewModel: WritingPracticeScreenContract.ViewModel = getMultiplatformViewModel()
) {

    val content = remember { getKoin().get<WritingPracticeScreenContract.Content>() }
    content.Draw(configuration, mainNavigationState, viewModel)

}
