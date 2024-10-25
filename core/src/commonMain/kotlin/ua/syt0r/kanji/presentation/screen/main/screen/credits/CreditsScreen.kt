package ua.syt0r.kanji.presentation.screen.main.screen.credits

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import org.koin.compose.koinInject
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(
    state: MainNavigationState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { state.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = {}
            )
        }
    ) { paddingValues ->

        val libs = koinInject<GetCreditLibrariesUseCase>().invoke()

        var selectedLib by remember { mutableStateOf<Library?>(null) }
        selectedLib?.let {
            MultiplatformDialog(
                onDismissRequest = { selectedLib = null },
                title = {
                    Text("License")
                },
                content = {
                    Text(
                        text = it.licenses.firstOrNull()?.licenseContent
                            ?: "Can't find license text"
                    )
                },
                buttons = {
                    TextButton(
                        onClick = { selectedLib = null }
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        LibrariesContainer(
            libraries = libs,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            onLibraryClick = { selectedLib = it }
        )

    }

}
