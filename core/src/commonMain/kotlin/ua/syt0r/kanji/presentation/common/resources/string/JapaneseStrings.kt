package ua.syt0r.kanji.presentation.common.resources.string

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ua.syt0r.kanji.presentation.common.withClickableUrl
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract
import kotlin.time.Duration

object JapaneseStrings : Strings {

    override val appName: String = "漢字・道場"

    override val hiragana: String = "ひらがな"
    override val katakana: String = "カタカナ"

    override val kunyomi: String = "訓読み"
    override val onyomi: String = "音読み"

    override val home: HomeStrings = JapaneseHomeStrings
    override val practiceDashboard = JapanesePracticeDashboardStrings
    override val createPracticeDialog = JapaneseCreatePracticeDialogStrings
    override val dailyGoalDialog = JapaneseDailyGoalDialogStrings
    override val stats: StatsStrings = JapaneseStatsStrings
    override val search: SearchStrings = JapaneseSearchStrings
    override val alternativeDialog: AlternativeDialogStrings = JapaneseAlternativeDialogStrings
    override val settings: SettingsStrings = JapaneseSettingsStrings
    override val reminderDialog: ReminderDialogStrings = JapaneseReminderDialogStrings
    override val about: AboutStrings = JapaneseAboutStrings
    override val practiceImport: PracticeImportStrings = JapanesePracticeImportStrings
    override val practiceCreate: PracticeCreateStrings = JapanesePracticeCreateStrings
    override val practicePreview: PracticePreviewStrings = JapanesePracticePreviewStrings
    override val commonPractice: CommonPracticeStrings = JapaneseCommonPracticeStrings
    override val writingPractice: WritingPracticeStrings = JapaneseWritingPracticeStrings
    override val readingPractice: ReadingPracticeStrings = JapaneseReadingPracticeString
    override val kanjiInfo: KanjiInfoStrings = JapaneseKanjiInfoStrings

    override val urlPickerMessage: String = "開く"
    override val urlPickerErrorMessage: String = "ブラウザーが見つかりません"

    override val reminderNotification: ReminderNotificationStrings =
        JapaneseReminderNotificationStrings

}

object JapaneseHomeStrings : HomeStrings {
    override val screenTitle: String = JapaneseStrings.appName
    override val dashboardTabLabel: String = "練習"
    override val statsTabLabel: String = "統計"
    override val searchTabLabel: String = "検索"
    override val settingsTabLabel: String = "設定"
}

object JapanesePracticeDashboardStrings : PracticeDashboardStrings {
    override val emptyScreenMessage = { color: Color ->
        buildAnnotatedString {
            append("アプリを使うためには練習が必要。\n")
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append("+") }
            append("ボタンを押して、練習を作ってください。")
        }
    }
    override val itemTimeMessage: (Duration?) -> String = {
        "最終練習日: " + when {
            it == null -> "なし"
            it.inWholeDays > 0 -> "${it.inWholeDays}日前"
            else -> "一日以内"
        }
    }

    override val itemWritingTitle: String = "書き方"
    override val itemReadingTitle: String = "読み方"
    override val itemTotal: String = "全体"
    override val itemDone: String = "完全"
    override val itemReview: String = "復習"
    override val itemNew: String = "新しい"
    override val itemQuickPracticeTitle: String = "クイック練習"
    override val itemQuickPracticeLearn: (Int) -> String = { "新しいを勉強 ($it)" }
    override val itemQuickPracticeReview: (Int) -> String = { "復習 ($it)" }
    override val itemGraphProgressTitle: String = "完了"

    override val dailyIndicatorPrefix: String = "毎日の目標: "
    override val dailyIndicatorCompleted: String = "完了"
    override val dailyIndicatorDisabled: String = "無効"
    override val dailyIndicatorNew: (Int) -> String = { "$it 勉強" }
    override val dailyIndicatorReview: (Int) -> String = { "$it 復習" }
}

