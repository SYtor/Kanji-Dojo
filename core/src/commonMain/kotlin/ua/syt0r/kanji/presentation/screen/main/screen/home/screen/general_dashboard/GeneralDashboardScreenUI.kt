package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.MultiplatformPopup
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.common.ui.PopupContentItem
import ua.syt0r.kanji.presentation.common.ui.kanji.PreviewKanji
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.SrsAnswerButton
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.days

@Composable
fun GeneralDashboardScreenUI() {

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .wrapContentWidth()
    ) {

        if (LocalOrientation.current == Orientation.Landscape)
            Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .width(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Row(
                modifier = Modifier.clip(ButtonDefaults.textShape)
                    .clickable { }
                    .padding(ButtonDefaults.TextButtonContentPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier.size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .alignBy { it.measuredHeight }
                )
                Text("What's new", Modifier.alignByBaseline())
                Icon(Icons.Outlined.Celebration, null)
            }

            var showTutorialDialog by remember { mutableStateOf(false) }
            if (showTutorialDialog) {
                TutorialDialog { showTutorialDialog = false }
            }

            TextButton(
                onClick = { showTutorialDialog = true },
                modifier = Modifier,
                colors = ButtonDefaults.neutralTextButtonColors()
            ) {
                Text("Tutorial", Modifier.padding(end = 8.dp))
                Icon(Icons.AutoMirrored.Outlined.HelpOutline, null)
            }

            TextButton(
                onClick = {},
                modifier = Modifier,
                colors = ButtonDefaults.neutralTextButtonColors()
            ) {
                Text("Daily Limit", Modifier.padding(end = 8.dp))
                Icon(Icons.Outlined.Settings, null)
            }

        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
                .wrapContentWidth()
                .widthIn(max = 380.dp)
                .padding(vertical = 8.dp)
        )

        DashboardItem(
            title = "Letter Decks",
            modeList = listOf(
                DashboardSrsModeData(
                    modeTitle = "Writing",
                    total = (0..100).map { PreviewKanji.randomKanji() },
                    done = (0..100).map { PreviewKanji.randomKanji() },
                    due = (0..100).map { PreviewKanji.randomKanji() },
                    new = (0..100).map { PreviewKanji.randomKanji() },
                )
            )
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
                .wrapContentWidth()
                .widthIn(max = 380.dp)
                .padding(bottom = 8.dp)
        )

        DashboardItem(
            title = "Vocab Decks",
            modeList = listOf(
                DashboardSrsModeData(
                    modeTitle = "Flashcard",
                    total = (0..100).map { PreviewKanji.randomKanji() },
                    done = (0..100).map { PreviewKanji.randomKanji() },
                    due = (0..100).map { PreviewKanji.randomKanji() },
                    new = (0..100).map { PreviewKanji.randomKanji() },
                )
            )
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {}) {
                Icon(imageVector = discordImageVector, contentDescription = null)
            }
            IconButton(onClick = {}) {
                Icon(imageVector = githubImageVector, contentDescription = null)
            }

        }
    }

}

@Composable
private fun <T> DashboardItem(
    title: String,
    modeList: List<DashboardSrsModeData<T>>
) {

    var selectedMode by remember { mutableStateOf(modeList.first()) }

    Column(
        modifier = Modifier.fillMaxWidth()
            .wrapContentWidth()
            .widthIn(max = 400.dp),
    ) {

        Row(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = resolveString { title },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = resolveString { "SRS Mode" },
                modifier = Modifier.alignByBaseline(),
            )

            var expanded by remember { mutableStateOf(false) }

            Box(Modifier.alignByBaseline()) {

                TextButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = selectedMode.modeTitle,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Default.ArrowDropDown, null)
                }

                MultiplatformPopup(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    modeList.forEach {
                        PopupContentItem(
                            onClick = {
                                selectedMode = it
                                expanded = false
                            }
                        ) {
                            Text(it.modeTitle)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReviewButton(
                onClick = {},
                color = MaterialTheme.extraColorScheme.new,
                count = selectedMode.new.size,
                text = "New",
                modifier = Modifier.weight(1f)
            )

            ReviewButton(
                onClick = {},
                color = MaterialTheme.extraColorScheme.due,
                count = selectedMode.due.size,
                text = "Due",
                modifier = Modifier.weight(1f)
            )

        }

        val allItems = selectedMode.run { new + due }

        ReviewButton(
            onClick = {},
            color = MaterialTheme.colorScheme.primary,
            count = allItems.size,
            text = "New & Due",
            modifier = Modifier.fillMaxWidth().padding(8.dp),
        )

    }
}

@Composable
private fun ReviewButton(
    onClick: () -> Unit,
    color: Color,
    count: Int,
    text: String,
    modifier: Modifier,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxHeight()
                .width(4.dp)
                .background(color, CircleShape)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Light
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Icon(Icons.Default.KeyboardArrowRight, null)
    }
}

