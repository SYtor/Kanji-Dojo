package ua.syt0r.kanji.presentation.common.resources.string

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.withClickableUrl
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreen
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract
import kotlin.time.Duration

object EnglishStrings : Strings {

    override val appName: String = "Kanji Dojo"

    override val hiragana: String = "Hiragana"
    override val katakana: String = "Katakana"

    override val kunyomi: String = "Kun"
    override val onyomi: String = "On"

    override val loading: String = "Loading"

    override val letterPracticeTypeWriting: String = "Writing"
    override val letterPracticeTypeReading: String = "Reading"
    override val vocabPracticeTypeFlashcard: String = "Flashcard"
    override val vocabPracticeTypeReadingPicker: String = "Reading Picker"
    override val vocabPracticeTypeWriting: String = "Writing"

    override val reviewStateDone: String = "Done"
    override val reviewStateDue: String = "Due"
    override val reviewStateNew: String = "New"

    override val home: HomeStrings = EnglishHomeStrings
    override val generalDashboard: GeneralDashboardStrings = EnglishGeneralDashboardStrings
    override val commonDashboard = EnglishCommonDashboardStrings
    override val dailyLimit: DailyLimitStrings = EnglishDailyLimitStrings
    override val tutorialDialog: TutorialDialogStrings = EnglishTutorialDialogStrings

    override val stats: StatsStrings = EnglishStatsStrings
    override val search: SearchStrings = EnglishSearchStrings
    override val alternativeDialog: AlternativeDialogStrings = EnglishAlternativeDialogStrings
    override val addWordToDeckDialog: AddWordToDeckDialogStrings = EnglishAddWordToDeckDialogStrings

    override val settings: SettingsStrings = EnglishSettingsStrings
    override val reminderDialog: ReminderDialogStrings = EnglishReminderDialogStrings
    override val about: AboutStrings = EnglishAboutStrings
    override val backup: BackupStrings = EnglishBackupStrings
    override val feedback: FeedbackStrings = EnglishFeedbackStrings
    override val sponsor: SponsorStrings = EnglishSponsorStrings

    override val account: AccountScreenStrings = EnglishAccountScreenStrings
    override val sync: SyncScreenStrings = EnglishSyncScreenStrings
    override val syncDialog: SyncDialogStrings = EnglishSyncDialogStrings
    override val syncSnackbar: SyncSnackbarStrings = EnglishSyncSnackbarStrings

    override val deckPicker: DeckPickerStrings = EnglishDeckPickerStrings
    override val deckEdit: DeckEditStrings = EnglishDeckEditStrings
    override val deckDetails: DeckDetailsStrings = EnglishDeckDetailsStrings
    override val commonPractice: CommonPracticeStrings = EnglishCommonPracticeStrings
    override val letterPractice: LetterPracticeStrings = EnglishLetterPracticeStrings
    override val vocabPractice: VocabPracticeStrings = EnglishVocabPracticeStrings
    override val kanjiInfo: KanjiInfoStrings = EnglishKanjiInfoStrings

    override val urlPickerMessage: String = "Open With"
    override val urlPickerErrorMessage: String = "Web browser not found"

    override val reminderNotification: ReminderNotificationStrings =
        EnglishReminderNotificationStrings

}

object EnglishHomeStrings : HomeStrings {
    override val screenTitle: String = "Kanji Dojo"
    override val generalDashboardTabLabel: String = "Home"
    override val lettersDashboardTabLabel: String = "Letters"
    override val vocabDashboardTabLabel: String = "Vocab"
    override val statsTabLabel: String = "Stats"
    override val searchTabLabel: String = "Search"
    override val settingsTabLabel: String = "Settings"
}

object EnglishGeneralDashboardStrings : GeneralDashboardStrings {
    override val headerButtonDailyLimit: String = "Daily Limit"
    override val headerButtonVersionChange: String = "What's new"
    override val headerButtonTutorial: String = "Tutorial"
    override val headerButtonDownloads: String = "Downloads"
    override val headerButtonDiscord: String = "Discord"
    override val headerButtonYoutube: String = "YouTube"
    override val letterDecksTitle: String = "Letter Decks"
    override val vocabDecksTitle: String = "Vocab Decks"
    override val buttonNoDecksTitle: String = "No decks"
    override val buttonNoDecksMessage: String = "Create"
    override val practiceTypeLabel: String = "Practice Type"
    override val buttonNew: String = "New"
    override val buttonDue: String = "Due"
    override val buttonAll: String = "New & Due"
    override val streakTitle: String = "Daily Streak"
    override val currentStreakLabel: String = "Current Streak"
    override val longestStreakLabel: String = "Longest Streak"
}

