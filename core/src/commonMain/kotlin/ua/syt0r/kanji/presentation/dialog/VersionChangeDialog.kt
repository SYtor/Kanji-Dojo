package ua.syt0r.kanji.presentation.dialog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

@Composable
fun VersionChangeDialog(
    onDismissRequest: () -> Unit
) {

    MultiplatformDialog(onDismissRequest) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 10.dp)
                .heightIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                resolveString { about.versionChangesTitle },
                style = MaterialTheme.typography.titleLarge
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                version("2.0.9", LocalDate(2024, 7, 26)) {
                    append(
                        """
                        - Added option to animate character strokes after character is drawn
                        - Added finish confirmation dialog on back click during vocab practice
                        - Not completed words will show up in summary when finishing vocab practice
                        - SRS review adjustments
                        - Small UI updates
                        - Bug fixes and improvements
                        """.trimIndent()
                    )
                }
                version("2.0.8", LocalDate(2024, 7, 16)) {
                    append(
                        """
                        - Implemented FSRS support for vocab practice
                        - Added text field in addition to slider for items selection when configuring practice
                        - Added more info about radicals on kanji info screen
                        - UI updates
                        - Added MacOS builds to Github Releases
                        """.trimIndent()
                    )
                }
                version("2.0.7", LocalDate(2024, 6, 27)) {
                    append(
                        """
                        - Added more vocab practice modes
                        - Small redesigns of home screen in landscape mode and bottom sheets
                        - Fixed crash with multiple click on leave confirmation dialog
                        - Added a sponsor screen and the option to financially support the app in the Google Play version
                        """.trimIndent()
                    )
                }
                version("2.0.6", LocalDate(2024, 6, 14)) {
                    append(
                        """
                        - Renamed practice sets, now they are referred as decks
                        - Added option to create custom vocab decks
                        - Added more into to app credits to include information about used libraries       
                        """.trimIndent()
                    )
                }
                version("2.0.5", LocalDate(2024, 5, 28)) {
                    append(
                        """
                        - Added vocab practice, only predefined decks and reading picker mode for now
                        - Clickable radicals on kanji details screen
                        - Small updated to expressions ranking and reading priorities
                        - Fix kana sound autoplay not saved on reading practice
                        - Fix crash when pressing buttons on writing practice screen during animation
                        """.trimIndent()
                    )
                }
                version("2.0.4", LocalDate(2024, 5, 3)) {
                    append(
                        """
                        - Added Input Mode configuration for writing practice, allowing to write characters instead of strokes
                        - Removed practice type dialog on practice details screen, now it's togglable button
                        - Small improvements in ranking of expressions and translations
                        """.trimIndent()
                    )
                }
                version("2.0.3", LocalDate(2024, 3, 28)) {
                    append(
                        """
                        - Improved filter configuration on practice details screen
                        - Added more expressions for better character coverage
                        - Updated expressions ranking
                        - Added feedback option to settings screen and alternative expressions dialogs
                        - Added Linux and Windows builds to GitHub's releases
                        """.trimIndent()
                    )
                }
                version("2.0.2", LocalDate(2024, 3, 11)) {
                    append(
                        """
                        - Fix migration issue that caused crash on practice preview screen and during backup
                        - Handle database connection getting broken when backup creation is failing
                        """.trimIndent()
                    )
                }
                version("2.0.1", LocalDate(2024, 3, 11)) {
                    append(
                        """
                        - Implemented app data backup, available in the settings screen
                        - Updated default daily limit
                        - Fixed incorrectly saved alternative stroke evaluator configuration when using left handed mode
                        - Fixed no translation layout and alternative stroke evaluator mistakenly enabled by default
                        """.trimIndent()
                    )
                }
                version("2.0.0", LocalDate(2024, 3, 3)) {
                    append(
                        """
                        - Added kana voice sounds to practice
                        - Added option to see romaji for kana practice
                        - Now labels next to practice progress chart are clickable, making it possible to quickly start practice from there
                        - Added kanji variants information to writing practice
                        - Fixed chinese character versions showing up in the labels across the app
                        - Updated Japanese translations, thanks to AttractLight
                        - Updated alternative stroke evaluator, thanks to sl08154711
                        """.trimIndent()
                    )
                }
                version("1.9", LocalDate(2024, 1, 28)) {
                    append(
                        """
                        - Added options to merge and sort practice sets
                        - Added monochrome icon
                        - Added alternative stroke detection algorithm by sl08154711
                        - Added a limit for single character review duration when calculating data on Stats Screen to ignore inactive sessions
                        - Added total studied characters count to Stats Screen
                        - Bug fixes and small UI changes
                        """.trimIndent()
                    )
                }

                version("1.7", LocalDate(2023, 11, 30)) {
                    append(
                        """
                        - Added statistics screen
                        - Daily goal was renamed to daily limit and it can be disabled now
                        - Added indicators to practice dashboard when daily limit is enabled
                        - New practice details screen layout and share button
                        - More options on practice configuration
                        """.trimIndent()
                    )
                }
                version("1.6", LocalDate(2023, 10, 7)) {
                    append(
                        """
                        - Bug fixes: wrong daily progress, missing group details data and others
                        - UI improvements for large screens
                        """.trimIndent()
                    )
                }
                version("1.5", LocalDate(2023, 10, 3)) {
                    append(
                        """
                        - Reminder notification. Enable in settings
                        - Daily goal and quick practice menu on home screen
                        - Version change dialog
                        - Additional menu for practice configuration and flexible practice saving
                        - Manual app theme toggle support for older versions of Android
                        """.trimIndent()
                    )
                }
                version("1.4", LocalDate(2023, 3, 29)) {
                    append(
                        """
                        - Fix migration crash when custom sorting was applied
                        """.trimIndent()
                    )
                }
                version("1.3", LocalDate(2023, 3, 28)) {
                    append(
                        """
                        - Loading time optimizations
                        - Left-handed mode
                        - Landscape mode for character info screen
                        - First desktop version
                        - Fix crash with unknown 々 character
                        - Fix stuck issue on writing practice screen when drawing strokes too fast
                        - Fix crash with in app review dialog for devices with faulty Play Services and Chromebooks
                        """.trimIndent()
                    )
                }
                version("1.2", LocalDate(2023, 3, 6)) {
                    append(
                        """
                        - Fixed bug with incorrect group selected on multiselect dialog when filter is used
                        - Fixed bug with low resolution input and characters on Android 8
                        - Added new reading practice mode and additional filter option
                        - Added select/deselect all buttons when selecting groups for multiselect mode
                        - Added WaniKani levels to Select screen
                        - Removed extra delays when using search
                        """.trimIndent()
                    )
                }
                version("1.1", LocalDate(2023, 2, 16)) {
                    append(
                        """
                        - Added option to search by kanji radicals
                        - Added option to see only characters that need review
                        - Added alternative expressions dialog to writing practice screen
                        - Improved review indicator algorithm
                        - Landscape mode improvements
                        """.trimIndent()
                    )
                }
                version("1.0", LocalDate(2023, 1, 30)) {
                    append(
                        """
                        - Added search screen
                        - Added highlighting to review groups based on last review date
                        - Added button to view alternative readings and translations of expression items
                        """.trimIndent()
                    )
                }
            }

            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(resolveString { about.versionChangesButton })
            }

        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.version(
    versionNumber: String,
    releaseDate: LocalDate,
    changes: AnnotatedString.Builder.() -> Unit
) {
    stickyHeader {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Version: $versionNumber", modifier = Modifier.weight(1f))
            Text(text = releaseDate.toString(), style = MaterialTheme.typography.bodySmall)
        }
    }
    item {
        Text(
            text = AnnotatedString.Builder().apply(changes).toAnnotatedString(),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}