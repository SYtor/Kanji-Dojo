package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString


@Composable
fun MergeConfirmationDialog(
    decks: List<DeckDashboardItem>,
    listMode: DeckDashboardListMode.MergeMode,
    onDismissRequest: () -> Unit,
    onConfirmed: (DecksMergeRequestData) -> Unit
) {
    val strings = resolveString { lettersDashboard }
    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = strings.mergeDialogTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        content = {
            val title = listMode.title.value
            val decksIdList = listMode.selected.value.toList()
            val mergedDeckTitles = decks.filter { decksIdList.contains(it.id) }.map { it.title }
            Text(
                text = strings.mergeDialogMessage(title, mergedDeckTitles),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(strings.mergeDialogCancelButton)
            }
            TextButton(
                onClick = {
                    onConfirmed(
                        DecksMergeRequestData(
                            title = listMode.title.value,
                            deckIds = listMode.selected.value.toList()
                        )
                    )
                }
            ) {
                Text(strings.mergeDialogAcceptButton)
            }
        }
    )
}