object JapaneseCreatePracticeDialogStrings : CreatePracticeDialogStrings {
    override val title: String = "練習を作る"
    override val selectMessage: String = "選ぶ (かな, きょういくかんじ, その他)"
    override val createMessage: String = "空から作る"
}

object JapaneseDailyGoalDialogStrings : DailyGoalDialogStrings {
    override val title: String = "毎日の目標"
    override val message: String = "クイック練習と通知の表す時を影響する"
    override val enabledLabel: String = "有効"
    override val studyLabel: String = "初学"
    override val reviewLabel: String = "復習"
    override val noteMessage: String = "注意: 文字の書き方と読み方は区別に数う"
    override val applyButton: String = "適用"
    override val cancelButton: String = "キャンセル"
}

private fun formatDuration(duration: Duration): String = when {
    duration.inWholeHours > 0 -> "${duration.inWholeHours}時 ${duration.inWholeMinutes % 60}分"
    duration.inWholeMinutes > 0 -> "${duration.inWholeMinutes}分 ${duration.inWholeSeconds % 60}秒"
    else -> "${duration.inWholeSeconds}秒"
}

object JapaneseStatsStrings : StatsStrings {
    override val monthCalendarTitle: (day: LocalDate) -> String =
        { "${it.monthNumber}月、${it.year}年" }
    override val todayTitle: String = "今日"
    override val yearTitle: String = "今年"
    override val yearDaysPracticedLabel = { practicedDays: Int, daysInYear: Int ->
        "練習日数: $practicedDays/$daysInYear"
    }
    override val totalTitle: String = "合計"
    override val timeSpentTitle: String = "掛かった時間"
    override val reviewsCountTitle: String = "練習の数"
    override val formattedDuration: (Duration) -> String = { formatDuration(it) }
}


object JapaneseSearchStrings : SearchStrings {
    override val inputHint: String = "文字や単語を入力"
    override val charactersTitle: (count: Int) -> String = { "文字 ($it)" }
    override val wordsTitle: (count: Int) -> String = { "単語 ($it)" }
    override val radicalsSheetTitle: String = "部首で検索"
    override val radicalsFoundCharacters: String = "見つかった文字"
    override val radicalsEmptyFoundCharacters: String = "何も見つかりません"
    override val radicalSheetRadicalsSectionTitle: String = "部首"
}

object JapaneseAlternativeDialogStrings : AlternativeDialogStrings {
    override val title: String = "別の単語"
    override val readingsTitle: String = "読み方"
    override val meaningsTitle: String = "意味"
    override val button: String = "キャンセル"
}

object JapaneseSettingsStrings : SettingsStrings {
    override val analyticsTitle: String = "分析レポート"
    override val analyticsMessage: String = "アプリを向上させるために匿名データの送信を許可する"
    override val themeTitle: String = "テーマ"
    override val themeSystem: String = "システム"
    override val themeLight: String = "ライト"
    override val themeDark: String = "ダーク"
    override val reminderTitle: String = "リマインダー通知"
    override val reminderEnabled: String = "有効"
    override val reminderDisabled: String = "無効"
    override val aboutTitle: String = "アプリについて"
}

object JapaneseReminderDialogStrings : ReminderDialogStrings {
    override val title: String = "リマインダー通知"
    override val noPermissionLabel: String = "通知の許可がない"
    override val noPermissionButton: String = "許可を付与する"
    override val enabledLabel: String = "通知"
    override val timeLabel: String = "時間"
    override val cancelButton: String = "キャンセル"
    override val applyButton: String = "適用"
}

object JapaneseAboutStrings : AboutStrings by EnglishAboutStrings {
    override val title: String = "アプリについて"
    override val version: (versionName: String) -> String = { "バージョン: $it" }
}

object JapanesePracticeImportStrings : PracticeImportStrings {

    override val title: String = "選ぶ"

