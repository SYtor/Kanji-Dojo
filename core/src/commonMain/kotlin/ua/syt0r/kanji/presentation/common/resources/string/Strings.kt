package ua.syt0r.kanji.presentation.common.resources.string

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Duration


typealias StringResolveScope <T> = Strings.() -> T

@Composable
fun <T> resolveString(resolveScope: Strings.() -> T): T {
    return LocalStrings.current.resolveScope()
}

fun getStrings(): Strings {
    return when (Locale.current.language) {
        "ja" -> JapaneseStrings
        else -> EnglishStrings
    }
}

val LocalStrings = compositionLocalOf<Strings> { EnglishStrings }

interface Strings {

    val appName: String

    val hiragana: String
    val katakana: String

    val kunyomi: String
    val onyomi: String

    val loading: String

    val home: HomeStrings
    val practiceDashboard: PracticeDashboardStrings
    val createPracticeDialog: CreatePracticeDialogStrings
    val dailyGoalDialog: DailyGoalDialogStrings

    val stats: StatsStrings
    val search: SearchStrings
    val alternativeDialog: AlternativeDialogStrings

    val settings: SettingsStrings
    val reminderDialog: ReminderDialogStrings
    val about: AboutStrings
    val backup: BackupStrings

    val practiceImport: PracticeImportStrings
    val practiceCreate: PracticeCreateStrings
    val practicePreview: PracticePreviewStrings

    val commonPractice: CommonPracticeStrings
    val writingPractice: WritingPracticeStrings
    val readingPractice: ReadingPracticeStrings

    val kanjiInfo: KanjiInfoStrings

    val urlPickerMessage: String
    val urlPickerErrorMessage: String

    val reminderNotification: ReminderNotificationStrings

}

interface HomeStrings {
    val screenTitle: String

    val dashboardTabLabel: String
    val statsTabLabel: String
    val searchTabLabel: String
    val settingsTabLabel: String
}

interface PracticeDashboardStrings {

    val emptyScreenMessage: (Color) -> AnnotatedString

    val mergeButton: String
    val mergeCancelButton: String
    val mergeAcceptButton: String
    val mergeTitle: String
    val mergeTitleHint: String
    val mergeSelectedCount: (Int) -> String
    val mergeClearSelectionButton: String

    val mergeDialogTitle: String
    val mergeDialogMessage: (newTitle: String, mergedTitles: List<String>) -> String
    val mergeDialogCancelButton: String
    val mergeDialogAcceptButton: String

    val sortButton: String
    val sortCancelButton: String
    val sortAcceptButton: String
    val sortTitle: String
    val sortByTimeTitle: String

    val itemTimeMessage: (reviewToNowDuration: Duration?) -> String
    val itemWritingTitle: String
    val itemReadingTitle: String
    val itemTotal: String
    val itemDone: String
    val itemReview: String
    val itemNew: String
    val itemQuickPracticeTitle: String
    val itemQuickPracticeLearn: (Int) -> String
    val itemQuickPracticeReview: (Int) -> String
    val itemGraphProgressTitle: String
    val itemGraphProgressValue: (Float) -> String
        get() = { " ${it.roundToInt()}%" }

    val dailyIndicatorPrefix: String
    val dailyIndicatorCompleted: String
    val dailyIndicatorDisabled: String
    val dailyIndicatorNew: (Int) -> String
    val dailyIndicatorReview: (Int) -> String

}

interface CreatePracticeDialogStrings {
    val title: String
    val selectMessage: String
    val createMessage: String
}

interface DailyGoalDialogStrings {
    val title: String
    val message: String
    val enabledLabel: String
    val studyLabel: String
    val reviewLabel: String
    val noteMessage: String
    val applyButton: String
    val cancelButton: String
}

interface StatsStrings {
    val todayTitle: String
    val monthTitle: String
    val monthLabel: (day: LocalDate) -> String
    val yearTitle: String
    val yearDaysPracticedLabel: (practicedDays: Int, daysInYear: Int) -> String
    val totalTitle: String
    val timeSpentTitle: String
    val reviewsCountTitle: String
    val formattedDuration: (Duration) -> String
    val charactersStudiedTitle: String
}

interface SearchStrings {
    val inputHint: String
    val charactersTitle: (count: Int) -> String
    val wordsTitle: (count: Int) -> String
    val radicalsSheetTitle: String
    val radicalsFoundCharacters: String
    val radicalsEmptyFoundCharacters: String
    val radicalSheetRadicalsSectionTitle: String
}

