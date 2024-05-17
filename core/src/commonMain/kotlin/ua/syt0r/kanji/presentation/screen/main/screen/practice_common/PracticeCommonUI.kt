package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.core.user_data.model.CharacterReviewOutcome
import ua.syt0r.kanji.presentation.common.ExtraOverlayBottomSpacingData
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.CustomRippleTheme
import kotlin.time.Duration

@Composable
fun PracticeLeaveConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {

    val strings = resolveString { commonPractice }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest
    ) {

        Surface(
            modifier = Modifier.clip(RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .padding(20.dp)
            ) {

                Text(
                    text = resolveString { strings.leaveDialogTitle },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                Text(
                    text = resolveString { strings.leaveDialogMessage },
                    style = MaterialTheme.typography.bodyMedium
                )

                TextButton(
                    onClick = onConfirmClick,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .align(Alignment.End)
                ) {
                    Text(text = resolveString { strings.leaveDialogButton })
                }

            }
        }

    }

}

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(align = Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ToolbarCountItem(
                            count = screenState.pending,
                            color = MaterialTheme.extraColorScheme.pending
                        )

                        ToolbarCountItem(
                            count = screenState.repeat,
                            color = MaterialTheme.colorScheme.primary
                        )

                        ToolbarCountItem(
                            count = screenState.completed,
                            color = MaterialTheme.extraColorScheme.success
                        )
                    }
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
private fun ToolbarCountItem(count: Int, color: Color) {
    val rippleTheme = remember { CustomRippleTheme(colorProvider = { color }) }
    CompositionLocalProvider(LocalRippleTheme provides rippleTheme) {
        TextButton(onClick = {}) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = count.toString(), color = color)
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

class PracticeConfigurationCharactersState(
    val characters: List<String>,
    val shuffle: Boolean
) {

    val selectedCountState = mutableStateOf(characters.size)

    val selectedShuffle = mutableStateOf(shuffle)
    val sortedCharacters = mutableStateOf(if (shuffle) characters.shuffled() else characters)

    val result: List<String>
        get() = sortedCharacters.value.take(selectedCountState.value)

}

@Composable
fun rememberPracticeConfigurationCharactersSelectionState(
    characters: List<String>,
    shuffle: Boolean
): PracticeConfigurationCharactersState {
    return remember { PracticeConfigurationCharactersState(characters, shuffle) }
}

@Composable
fun PracticeConfigurationCharactersSelection(
    state: PracticeConfigurationCharactersState
) {

    var shuffle by state.selectedShuffle
    var resultCharacters by state.sortedCharacters
    var selectedCharactersCount by state.selectedCountState

    var previewExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = resolveString {
                commonPractice.configurationCharactersCount(
                    selectedCharactersCount,
                    state.characters.size
                )
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).wrapContentSize()
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
            value = selectedCharactersCount.toFloat(),
            onValueChange = { selectedCharactersCount = it.toInt() },
            steps = state.characters.size,
            valueRange = 1f..state.characters.size.toFloat(),
            modifier = Modifier.weight(1f)
        )

        Text(text = state.characters.size.toString())

    }

    PracticeConfigurationOption(
        title = resolveString { commonPractice.shuffleConfigurationTitle },
        subtitle = resolveString { commonPractice.shuffleConfigurationMessage },
        checked = shuffle,
        onChange = {
            shuffle = it
            resultCharacters = if (it) state.characters.shuffled()
            else state.characters
        }
    )

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
                append(resultCharacters.joinToString(""))
                addStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.surfaceVariant),
                    start = selectedCharactersCount,
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
fun PracticeConfigurationItem(
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
fun PracticeConfigurationDropDownButton(
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

data class PracticeCharacterReviewResult(
    val character: String,
    val mistakes: Int
)

data class PracticeSavingResult(
    val toleratedMistakesCount: Int,
    val outcomes: Map<String, CharacterReviewOutcome>
)

private val MistakesRange = 0..10

@Composable
fun PracticeSavingState(
    defaultToleratedMistakesCount: Int,
    reviewResults: List<PracticeCharacterReviewResult>,
    onSaveClick: (PracticeSavingResult) -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        val fabCoordinatesState = remember { mutableStateOf<LayoutCoordinates?>(null) }
        val listCoordinatesState = remember { mutableStateOf<LayoutCoordinates?>(null) }
        val extraOverlayBottomSpacingData = remember {
            ExtraOverlayBottomSpacingData(listCoordinatesState, fabCoordinatesState)
        }

        val toleratedMistakesCount = remember { mutableStateOf(defaultToleratedMistakesCount) }

        val outcomes = remember(toleratedMistakesCount.value) {
            val limit = toleratedMistakesCount.value
            val outcomes = reviewResults.map { (character, mistakeCount) ->
                character to if (mistakeCount > limit) CharacterReviewOutcome.Fail else CharacterReviewOutcome.Success
            }
            outcomes.toMutableStateMap()
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp),
            modifier = Modifier.fillMaxSize()
                .wrapContentWidth()
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .onGloballyPositioned { listCoordinatesState.value = it },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {

                Column {

                    var editSectionExpanded by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = resolveString { commonPractice.savingPreselectTitle },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { editSectionExpanded = !editSectionExpanded }) {
                            Icon(Icons.Outlined.Settings, null)
                        }
                    }

                    AnimatedVisibility(
                        visible = editSectionExpanded
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(MistakesRange.first.toString())
                                Slider(
                                    value = toleratedMistakesCount.value.toFloat(),
                                    onValueChange = { toleratedMistakesCount.value = it.toInt() },
                                    valueRange = MistakesRange.first.toFloat()..MistakesRange.last.toFloat(),
                                    steps = MistakesRange.count(),
                                    modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
                                )
                                Text(MistakesRange.last.toString())
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null)
                                Text(
                                    text = resolveString {
                                        commonPractice.savingPreselectCount(toleratedMistakesCount.value)
                                    },
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                }

            }

            items(reviewResults) {
                val isSelected = outcomes[it.character] == CharacterReviewOutcome.Fail
                SavingStateItem(
                    character = it.character,
                    mistakes = it.mistakes,
                    isSelected = isSelected,
                    onClick = {
                        outcomes[it.character] =
                            if (isSelected) CharacterReviewOutcome.Success
                            else CharacterReviewOutcome.Fail
                    },
                    modifier = Modifier
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                extraOverlayBottomSpacingData.ExtraSpacer()
            }

        }

        ExtendedFloatingActionButton(
            onClick = {
                val result = PracticeSavingResult(toleratedMistakesCount.value, outcomes)
                onSaveClick(result)
            },
            text = { Text(text = resolveString { commonPractice.savingButton }) },
            icon = { Icon(Icons.Default.Save, null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp)
                .onGloballyPositioned { fabCoordinatesState.value = it }
        )

    }

}

@Composable
private fun SavingStateItem(
    character: String,
    mistakes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val textColor = when (isSelected) {
            false -> MaterialTheme.colorScheme.onSurface
            true -> MaterialTheme.colorScheme.primary
        }

        Text(
            text = character,
            fontSize = 35.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = textColor,
            modifier = Modifier
        )

        Text(
            text = resolveString { commonPractice.savingMistakesMessage(mistakes) },
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )

    }

}

