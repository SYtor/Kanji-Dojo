package ua.syt0r.kanji.presentation.common

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ua.syt0r.kanji.R
import ua.syt0r.kanji.core.japanese.CharacterClassification

@Composable
fun CharacterClassification.Kana.stringResource(): String {
    return when (this) {
        CharacterClassification.Kana.Hiragana -> stringResource(R.string.hiragana)
        CharacterClassification.Kana.Katakana -> stringResource(R.string.katakana)
    }
}

fun Context.asActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.asActivity()
    else -> null
}

fun SnackbarHostState.showSnackbarFlow(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
): Flow<Unit> {
    return flow { showSnackbar(message, actionLabel, withDismissAction, duration) }
}