object EnglishCommonDashboardStrings : CommonDashboardStrings {

    override val emptyScreenMessage: (inlineIconId: String) -> AnnotatedString = { inlineIconId ->
        buildAnnotatedString {
            append("Create deck by clicking on ")
            appendInlineContent(inlineIconId)
            append(" button. Decks are used to track your progress")
        }
    }

    override val mergeButton: String = "Merge"
    override val mergeCancelButton: String = "Cancel"
    override val mergeAcceptButton: String = "Merge"
    override val mergeTitle: String = "Merge multiple decks into one"
    override val mergeTitleHint: String = "Enter title here"
    override val mergeSelectedCount: (Int) -> String = { "$it selected" }
    override val mergeClearSelectionButton: String = "Clear"

    override val mergeDialogTitle: String = "Merge Confirmation"
    override val mergeDialogMessage: (String, List<String>) -> String = { newTitle, mergedTitles ->
        "Following ${mergedTitles.size} decks will be merged into the new \"$newTitle\" deck: ${mergedTitles.joinToString()}"
    }
    override val mergeDialogCancelButton: String = "Cancel"
    override val mergeDialogAcceptButton: String = "Merge"

    override val sortButton: String = "Sort"
    override val sortCancelButton: String = "Cancel"
    override val sortAcceptButton: String = "Apply"
    override val sortTitle: String = "Change decks order"
    override val sortByTimeTitle: String = "Sort by last review time"

    override val itemTimeMessage: (Duration?) -> String = {
        "Last review: " + when {
            it == null -> "Never"
            it.inWholeDays == 1L -> "1 day ago"
            it.inWholeDays > 0 -> "${it.inWholeDays} days ago"
            else -> "< 1 day ago"
        }
    }
    override val itemTotal: String = "Total"
    override val itemDone: String = "Done"
    override val itemReview: String = "Due"
    override val itemNew: String = "New"
    override val dailyPracticeTitle: String = "Daily practice"
    override val dailyPracticeNew: (Int) -> String = { "New ($it)" }
    override val dailyPracticeDue: (Int) -> String = { "Due ($it)" }
    override val itemGraphProgressTitle: String = "Completion"

    override val selectedPracticeTypeTemplate: (practiceType: String) -> String = {
        "Practice Type: $it"
    }
}

object EnglishDailyLimitStrings : DailyLimitStrings {
    override val enableSwitchTitle: String = "Daily Limit"
    override val enableSwitchDescription: String =
        "Limit the number of daily reviews prompted by the app"
    override val lettersSectionTitle: String = "Letters"
    override val vocabSectionTitle: String = "Vocab"
    override val combinedLimitSwitchTitle: String = "Combined Limit"
    override val combinedLimitSwitchDescription: String = "Share limit across all practice types"
    override val newLabel: String = "New"
    override val dueLabel: String = "Due"
    override val noteMessage: String =
        "Note: Writing and reading reviews are counted separately towards the limit"
    override val button: String = "Save"
    override val changesSavedMessage: String = "Done"
}

object EnglishTutorialDialogStrings : TutorialDialogStrings {
    override val title: String = "Tutorial"

    override val page1: String = """
        • The app uses the Spaced Repetition System (SRS) - a highly effective learning method that optimizes the timing of reviews based on how well you remember information
        • When you do a review the SRS schedules a follow-up review according to your recall ability
        • If you recall an item easily, the interval between reviews increases. If you struggle, the interval is shortened
    """.trimIndent()

    override val page2Top: String = """
        • To estimate your recall ability the app will offer you various rating options after review
    """.trimIndent()

    override val page2Bottom: String = """
        • Choose the option that best matches your ability to recall the item you're reviewing
        • Grading your own answers allows the app to adjust the learning pace according to individual memory capabilities
    """.trimIndent()

    override val page3Top: String = """
        • Every day, the status of each reviewed item is updated
        • The app will let you review several new items and due items that are past their scheduled review time
    """.trimIndent()

    override val page3Bottom: String = """
        • You can set a daily limit to control your workload at a comfortable level
    """.trimIndent()

    override val page4Top: String = """
        • To start using the app create a deck
        • Decks are used to organize the items you want to master. There are letter and vocab decks in the app
    """.trimIndent()

    override val page4Bottom: String = """
        • You can create your own decks from scratch or select from several pre-made ones
    """.trimIndent()

    override val page5: String = """
        • Once any deck is created you can start doing reviews
        • There are several practice modes available, check them all
        • Stay consistent - regular practice is key to making steady progress. Don't hesitate to lower your daily limit to avoid burnout
        • Good luck on your way to mastering Japanese! \(^_^)/
    """.trimIndent()
}