interface AlternativeDialogStrings {
    val title: String
    val readingsTitle: String
    val meaningsTitle: String
    val button: String
}

interface SettingsStrings {
    val analyticsTitle: String
    val analyticsMessage: String

    val themeTitle: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String

    val reminderTitle: String
    val reminderEnabled: String
    val reminderDisabled: String

    val backupTitle: String
    val aboutTitle: String
}

interface ReminderDialogStrings {
    val title: String
    val noPermissionLabel: String
    val noPermissionButton: String
    val enabledLabel: String
    val timeLabel: String
    val cancelButton: String
    val applyButton: String
}

interface AboutStrings {
    val title: String
    val version: (versionName: String) -> String
    val githubTitle: String
    val githubDescription: String
    val versionChangesTitle: String
    val versionChangesButton: String
    val creditsTitle: String
    val licenseTemplate: (licenseText: String) -> String
    val licenseKanjiVgTitle: String
    val licenseKanjiVgDescription: String
    val licenseKanjiDicTitle: String
    val licenseKanjiDicDescription: String
    val licenseTanosTitle: String
    val licenseTanosDescription: String
    val licenseJmDictTitle: String
    val licenseJmDictDescription: String
    val licenseJmDictFuriganaTitle: String
    val licenseJmDictFuriganaDescription: String
    val licenseLeedsCorpusTitle: String
    val licenseLeedsCorpusDescription: String
    val licenseWanakanaKtTitle: String
        get() = "WanaKana Kt"
    val licenseWanakanaKtDescription: String
        get() = "Kotlin utility library for detecting and transliterating Hiragana, Katakana, and Romaji. Ported from WaniKani/WanaKana"
    val licenseCCASA3: String
    val licenseCCASA4: String
    val licenseCCBY: String
    val licenseMIT: String
        get() = "MIT license"
}


interface BackupStrings {
    val title: String
    val backupButton: String
    val restoreButton: String
    val unknownError: String
    val restoreVersionMessage: (backupVersion: Long, currentVersion: Long) -> String
    val restoreTimeMessage: (Instant) -> String
    val restoreNote: String
    val restoreApplyButton: String
    val completeMessage: String
}

interface PracticeImportStrings {

    val title: String

    val kanaTitle: String
    val kanaDescription: (urlColor: Color) -> AnnotatedString
    val hiragana: String
    val katakana: String

    val jltpTitle: String
    val jlptDescription: (urlColor: Color) -> AnnotatedString
    val jlptItem: (level: Int) -> String

    val gradeTitle: String
    val gradeDescription: (urlColor: Color) -> AnnotatedString

    fun getGradeItem(grade: Int): String {
        return when {
            grade <= 6 -> gradeItemNumbered(grade)
            grade == 8 -> gradeItemSecondary
            grade == 9 -> gradeItemNames
            grade == 10 -> gradeItemNamesVariants
            else -> throw IllegalStateException("Unexpected grade $grade")
        }
    }

    val gradeItemNumbered: (Int) -> String
    val gradeItemSecondary: String
    val gradeItemNames: String
    val gradeItemNamesVariants: String

    val wanikaniTitle: String
    val wanikaniDescription: (urlColor: Color) -> AnnotatedString
    val wanikaniItem: (Int) -> String

}

interface PracticeCreateStrings {
    val newTitle: String
    val ediTitle: String

    val searchHint: String

    val infoAction: String
    val returnAction: String
    val removeAction: String

    val saveTitle: String
    val saveInputHint: String
    val saveButtonDefault: String
    val saveButtonCompleted: String

    val deleteTitle: String
    val deleteMessage: (practiceTitle: String) -> String
    val deleteButtonDefault: String
    val deleteButtonCompleted: String

    val unknownTitle: String
    val unknownMessage: (characters: List<String>) -> String
    val unknownButton: String

    val leaveConfirmationTitle: String
    val leaveConfirmationMessage: String
    val leaveConfirmationCancel: String
    val leaveConfirmationAccept: String
}

interface PracticePreviewStrings {

    val emptyListMessage: String
    fun listGroupTitle(index: Int, characters: String): String = "$index. $characters"

    val detailsGroupTitle: (index: Int) -> String
    val reviewStateRecently: String
    val reviewStateNeedReview: String
    val reviewStateNever: String

    val firstTimeReviewMessage: (LocalDateTime?) -> String
    val lastTimeReviewMessage: (LocalDateTime?) -> String

    val groupDetailsDateTimeFormatter: (LocalDateTime) -> String
        get() = {
            it.run { "${dayOfMonth.withLeading0}/${monthNumber.withLeading0}/$year ${hour.withLeading0}:${minute.withLeading0}" }
        }
    val groupDetailsButton: String

