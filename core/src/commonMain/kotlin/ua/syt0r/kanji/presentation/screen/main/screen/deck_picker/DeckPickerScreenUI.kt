package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.core.app_data.WordClassification
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.presentation.common.detectUrlClick
import ua.syt0r.kanji.presentation.common.jsonSaver
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.DeckPickerScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerCategory
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerDeck
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.LetterDeckPickerDeck
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.VocabDeckPickerDeck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckPickerScreenUI(
    state: State<ScreenState>,
    onUpButtonClick: () -> Unit,
    createEmpty: () -> Unit,
    onLetterDeckClick: (classification: CharacterClassification, title: String) -> Unit,
    onVocabDeckClick: (classification: WordClassification, title: String) -> Unit,
    onLinkClick: (String) -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = resolveString { deckPicker.title }) },
                navigationIcon = {
                    IconButton(onClick = onUpButtonClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> LoadingState()
                is ScreenState.Loaded -> LoadedState(
                    screenState = screenState,
                    createEmpty = createEmpty,
                    onLetterDeckClick = onLetterDeckClick,
                    onVocabDeckClick = onVocabDeckClick,
                    onLinkClick = onLinkClick
                )
            }

        }

    }

}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadedState(
    screenState: ScreenState.Loaded,
    createEmpty: () -> Unit,
    onLetterDeckClick: (classification: CharacterClassification, title: String) -> Unit,
    onVocabDeckClick: (classification: WordClassification, title: String) -> Unit,
    onLinkClick: (String) -> Unit
) {

    val categoryWithIndexMap = screenState.categories.mapIndexed { i, c -> i to c }
    var categoryIndexToExpandedMap by rememberSaveable(stateSaver = jsonSaver()) {
        mutableStateOf(mapOf<Int, Boolean>())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 10.dp)
    ) {

        val divider = { id: String ->
            item(id) {
                HorizontalDivider(
                    modifier = Modifier.animateItemPlacement()
                        .padding(vertical = 4.dp, horizontal = 10.dp)

                )
            }
        }

        item {
            ClickableRow(onClick = createEmpty) {
                Text(
                    text = resolveString { deckPicker.customDeckButton },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = createEmpty) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }
        }

        divider("empty_deck_divider")

        categoryWithIndexMap.forEach { (categoryIndex, category) ->

            val isExpanded = categoryIndexToExpandedMap[categoryIndex] == true

            val toggleCategoryExpanded = {
                categoryIndexToExpandedMap = categoryIndexToExpandedMap.plus(
                    categoryIndex to !isExpanded
                )
            }

            item(
                key = "$categoryIndex header"
            ) {
                CategoryHeader(
                    category = category,
                    isExpanded = isExpanded,
                    toggleCategoryExpanded = toggleCategoryExpanded,
                    modifier = Modifier.animateItemPlacement()
                )
            }

            if (isExpanded) {

                item(
                    key = "$categoryIndex description"
                ) {
                    val description = resolveString(category.description)
                    ClickableText(
                        text = description,
                        onClick = { position -> description.detectUrlClick(position, onLinkClick) },
                        modifier = Modifier.animateItemPlacement()
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Justify
                        ),
                    )
                }

                itemsIndexed(
                    items = category.items,
                    key = { index, _ -> "$categoryIndex $index" }
                ) { _, it ->
                    CategoryItem(
                        deck = it,
                        onLetterDeckClick = onLetterDeckClick,
                        onVocabDeckClick = onVocabDeckClick,
                        modifier = Modifier.animateItemPlacement()
                    )
                }

            }

            val isLast = categoryIndex == screenState.categories.size - 1
            if (!isLast) divider("$categoryIndex divider")

        }

        item { Spacer(Modifier.height(20.dp)) }

    }
}

@Composable
private fun CategoryHeader(
    category: DeckPickerCategory,
    isExpanded: Boolean,
    toggleCategoryExpanded: () -> Unit,
    modifier: Modifier
) {
    ClickableRow(
        onClick = toggleCategoryExpanded,
        modifier = modifier
    ) {
        Text(
            text = resolveString(category.title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = toggleCategoryExpanded) {
            val icon = if (isExpanded) {
                Icons.Default.KeyboardArrowUp
            } else {
                Icons.Default.KeyboardArrowDown
            }
            Icon(icon, null)
        }

    }
}

@Composable
private fun CategoryItem(
    deck: DeckPickerDeck,
    onLetterDeckClick: (classification: CharacterClassification, title: String) -> Unit,
    onVocabDeckClick: (classification: WordClassification, title: String) -> Unit,
    modifier: Modifier
) {

    val title = resolveString(deck.title)
    ClickableRow(
        onClick = {
            when (deck) {
                is LetterDeckPickerDeck -> onLetterDeckClick(deck.classification, title)
                is VocabDeckPickerDeck -> onVocabDeckClick(deck.classification, title)
            }
        },
        modifier = modifier
    ) {

        if (deck is LetterDeckPickerDeck) {

            Card(
                modifier = Modifier.padding(vertical = 8.dp).size(46.dp)
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = deck.previewText, fontSize = 30.sp)
                }
            }

        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
        )

        if (deck is VocabDeckPickerDeck) {
            Text(
                text = resolveString { deckPicker.vocabDeckItemWordsCountLabel(deck.wordsCount) },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }

    }

}

@Composable
private fun ClickableRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(50.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 6.dp)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }

}
