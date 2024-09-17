package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ua.syt0r.kanji.presentation.common.getBottomLineShape
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.snapSizeTransform
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerSizeTransform
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.daily_limit.DailyLimitScreenContract.ScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLimitScreenUI(
    state: State<ScreenState>,
    saveChanges: () -> Unit,
    navigateBack: () -> Unit,
) {

    val strings = resolveString { dailyLimit }

    ScreenLayout(
        state = state,
        topBarContent = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        loadingStateContent = { FancyLoading(Modifier.fillMaxSize().wrapContentSize()) },
        loadedStateContent = { screenState, containerColor ->

            ListItem(
                headlineContent = {
                    Text(text = strings.enableSwitchTitle)
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { screenState.enabled.run { value = !value } },
                supportingContent = {
                    Text(text = strings.enableSwitchDescription)
                },
                trailingContent = {
                    Switch(
                        checked = screenState.enabled.value,
                        onCheckedChange = { screenState.enabled.value = it }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = containerColor)
            )

            CategoryContainer(
                title = strings.lettersSectionTitle,
                combinedLimit = screenState.isLetterLimitCombined,
                containerColor = containerColor
            ) { selectedCombinedLimit ->

                when {
                    selectedCombinedLimit -> {

                        InputColumn(
                            limitItem = screenState.letterCombined
                        )

                    }

                    else -> {

                        screenState.letterSeparate.forEach { (practiceType, limitItem) ->

                            InputColumn(
                                title = resolveString(practiceType.titleResolver),
                                limitItem = limitItem
                            )

                        }
                    }
                }

            }

            CategoryContainer(
                title = strings.vocabSectionTitle,
                combinedLimit = screenState.isVocabLimitCombined,
                containerColor = containerColor
            ) { selectedCombinedLimit ->

                when {
                    selectedCombinedLimit -> {
                        InputColumn(
                            limitItem = screenState.vocabCombined
                        )
                    }

                    else -> {
                        screenState.vocabSeparate.forEach { (practiceType, limitItem) ->
                            InputColumn(
                                title = resolveString(practiceType.titleResolver),
                                limitItem = limitItem
                            )
                        }
                    }
                }
            }
        },
        loadedStateFabContent = {
            ExtendedFloatingActionButton(
                onClick = saveChanges,
                text = { Text(text = strings.button) },
                icon = { Icon(Icons.Default.Save, null) }
            )
        },
        savingStateContent = { FancyLoading(Modifier.fillMaxSize().wrapContentSize()) },
        doneStateContent = {
            Row(
                modifier = Modifier.fillMaxSize().wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.changesSavedMessage
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .background(MaterialTheme.extraColorScheme.success, CircleShape)
                        .size(24.dp)
                        .padding(2.dp)
                )
            }
            LaunchedEffect(Unit) {
                delay(1000)
                navigateBack()
            }
        }
    )

}


@Composable
private fun ScreenLayout(
    state: State<ScreenState>,
    topBarContent: @Composable () -> Unit,
    loadingStateContent: @Composable () -> Unit,
    loadedStateContent: @Composable ColumnScope.(ScreenState.Loaded, Color) -> Unit,
    loadedStateFabContent: @Composable (ScreenState.Loaded) -> Unit,
    savingStateContent: @Composable () -> Unit,
    doneStateContent: @Composable () -> Unit,
) {

    val extraListSpacerState = rememberExtraListSpacerState()

    Scaffold(
        topBar = { topBarContent() },
        floatingActionButton = {
            FabContainer(
                state = state,
                modifier = Modifier.onGloballyPositioned {
                    extraListSpacerState.updateOverlay(
                        it
                    )
                },
                content = loadedStateFabContent
            )
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() using snapSizeTransform() },
            modifier = Modifier.padding(paddingValues)
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> loadingStateContent()
                is ScreenState.Loaded -> {

                    BoxWithConstraints {

                        val isWide = maxWidth > 500.dp

                        Column(
                            modifier = Modifier.fillMaxSize()
                                .onGloballyPositioned { extraListSpacerState.updateList(it) }
                                .verticalScroll(rememberScrollState())
                        ) {

                            val containerColor: Color
                            val columnModifier: Modifier

                            if (isWide) {
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                columnModifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth()
                                    .width(400.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(containerColor)
                                    .padding(20.dp)
                            } else {
                                containerColor = MaterialTheme.colorScheme.surface
                                columnModifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            }

                            Column(
                                modifier = columnModifier,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                loadedStateContent(screenState, containerColor)
                            }

                            extraListSpacerState.ExtraSpacer()

                        }

                    }

                }

                ScreenState.Saving -> savingStateContent()
                ScreenState.Done -> doneStateContent()
            }

        }
    }

}

