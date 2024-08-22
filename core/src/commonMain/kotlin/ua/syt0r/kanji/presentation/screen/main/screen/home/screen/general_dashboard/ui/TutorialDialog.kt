package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardNoDecksButton
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardReviewButton
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.SrsAnswerButton
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.days


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialDialog(
    onDismissRequest: () -> Unit
) {

    val pages = listOf(
        TutorialPage {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    """
                    • The app is built with Spaced Repetition System (SRS) in mind
                    • You'll be prompted to repeatedly review letters and vocab you study withing the app according to you ability to recall relevant information
                    """.trimIndent(),
                    textAlign = TextAlign.Justify
                )

                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GeneralDashboardReviewButton(
                        onClick = {},
                        count = 4,
                        text = "New",
                        modifier = Modifier.weight(1f)
                    )

                    GeneralDashboardReviewButton(
                        onClick = {},
                        count = 40,
                        text = "Due",
                        modifier = Modifier.weight(1f)
                    )

                    GeneralDashboardReviewButton(
                        onClick = {},
                        count = 44,
                        text = "New & Due",
                        modifier = Modifier.weight(1f)
                    )
                }

            }
        },
        TutorialPage {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    """
                    • To estimate your recall ability the app will offer you various rating options after review
                    """.trimIndent(),
                    textAlign = TextAlign.Justify
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    SrsAnswerButton(
                        label = resolveString { vocabPractice.hardButton },
                        interval = 1.days,
                        onClick = { },
                        color = MaterialTheme.extraColorScheme.due,
                        outerModifier = Modifier
                    )
                    SrsAnswerButton(
                        label = resolveString { vocabPractice.goodButton },
                        interval = 3.days,
                        onClick = { },
                        color = MaterialTheme.extraColorScheme.success,
                        outerModifier = Modifier
                    )
                    SrsAnswerButton(
                        label = resolveString { vocabPractice.easyButton },
                        interval = 12.days,
                        onClick = { },
                        color = MaterialTheme.extraColorScheme.new,
                        outerModifier = Modifier
                    )
                }

                Text(
                    """
                    • Choose the option that best matches your ability to recall the item you're reviewing
                    • Grading your own answers lets you adjust the learning experience to match your memory
                    """.trimIndent(),
                    textAlign = TextAlign.Justify
                )
            }
        },
        TutorialPage {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    """
                    • Every day, the status of all reviewed items is updated, prompting you to review some of them again
                    • Make sure to visit the app daily because due items that are pending for review will build up over time
                    """.trimIndent(),
                    textAlign = TextAlign.Justify
                )

                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GeneralDashboardReviewButton(
                        onClick = {},
                        count = 0,
                        text = "New",
                        modifier = Modifier.weight(1f)
                    )

                    GeneralDashboardReviewButton(
                        onClick = {},
                        count = 999,
                        text = "Due",
                        modifier = Modifier.weight(1f)
                    )

                    GeneralDashboardReviewButton(
                        onClick = {},
                        count = 999,
                        text = "New & Due",
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    """
                    • Setup daily limit to avoid getting overwhelmed by reviews number
                    """.trimIndent(),
                    textAlign = TextAlign.Justify
                )

            }

        },
        TutorialPage {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    """
                    • To start using tha app create a deck. Decks are used to organize the data you are learning and consist of either letters or words
                    """.trimIndent(),
                    textAlign = TextAlign.Justify
                )

                GeneralDashboardNoDecksButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    """
                    • You can create your own decks or select from several pre-made ones based on your skill level
                    • Once any deck is created you can start doing reviews
                    """.trimIndent(),
                    textAlign = TextAlign.Justify
                )
            }
        },
    )

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Tutorial") },
        paddedContent = false,
        content = {
            val pagerState = rememberPagerState { pages.size }
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.heightIn(200.dp).animateContentSize()
            ) {
                val page = pages[it]
                Box(
                    Modifier.padding(horizontal = 20.dp)
                ) {
                    page.content()
                }
            }
            PagerIndicator(pagerState)

        },
        buttons = {}
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerIndicator(pagerState: PagerState) {

    val coroutineScope = rememberCoroutineScope()
    val animatePagerToPage: (Int) -> Unit = {
        coroutineScope.launch { pagerState.animateScrollToPage(it) }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = { animatePagerToPage(max(0, pagerState.currentPage - 1)) }
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
        }

        repeat(pagerState.pageCount) {
            Box(
                Modifier.size(8.dp)
                    .background(
                        color = if (pagerState.currentPage == it)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            )
        }

        IconButton(
            onClick = { animatePagerToPage(min(pagerState.pageCount, pagerState.currentPage + 1)) }
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}

data class TutorialPage(
    val content: @Composable () -> Unit
)
