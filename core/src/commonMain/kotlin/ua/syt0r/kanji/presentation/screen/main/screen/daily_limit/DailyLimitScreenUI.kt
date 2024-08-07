package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.presentation.common.getBottomLineShape
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.snapSizeTransform
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
        topBar = {
            TopAppBar(
                title = { Text(text = strings.title) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        loadingState = { FancyLoading(Modifier.fillMaxSize().wrapContentSize()) },
        loadedState = { screenState ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(MaterialTheme.shapes.medium)
                    .clickable { screenState.enabled.run { value = !value } }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(strings.enabledLabel, Modifier.weight(1f))
                Switch(
                    checked = screenState.enabled.value,
                    onCheckedChange = { screenState.enabled.value = it }
                )
            }

            val newInput = rememberSaveable {
                mutableStateOf(screenState.newLimit.value.toString())
            }

            val dueInput = rememberSaveable {
                mutableStateOf(screenState.dueLimit.value.toString())
            }

            LaunchedEffect(Unit) {
                snapshotFlow { newInput.value }
                    .filter { it.isValidLimitNumber() }
                    .onEach { screenState.newLimit.value = it.toInt() }
                    .launchIn(this)

                snapshotFlow { dueInput.value }
                    .filter { it.isValidLimitNumber() }
                    .onEach { screenState.dueLimit.value = it.toInt() }
                    .launchIn(this)
            }

            Column(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = resolveString { "Letters" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                InputRow(Icons.Default.LocalLibrary, strings.newLabel, newInput)
                InputRow(Icons.Default.Cached, strings.dueLabel, dueInput)
            }

            Text(
                text = strings.noteMessage,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = resolveString { "Vocab" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = resolveString { "// Under development" },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

        },
        loadedStateFab = {
            ExtendedFloatingActionButton(
                onClick = {
                    val valid = true
                    if (valid) saveChanges()
                },
                text = { Text(text = strings.button) },
                icon = { Icon(Icons.Default.Save, null) }
            )
        },
        savingState = {
            Text("Saving", Modifier.fillMaxSize().wrapContentSize())
        },
        doneState = {
            Text("Done", Modifier.fillMaxSize().wrapContentSize())
        }
    )

}

@Composable
private fun ScreenLayout(
    state: State<ScreenState>,
    topBar: @Composable () -> Unit,
    loadingState: @Composable () -> Unit,
    loadedState: @Composable ColumnScope.(ScreenState.Loaded) -> Unit,
    loadedStateFab: @Composable () -> Unit,
    savingState: @Composable () -> Unit,
    doneState: @Composable () -> Unit,
) {

    Scaffold(
        topBar = { topBar() }
    ) {

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() using snapSizeTransform() },
            modifier = Modifier.padding(it)
        ) {

            when (it) {
                ScreenState.Loading -> loadingState()
                is ScreenState.Loaded -> {

                    Box {

                        val extraListSpacerState = rememberExtraListSpacerState()

                        Column(
                            modifier = Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .wrapContentWidth()
                                .onGloballyPositioned { extraListSpacerState.updateList(it) }
                                .widthIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            loadedState(it)
                            extraListSpacerState.ExtraSpacer()
                        }

                        Box(
                            Modifier.align(Alignment.BottomEnd)
                                .padding(20.dp)
                                .onGloballyPositioned { extraListSpacerState.updateOverlay(it) }
                        ) {
                            loadedStateFab()
                        }

                    }

                }

                ScreenState.Saving -> savingState()
                ScreenState.Done -> doneState()
            }

        }
    }

}


@Composable
private fun InputRow(icon: ImageVector, label: String, input: MutableState<String>) {
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
                    color = if (input.value.isValidLimitNumber()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    shape = getBottomLineShape(2.dp)
                )
                .padding(4.dp)
        )

    }
}

private fun String.isValidLimitNumber(): Boolean {
    return toIntOrNull().let { it == null || it < 0 }
}
