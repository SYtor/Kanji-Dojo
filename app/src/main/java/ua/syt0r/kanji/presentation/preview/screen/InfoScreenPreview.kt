package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenUI


@Preview
@Composable
private fun Preview(
    state: ScreenState = ScreenState.Loading
) {

    AppTheme {
        InfoScreenUI(
            state = rememberUpdatedState(state),
            onUpButtonClick = {},
            onFuriganaClick = {},
            onWordClick = {}
        )
    }

}

@Preview
@Composable
private fun NoDataPreview() {
    Preview(ScreenState.NoData)
}