    override val kanaTitle: String = "かな"

    override val kanaDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("いちばんかんたんな日本語の文字。かなには二つ部分があります:\n")
            append("•　平仮名（ひらがな）ー　日本語のたんごやおとを伝えに使う\n")
            append("•　片仮名（かたかな）ー　外国語のたんごを書くときに使う")
        }
    }
    override val hiragana: String = JapaneseStrings.hiragana
    override val katakana: String = JapaneseStrings.katakana

    override val jltpTitle: String = "日本語能力試験"
    override val jlptDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("本語能力試験(JLPT)は、外国人のため日本語を試す試験。N5からN1まで難しさが上がる。")
        }
    }
    override val jlptItem: (level: Int) -> String = { "JLPT・N$it" }

    override val gradeTitle: String = "常用漢字"
    override val gradeDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("常用漢字は2,136よく使う漢字。その中には:\n")
            append("•　最初の1,026漢字が学校の６年生まで学ぶ。\n")
            append("•　他の1,110漢字が高校生がまなぶ。")
        }
    }
    override val gradeItemNumbered: (Int) -> String = { "${it}年生" }
    override val gradeItemSecondary: String = "高校生"
    override val gradeItemNames: String = "人名用漢字"
    override val gradeItemNamesVariants: String = "人名用漢字２"

    override val wanikaniTitle: String = EnglishPracticeImportStrings.wanikaniTitle
    override val wanikaniDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("Wanikaniと言うWebサイトのレベル別漢字一覧。")
            withClickableUrl("https://www.wanikani.com/kanji?difficulty=pleasant", urlColor) {
                append("もっと見る. ")
            }
        }
    }
    override val wanikaniItem: (Int) -> String = { "Wanikaniのレベル$it" }

}

object JapanesePracticeCreateStrings : PracticeCreateStrings {
    override val newTitle: String = "練習を作成する"
    override val ediTitle: String = "練習を変える"
    override val searchHint: String = "かなと漢字をここに書いて"
    override val infoAction: String = "情報"
    override val returnAction: String = "戻る"
    override val removeAction: String = "消す"
    override val saveTitle: String = "練習を保存する"
    override val saveInputHint: String = "練習の名は"
    override val saveButtonDefault: String = "保存"
    override val saveButtonCompleted: String = "終わり"
    override val deleteTitle: String = "削除の確認"
    override val deleteMessage: (practiceTitle: String) -> String = {
        "「$it」とうい練習を削除してもよろしいですか?"
    }
    override val deleteButtonDefault: String = "削除"
    override val deleteButtonCompleted: String = "終わり"
    override val unknownTitle: String = "不明文字"
    override val unknownMessage: (characters: List<String>) -> String = {
        "データがない漢字: ${it.joinToString()}"
    }
    override val unknownButton: String = "OK"
}

object JapanesePracticePreviewStrings : PracticePreviewStrings {
    override val emptyListMessage: String = "何もない"
    override val detailsGroupTitle: (index: Int) -> String = { "グループ $it" }
    override val reviewStateRecently: String = "最近復習した"
    override val reviewStateNeedReview: String = "復習おすすめ"
    override val reviewStateNever: String = "新しい"
    override val firstTimeReviewMessage: (LocalDateTime?) -> String = {
        "初めて勉強した時間: " + when (it) {
            null -> "一度もない"
            else -> groupDetailsDateTimeFormatter(it)
        }
    }
    override val lastTimeReviewMessage: (LocalDateTime?) -> String = {
        "最後に勉強した時間: " + when (it) {
            null -> "一度もない"
            else -> groupDetailsDateTimeFormatter(it)
        }
    }
    override val groupDetailsButton: String = "練習へ"
    override val expectedReviewDate: (LocalDateTime?) -> String = {
        "予定の復習日:  ${it?.date ?: "-"}"
    }
    override val lastReviewDate: (LocalDateTime?) -> String = {
        "最後の復習日:  ${it?.date ?: "-"}"
    }
    override val repetitions: (Int) -> String = { "繰り返し回数: $it" }
    override val lapses: (Int) -> String = { "エラーの数: $it" }

