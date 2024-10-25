package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.FilledTextField
import kotlin.time.Duration

sealed interface PracticeToolbarState {

    object Idle : PracticeToolbarState

    object Configuration : PracticeToolbarState

    data class Review(
        val practiceQueueProgress: PracticeQueueProgress
    ) : PracticeToolbarState

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeToolbar(
    state: State<PracticeToolbarState>,
    onUpButtonClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onUpButtonClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        title = {
            AnimatedContent(
                targetState = state.value,
                transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec()
            ) {
                when (it) {
                    PracticeToolbarState.Configuration -> {
                        Text(
                            text = resolveString { commonPractice.configurationTitle }
                        )
                    }

                    else -> {}
                }
            }
        },
        actions = {
            AnimatedContent(
                targetState = state.value,
                transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec(),
                contentKey = { it is PracticeToolbarState.Review }
            ) {
                when (it) {
                    is PracticeToolbarState.Review -> {
                        val progress = it.practiceQueueProgress
                        PracticeProgressCounter(
                            pending = progress.pending,
                            repeat = progress.repeats,
                            completed = progress.completed
                        )
                    }

                    else -> {}
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolbarCountItem(count: Int, color: Color) {
    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(color)
    ) {
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
    practiceTypeMessage: String,
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

            Text(
                text = practiceTypeMessage,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )

            content()

        }

        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
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


@OptIn(ExperimentalMaterial3Api::class)
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

        val interactionSource = remember { MutableInteractionSource() }
        val colors = SliderDefaults.colors(
            activeTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            activeTickColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            inactiveTickColor = MaterialTheme.colorScheme.primary
        )

        Slider(
            value = selectedCharactersCount.coerceIn(range).toFloat(),
            onValueChange = {
                selectedCharactersCount = it.toInt()
                selectedCharactersCountText = it.toInt().toString()
            },
            steps = state.itemToDeckIdMap.size,
            valueRange = 1f..range.last.toFloat(),
            modifier = Modifier.weight(1f),
            interactionSource = interactionSource,
            track = {
                SliderDefaults.Track(
                    sliderState = it,
                    thumbTrackGapSize = 2.dp,
                    colors = colors,
                    drawTick = { _, _ -> }
                )
            },
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource,
                    colors = colors,
                    thumbSize = DpSize(6.dp, 30.dp)
                )
            }
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

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                values.forEach {
                    DropdownMenuItem(
                        onClick = {
                            onSelected(it)
                            expanded = false
                        },
                        text = { Text(resolveString(it.titleResolver)) }
                    )
                }
            }
        }

    }

}

@Composable
fun PracticeSummaryContainer(
    practiceDuration: Duration,
    summaryItemsCount: Int,
    onFinishClick: () -> Unit,
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
            modifier = Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            PracticeSummaryInfoLabel(
                title = resolveString { commonPractice.summaryTimeSpentLabel },
                data = resolveString { commonPractice.summaryTimeSpentValue(practiceDuration) }
            )

            PracticeSummaryInfoLabel(
                title = resolveString { commonPractice.summaryItemsCountTitle },
                data = summaryItemsCount.toString()
            )

            content()

        }

        Button(
            onClick = onFinishClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.neutralButtonColors()
        ) {
            Text(
                text = resolveString { commonPractice.summaryButton }
            )
        }

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
                text = resolveString { commonPractice.summaryNextReviewLabel },
                modifier = Modifier.weight(1f).alignByBaseline()
            )
            Text(
                text = resolveString { commonPractice.formattedSrsInterval(nextInterval) },
                modifier = Modifier.alignByBaseline()
            )
        }

    }
}

@Composable
fun PracticeEarlyFinishDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {

    val strings = resolveString { commonPractice }

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
