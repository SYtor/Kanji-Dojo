package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditingMode
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditingModeSelector

@Composable
fun VocabDeckEditingUI(
    screenState: ScreenState.VocabDeckEditing
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        val selectedMode = rememberSaveable { mutableStateOf(DeckEditingMode.Search) }

        DeckEditingModeSelector(
            selectedMode = selectedMode,
            availableOptions = listOf(DeckEditingMode.Search, DeckEditingMode.Removal)
        )

        when (selectedMode.value) {
            DeckEditingMode.Search -> {
                Text(
                    text = buildAnnotatedString {
                        append("Add new words by clicking on ")
                        appendInlineContent("icon")
                        append(" icon on search screen, during writing reviews and other places in the app")
                    },
                    inlineContent = mapOf(
                        "icon" to InlineTextContent(
                            Placeholder(24.sp, 24.sp, PlaceholderVerticalAlign.AboveBaseline),
                            children = {
                                Icon(Icons.Default.AddCircleOutline, null)
                            }
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .widthIn(max = 300.dp)
                )
            }

            DeckEditingMode.Removal -> {

            }

            DeckEditingMode.ResetSrs -> {}
        }

    }

}