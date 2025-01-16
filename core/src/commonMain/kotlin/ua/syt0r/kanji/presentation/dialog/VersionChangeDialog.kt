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
                version("2.1.4", LocalDate(2025, 1, 17)) {
                    append(
                        """
                        - Added an option to the Settings tab to select the default tab opened when starting the app
                        - Added controls to calendars on the Stats tab to view stats from the past
                        - Added new cloud sync feature
                        - Updated FSRS v5 algorithm with new weights, implemented standard rounding to days and fixed incorrect hard interval in some cases
                        - Minor UI changes
                        - Fixed crash when overusing Again button
                        """.trimIndent()
                    )
                }
                version("2.1.3", LocalDate(2024, 9, 30)) {
                    append(
                        """
                        - Updated JLPT vocab decks using data from yomichan-jlpt-vocab open source project
                        - Fixed several misclassified letters from grade 4-6 and secondary school decks
                        - Fixed daily limit toggle value not being saved
                        - Updated vocab reading picker mode, now meanings are shown for all kana words
                        - Small UI updates
                        """.trimIndent()
                    )
                }
                version("2.1.2", LocalDate(2024, 9, 20)) {
                    append(
                        """
                        - Note for old users: read the migration notice down below!
                        - Added daily limit for vocab practice
                        - Added more daily limit configurations
                        - Added new sorting option by expected review date on letter deck details screen
                        - Added pending review indicators to dropdown menus when selecting practice types
                        - New MacOS redistributable
                        - Fixed translations being visible when doing letter reading practice and card is hidden
                        - Fixed letters being hidden when learning writing with hints
                        - Fixed timezone not being considered when calculating daily streak
                        - Migration notice: after migration to the FSRS, which is far less strict than the old algorithm that was resetting study progress each time you do a mistake, the next review intervals for the letters practiced before the migration can end up quite big
                        - To reduce the intervals visit the letter deck details screen (> button next to the deck on the Letters tab)
                        - Then sort letters with the new Expected Review option and review them using Again button
                        - Always use Again button to reduce given intervals if you are not confident about your recall ability
                        """.trimIndent()
                    )
                }
                version("2.1.1", LocalDate(2024, 9, 5)) {
                    append(
                        """
                        - Added daily streak section to home screen
                        - Added fancy kana voice button to letter practice
                        - Upgraded FSRS algorithm to version 5
                        - Fixed issue with FSRS intervals not decreasing when using Again and Hard buttons
                        - Fixed issue with redundant filtering of some letters in search
                        """.trimIndent()
                    )
                }
                version("2.1.0", LocalDate(2024, 8, 30)) {
                    append(
                        """
                        - Added new home screen with aggregated review data
                        - Added tutorial
                        - Replaced previous custom SRS algorithm for letter practice with FSRS 
                        - Now letter practice is saving review results in the middle of practice
                        - Added ability to merge and sort vocab decks
                        - Added experimental JLPT vocab decks
                        - Added vocab deck details screen with ability to filter and select individual words for practice
                        - Added tracking for vocab reviews so now they accounted in stats too
                        - Fixed issues with FSRS algorithm
                        """.trimIndent()
                    )
                }
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