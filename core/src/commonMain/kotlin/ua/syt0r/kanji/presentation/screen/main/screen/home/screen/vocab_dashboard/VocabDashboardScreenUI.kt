package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState


private val vocabPracticeSets: List<VocabPracticeSet> = listOf(
    vocabSets,
    vocabSets,
    vocabSets
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabDashboardScreenUI(
    state: State<ScreenState>,
    select: (VocabPracticeSet) -> Unit,
    navigateToPractice: (VocabPracticeSet) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = { BottomSheetContent(state, navigateToPractice) },
        sheetShape = MaterialTheme.shapes.medium,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 10.dp,
        sheetPeekHeight = 0.dp,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(paddingValues)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .wrapContentWidth()
                .widthIn(max = 400.dp)
        ) {

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Spacer(Modifier.height(20.dp))
            }

            items(vocabPracticeSets) { vocabPracticeSet ->
                PracticeGridItem(
                    title = vocabPracticeSet.title,
                    onClick = {
                        select(vocabPracticeSet)
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                )
            }

        }

    }

}

@Composable
private fun PracticeGridItem(
    title: String,
    onClick: () -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomSheetContent(
    state: State<ScreenState>,
    navigateToPractice: (VocabPracticeSet) -> Unit
) {


    val currentState = state.value

    if (currentState == ScreenState.NothingSelected) {
        CircularProgressIndicator(Modifier.fillMaxWidth().wrapContentWidth())
        return
    }

    currentState as ScreenState.SelectedSet

    val wordsState = currentState.words.collectAsState()
    var wordsVisible by rememberSaveable(currentState.set.title) { mutableStateOf(false) }
    val wordsHidingOverlayAlpha = animateFloatAsState(
        targetValue = if (wordsVisible) 0f else 1f
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    ) {

        item {

            Row(
                modifier = Modifier.padding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentState.set.title,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = { wordsVisible = !wordsVisible }
                ) {
                    Icon(
                        imageVector = when {
                            wordsVisible -> Icons.Default.Visibility
                            else -> Icons.Default.VisibilityOff
                        },
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = { navigateToPractice(currentState.set) },
                    colors = ButtonDefaults.neutralTextButtonColors()
                ) {
                    Text("Review")
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }

        }

        item {
            Text(
                text = "Expressions count: ${currentState.set.expressionIds.size}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item { Spacer(Modifier.height(8.dp)) }

        when (val vocabPracticePreviewState = wordsState.value) {
            is VocabPracticePreviewState.Loaded -> {
                itemsIndexed(vocabPracticePreviewState.words) { index, word ->
                    val hiddenColor = MaterialTheme.colorScheme.surfaceVariant
                    FuriganaText(
                        furiganaString = word.orderedPreview(index),
                        modifier = Modifier.drawWithContent {
                            drawContent()
                            drawRoundRect(
                                color = hiddenColor.copy(alpha = wordsHidingOverlayAlpha.value),
                                size = size,
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                    )
                }
            }

            VocabPracticePreviewState.Loading -> {
                item { CircularProgressIndicator(Modifier.size(24.dp)) }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

    }
}
