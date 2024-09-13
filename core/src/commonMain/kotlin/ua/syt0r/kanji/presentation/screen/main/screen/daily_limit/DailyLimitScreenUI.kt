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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
                title = { Text(text = strings.title) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        loadingStateContent = { FancyLoading(Modifier.fillMaxSize().wrapContentSize()) },
        loadedStateContent = { screenState ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(MaterialTheme.shapes.medium)
                    .clickable { screenState.enabled.run { value = !value } }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = strings.enableSwitchTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = strings.enableSwitchDescription,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Switch(
                    checked = screenState.enabled.value,
                    onCheckedChange = { screenState.enabled.value = it }
                )
            }

            CardContainer(
                title = strings.lettersSectionTitle,
                combinedLimit = screenState.isLetterLimitCombined
            ) { selectedCombinedLimit ->

                when {
                    selectedCombinedLimit -> {

                        InputColumn(
                            title = resolveString { "Total reviews cap" },
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

            CardContainer(
                title = strings.vocabSectionTitle,
                combinedLimit = screenState.isVocabLimitCombined
            ) { selectedCombinedLimit ->

                when {
                    selectedCombinedLimit -> {

                        InputColumn(
                            title = resolveString { "Total reviews cap" },
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
    loadedStateContent: @Composable ColumnScope.(ScreenState.Loaded) -> Unit,
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
                modifier = Modifier.onGloballyPositioned { extraListSpacerState.updateOverlay(it) },
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
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .onGloballyPositioned { extraListSpacerState.updateList(it) }
                            .verticalScroll(rememberScrollState())
                            .wrapContentWidth()
                            .padding(horizontal = 20.dp)
                            .widthIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        loadedStateContent(screenState)
                        extraListSpacerState.ExtraSpacer()
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
private fun CardContainer(
    title: String,
    combinedLimit: MutableState<Boolean>,
    content: @Composable ColumnScope.(isCombinedLimit: Boolean) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        LimitModeSwitch(
            combinedLimit = combinedLimit,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedContent(
            targetState = combinedLimit.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 4.dp)
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
private fun LimitModeSwitch(
    combinedLimit: MutableState<Boolean>,
    modifier: Modifier
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {

        Text(
            text = resolveString { "Combined Limit" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraLight,
            modifier = Modifier.weight(1f)
        )

        val outline = MaterialTheme.colorScheme.outline
        val variant = MaterialTheme.colorScheme.surface

        Switch(
            checked = combinedLimit.value,
            onCheckedChange = { combinedLimit.value = it },
            thumbContent = {
                Icon(
                    imageVector = when (combinedLimit.value) {
                        true -> Icons.Default.Check
                        false -> Icons.Default.Close
                    },
                    contentDescription = null
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = outline,
                checkedTrackColor = variant,
                checkedIconColor = variant,
                checkedBorderColor = variant,
                uncheckedThumbColor = outline,
                uncheckedTrackColor = variant,
                uncheckedIconColor = variant,
                uncheckedBorderColor = variant,
            )
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxHeight().width(4.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(indicatorColor)
        )

        Text(
            text = label,
            modifier = Modifier.weight(1f).alignByBaseline()
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
            modifier = Modifier.weight(2f)
                .border(2.dp, borderColor, getBottomLineShape(2.dp))
                .alignByBaseline()
                .padding(8.dp)
        )

    }
}