private val months = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
    "Sep", "Oct", "Nov", "Dec"
)

private fun formatDuration(duration: Duration): String = when {
    duration.inWholeHours > 0 -> "${duration.inWholeHours}h ${duration.inWholeMinutes % 60}m"
    duration.inWholeMinutes > 0 -> "${duration.inWholeMinutes}m ${duration.inWholeSeconds % 60}s"
    else -> "${duration.inWholeSeconds}s"
}

object EnglishStatsStrings : StatsStrings {
    override val todayTitle: String = "Today"
    override val monthTitle: String = "This month"
    override val monthLabel: (day: LocalDate) -> String = {
        it.run { "${months[monthNumber - 1]}, $year" }
    }
    override val yearTitle: String = "This year"
    override val yearDaysPracticedLabel = { practicedDays: Int, daysInYear: Int ->
        "Days practiced: $practicedDays/$daysInYear"
    }
    override val totalTitle: String = "Total"
    override val timeSpentTitle: String = "Time spent"
    override val reviewsCountTitle: String = "Reviews"
    override val formattedDuration: (Duration) -> String = { formatDuration(it) }
    override val uniqueLettersReviewed: String = "Unique letters reviewed"
    override val uniqueWordsReviewed: String = "Unique words reviewed"
}

object EnglishSearchStrings : SearchStrings {
    override val inputHint: String = "Search for character or words"
    override val charactersTitle: (count: Int) -> String = { "Characters ($it)" }
    override val wordsTitle: (count: Int) -> String = { "Expressions ($it)" }
    override val radicalsSheetTitle: String = "Search by radicals"
    override val radicalsFoundCharacters: String = "Found characters"
    override val radicalsEmptyFoundCharacters: String = "Nothing found"
    override val radicalSheetRadicalsSectionTitle: String = "Radicals"
}

object EnglishAlternativeDialogStrings : AlternativeDialogStrings {
    override val title: String = "Alternative expressions"
    override val readingsTitle: String = "Readings"
    override val meaningsTitle: String = "Meanings"
    override val reportButton: String = "Report"
    override val closeButton: String = "Close"
}

object EnglishAddWordToDeckDialogStrings : AddWordToDeckDialogStrings {
    override val title: (reading: String) -> String = { "Add $it to vocab deck" }
    override val createDeckButton: String = " + Create New Deck"
    override val createDeckTitleHint: String = "Enter deck title here..."
    override val savingStateMessage: String = "Adding"
    override val completedStateMessage: String = "Added"
    override val buttonCancel: String = "Cancel"
    override val buttonAdd: String = "Add"
}

object EnglishSettingsStrings : SettingsStrings {
    override val analyticsTitle: String = "Analytics"
    override val analyticsMessage: String = "Allow sending anonymous app usage data"
    override val themeTitle: String = "Theme"
    override val themeSystem: String = "System"
    override val themeLight: String = "Light"
    override val themeDark: String = "Dark"
    override val reminderTitle: String = "Reminder Notification"
    override val reminderEnabled: String = "Enabled"
    override val reminderDisabled: String = "Disabled"
    override val defaultTab: String = "Default Tab"
    override val feedbackTitle: String = "Feedback"
    override val account: String = "Account"
    override val sync: String = "Sync (Preview)"
    override val backupTitle: String = "Backup & Restore"
    override val aboutTitle: String = "About"
    override val pickerDialogCancel: String = "Cancel"
    override val pickerDialogApply: String = "Apply"
}

object EnglishReminderDialogStrings : ReminderDialogStrings {
    override val title: String = "Reminder Notification"
    override val noPermissionLabel: String = "Missing notification permission"
    override val noPermissionButton: String = "Grant"
    override val enabledLabel: String = "Enabled"
    override val timeLabel: String = "Time"
    override val cancelButton: String = "Close"
    override val applyButton: String = "Apply"
}

object EnglishAboutStrings : AboutStrings {
    override val title: String = "About"
    override val version: (versionName: String) -> String = { "Version: $it" }
    override val versionChangesTitle: String = "Version Changes"
    override val versionChangesDescription: String = "App changes history"
    override val versionChangesButton: String = "Close"
    override val githubTitle: String = "GitHub"
    override val githubDescription: String = "Source code, bug reports, discussions"
    override val creditsTitle: String = "Credits"
    override val creditsDescription: String = "Used libraries and data sources"
}

