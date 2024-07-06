package ua.syt0r.kanji.presentation.screen.main.screen.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.BuildKonfig
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.VersionChangeDialog

private const val KanjiDojoGithubLink = "https://github.com/syt0r/Kanji-Dojo"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreenUI(
    onUpButtonClick: () -> Unit,
    openLink: (String) -> Unit,
    navigateToCredits: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = resolveString { about.title })
                },
                navigationIcon = {
                    IconButton(onClick = onUpButtonClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) {

        Column(
            modifier = Modifier.padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .wrapContentWidth()
                .widthIn(max = 400.dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(
                    text = resolveString { appName },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = resolveString { about.version(BuildKonfig.versionName) },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            AboutTextRow(
                title = resolveString { about.githubTitle },
                subtitle = resolveString { about.githubDescription },
                onClick = { openLink(KanjiDojoGithubLink) }
            )

            var shouldShowVersionChangeDialog by remember { mutableStateOf(false) }
            if (shouldShowVersionChangeDialog) {
                VersionChangeDialog { shouldShowVersionChangeDialog = false }
            }

            AboutTextRow(
                title = resolveString { about.versionChangesTitle },
                subtitle = resolveString { about.versionChangesDescription },
                onClick = { shouldShowVersionChangeDialog = true }
            )

            AboutTextRow(
                title = resolveString { about.creditsTitle },
                subtitle = resolveString { about.creditsDescription },
                onClick = navigateToCredits
            )

        }

    }

}

@Composable
private fun AboutTextRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Column(
        Modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall
        )
    }

}
