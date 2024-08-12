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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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

    val snackbarHostState = remember { SnackbarHostState() }

    ScreenLayout(
        state = state,
        snackbarHostState = snackbarHostState,
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
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(strings.enabledLabel)
                    Text(
                        text = "Limit number of daily reviews prompted by the app",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Switch(
                    checked = screenState.enabled.value,
                    onCheckedChange = { screenState.enabled.value = it }
                )
            }

            ScreenContainer(
                title = resolveString { "Letters" }
            ) {

                LimitInputRow(
                    icon = Icons.Default.LocalLibrary,
                    label = strings.newLabel,
                    input = screenState.newLimitInput,
                    validatedValue = screenState.newLimitValidated
                )

                LimitInputRow(
                    icon = Icons.Default.Cached,
                    label = strings.dueLabel,
                    input = screenState.dueLimitInput,
                    validatedValue = screenState.dueLimitValidated
                )

                Text(
                    text = strings.noteMessage,
                    style = MaterialTheme.typography.bodySmall
                )

            }

            ScreenContainer(
                title = resolveString { "Vocab" }
            ) {

                Text(
                    text = resolveString { "// Under development" },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

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
                    text = resolveString { "Done" }
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
    snackbarHostState: SnackbarHostState,
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
            val fabState = remember {
                derivedStateOf {
                    val loadedState = state.value as? ScreenState.Loaded
                        ?: return@derivedStateOf null

                    val isInputValid = loadedState.newLimitValidated.value != null &&
                            loadedState.dueLimitValidated.value != null

                    loadedState to isInputValid
                }
            }
            AnimatedContent(
                targetState = fabState.value,
                transitionSpec = {
                    scaleIn() togetherWith scaleOut() using snapToBiggerSizeTransform()
                },
                modifier = Modifier.onGloballyPositioned { extraListSpacerState.updateOverlay(it) }
            ) {

                if (it == null || !it.second) return@AnimatedContent
                loadedStateFabContent(it.first)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() using snapSizeTransform() },
            modifier = Modifier.padding(it)
        ) {

            when (it) {
                ScreenState.Loading -> loadingStateContent()
                is ScreenState.Loaded -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .wrapContentWidth()
                            .onGloballyPositioned { extraListSpacerState.updateList(it) }
                            .padding(horizontal = 20.dp)
                            .widthIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        loadedStateContent(it)
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
private fun ScreenContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        content()

    }
}


@Composable
private fun LimitInputRow(
    icon: ImageVector,
    label: String,
    input: MutableState<String>,
    validatedValue: State<Int?>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
        )

        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )

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
                .border(
                    width = 2.dp,
                    color = if (validatedValue.value == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    shape = getBottomLineShape(2.dp)
                )
                .padding(4.dp)
        )

    }
}
