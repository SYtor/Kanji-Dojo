package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabPracticeScreenUI(
    navigateBack: () -> Unit
) {

    val tryNavigateBack = {
        // TODO leave confirmation
        navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = tryNavigateBack) {}
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {


    }

}