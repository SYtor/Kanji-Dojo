package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.getHiraganaReading
import ua.syt0r.kanji.core.stroke_evaluator.DefaultKanjiStrokeEvaluator
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.common.ui.kanji.PreviewKanji
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DefaultCharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeCharacterReviewResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeProgress
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewCharacterDetails
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingScreenLayoutConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ui.WritingPracticeScreenUI
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@Composable
private fun WritingPracticeScreenPreview(
    state: ScreenState,
    darkTheme: Boolean = false
) {
    AppTheme(darkTheme) {
        WritingPracticeScreenUI(
            state = rememberUpdatedState(state),
            navigateBack = {},
            navigateToWordFeedback = {},
            onConfigured = {},
            onPracticeSaveClick = {},
            onPracticeCompleteButtonClick = {},
            onNextClick = {},
            toggleRadicalsHighlight = {},
            toggleAutoPlay = {},
            speakKana = {}
        )
    }
}

@Preview
@Composable
private fun KanjiPreview(
    darkTheme: Boolean = false,
    isStudyMode: Boolean = true
) {
    WritingPracticeScreenPreview(
        state = WritingPracticeScreenUIPreviewUtils.reviewState(
            isKana = false,
            isStudyMode = isStudyMode,
            wordsCount = 10
        ),
        darkTheme = darkTheme
    )
}

@Preview(showBackground = true, heightDp = 600, locale = "ja")
@Composable
private fun KanjiStudyPreview() {
    KanjiPreview(darkTheme = true, isStudyMode = true)
}

@Preview(showBackground = true, locale = "　ja")
@Composable
private fun KanaPreview(
    darkTheme: Boolean = false,
    isStudyMode: Boolean = false
) {
    WritingPracticeScreenPreview(
        state = WritingPracticeScreenUIPreviewUtils.reviewState(
            isKana = true,
            isStudyMode = isStudyMode
        ),
        darkTheme = darkTheme
    )
}

@Preview(showBackground = true)
@Composable
private fun KanaStudyPreview() {
    KanaPreview(darkTheme = true, isStudyMode = true)
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    WritingPracticeScreenPreview(
        state = ScreenState.Loading
    )
}

@Preview(showBackground = true)
@Composable
private fun SavingPreview() {
    WritingPracticeScreenPreview(
        state = ScreenState.Saving(
            reviewResultList = (0..20).map {
                PracticeCharacterReviewResult(
                    character = PreviewKanji.randomKanji(),
                    mistakes = Random.nextInt(0, 9)
                )
            },
            toleratedMistakesCount = 2,
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun SavedPreview() {
    WritingPracticeScreenPreview(
        state = ScreenState.Saved(
            practiceDuration = 63.seconds.plus(5.milliseconds),
            accuracy = 88.6666f,
            repeatCharacters = listOf("国", "年"),
            goodCharacters = "時行見月後前生五間上東四今".map { it.toString() }
        )
    )
}

@Preview(device = Devices.PIXEL_C)
@Composable
private fun TabletPreview() {
    KanjiPreview(darkTheme = true)
}

object WritingPracticeScreenUIPreviewUtils {

    @Composable
    fun reviewState(
        isKana: Boolean = true,
        isStudyMode: Boolean = false,
        wordsCount: Int = 3,
        progress: PracticeProgress = PracticeProgress(2, 2, 2, 0),
        drawnStrokesCount: Int = 2
    ): ScreenState.Review {
        val words = PreviewKanji.randomWords(wordsCount)
        return ScreenState.Review(
            layoutConfiguration = WritingScreenLayoutConfiguration(
                noTranslationsLayout = false,
                radicalsHighlight = rememberUpdatedState(true),
                kanaAutoPlay = rememberUpdatedState(true),
                leftHandedMode = false
            ),
            reviewState = MutableStateFlow(
                WritingReviewState(
                    practiceProgress = progress,
                    characterDetails = when {
                        isKana -> WritingReviewCharacterDetails.KanaReviewDetails(
                            character = "ぢ",
                            strokes = PreviewKanji.strokes,
                            words = words,
                            encodedWords = words,
                            kanaSystem = CharacterClassification.Kana.Hiragana,
                            reading = getHiraganaReading('ぢ')
                        )

                        else -> WritingReviewCharacterDetails.KanjiReviewDetails(
                            character = PreviewKanji.kanji,
                            strokes = PreviewKanji.strokes,
                            words = words,
                            encodedWords = words,
                            radicals = PreviewKanji.radicals,
                            kun = PreviewKanji.kun,
                            on = PreviewKanji.on,
                            meanings = PreviewKanji.meanings,
                            variants = null
                        )
                    },
                    writerState = DefaultCharacterWriterState(
                        coroutineScope = rememberCoroutineScope(),
                        strokeEvaluator = DefaultKanjiStrokeEvaluator(),
                        character = "",
                        strokes = PreviewKanji.strokes,
                        configuration = CharacterWriterConfiguration.StrokeInput(true)
                    )
                )
            )
        )
    }

}