    val expectedReviewDate: (LocalDateTime?) -> String
    val lastReviewDate: (LocalDateTime?) -> String
    val repetitions: (Int) -> String
    val lapses: (Int) -> String

    val multiselectTitle: (selectedCount: Int) -> String
    val multiselectDataNotLoaded: String
    val multiselectNoSelected: String

    val kanaGroupsModeActivatedLabel: String

    val dialogCommon: PracticePreviewDialogCommonStrings
    val practiceTypeDialog: PracticeTypeDialogStrings
    val filterDialog: FilterDialogStrings
    val sortDialog: SortDialogStrings
    val layoutDialog: PracticePreviewLayoutDialogStrings

}

interface PracticeTypeDialogStrings {
    val title: String
    val practiceTypeWriting: String
    val practiceTypeReading: String
}

interface FilterDialogStrings {
    val title: String
    val filterAll: String
    val filterReviewOnly: String
    val filterNewOnly: String
}

interface SortDialogStrings {
    val title: String

    val sortOptionAddOrder: String
    val sortOptionAddOrderHint: String
    val sortOptionFrequency: String
    val sortOptionFrequencyHint: String
    val sortOptionName: String
    val sortOptionNameHint: String
}

interface PracticePreviewLayoutDialogStrings {
    val title: String
    val singleCharacterOptionLabel: String
    val groupsOptionLabel: String
    val kanaGroupsTitle: String
    val kanaGroupsSubtitle: String
}

interface PracticePreviewDialogCommonStrings {
    val buttonCancel: String
    val buttonApply: String
}

interface CommonPracticeStrings {
    val configurationTitle: String
    val configurationCharactersCount: (selected: Int, total: Int) -> String
    val configurationCharactersPreview: String
    val shuffleConfigurationTitle: String
    val shuffleConfigurationMessage: String
    val configurationCompleteButton: String

    val additionalKanaReadingsNote: (List<String>) -> String

    val leaveDialogTitle: String
    val leaveDialogMessage: String
    val leaveDialogButton: String

    val savingTitle: String
    val savingPreselectTitle: String
    val savingPreselectCount: (Int) -> String
    val savingMistakesMessage: (count: Int) -> String
    val savingButton: String

    val savedTitle: String
    val savedReviewedCountLabel: String
    val savedTimeSpentLabel: String
    val savedTimeSpentValue: (Duration) -> String
    val savedAccuracyLabel: String
    val savedRepeatCharactersLabel: String
    val savedRetainedCharactersLabel: String
    val savedButton: String
}

interface WritingPracticeStrings {
    val hintStrokesTitle: String
    val hintStrokesMessage: String
    val hintStrokeNewOnlyMode: String
    val hintStrokeAllMode: String
    val hintStrokeNoneMode: String
    val kanaRomajiTitle: String
    val kanaRomajiMessage: String
    val noTranslationLayoutTitle: String
    val noTranslationLayoutMessage: String
    val leftHandedModeTitle: String
    val leftHandedModeMessage: String

    val headerWordsMessage: (count: Int) -> String
    val wordsBottomSheetTitle: String
    val studyFinishedButton: String
    val nextButton: String
    val repeatButton: String
    val noKanjiTranslationsLabel: String

    val altStrokeEvaluatorTitle: String
    val altStrokeEvaluatorMessage: String

    val variantsTitle: String
    val variantsHint: String
    val unicodeTitle: (hexRepresentation: String) -> String
    val strokeCountTitle: (Int) -> String
}

interface ReadingPracticeStrings {
    val kanaRomajiTitle: String
    val kanaRomajiMessage: String
    val words: String
    val showAnswerButton: String
    val goodButton: String
    val repeatButton: String
}

interface KanjiInfoStrings {
    val strokesMessage: (count: Int) -> AnnotatedString
    val clipboardCopyMessage: String
    val radicalsSectionTitle: (count: Int) -> String
    val noRadicalsMessage: String
    val wordsSectionTitle: (count: Int) -> String
    val romajiMessage: (romaji: List<String>) -> String
    val gradeMessage: (grade: Int) -> String
    val jlptMessage: (level: Int) -> String
    val frequencyMessage: (frequency: Int) -> String
    val noDataMessage: String
}

interface ReminderNotificationStrings {
    val channelName: String
    val title: String
    val noDetailsMessage: String
    val learnOnlyMessage: (Int) -> String
    val reviewOnlyMessage: (Int) -> String
    val message: (Int, Int) -> String
}