private val gridItemSize = 60.dp

@Composable
fun PracticeSavedState(
    charactersReviewed: Int,
    practiceDuration: Duration,
    accuracy: Float,
    failedCharacters: List<String>,
    goodCharacters: List<String>,
    onFinishClick: () -> Unit
) {

    val strings = resolveString { commonPractice }

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        val fabCoordinatesState = remember { mutableStateOf<LayoutCoordinates?>(null) }
        val listCoordinatesState = remember { mutableStateOf<LayoutCoordinates?>(null) }
        val extraOverlayBottomSpacingData = remember {
            ExtraOverlayBottomSpacingData(listCoordinatesState, fabCoordinatesState)
        }

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize()
                .wrapContentWidth()
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .onGloballyPositioned { listCoordinatesState.value = it },
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            columns = GridCells.FixedSize(gridItemSize)
        ) {

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {

                Column {
                    PracticeSavedStateInfoLabel(
                        title = strings.savedReviewedCountLabel,
                        data = charactersReviewed.toString()
                    )

                    PracticeSavedStateInfoLabel(
                        title = strings.savedTimeSpentLabel,
                        data = strings.savedTimeSpentValue(practiceDuration)
                    )

                    PracticeSavedStateInfoLabel(
                        title = strings.savedAccuracyLabel,
                        data = "%.2f%%".format(accuracy)
                    )

                    PracticeSavedStateInfoLabel(
                        title = strings.savedRepeatCharactersLabel,
                        data = failedCharacters.size.toString()
                    )
                }

            }

            items(failedCharacters) { SavedStateCharacter(it) }

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                PracticeSavedStateInfoLabel(
                    title = strings.savedRetainedCharactersLabel,
                    data = goodCharacters.size.toString()
                )
            }

            items(goodCharacters) { SavedStateCharacter(it) }

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                extraOverlayBottomSpacingData.ExtraSpacer()
            }

        }

        ExtendedFloatingActionButton(
            onClick = onFinishClick,
            text = { Text(text = strings.savedButton) },
            icon = { Icon(Icons.Default.Check, null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp)
                .onGloballyPositioned { fabCoordinatesState.value = it }
        )

    }

}

@Composable
fun PracticeSavedStateInfoLabel(title: String, data: String) {
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
private fun SavedStateCharacter(character: String) {
    Text(
        text = character,
        fontSize = 30.textDp,
        modifier = Modifier
            .size(gridItemSize)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .wrapContentSize(unbounded = true)
    )
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
