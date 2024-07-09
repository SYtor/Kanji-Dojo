package ua.syt0r.kanji.presentation.common.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun FilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    hintContent: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    FilledTextFieldDecorations(
        isEmpty = rememberUpdatedState(value.isEmpty()),
        modifier = modifier,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        hintContent = hintContent,
    ) { isInputFocused ->
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isInputFocused.value = it.isFocused },
            maxLines = 1,
            singleLine = true,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(color),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color)
        )
    }
}

@Composable
fun FilledTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    hintContent: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {

    FilledTextFieldDecorations(
        isEmpty = rememberUpdatedState(value.text.isEmpty()),
        modifier = modifier,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        hintContent = hintContent,
    ) { isInputFocused ->
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isInputFocused.value = it.isFocused },
            maxLines = 1,
            singleLine = true,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(color),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color)
        )
    }

}

@Composable
private fun FilledTextFieldDecorations(
    isEmpty: State<Boolean>,
    modifier: Modifier,
    leadingContent: (@Composable () -> Unit)?,
    trailingContent: (@Composable () -> Unit)?,
    hintContent: (@Composable () -> Unit)?,
    content: @Composable (isInputFocused: MutableState<Boolean>) -> Unit
) {

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        leadingContent?.invoke()

        val isInputFocused = remember { mutableStateOf(false) }
        val isHintVisible = remember { derivedStateOf { isEmpty.value && !isInputFocused.value } }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            CompositionLocalProvider(
                LocalTextSelectionColors provides TextSelectionColors(
                    handleColor = MaterialTheme.colorScheme.onSurface,
                    backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            ) {
                content(isInputFocused)
            }

            hintContent?.let {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isHintVisible.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    it.invoke()
                }
            }

        }

        trailingContent?.invoke()

    }

}