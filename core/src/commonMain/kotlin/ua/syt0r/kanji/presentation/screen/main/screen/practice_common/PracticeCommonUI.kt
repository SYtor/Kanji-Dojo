package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.common.ui.CustomRippleTheme
import ua.syt0r.kanji.presentation.common.ui.FilledTextField
import ua.syt0r.kanji.presentation.common.ui.MultiplatformPopup
import ua.syt0r.kanji.presentation.common.ui.PopupContentItem
import kotlin.time.Duration

sealed interface PracticeToolbarState {

    object Loading : PracticeToolbarState
    object Configuration : PracticeToolbarState

    data class Review(
        val pending: Int,
        val repeat: Int,
        val completed: Int
    ) : PracticeToolbarState

    object Saving : PracticeToolbarState
    object Saved : PracticeToolbarState

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeToolbar(
    state: State<PracticeToolbarState?>,
    onUpButtonClick: () -> Unit
) {
    TopAppBar(
        title = {
            when (val screenState = state.value) {
                null, PracticeToolbarState.Loading -> {}
                is PracticeToolbarState.Review -> {
                    PracticeProgressCounter(
                        pending = screenState.pending,
                        repeat = screenState.repeat,
                        completed = screenState.completed
                    )
                }

                is PracticeToolbarState.Configuration -> {
                    Text(text = resolveString { commonPractice.configurationTitle })
                }

                is PracticeToolbarState.Saving -> {
                    Text(text = resolveString { commonPractice.savingTitle })
                }

                is PracticeToolbarState.Saved -> {
                    Text(text = resolveString { commonPractice.savedTitle })
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onUpButtonClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }
    )
}

@Composable
fun PracticeProgressCounter(pending: Int, repeat: Int, completed: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(align = Alignment.CenterEnd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarCountItem(
            count = pending,
            color = MaterialTheme.extraColorScheme.pending
        )

        ToolbarCountItem(
            count = repeat,
            color = MaterialTheme.extraColorScheme.due
        )

        ToolbarCountItem(
            count = completed,
            color = MaterialTheme.extraColorScheme.success
        )
    }
}

@Composable
private fun ToolbarCountItem(count: Int, color: Color) {
    val rippleTheme = remember { CustomRippleTheme(colorProvider = { color }) }
    CompositionLocalProvider(LocalRippleTheme provides rippleTheme) {
        TextButton(onClick = {}) {
            Box(
                modifier = Modifier
                    .alignBy { it.measuredHeight }
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = count.toString(),
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}


@Composable
fun PracticeConfigurationContainer(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
            .wrapContentSize()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
        ) {
            content()
        }

        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = resolveString { commonPractice.configurationCompleteButton }
            )
        }

    }

}

class PracticeConfigurationItemsSelectorState<T>(
    val itemToDeckIdMap: List<Pair<T, Long>>,
    shuffle: Boolean
) {

    val selectedCountState = mutableStateOf(itemToDeckIdMap.size)

    val shuffleEnabled = mutableStateOf(shuffle)
    val sortedList = mutableStateOf(if (shuffle) itemToDeckIdMap.shuffled() else itemToDeckIdMap)

    val result: List<Pair<T, Long>>
        get() = sortedList.value.take(selectedCountState.value)

}

@Composable
fun <T> rememberPracticeConfigurationItemsSelectorState(
    itemToDeckIdMap: List<Pair<T, Long>>,
    shuffle: Boolean
): PracticeConfigurationItemsSelectorState<T> {
    return remember { PracticeConfigurationItemsSelectorState<T>(itemToDeckIdMap, shuffle) }
}


@Composable
fun <T> PracticeConfigurationItemsSelector(
    state: PracticeConfigurationItemsSelectorState<T>
) {

    val range = 1..state.itemToDeckIdMap.size

    var shuffle by state.shuffleEnabled
    var resultList by state.sortedList

    var selectedCharactersCount by state.selectedCountState
    var selectedCharactersCountText by rememberSaveable {
        mutableStateOf(selectedCharactersCount.toString())
    }

    Row(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = resolveString { commonPractice.configurationSelectedItemsLabel },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.alignByBaseline()
        )

        FilledTextField(
            value = selectedCharactersCountText,
            onValueChange = {
                selectedCharactersCountText = it
                it.toIntOrNull()?.coerceIn(range)?.also { selectedCharactersCount = it }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.widthIn(min = 80.dp).alignByBaseline()
        )

    }

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(text = 1.toString())

        Slider(
            value = selectedCharactersCount.coerceIn(range).toFloat(),
            onValueChange = {
                selectedCharactersCount = it.toInt()
                selectedCharactersCountText = it.toInt().toString()
            },
            steps = state.itemToDeckIdMap.size,
            valueRange = 1f..range.last.toFloat(),
            modifier = Modifier.weight(1f)
        )

        Text(text = state.itemToDeckIdMap.size.toString())

    }

    PracticeConfigurationOption(
        title = resolveString { commonPractice.shuffleConfigurationTitle },
        subtitle = resolveString { commonPractice.shuffleConfigurationMessage },
        checked = shuffle,
        onChange = {
            shuffle = it
            resultList = if (it) state.itemToDeckIdMap.shuffled()
            else state.itemToDeckIdMap
        }
    )

}

@Composable
fun ColumnScope.PracticeConfigurationCharactersPreview(
    characters: List<String>,
    selectedCharactersCount: State<Int>
) {

    var previewExpanded by remember { mutableStateOf(false) }

    Row(
        Modifier.clip(MaterialTheme.shapes.medium)
            .clickable(onClick = { previewExpanded = !previewExpanded })
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = resolveString { commonPractice.configurationCharactersPreview },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { previewExpanded = !previewExpanded }) {
            val icon = if (previewExpanded) Icons.Default.KeyboardArrowUp
            else Icons.Default.KeyboardArrowDown
            Icon(imageVector = icon, contentDescription = null)
        }
    }