@Composable
private fun FabContainer(
    state: State<ScreenState>,
    modifier: Modifier,
    content: @Composable (ScreenState.Loaded) -> Unit
) {

    fun LimitItem.isValid(): Boolean {
        return new.validated.value != null && due.validated.value != null
    }

    val fabState = remember {
        derivedStateOf {
            val loadedState = state.value as? ScreenState.Loaded
                ?: return@derivedStateOf null

            val isLetterInputValid = when {
                loadedState.isLetterLimitCombined.value -> loadedState.letterCombined.isValid()
                else -> loadedState.letterSeparate.all { (_, item) -> item.isValid() }
            }

            val isVocabInputValid = when {
                loadedState.isVocabLimitCombined.value -> loadedState.vocabCombined.isValid()
                else -> loadedState.vocabSeparate.all { (_, item) -> item.isValid() }
            }

            val isInputValid = isLetterInputValid && isVocabInputValid

            loadedState to isInputValid
        }
    }

    AnimatedContent(
        targetState = fabState.value,
        transitionSpec = { scaleIn() togetherWith scaleOut() using snapToBiggerSizeTransform() },
        modifier = modifier
    ) {
        if (it == null || !it.second) return@AnimatedContent
        content(it.first)
    }

}

@Composable
private fun CategoryContainer(
    title: String,
    combinedLimit: MutableState<Boolean>,
    containerColor: Color,
    content: @Composable (ColumnScope.(isCombinedLimit: Boolean) -> Unit)
) {

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        ListItem(
            headlineContent = {
                Text(text = resolveString { dailyLimit.combinedLimitSwitchTitle })
            },
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { combinedLimit.run { value = !value } },
            supportingContent = {
                Text(text = resolveString { dailyLimit.combinedLimitSwitchDescription })
            },
            trailingContent = {
                Switch(
                    checked = combinedLimit.value,
                    onCheckedChange = { combinedLimit.value = it }
                )
            },
            colors = ListItemDefaults.colors(containerColor = containerColor)
        )

        AnimatedContent(
            targetState = combinedLimit.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                content(it)
            }

        }

    }
}

@Composable
private fun InputColumn(
    title: String? = null,
    limitItem: LimitItem
) {

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        LimitInputRow(
            indicatorColor = MaterialTheme.extraColorScheme.new,
            label = resolveString { dailyLimit.newLabel },
            input = limitItem.new.input,
            validatedValue = limitItem.new.validated
        )

        LimitInputRow(
            indicatorColor = MaterialTheme.extraColorScheme.due,
            label = resolveString { dailyLimit.dueLabel },
            input = limitItem.due.input,
            validatedValue = limitItem.due.validated
        )

    }

}

@Composable
private fun LimitInputRow(
    indicatorColor: Color,
    label: String,
    input: MutableState<String>,
    validatedValue: State<Int?>
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.fillMaxHeight(0.8f).width(4.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(indicatorColor)
        )

        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .alignByBaseline()
                .padding(start = 12.dp, end = 20.dp)
        )

        val borderColor = when (validatedValue.value) {
            null -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.outline
        }

        BasicTextField(
            value = input.value,
            onValueChange = { input.value = it },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.weight(1f)
                .border(2.dp, borderColor, getBottomLineShape(2.dp))
                .alignByBaseline()
                .padding(8.dp)
        )

    }
}