object EnglishBackupStrings : BackupStrings {
    override val title: String = "Backup & Restore"
    override val backupButton: String = "Create backup"
    override val restoreButton: String = "Restore from backup"
    override val unknownError: String = "Unknown error"
    override val restoreVersionMessage: (Long, Long) -> String = { backupVersion, currentVersion ->
        "Database version: $backupVersion (Current: $currentVersion)"
    }
    override val restoreTimeMessage: (Instant) -> String = { "Create time: $it" }
    override val restoreNote: String =
        "Note! All current progress will be replaced with the progress from the selected backup"
    override val restoreApplyButton: String = "Restore"
    override val completeMessage: String = "Done"
}

object EnglishFeedbackStrings : FeedbackStrings {
    override val title: String = "Feedback"
    override val topicTitle: String = "Topic"
    override val topicGeneral: String = "General"
    override val topicExpression: (id: Long, screen: FeedbackScreen) -> String = { id, screen ->
        val screenName: String = when (screen) {
            FeedbackScreen.WritingPractice -> "Writing practice"
            FeedbackScreen.ReadingPractice -> "Reading practice"
            FeedbackScreen.Search -> "Search"
            FeedbackScreen.CharacterInfo -> "Character info"
            FeedbackScreen.VocabPractice -> "Vocab practice"
        }
        "$screenName, expression $id"
    }
    override val messageLabel: String = "Enter feedback here"
    override val button: String = "Send"
    override val successMessage: String = "Feedback sent"
    override val errorMessage: (String?) -> String = { "Error: $it" }
}

object EnglishSponsorStrings : SponsorStrings {
    override val message: String = """
        Development of Kanji Dojo started in 2021 by a single person and it remains free for all users who want to learn Japanese
        
        If you find the app useful please consider supporting this project financially, every contribution counts
        
        Financial support will allow me to focus more on development, bring extra features, add more voiced content and translations
    """.trimIndent()

}

object EnglishDeckPickerStrings : DeckPickerStrings {

    override val title: String = "Select Deck"

    override val customDeckButton: String = "Create Empty"
    override val kanaTitle: String = "Kana"

    override val kanaDescription = { urlColor: Color ->
        buildAnnotatedString {
            append(
                "Japanese kana characters are a set of syllabic characters used in the Japanese writing system. There are two main types of kana: \n" +
                        " • Hiragana - used for native Japanese words and grammatical elements\n" +
                        " • Katakana - often used for loanwords, names, and technical terms\n" +
                        "Kana characters represent sound units, making them an essential part of reading and writing in the Japanese language. "
            )
            withClickableUrl(
                url = "https://en.wikipedia.org/wiki/Kana",
                color = urlColor
            ) {
                append("More info.")
            }
        }
    }
    override val hiragana: String = "Hiragana"
    override val katakana: String = "Katakana"

    override val jltpTitle: String = "JLPT"
    override val jlptDescription: StringResolveScope<AnnotatedString> = {
        buildAnnotatedString {
            append("The Japanese-Language Proficiency Test, or JLPT, is a standardized criterion-referenced test to evaluate and certify Japanese language proficiency for non-native speakers, covering language knowledge, reading ability, and listening ability. ")
            withClickableUrl(
                url = "https://en.wikipedia.org/wiki/Japanese-Language_Proficiency_Test",
                color = MaterialTheme.extraColorScheme.link
            ) {
                append("More info.")
            }
        }
    }
    override val jlptItem: (level: Int) -> String = { "JLPT・N$it" }

    override val gradeTitle: String = "Grade"
    override val gradeDescription = { urlColor: Color ->
        buildAnnotatedString {
            withClickableUrl("https://en.wikipedia.org/wiki/J%C5%8Dy%C5%8D_kanji", urlColor) {
                append("The Jōyō kanji")
            }
            append(" is a list of 2,136 frequently used characters maintained officially by the Japanese Ministry of Education. ")
            append("All these characters are taught in Japanese schools:\n")
            append(" • 1,026 kanji taught in primary school (Grade 1-6) (the ")
            withClickableUrl("https://en.wikipedia.org/wiki/Ky%C5%8Diku_kanji", urlColor) {
                append("kyōiku kanji")
            }
            append(")\n")
            append(" • 1,110 additional kanji taught in secondary school (Grade 7-12)")
        }
    }
    override val gradeItemNumbered: (Int) -> String = { "Grade $it" }
    override val gradeItemSecondary: String = "Secondary school"
    override val gradeItemNames: String = "Kanji for use in names (Jinmeiyō)"
    override val gradeItemNamesVariants: String = "Jinmeiyō kanji variants of Jōyō"

