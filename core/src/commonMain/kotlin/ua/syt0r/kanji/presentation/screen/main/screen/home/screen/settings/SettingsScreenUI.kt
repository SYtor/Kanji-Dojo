package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum
import kotlin.enums.EnumEntries

@Composable
fun SettingsScreenUI(
    state: State<ScreenState>,
    onBackupButtonClick: () -> Unit,
    onAccountButtonClick: () -> Unit,
    onSyncButtonClick: () -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onAboutButtonClick: () -> Unit,
    loadedContent: @Composable ColumnScope.(ScreenState.Loaded) -> Unit
) {

    AnimatedContent(
        state.value,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { screenState ->

        when (screenState) {
            ScreenState.Loading -> {
                CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
            }

            is ScreenState.Loaded -> {
                SettingsContent {

                    loadedContent(screenState)

                    SettingsBackupButton(onBackupButtonClick)

                    SettingsAccountButton(onAccountButtonClick)

                    SettingsSyncButton(onSyncButtonClick)

                    SettingsFeedbackButton(onFeedbackButtonClick)

                    SettingsAboutButton(onAboutButtonClick)

                }
            }
        }

    }

}

@Composable
fun SettingsContent(
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth()
            .verticalScroll(rememberScrollState())
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
    ) {

        val orientation = LocalOrientation.current
        if (orientation == Orientation.Landscape) {
            Spacer(Modifier.height(20.dp))
        }

        content()

    }

}

@Composable
fun SettingsSwitchRow(
    title: String,
    message: String,
    isEnabled: Boolean,
    onToggled: () -> Unit
) {

    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(message) },
        trailingContent = {
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggled() },
                colors = SwitchDefaults.colors(
                    uncheckedTrackColor = MaterialTheme.colorScheme.background
                )
            )
        }
    )

}

@Composable
fun SettingsBackupButton(onClick: () -> Unit) {
    SettingsTextButton(
        title = resolveString { settings.backupTitle },
        onClick = onClick
    )
}

@Composable
fun SettingsAccountButton(onClick: () -> Unit) {
    SettingsTextButton(
        title = resolveString { "Account" },
        onClick = onClick
    )
}

@Composable
fun SettingsSyncButton(onClick: () -> Unit) {
    SettingsTextButton(
        title = resolveString { "Sync" },
        onClick = onClick
    )
}

@Composable
fun SettingsFeedbackButton(onClick: () -> Unit) {
    SettingsTextButton(
        title = resolveString { settings.feedbackTitle },
        onClick = onClick
    )
}

@Composable
fun SettingsAboutButton(onClick: () -> Unit) {
    SettingsTextButton(
        title = resolveString { settings.aboutTitle },
        onClick = onClick
    )
}

@Composable
fun SettingsTextButton(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = when {
            subtitle != null -> {
                { Text(subtitle) }
            }

            else -> null
        },
        modifier = Modifier.clip(MaterialTheme.shapes.large)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}

@Composable
fun <T> SettingsPreferencePickerDialog(
    onDismissRequest: () -> Unit,
    title: String,
    options: EnumEntries<T>,
    defaultSelected: T,
    onSelected: (T) -> Unit
) where T : DisplayableEnum, T : Enum<T> {

    var selected by remember { mutableStateOf(defaultSelected) }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        content = {
            options.forEach {
                val isOptionSelected = selected == it
                ListItem(
                    headlineContent = {
                        Text(resolveString(it.titleResolver))
                    },
                    trailingContent = {
                        if (isOptionSelected) Icon(Icons.Default.Check, null)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = when {
                            isOptionSelected -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .clickable { selected = it }
                )
            }
        },
        buttons = {
            TextButton(onDismissRequest) { Text("Cancel") }
            TextButton(
                onClick = {
                    onSelected(selected)
                    onDismissRequest()
                }
            ) { Text("Apply") }
        }
    )

}
