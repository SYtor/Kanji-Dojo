package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.formattedVocabStringReading
import ua.syt0r.kanji.core.user_data.database.VocabCardData
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.ui.FilledTextField
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.VocabDeckEditListItem

interface VocabEditDialogState {
    val item: VocabDeckEditListItem
    val state: StateFlow<VocabEditDialogContentState>
    fun updateItem()
}

sealed interface VocabEditDialogContentState {
    data object Loading : VocabEditDialogContentState
    data class Loaded(
        val kanjiToKanaReadingsMap: Map<String?, List<String>>,
        val selectedKanjiReading: MutableState<String?>,
        val selectedKanaReading: MutableState<String>,
        val selectedMeaning: MutableState<String?>
    ) : VocabEditDialogContentState
}

@Composable
fun rememberVocabEditDialogState(item: VocabDeckEditListItem): VocabEditDialogState {
    val coroutineScope = rememberCoroutineScope()
    val appDataRepository = koinInject<AppDataRepository>()
    return remember { DefaultVocabEditDialogState(item, coroutineScope, appDataRepository) }
}

@Composable
fun VocabEditDialog(
    state: VocabEditDialogState,
    onDismissRequest: () -> Unit
) {

    val applyEdit = {
        state.updateItem()
        onDismissRequest()
    }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = {
            val (kanji, kana) = state.item.card.data.run { kanjiReading to kanaReading }
            val title = formattedVocabStringReading(kana, kanji)
            Text("Edit $title")
        },
        content = {

            AnimatedContent(
                targetState = state.state.collectAsState().value,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { contentState ->

                when (contentState) {
                    VocabEditDialogContentState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentWidth()
                            .padding(vertical = 100.dp)
                    )

                    is VocabEditDialogContentState.Loaded -> {
                        Column(
                            modifier = Modifier.animateContentSize()
                        ) {

                            var selectedKanjiReading by contentState.selectedKanjiReading
                            var selectedKanaReading by contentState.selectedKanaReading
                            var selectedMeaning by contentState.selectedMeaning

                            SwitchListItem(
                                headlineContent = { Text("Kanji") },
                                enabled = contentState.kanjiToKanaReadingsMap.size > 1,
                                isChecked = selectedKanjiReading != null,
                                onCheckedChange = {
                                    if (selectedKanjiReading == null) {
                                        val kanjiToKanaReadings = contentState
                                            .kanjiToKanaReadingsMap.entries
                                            .first { it.key != null }
                                        selectedKanjiReading = kanjiToKanaReadings.key
                                        selectedKanaReading = kanjiToKanaReadings.value.first()
                                    } else {
                                        selectedKanjiReading = null
                                    }
                                }
                            )

                            if (selectedKanjiReading != null) {
                                contentState.kanjiToKanaReadingsMap.entries
                                    .mapNotNull { it.key }
                                    .forEach {
                                        SelectableListItem(
                                            headlineContent = { ReadingText(it) },
                                            isSelected = it == selectedKanjiReading,
                                            select = { selectedKanjiReading = it }
                                        )
                                    }
                            }

                            ListItem(
                                headlineContent = { Text("Kana") }
                            )

                            val kanaReadings = contentState.kanjiToKanaReadingsMap
                                .getValue(selectedKanjiReading)

                            kanaReadings.forEach {
                                SelectableListItem(
                                    headlineContent = { ReadingText(it) },
                                    isSelected = it == selectedKanaReading,
                                    select = { selectedKanaReading = it }
                                )
                            }

                            SwitchListItem(
                                headlineContent = { Text("Dictionary Meaning") },
                                enabled = true,
                                isChecked = selectedMeaning == null,
                                onCheckedChange = {
                                    selectedMeaning = when {
                                        selectedMeaning != null -> null
                                        else -> state.item.card.meaning
                                    }
                                }
                            )

                            if (selectedMeaning != null) {
                                ListItem(
                                    headlineContent = {
                                        FilledTextField(
                                            value = selectedMeaning!!,
                                            onValueChange = { selectedMeaning = it },
                                            hintContent = { Text("Meaning") }
                                        )
                                    },
                                )
                            }

                        }
                    }
                }

            }

        },
        buttons = {
            TextButton(onDismissRequest) { Text("Cancel") }
            TextButton(applyEdit) { Text("Apply") }
        }
    )

}

@Composable
private fun SwitchListItem(
    headlineContent: @Composable () -> Unit,
    enabled: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = headlineContent,
        trailingContent = { Switch(isChecked, onCheckedChange, enabled = enabled) },
        modifier = Modifier.clip(MaterialTheme.shapes.large)
            .clickable(enabled) { onCheckedChange(!isChecked) }
    )
}

@Composable
private fun SelectableListItem(
    headlineContent: @Composable () -> Unit,
    isSelected: Boolean,
    select: () -> Unit
) {
    ListItem(
        headlineContent = headlineContent,
        trailingContent = {
            if (isSelected) Icon(Icons.Default.Check, null)
        },
        modifier = Modifier.clip(MaterialTheme.shapes.large)
            .clickable(onClick = select)
    )
}

@Composable
private fun ReadingText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 20.dp)
    )
}

class DefaultVocabEditDialogState(
    override val item: VocabDeckEditListItem,
    coroutineScope: CoroutineScope,
    appDataRepository: AppDataRepository
) : VocabEditDialogState {

    private val _state = MutableStateFlow<VocabEditDialogContentState>(
        VocabEditDialogContentState.Loading
    )

    override val state: StateFlow<VocabEditDialogContentState> = _state

    init {
        coroutineScope.launch {
            val cardData = item.card.run { modifiedData.value ?: data }
            val word = appDataRepository.getDetailedWord(item.card.data.dictionaryId)
            val sense = word.senseList.find {
                it.readings.any {
                    cardData.kanjiReading == it.kanji &&
                            cardData.kanaReading == it.kana
                }
            }

            if (sense == null) error("No relevant sense found for $cardData")

            val kanjiToKanaReadingsMap = sense.readings
                .groupBy { it.kanji }
                .mapValues { (kanji, readings) -> readings.map { it.kana }.distinct() }
                .toMap()

            _state.value = VocabEditDialogContentState.Loaded(
                kanjiToKanaReadingsMap = kanjiToKanaReadingsMap,
                selectedKanjiReading = mutableStateOf(cardData.kanjiReading),
                selectedKanaReading = mutableStateOf(cardData.kanaReading),
                selectedMeaning = mutableStateOf(cardData.meaning)
            )
        }
    }

    override fun updateItem() {
        val currentState = _state.value as VocabEditDialogContentState.Loaded
        val modifiedData = VocabCardData(
            kanjiReading = currentState.selectedKanjiReading.value,
            kanaReading = currentState.selectedKanaReading.value,
            meaning = currentState.selectedMeaning.value,
            dictionaryId = item.card.data.dictionaryId
        )
        item.card.modifiedData.value = modifiedData
    }

}