    override val wanikaniTitle: String = "WaniKani"
    override val wanikaniDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("Kanji lists according to levels on website WaniKani by Tofugu. ")
            withClickableUrl("https://www.wanikani.com/kanji?difficulty=pleasant", urlColor) {
                append("More info. ")
            }
        }
    }
    override val wanikaniItem: (Int) -> String = { "WaniKani Level $it" }

    override val vocabOtherTitle: String = "Other"
    override val vocabOtherDescription: AnnotatedString = "Some basic decks to get started"
        .let { AnnotatedString(it) }

    override val vocabDeckItemWordsCountLabel: (words: Int) -> String = { "$it words" }

    override val vocabDeckTitleTime: String = "Time"
    override val vocabDeckTitleWeek: String = "Week Days"
    override val vocabDeckTitleCommonVerbs: String = "Common Verbs"
    override val vocabDeckTitleColors: String = "Colors"
    override val vocabDeckTitleRegularFood: String = "Regular Food"
    override val vocabDeckTitleJapaneseFood: String = "Japanese Food"
    override val vocabDeckTitleGrammarTerms: String = "Grammar Terms"
    override val vocabDeckTitleAnimals: String = "Animals"
    override val vocabDeckTitleBody: String = "Body"
    override val vocabDeckTitleCommonPlaces: String = "Common Places"
    override val vocabDeckTitleCities: String = "Cities"
    override val vocabDeckTitleTransport: String = "Transport"

}

object EnglishDeckEditStrings : DeckEditStrings {
    override val createTitle: String = "Create Deck"
    override val ediTitle: String = "Edit Deck"
    override val searchHint: String = "Enter kana or kanji"
    override val editingModeSearchTitle: String = "Search"
    override val editingModeRemovalTitle: String = "Removal"
    override val editingModeDetailsTitle: String = "Details"
    override val vocabDetailsMessage: (inlineIconId: String) -> AnnotatedString = {
        buildAnnotatedString {
            append("To add a word from dictionary use ")
            appendInlineContent(it)
            append(" icon on search screen, during writing reviews and other places in the app")
        }
    }
    override val completeMessage: String = "Done"
    override val saveTitle: String = "Save changes"
    override val saveInputHint: String = "Deck Title"
    override val saveButtonDefault: String = "Save"
    override val saveButtonCompleted: String = "Done"
    override val deleteTitle: String = "Delete confirmation"
    override val deleteMessage: (deckTitle: String) -> String = {
        "Are you sure you want to delete \"$it\" deck?"
    }
    override val deleteButtonDefault: String = "Delete"
    override val deleteButtonCompleted: String = "Done"

    override val unknownTitle: String = "Unknown characters"
    override val unknownMessage: (characters: List<String>) -> String = {
        "Some characters where not found: ${it.joinToString()}"
    }
    override val unknownButton: String = "Close"

    override val leaveConfirmationTitle: String = "Leave confirmation"
    override val leaveConfirmationMessage: String = "All changes will be lost"
    override val leaveConfirmationCancel: String = "Cancel"
    override val leaveConfirmationAccept: String = "Leave"
}

object EnglishDeckDetailsStrings : DeckDetailsStrings {
    override val emptyListMessage: String = "Nothing here"
    override val detailsGroupTitle: (index: Int) -> String = { "Group $it" }
    override val firstTimeReviewMessage: (LocalDateTime?) -> String = {
        "First review time: " + when (it) {
            null -> "Never"
            else -> groupDetailsDateTimeFormatter(it)
        }
    }
    override val lastTimeReviewMessage: (LocalDateTime?) -> String = {
        "Last review time: " + when (it) {
            null -> "Never"
            else -> groupDetailsDateTimeFormatter(it)
        }
    }
    override val groupDetailsButton: String = "Start"

    override val expectedReviewDate: (LocalDate?) -> String =
        { "Expected Review: ${it ?: "-"}" }
    override val lastReviewDate: (LocalDateTime?) -> String = {
        "Last Review: ${it?.date ?: "-"}"
    }
    override val repetitions: (Int) -> String = { "Repetitions: $it" }
    override val lapses: (Int) -> String = { "Lapses: $it" }

    override val dialogCommon: LetterDeckDetailDialogCommonStrings =
        EnglishLetterDeckDetailDialogCommonStrings
    override val filterDialog: FilterDialogStrings = EnglishFilterDialogStrings
    override val sortDialog: SortDialogStrings = EnglishSortDialogStrings
    override val layoutDialog: PracticePreviewLayoutDialogStrings =
        EnglishPracticePreviewLayoutDialogStrings

    override val multiselectTitle: (selectedCount: Int) -> String = { "$it Selected" }
    override val multiselectDataNotLoaded: String = "Loading, wait a moment…"
    override val multiselectNoSelected: String = "Select at least one group"

