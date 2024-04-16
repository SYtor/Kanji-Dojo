package ua.syt0r.kanji.presentation.screen.main.screen.reading_practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.java.KoinJavaComponent
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun ReadingPracticeScreen(
    navigationState: MainNavigationState,
    configuration: MainDestination.Practice.Reading,
    viewModel: ReadingPracticeContract.ViewModel = getMultiplatformViewModel()
) {

    val content = remember { KoinJavaComponent.getKoin().get<ReadingPracticeContract.Content>() }
    content.Draw(configuration, navigationState, viewModel)

}
