package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardBottomBarLayout
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VocabDashboardBottomBarUI(
    state: State<ScreenState>,
    navigateToDeckPicker: () -> Unit,
    modifier: Modifier
) {

    AnimatedContent(
        targetState = state.value,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth()
    ) { screenState ->

        when (screenState) {
            is ScreenState.Loaded -> {
                DeckDashboardBottomBarLayout(
                    modifier = Modifier.fillMaxWidth(),
                    centralContent = {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surface.copy(0.9f))
                                    .clickable(onClick = { dropdownExpanded = true })
                                    .padding(ButtonDefaults.TextButtonContentPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val practiceTypeLabel = resolveString(
                                    screenState.srsPracticeType.value.titleResolver
                                )
                                Text(
                                    text = resolveString {
                                        vocabDashboard
                                            .selectedPracticeTypeTemplate(practiceTypeLabel)
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Light
                                )
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {

                                ScreenVocabPracticeType.values().forEach {
                                    DropdownMenuItem(
                                        text = { Text(resolveString(it.titleResolver)) },
                                        onClick = {
                                            screenState.srsPracticeType.value = it
                                            dropdownExpanded = false
                                        }
                                    )
                                }

                            }
                        }
                    },
                    fabContent = {
                        FloatingActionButton(
                            onClick = navigateToDeckPicker,
                            modifier = Modifier.animateEnterExit(
                                enter = scaleIn(),
                                exit = scaleOut()
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            ScreenState.Loading -> Box(Modifier.fillMaxWidth())
        }

    }
}
