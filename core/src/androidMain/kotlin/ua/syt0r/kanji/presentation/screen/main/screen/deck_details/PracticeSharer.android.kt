package ua.syt0r.kanji.presentation.screen.main.screen.deck_details

import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPracticeSharer(snackbarHostState: SnackbarHostState): PracticeSharer {
    val context = LocalContext.current
    return remember {
        object : PracticeSharer {
            override fun share(data: String) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, data)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        }
    }
}