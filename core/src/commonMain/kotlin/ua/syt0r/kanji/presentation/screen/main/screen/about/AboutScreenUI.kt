package ua.syt0r.kanji.presentation.screen.main.screen.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.BuildKonfig
import ua.syt0r.kanji.presentation.common.resources.icon.AppIconBackground
import ua.syt0r.kanji.presentation.common.resources.icon.AppIconForeground
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.VersionChangeDialog

private const val KanjiDojoGithubLink = "https://github.com/syt0r/Kanji-Dojo"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreenUI(
    onUpButtonClick: () -> Unit = {},
    openLink: (String) -> Unit = {}
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
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = TopCenter)
                .widthIn(max = 400.dp)
                .padding(top = it.calculateTopPadding(), bottom = it.calculateBottomPadding())
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            ElevatedCard(
                modifier = Modifier
                    .size(128.dp)
                    .align(CenterHorizontally)
            ) {

                Box {

                    Icon(
                        imageVector = ExtraIcons.AppIconBackground,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Unspecified
                    )

                    Icon(
                        imageVector = ExtraIcons.AppIconForeground,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Unspecified
                    )

                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = resolveString { appName },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().wrapContentSize()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = resolveString { about.version(BuildKonfig.versionName) },
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth().wrapContentSize()
            )

            Spacer(modifier = Modifier.height(8.dp))

            ClickableRow(
                content = {
                    Text(
                        text = resolveString { about.githubTitle },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = resolveString { about.githubDescription },
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                onClick = { openLink(KanjiDojoGithubLink) }
            )

            var shouldShowVersionChangeDialog by remember { mutableStateOf(false) }
            if (shouldShowVersionChangeDialog) {
                VersionChangeDialog { shouldShowVersionChangeDialog = false }
            }

            ClickableRow(
                content = {
                    Text(
                        text = resolveString { about.versionChangesTitle },
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                onClick = { shouldShowVersionChangeDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = resolveString { about.creditsTitle },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            AboutCredit.values().forEach { creditItem ->
                CreditItem(
                    creditItem = creditItem,
                    onClick = { openLink(creditItem.url) }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

        }

    }

}

@Composable
private fun CreditItem(
    creditItem: AboutCredit,
    onClick: () -> Unit
) {
    ClickableRow(
        content = {
            Text(
                text = resolveString(creditItem.title),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = resolveString(creditItem.description),
                style = MaterialTheme.typography.bodySmall
            )
            val licenseMessage: String = resolveString(creditItem.license)
            Text(
                text = resolveString { about.licenseTemplate(licenseMessage) },
                style = MaterialTheme.typography.bodySmall
            )
        },
        onClick = onClick
    )
}

@Composable
private fun ClickableRow(
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {

    Column(
        Modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        content()
    }

}
