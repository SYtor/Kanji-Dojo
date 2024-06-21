package ua.syt0r.kanji.presentation.screen.main.screen.sponsor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorScreenUI(
    onUpClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onUpClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = { }
            )
        }
    ) {

        Column(
            modifier = Modifier.padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .wrapContentWidth()
                .padding(20.dp)
                .widthIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = resolveString { appName },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(20.dp)
            ) {
                Text(
                    text = "The development of Kanji Dojo started in 2021 by a single person and it stays free for all users who want to learn Japanese" +
                            "\n\nIf you find the app useful please consider supporting this project financially, every contribution counts" +
                            "\n\nFinancial support will allow me to focus more on development, bring extra features, add more voiced content and translations",
                    textAlign = TextAlign.Justify
                )
            }

            content()

        }

    }

}