    if (previewExpanded) {
        Text(
            text = buildAnnotatedString {
                append(characters.joinToString(""))
                addStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.surfaceVariant),
                    start = selectedCharactersCount.value,
                    end = length
                )
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
            style = MaterialTheme.typography.titleLarge
        )
    }
}


@Composable
fun PracticeConfigurationOption(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {

    PracticeConfigurationItem(
        title = title,
        subtitle = subtitle,
        onClick = { if (enabled) onChange(!checked) }
    ) {

        Switch(
            checked = checked,
            onCheckedChange = { onChange(it) },
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = MaterialTheme.colorScheme.background
            ),
            enabled = enabled
        )
    }

}

@Composable
private fun PracticeConfigurationItem(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .run { if (onClick != null) clickable(onClick = onClick) else this }
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }

        content()

    }

}

@Composable
private fun PracticeConfigurationDropDownButton(
    text: String,
    onClick: () -> Unit
) {

    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.width(IntrinsicSize.Max)
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(Icons.Default.ArrowDropDown, null)
    }

}

interface DisplayableEnum {
    val titleResolver: StringResolveScope<String>
}

@Composable
fun <T> PracticeConfigurationEnumSelector(
    title: String,
    subtitle: String,
    values: Array<T>,
    selected: T,
    onSelected: (T) -> Unit
) where T : Enum<T>, T : DisplayableEnum {

    PracticeConfigurationItem(
        title = title,
        subtitle = subtitle,
    ) {

        var expanded by remember { mutableStateOf(false) }

        Box {

            PracticeConfigurationDropDownButton(
                text = resolveString(selected.titleResolver),
                onClick = { expanded = true }
            )

            MultiplatformPopup(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                values.forEach {
                    PopupContentItem(
                        onClick = {
                            onSelected(it)
                            expanded = false
                        }
                    ) {
                        Text(resolveString(it.titleResolver))
                    }
                }
            }
        }

    }

}

@Composable
fun PracticeSummaryContainer(
    onFinishClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    AutopaddedScrollableColumn(
        modifier = Modifier.fillMaxWidth()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp),
        bottomOverlayContent = {

            Button(
                onClick = onFinishClick,
                colors = ButtonDefaults.neutralButtonColors(),
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(0.4f))
                    .padding(vertical = 20.dp)
            ) {
                Text(text = resolveString { commonPractice.summaryButton })
            }

        }
    ) {

        content()

    }

}

@Composable
fun PracticeSummaryInfoLabel(title: String, data: String) {
    // Fixes text with bigger size getting clipped at the top
    val textStyle = LocalTextStyle.current.copy(lineHeight = TextUnit.Unspecified)

    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                append(title)
            }

            append(" ")

            withStyle(
                SpanStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light
                )
            ) {
                append(data)
            }
        },
        style = textStyle
    )
}


@Composable
fun PracticeSummaryItem(
    header: @Composable ColumnScope.() -> Unit,
    nextInterval: Duration
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(IntrinsicSize.Min)
    ) {

        header()

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = resolveString { vocabPractice.summaryNextReviewLabel },
                modifier = Modifier.weight(1f).alignByBaseline()
            )
            Text(
                text = resolveString { vocabPractice.formattedSrsInterval(nextInterval) },
                modifier = Modifier.alignByBaseline()
            )
        }

    }
}


@Composable
fun KanaVoiceAutoPlayToggle(
    enabledState: State<Boolean>,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.clip(MaterialTheme.shapes.small)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {

        Text(
            text = "Autoplay",
            style = MaterialTheme.typography.bodySmall
        )

        val (circleColor, iconColor, icon) = when (enabledState.value) {
            true -> Triple(
                MaterialTheme.colorScheme.onSurfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant,
                Icons.Default.PlayArrow
            )

            false -> Triple(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                Icons.Default.Pause
            )
        }

        Box(
            modifier = Modifier.size(16.dp).background(circleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }

}


@Composable
fun PracticeEarlyFinishDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {

    val strings = resolveString { vocabPractice }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = strings.earlyFinishDialogTitle,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        content = {
            Text(
                text = strings.earlyFinishDialogMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(text = strings.earlyFinishDialogCancelButton)
            }
            TextButton(onClick = onConfirmClick) {
                Text(text = strings.earlyFinishDialogAcceptButton)
            }
        }
    )
}
