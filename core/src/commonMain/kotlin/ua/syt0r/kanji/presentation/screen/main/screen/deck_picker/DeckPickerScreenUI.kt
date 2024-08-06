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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.presentation.common.detectUrlClick
import ua.syt0r.kanji.presentation.common.jsonSaver
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.DeckPickerScreenContract.ScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckPickerScreenUI(
    state: State<ScreenState>,
    onUpButtonClick: () -> Unit,
    createEmpty: () -> Unit,
    onItemSelected: (classification: CharacterClassification, title: String) -> Unit,
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
                    onItemClick = onItemSelected,
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
    onItemClick: (classification: CharacterClassification, title: String) -> Unit = { _, _ -> },
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

        item { HorizontalDivider(Modifier.padding(horizontal = 8.dp)) }

        categoryWithIndexMap.forEach { (index, category) ->

            val isExpanded = categoryIndexToExpandedMap[index] == true

            val toggleCategoryExpanded = {
                categoryIndexToExpandedMap = categoryIndexToExpandedMap.plus(
                    index to !isExpanded
                )
            }

            item(
                key = "$index header"
            ) {

                ClickableRow(
                    onClick = toggleCategoryExpanded,
                    modifier = Modifier.animateItemPlacement()
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

            if (isExpanded) {

                item(
                    key = "$index description"
                ) {
                    val description = resolveString(category.description)
                    ClickableText(
                        text = description,
                        onClick = { position -> description.detectUrlClick(position, onLinkClick) },
                        modifier = Modifier.animateItemPlacement()
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                items(
                    items = category.items,
                    key = { it.classification.toString() }
                ) {
                    val title = it.title()
                    ClickableRow(
                        onClick = { onItemClick(it.classification, title) },
                        modifier = Modifier.animateItemPlacement()
                    ) {

                        Card(
                            modifier = Modifier.padding(vertical = 8.dp).size(46.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = it.previewText, fontSize = 30.sp)
                            }
                        }

                        Text(
                            text = title,
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                }

            }

            val isLast = index == screenState.categories.size - 1
            if (!isLast) {
                item(
                    key = "$index divider"
                ) {
                    HorizontalDivider(
                        modifier = Modifier.animateItemPlacement().padding(horizontal = 8.dp)
                    )
                }
            }

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