data class TutorialPage(
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialDialog(
    onDismissRequest: () -> Unit
) {

    val pages = listOf(
        TutorialPage {
            Column {
                Text(
                    """
                    • The app is built around SRS (Spaced Repetition System)
                    • You'll be prompted to repeatedly review letters and vocab you study from time to time
                    • Words and letters you find challenging to memorize will be prompted for review more frequently 
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
                    • After reviewing a word or letter you'll be given various ratings options
                    • Choose the option that best matches your ability to recall it
                    • Each option also includes an interval that indicates when you'll be prompted to review it again 
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
            }
        },
        TutorialPage {
            Text(
                """
                    • Every day, the status of all reviewed items is updated, prompting you to review some of them again
                    • Make sure to check-in daily because due items pending for review will build up over time
                    • You can set daily limit to avoid getting overwhelmed by reviews number 
                    """.trimIndent(),
                textAlign = TextAlign.Justify
            )
        },
        TutorialPage {
            Text(
                """
                    • To start using the app navigate to letters or vocab tab and create a deck
                    • Decks consist of letters or words and are used to track your study progress
                    • You can create your own decks or pick suggested decks according to your level
                    """.trimIndent(),
                textAlign = TextAlign.Justify
            )
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

@Composable
private fun RowScope.SelectableMode(
    onClick: () -> Unit,
    isSelected: Boolean,
    new: Boolean,
    due: Boolean,
    text: String
) {

    Row(
        modifier = Modifier
            .clip(ButtonDefaults.textShape)
            .clickable(onClick = onClick)
            .background(
                when (isSelected) {
                    true -> MaterialTheme.colorScheme.surfaceVariant
                    false -> MaterialTheme.colorScheme.surface
                }
            )
            .padding(ButtonDefaults.TextButtonContentPadding)
            .height(IntrinsicSize.Max)
            .alignByBaseline(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text)
        if (new || due)
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                    .size(8.dp)
            )
//        if (due)
//            Box(
//                modifier = Modifier.background(MaterialTheme.extraColorScheme.due, CircleShape)
//                    .width(8.dp)
//                    .fillMaxHeight()
//                    .alignBy { it.measuredHeight })
    }

}

val githubImageVector = materialIcon(name = "github") {
    materialPath {
        moveTo(10.9f, 2.1f)
        curveTo(6.3f, 2.6f, 2.6f, 6.3f, 2.1f, 10.8f)
        curveTo(1.6f, 15.5f, 4.3f, 19.7f, 8.4f, 21.3f)
        curveTo(8.7f, 21.4f, 9.0f, 21.2f, 9.0f, 20.8f)
        lineTo(9.0f, 19.2f)
        curveTo(9.0f, 19.2f, 8.6f, 19.3f, 8.1f, 19.3f)
        curveTo(6.7f, 19.3f, 6.1f, 18.1f, 6.0f, 17.4f)
        curveTo(5.9f, 17.0f, 5.7f, 16.7f, 5.4f, 16.4f)
        curveTo(5.1f, 16.3f, 5.0f, 16.3f, 5.0f, 16.2f)
        curveTo(5.0f, 16.0f, 5.3f, 16.0f, 5.4f, 16.0f)
        curveTo(6.0f, 16.0f, 6.5f, 16.7f, 6.7f, 17.0f)
        curveTo(7.2f, 17.8f, 7.8f, 18.0f, 8.1f, 18.0f)
        curveTo(8.5f, 18.0f, 8.8f, 17.9f, 9.0f, 17.8f)
        curveTo(9.1f, 17.1f, 9.4f, 16.4f, 10.0f, 16.0f)
        curveTo(7.7f, 15.5f, 6.0f, 14.2f, 6.0f, 12.0f)
        curveTo(6.0f, 10.9f, 6.5f, 9.8f, 7.2f, 9.0f)
        curveTo(7.1f, 8.3f, 7.0f, 7.8f, 7.0f, 7.1f)
        curveTo(7.0f, 6.7f, 7.0f, 6.1f, 7.3f, 5.5f)
        curveTo(7.3f, 5.5f, 8.7f, 5.5f, 10.1f, 6.8f)
        curveTo(10.6f, 6.7f, 11.3f, 6.6f, 12.0f, 6.6f)
        curveTo(12.7f, 6.6f, 13.4f, 6.7f, 14.0f, 6.9f)
        curveTo(15.3f, 5.6f, 16.8f, 5.6f, 16.8f, 5.6f)
        curveTo(17.0f, 6.1f, 17.0f, 6.7f, 17.0f, 7.1f)
        curveTo(17.0f, 7.9f, 16.9f, 8.3f, 16.8f, 8.5f)
        curveTo(17.5f, 9.3f, 18.0f, 10.3f, 18.0f, 11.5f)
        curveTo(18.0f, 13.7f, 16.3f, 15.0f, 14.0f, 15.5f)
        curveTo(14.6f, 16.0f, 15.0f, 16.9f, 15.0f, 17.8f)
        lineTo(15.0f, 20.4f)
        curveTo(15.0f, 20.7f, 15.3f, 21.0f, 15.7f, 20.9f)
        curveTo(19.4f, 19.4f, 22.0f, 15.8f, 22.0f, 11.6f)
        curveTo(22.0f, 6.1f, 16.9f, 1.4f, 10.9f, 2.1f)
        close()
    }
}

val discordImageVector = materialIcon(name = "discord") {
    materialPath {
        moveTo(19.952f, 5.672f)
        curveTo(18.048f, 4.141f, 15.036f, 3.882f, 14.908f, 3.871f)
        curveTo(14.707f, 3.854f, 14.516f, 3.968f, 14.434f, 4.152f)
        curveTo(14.428f, 4.164f, 14.362f, 4.315f, 14.289f, 4.55f)
        curveTo(15.548f, 4.762f, 17.095f, 5.19f, 18.495f, 6.059f)
        curveTo(18.719f, 6.198f, 18.788f, 6.493f, 18.649f, 6.718f)
        curveTo(18.559f, 6.864f, 18.402f, 6.944f, 18.242f, 6.944f)
        curveTo(18.156f, 6.944f, 18.069f, 6.921f, 17.99f, 6.872f)
        curveTo(15.584f, 5.38f, 12.578f, 5.305f, 12.0f, 5.305f)
        curveTo(11.422f, 5.305f, 8.415f, 5.38f, 6.011f, 6.872f)
        curveTo(5.786f, 7.012f, 5.492f, 6.942f, 5.352f, 6.718f)
        curveTo(5.212f, 6.493f, 5.282f, 6.198f, 5.506f, 6.059f)
        curveTo(6.906f, 5.19f, 8.452f, 4.762f, 9.711f, 4.55f)
        curveTo(9.637f, 4.314f, 9.572f, 4.164f, 9.567f, 4.152f)
        curveTo(9.485f, 3.968f, 9.294f, 3.852f, 9.092f, 3.872f)
        curveTo(8.965f, 3.882f, 5.953f, 4.141f, 4.023f, 5.672f)
        curveTo(3.015f, 6.625f, 1.0f, 12.073f, 1.0f, 16.783f)
        curveTo(1.0f, 16.866f, 1.022f, 16.948f, 1.063f, 17.02f)
        curveTo(2.454f, 19.463f, 6.248f, 20.103f, 7.113f, 20.131f)
        curveTo(7.118f, 20.131f, 7.123f, 20.131f, 7.128f, 20.131f)
        curveTo(7.281f, 20.131f, 7.425f, 20.058f, 7.515f, 19.934f)
        lineTo(8.39f, 18.732f)
        curveTo(6.031f, 18.122f, 4.826f, 17.087f, 4.756f, 17.026f)
        curveTo(4.558f, 16.851f, 4.539f, 16.549f, 4.714f, 16.351f)
        curveTo(4.889f, 16.153f, 5.19f, 16.134f, 5.388f, 16.309f)
        curveTo(5.417f, 16.335f, 7.636f, 18.218f, 12.0f, 18.218f)
        curveTo(16.372f, 18.218f, 18.591f, 16.327f, 18.613f, 16.309f)
        curveTo(18.811f, 16.136f, 19.113f, 16.154f, 19.287f, 16.352f)
        curveTo(19.461f, 16.549f, 19.442f, 16.85f, 19.245f, 17.025f)
        curveTo(19.175f, 17.086f, 17.97f, 18.121f, 15.611f, 18.731f)
        lineTo(16.486f, 19.933f)
        curveTo(16.576f, 20.057f, 16.72f, 20.131f, 16.873f, 20.131f)
        curveTo(16.878f, 20.131f, 16.883f, 20.131f, 16.888f, 20.131f)
        curveTo(17.753f, 20.103f, 21.547f, 19.463f, 22.937f, 17.02f)
        curveTo(22.978f, 16.947f, 23.0f, 16.866f, 23.0f, 16.783f)
        curveTo(23.0f, 12.073f, 20.985f, 6.625f, 19.952f, 5.672f)
        close()
        moveTo(8.891f, 14.87f)
        curveTo(7.967f, 14.87f, 7.217f, 14.013f, 7.217f, 12.957f)
        curveTo(7.217f, 11.902f, 7.967f, 11.045f, 8.891f, 11.045f)
        curveTo(9.816f, 11.045f, 10.565f, 11.902f, 10.565f, 12.957f)
        curveTo(10.565f, 14.013f, 9.816f, 14.87f, 8.891f, 14.87f)
        close()
        moveTo(15.109f, 14.87f)
        curveTo(14.184f, 14.87f, 13.435f, 14.013f, 13.435f, 12.957f)
        curveTo(13.435f, 11.902f, 14.184f, 11.045f, 15.109f, 11.045f)
        curveTo(16.033f, 11.045f, 16.783f, 11.902f, 16.783f, 12.957f)
        curveTo(16.783f, 14.013f, 16.033f, 14.87f, 15.109f, 14.87f)
        close()
    }
}