    override val filterAllLabel: String = "All"
    override val filterNoneLabel: String = "None"
    override val kanaGroupsModeActivatedLabel: String = "Kana Groups Mode"
    override val shareLetterDeckClipboardMessage: String =
        "Letters from deck were copied to the clipboard"

}

object EnglishLetterDeckDetailDialogCommonStrings : LetterDeckDetailDialogCommonStrings {
    override val buttonCancel: String = "Cancel"
    override val buttonApply: String = "Apply"
}

object EnglishFilterDialogStrings : FilterDialogStrings {
    override val title: String = "Filter"
}

object EnglishSortDialogStrings : SortDialogStrings {
    override val title: String = "Sort"
    override val sortOptionAddOrder: String = "Add order"
    override val sortOptionAddOrderHint: String = "↑ New items last\n↓ New items first"
    override val sortOptionFrequency: String = "Frequency"
    override val sortOptionFrequencyHint: String =
        "Occurrence frequency of a character in newspapers\n↑ Frequent first\n↓ Frequent last"
    override val sortOptionName: String = "Name"
    override val sortOptionNameHint: String = "↑ Smaller first\n↓ Smaller last"
    override val sortOptionReviewTime: String = "Expected Review"
    override val sortOptionReviewTimeHint: String =
        "↑ Never reviewed first\n↓ Furthest scheduled first"
}

object EnglishPracticePreviewLayoutDialogStrings : PracticePreviewLayoutDialogStrings {
    override val title: String = "Layout"
    override val singleCharacterOptionLabel: String = "Single Character"
    override val groupsOptionLabel: String = "Groups"
    override val kanaGroupsTitle: String = "Kana Groups"
    override val kanaGroupsSubtitle: String =
        "Make group sizes according to kana table if practice contains all kana characters"
}

object EnglishCommonPracticeStrings : CommonPracticeStrings {
    override val configurationTitle: String = "Configuration"
    override val configurationSelectedItemsLabel: String = "Selected:"
    override val configurationCharactersPreview: String = "Characters preview"
    override val shuffleConfigurationTitle: String = "Shuffle"
    override val shuffleConfigurationMessage: String = "Randomizes review order"
    override val configurationCompleteButton: String = "Start"

    override val additionalKanaReadingsNote: (List<String>) -> String = {
        "Note: can also be written as ${it.joinToString()}"
    }

    override val formattedSrsInterval: (Duration) -> String = { formattedSrsDuration(it) }
    override val flashcardRevealButton: String = "Show Answer"
    override val againButton: String = "Again"
    override val hardButton: String = "Hard"
    override val goodButton: String = "Good"
    override val easyButton: String = "Easy"
    override val summaryItemsCountTitle: String = "Reviewed Items"
    override val summaryNextReviewLabel: String = "Next review:"

    override val summaryTimeSpentLabel: String = "Time spent"
    override val summaryTimeSpentValue: (Duration) -> String = { formatDuration(it) }
    override val summaryAccuracyLabel: String = "Accuracy"
    override val summaryButton: String = "Finish"

    override val earlyFinishDialogTitle: String = "Finish practice?"
    override val earlyFinishDialogMessage: String =
        "Navigate to the summary, your current progress is already saved"
    override val earlyFinishDialogCancelButton: String = "Cancel"
    override val earlyFinishDialogAcceptButton: String = "Finish"
}

object EnglishLetterPracticeStrings : LetterPracticeStrings {
    override val configurationTitle: (practiceType: String) -> String = { "Letter Practice・$it" }
    override val hintStrokesTitle: String = "Hint Strokes"
    override val hintStrokesMessage: String = "Controls when to show hint strokes for characters"
    override val hintStrokeNewOnlyMode: String = "New only"
    override val hintStrokeAllMode: String = "For all"
    override val hintStrokeNoneMode: String = "Never"
    override val inputModeTitle: String = "Input Mode"
    override val inputModeMessage: String =
        "Choose whether to validate each stroke or the entire character"
    override val inputModeStroke: String = "Stroke"
    override val inputModeCharacter: String = "Character"
    override val kanaRomajiTitle: String = "Show romaji in kana practice"
    override val kanaRomajiMessage: String =
        "When reviewing kana show romaji expressions instead of kana"
    override val noTranslationLayoutTitle: String = "No translation layout"
    override val noTranslationLayoutMessage: String =
        "Hides character translations during writing practice"
    override val leftHandedModeTitle: String = "Left-handed mode"
    override val leftHandedModeMessage: String =
        "Adjusts position of input in landscape mode of writing practice screen"

    override val headerWordsMessage: (count: Int) -> String = {
        "Expressions " + if (it > LetterPracticeScreenContract.WordsLimit) "(100+)" else "($it)"
    }
    override val studyFinishedButton: String = "Review"
    override val noKanjiTranslationsLabel: String = "[No translations]"