    override val dialogCommon: PracticePreviewDialogCommonStrings =
        JapanesePracticePreviewDialogCommonStrings
    override val practiceTypeDialog: PracticeTypeDialogStrings = JapanesePracticeTypeDialogStrings
    override val filterDialog: FilterDialogStrings = JapaneseFilterDialogStrings
    override val sortDialog: SortDialogStrings = JapaneseSortDialogStrings
    override val layoutDialog: PracticePreviewLayoutDialogStrings =
        JapanesePracticePreviewLayoutDialogStrings

    override val multiselectTitle: (selectedCount: Int) -> String = { "$it 件選択済み" }
    override val multiselectDataNotLoaded: String = "しばらくお待ちください…"
    override val multiselectNoSelected: String = "少なくとも１件を選んでください"
    override val kanaGroupsModeActivatedLabel: String = "仮名グループモード"
}

object JapanesePracticePreviewDialogCommonStrings : PracticePreviewDialogCommonStrings {
    override val buttonCancel: String = "キャンセル"
    override val buttonApply: String = "適用"
}

object JapanesePracticeTypeDialogStrings : PracticeTypeDialogStrings {
    override val title: String = "練習の対象"
    override val practiceTypeWriting: String = "書き方"
    override val practiceTypeReading: String = "読み方"
}

object JapaneseFilterDialogStrings : FilterDialogStrings {
    override val title: String = "見える文字"
    override val filterAll: String = "全て"
    override val filterReviewOnly: String = "復習必要だけ"
    override val filterNewOnly: String = "新しいだけ"
}

object JapaneseSortDialogStrings : SortDialogStrings {
    override val title: String = "順序"
    override val sortOptionAddOrder: String = "文字を足す順"
    override val sortOptionAddOrderHint: String = "↑ 新しい文字は最後\n↓ 新しい文字は最初"
    override val sortOptionFrequency: String = "頻度"
    override val sortOptionFrequencyHint: String =
        "新聞にある頻度\n↑ 頻度が高い文字は最初\n↓ 頻度が高い文字は最後"
    override val sortOptionName: String = "ABC順"
    override val sortOptionNameHint: String = "↑ 小さい文字は最初\n↓ 小さい文字は最後"
}

object JapanesePracticePreviewLayoutDialogStrings : PracticePreviewLayoutDialogStrings {
    override val title: String = "レイアウト"
    override val singleCharacterOptionLabel: String = "単一文字"
    override val groupsOptionLabel: String = "グループ"
    override val kanaGroupsTitle: String = "仮名グループ"
    override val kanaGroupsSubtitle: String =
        "すべての仮名文字が含まれている場合、仮名表に従ってグループのサイズを設定します"
}

object JapaneseCommonPracticeStrings : CommonPracticeStrings {
    override val leaveDialogTitle: String = "練習をやめますか？"
    override val leaveDialogMessage: String = "今の進行状況が失われます"
    override val leaveDialogButton: String = "やめます"

    override val configurationTitle: String = "練習の設定"
    override val configurationCharactersCount: (Int, Int) -> String = { selected, total ->
        "練習の文字($selected/$total)"
    }
    override val configurationCharactersPreview: String = "文字のプレビュー"
    override val shuffleConfigurationTitle: String = "順序を替える"
    override val shuffleConfigurationMessage: String = "文字の復習順をランダムにする"
    override val configurationCompleteButton: String = "はじめ"

    override val savingTitle: String = "練習の保存"
    override val savingPreselectTitle: String = "明日に練習したい文字を選択"
    override val savingPreselectCount: (Int) -> String = {
        "${it}つ以上間違いがある文字は自動で選択され"
    }
    override val savingMistakesMessage: (count: Int) -> String = { "${it}つ間違い" }
    override val savingButton: String = "保存"

