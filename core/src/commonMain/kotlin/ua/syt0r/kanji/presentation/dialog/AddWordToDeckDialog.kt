package ua.syt0r.kanji.presentation.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.FilledTextField

@Composable
fun AddWordToDeckDialog(
    wordId: Long,
    wordPreviewReading: String,
    onDismissRequest: () -> Unit
) {

    val dialogState = rememberAddWordToDeckDialogState(wordId)

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        scrollableContent = false,
        title = { Text("Add $wordPreviewReading to vocab deck") },
        content = {
            AnimatedContent(
                targetState = dialogState.state.value,
                modifier = Modifier.fillMaxWidth().weight(1f, false)
            ) {
                DialogContent(
                    state = it,
                    onDismissRequest = onDismissRequest
                ) { dialogState.createNewDeck() }
            }
        },
        buttons = {
            TextButton(onDismissRequest) {
                Text("Cancel")
            }
            val isAddButtonEnabled = remember {
                derivedStateOf {
                    dialogState.state.value.let {
                        (it is AddingState.SelectingDeck && it.selectedDeck.value != null) ||
                                (it is AddingState.CreateNewDeck && it.title.value.isNotEmpty())
                    }
                }
            }
            TextButton(
                onClick = { dialogState.save() },
                enabled = isAddButtonEnabled.value
            ) {
                Text("Add")
            }
        }
    )

}

@Composable
private fun DialogContent(
    state: AddingState,
    onDismissRequest: () -> Unit,
    createNewDeck: () -> Unit
) {
    when (state) {
        AddingState.Loading -> {
            CircularProgressIndicator(Modifier.fillMaxWidth().wrapContentWidth())
        }

        is AddingState.SelectingDeck -> {
            Column(
                modifier = Modifier.heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = createNewDeck)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(" + Create New Deck")
                }
                state.decks.forEach { deck ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { state.selectedDeck.value = deck.id }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(deck.title, Modifier.weight(1f))
                        if (deck.id == state.selectedDeck.value)
                            Icon(Icons.Default.Check, null)
                    }
                }
            }
        }

        is AddingState.CreateNewDeck -> {
            FilledTextField(
                value = state.title.value,
                onValueChange = { state.title.value = it },
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                hintContent = {
                    Text(
                        text = "Enter deck title here...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
            )
        }

        AddingState.Saving -> {
            Text("Adding", Modifier.fillMaxWidth().wrapContentWidth())
        }

        AddingState.Completed -> {
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Added")
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
                delay(600)
                onDismissRequest()
            }
        }
    }
}

private sealed interface AddingState {
    object Loading : AddingState

    data class SelectingDeck(
        val decks: List<AddingDeckInfo>,
        val selectedDeck: MutableState<Long?>
    ) : AddingState

    data class CreateNewDeck(
        val title: MutableState<String>
    ) : AddingState

    object Saving : AddingState
    object Completed : AddingState
}

private data class AddingDeckInfo(
    val id: Long,
    val title: String
)

@Composable
private fun rememberAddWordToDeckDialogState(wordId: Long): AddWordToDeckDialogState {
    val coroutineScope = rememberCoroutineScope()
    return remember {
        val koin = getKoin()
        AddWordToDeckDialogState(
            wordId = wordId,
            coroutineScope = coroutineScope,
            repository = koin.get()
        )
    }
}

private class AddWordToDeckDialogState(
    private val wordId: Long,
    private val coroutineScope: CoroutineScope,
    private val repository: VocabPracticeRepository
) {

    private val _state = mutableStateOf<AddingState>(AddingState.Loading)
    val state: State<AddingState> = _state

    init {
        coroutineScope.launch {
            _state.value = AddingState.SelectingDeck(
                decks = repository.getDecks()
                    .map { AddingDeckInfo(it.id, it.title) },
                selectedDeck = mutableStateOf(null)
            )
        }
    }

    fun createNewDeck() {
        _state.value = AddingState.CreateNewDeck(
            title = mutableStateOf("")
        )
    }

    fun save() {
        val currentState = _state.value
        coroutineScope.launch {
            when (currentState) {
                is AddingState.CreateNewDeck -> {
                    _state.value = AddingState.Saving
                    repository.createDeck(
                        title = currentState.title.value,
                        words = listOf(wordId)
                    )
                }

                is AddingState.SelectingDeck -> {
                    val deckId = currentState.selectedDeck.value ?: return@launch
                    _state.value = AddingState.Saving
                    repository.addWord(deckId, wordId)
                }

                else -> return@launch
            }
            _state.value = AddingState.Completed
        }
    }

}