    override val altStrokeEvaluatorTitle: String = "Strict Stroke Evaluator"
    override val altStrokeEvaluatorMessage: String = "Alternative algorithm for stroke evaluation"

    override val variantsTitle: String = "Variants: "
    override val variantsHint: String = "Click to reveal"
    override val unicodeTitle: (String) -> String = { "Unicode: $it" }
    override val strokeCountTitle: (count: Int) -> String = { "Stroke count: $it" }
}

object EnglishVocabPracticeStrings : VocabPracticeStrings {
    override val configurationTitle: (practiceType: String) -> String = {
        "Vocab Practice・$it"
    }
    override val readingPriorityConfigurationTitle: String = "Reading Priority"
    override val readingPriorityConfigurationMessage: String =
        "Choose which reading to use if the word has multiple readings"
    override val readingPriorityConfigurationDefault: String = "Default"
    override val readingPriorityConfigurationKanji: String = "Kanji"
    override val readingPriorityConfigurationKana: String = "Kana"
    override val readingMeaningConfigurationTitle: String = "Always Show Meanings"
    override val readingMeaningConfigurationMessage: String =
        "Choose meaning visibility when answer is not selected"
    override val translationInFrontConfigurationTitle: String = "Translation In Front"
    override val translationInFrontConfigurationMessage: String =
        "Show translation instead of word when flashcard is hidden"
    override val detailsButton: String = "Details"
}

fun formattedSrsDuration(
    duration: Duration,
    dayLabel: String = "d",
    hourLabel: String = "h",
    minuteLabel: String = "m",
    secondLabel: String = "s",
    separator: String = " "
): String = when {
    duration.inWholeDays > 0 -> buildString {
        append("${duration.inWholeDays}$dayLabel")
        appendIfNot0(duration.inWholeHours % 24) { "$separator${it}$hourLabel" }
    }

    duration.inWholeHours > 0 -> buildString {
        append("${duration.inWholeHours}$hourLabel")
        appendIfNot0(duration.inWholeMinutes % 60) { "$separator${it}$minuteLabel" }
    }

    duration.inWholeMinutes > 0 -> buildString {
        append("${duration.inWholeMinutes}$minuteLabel")
        appendIfNot0(duration.inWholeSeconds % 60) { "$separator${it}$secondLabel" }
    }

    else -> "${duration.inWholeSeconds}$secondLabel"
}

private fun StringBuilder.appendIfNot0(number: Long, text: (Long) -> String) {
    if (number != 0L) append(text(number))
}

object EnglishKanjiInfoStrings : KanjiInfoStrings {
    override val strokesMessage: (count: Int) -> AnnotatedString = {
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(it.toString()) }
            if (it == 1) append(" stroke")
            else append(" strokes")
        }
    }
    override val clipboardCopyMessage: String = "Copied"
    override val radicalsSectionTitle: (count: Int) -> String = { "Radicals ($it)" }
    override val noRadicalsMessage: String = "No radicals"
    override val wordsSectionTitle: (count: Int) -> String = { "Expressions ($it)" }
    override val romajiMessage: (romaji: List<String>) -> String = {
        "Romaji readings: ${it.joinToString()}"
    }
    override val gradeMessage: (grade: Int) -> String = {
        when {
            it <= 6 -> "Jōyō kanji, taught in $it grade"
            it == 8 -> "Jōyō kanji, taught in junior high"
            it >= 9 -> "Jinmeiyō kanji, used in names"
            else -> throw IllegalStateException("Unknown grade $it")
        }
    }
    override val jlptMessage: (level: Int) -> String = { "JLPT level $it" }
    override val frequencyMessage: (frequency: Int) -> String = {
        "$it of 2500 most used kanji in newspapers"
    }
    override val noDataMessage: String = "No data"

}

object EnglishReminderNotificationStrings : ReminderNotificationStrings {
    override val channelName: String = "Reminder Notifications"
    override val title: String = "It's review time!"
    override val noDetailsMessage: String = "Continue to learn Japanese now"
    override val newOnlyMessage: (Int) -> String = {
        "$it new cards to study today"
    }
    override val dueOnlyMessage: (Int) -> String = {
        "$it due cards to review today"
    }
    override val message: (Int, Int) -> String = { new, due ->
        "$new new cards and $due due cards to review today"
    }
}