    override val savedTitle: String = "まとめ"
    override val savedReviewedCountLabel: String = "練習した文字の数"
    override val savedTimeSpentLabel: String = "時間"
    override val savedAccuracyLabel: String = "正確さ"
    override val savedRepeatCharactersLabel: String = "忘れている文字"
    override val savedRetainedCharactersLabel: String = "覚えている文字"
    override val savedButton: String = "終わる"
}

object JapaneseWritingPracticeStrings : WritingPracticeStrings {
    override val hintStrokesTitle: String = "字画のヒント"
    override val hintStrokesMessage: String = "ヒントを表すかどうか調節する"
    override val hintStrokeNewOnlyMode: String = "新しい文字のみ"
    override val hintStrokeAllMode: String = "いつも"
    override val hintStrokeNoneMode: String = "無効"
    override val noTranslationLayoutTitle: String = "英語がない書く練習の配置"
    override val noTranslationLayoutMessage: String = "書く練習をする時に英語の翻訳を隠す"
    override val leftHandedModeTitle: String = "左手で書くモード"
    override val leftHandedModeMessage: String = "書く練習の画面には書く所を左に移す"

    override val headerWordsMessage: (count: Int) -> String = {
        "単語  " + if (it > WritingPracticeScreenContract.WordsLimit) "(100+)" else "($it)"
    }
    override val wordsBottomSheetTitle: String = "単語"
    override val studyFinishedButton: String = "復習"
    override val nextButton: String = "正解"
    override val repeatButton: String = "もう一度"
    override val altStrokeEvaluatorTitle: String = "代替点画認識を使う"
    override val altStrokeEvaluatorMessage: String = "オリジナルの点画認識の代わりに代替のアルゴリズムを使う"
}

object JapaneseReadingPracticeString : ReadingPracticeStrings {
    override val words: String = "単語"
    override val showAnswerButton: String = "解答を表示"
    override val goodButton: String = "正解"
    override val repeatButton: String = "もう一度"
}

object JapaneseKanjiInfoStrings : KanjiInfoStrings {
    override val strokesMessage: (count: Int) -> AnnotatedString = {
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("${it}画") }
        }
    }
    override val clipboardCopyMessage: String = "コピーをできた"
    override val radicalsSectionTitle: (count: Int) -> String = { "部首 ($it)" }
    override val noRadicalsMessage: String = "部首なし"
    override val wordsSectionTitle: (count: Int) -> String = { "単語 ($it)" }
    override val romajiMessage: (romaji: String) -> String = { "ロマジ: $it" }
    override val gradeMessage: (grade: Int) -> String = {
        when {
            it <= 6 -> "常用漢字、${it}年生で学ぶ"
            it == 8 -> "常用漢字, 高校で学ぶ"
            it >= 9 -> "人名用漢字"
            else -> throw IllegalStateException("Unknown grade $it")
        }
    }
    override val jlptMessage: (level: Int) -> String = { "JLPTのレベル$it" }
    override val frequencyMessage: (frequency: Int) -> String = {
        "新聞の2500よく使う漢字の中に${it}番"
    }
    override val noDataMessage: String = "データなし"

}

object JapaneseReminderNotificationStrings : ReminderNotificationStrings {
    override val channelName: String = "リマインダー通知"
    override val title: String = "お勉強の時間!"
    override val noDetailsMessage: String = "日本語の学習を続ける"
    override val learnOnlyMessage: (Int) -> String = {
        "今日は勉強する文字が${it}個残っている"
    }
    override val reviewOnlyMessage: (Int) -> String = {
        "今日は復習する文字が${it}個残っている"
    }
    override val message: (Int, Int) -> String = { learn, review ->
        "今日は勉強する文字が${learn}個、復習する文字が${review}個残っている"
    }
}
