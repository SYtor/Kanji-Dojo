package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.java.KoinJavaComponent.getKoin
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration

@Composable
fun LetterPracticeScreen(
    configuration: LetterPracticeScreenConfiguration,
    mainNavigationState: MainNavigationState,
    viewModel: LetterPracticeScreenContract.ViewModel = getMultiplatformViewModel()
) {

    val content = remember { getKoin().get<LetterPracticeScreenContract.Content>() }
    content(configuration, mainNavigationState, viewModel)

}