object EnglishAccountScreenStrings : AccountScreenStrings {
    override val title = "Account"
    override val loggedOutMessage = "Logged out"
    override val signInButton = "Sign in"
    override val signOutButton = "Sign out"
    override val emailTitle = "E-mail"
    override val subscriptionTitle = "Subscription"
    override val subscriptionStatusActive = "Active"
    override val subscriptionStatusExpired = "Expired"
    override val subscriptionStatusInactive = "Inactive"
    override val subscriptionValidUntilTemplate = "Valid until %s"
    override val issueNoConnectionTitle = "No Connection"
    override val issueNoConnectionMessage = "Showing cached data"
    override val issueSessionExpiredTitle = "Session Expired"
    override val issueSessionExpiredMessage = "Click to sign in again"
    override val issueSubscriptionOutdatedTitle = "Subscription status outdated"
    override val issueSubscriptionOutdatedMessage = "Click to refresh"
    override val issueOtherTitle = "Error"
    override val issueOtherMessageFallback = "Unknown error"
}

object EnglishSyncScreenStrings : SyncScreenStrings {
    override val title = "Sync (Preview)"
    override val guideTitle: String = "Sync Your Progress Across Devices"
    override val guideMessage =
        "Automatically upload your data to the cloud, keep it as a backup and stay in sync across all your devices"
    override val guideStepAccountTitle = "Create account and sign in"
    override val guideStepAccountMessage: String = "Go to account"
    override val guideStepSubscriptionTitle =
        "Purchase subscription (free during preview until further notice)"
    override val guideStepSubscriptionMessage: String =
        "Follow announcements on Discord for updates"
    override val accountErrorMessage: String = "There's an error with your account"
    override val syncButton = "Sync now"
    override val statusTitle = "Status"
    override val statusMessageLoading = "Checking server for updates..."
    override val statusMessageDataDiffer = "Local and remote data differs"
    override val statusMessageLocalNewer = "Can upload updated data"
    override val statusMessageUpToDate = "Up to date with the server"
    override val statusMessageError = "Error"
    override val statusMessageUploading = "Uploading"
    override val statusMessageDownloading = "Downloading"
    override val statusMessageCanceled = "Canceled, click on sync button to restart"
    override val localDataTitle = "Local Data"
    override val localDataIdTemplate = "ID: %s"
    override val localDataTimestampTemplate = "Timestamp: %s"

    override val errorNoConnectionTitle = "No Connection"
    override val errorNoConnectionMessage = "Couldn't access the server"
    override val errorSessionExpiredTitle = "Session Expired"
    override val errorSessionExpiredMessage = "Click to sign in again"
    override val errorNoSubscriptionTitle = "Subscription status outdated"
    override val errorNoSubscriptionMessage = "Update your subscription status on account screen"
    override val errorOtherTitle = "Error"
    override val errorOtherMessageFallback = "Unknown error"
}

object EnglishSyncDialogStrings : SyncDialogStrings {
    override val title = "Sync"
    override val buttonCancel = "Cancel"
    override val buttonUpload = "Upload"
    override val buttonDownload = "Download"
    override val buttonAccount = "Account"
    override val uploadingMessage = "Uploading..."
    override val downloadingMessage = "Downloading..."
    override val conflictRemoteNewerTitle = "New Data Found"
    override val conflictRemoteNewerMessage = "Data on the server is newer than your local copy"
    override val conflictIncompatibleTitle = "Data Conflict"
    override val conflictIncompatibleMessage =
        "Both remote and local data were changed since the last sync, result can't be merged"
    override val errorNoNetworkTitle = "No Network"
    override val errorNoNetworkMessage = "Couldn't establish network connection"
    override val errorNoSubscriptionTitle = "Subscription Expired"
    override val errorNoSubscriptionMessage =
        "Your subscription has expired, sync will be disabled, refresh your subscription status on the Account screen"
    override val errorNotAuthenticatedTitle = "Session Expired"
    override val errorNotAuthenticatedMessage = "Sign in to your account again to continue"
    override val errorUnexpectedErrorTitle = "Unexpected Error"
    override val errorUnexpectedErrorMessage = "Unknown issue"
    override val errorUnsupportedDataTitle = "Data on the server is unsupported"
    override val errorUnsupportedDataMessage =
        "The data on the server was created using the newer version of the application and is not compatible with the currently installed version. Update the app to retrieve your data or upload your local data to the server"
}

object EnglishSyncSnackbarStrings : SyncSnackbarStrings {
    override val errorNoConnection = "No Connection"
    override val errorNoSubscription = "Subscription expired"
    override val errorNotAuthenticated = "Sign in data expired"
    override val errorDataNotSupported = "Remote data unsupported"
    override val errorMessageTemplate = "Sync Error: %s"
    override val errorMessageNoReason = "Sync Error"
    override val actionButton = "Details"
}