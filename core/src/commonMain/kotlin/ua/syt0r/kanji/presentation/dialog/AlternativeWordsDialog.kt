package ua.syt0r.kanji.presentation.dialog

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.resources.string.resolveString


@Composable
fun AlternativeWordsDialog(
    word: JapaneseWord,
    onDismissRequest: () -> Unit,
    onFuriganaClick: ((String) -> Unit)? = null,
    onFeedbackClick: (() -> Unit)? = null
) {

    val strings = resolveString { alternativeDialog }

    TODO()
    // TODO remove

}
