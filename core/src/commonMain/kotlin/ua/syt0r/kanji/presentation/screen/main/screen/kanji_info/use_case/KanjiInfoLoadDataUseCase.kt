package ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.use_case

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.CharacterClassifier
import ua.syt0r.kanji.core.japanese.getKanaInfo
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalDetails
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalsSectionData
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreenContract.ScreenState

class KanjiInfoLoadDataUseCase(
    private val appDataRepository: AppDataRepository,
    private val characterClassifier: CharacterClassifier,
    private val analyticsManager: AnalyticsManager
) : KanjiInfoScreenContract.LoadDataUseCase {

    companion object {
        private const val NoStrokesErrorMessage = "No strokes found"
    }

    override suspend fun load(character: String): ScreenState {
        return kotlin.runCatching {
            val char = character.first()
            when {
                char.isKana() -> getKana(character)
                else -> getKanji(character)
            }
        }.getOrElse {
            analyticsManager.sendEvent("kanji_info_loading_error") {
                put("message", it.message ?: "No message")
            }
            ScreenState.NoData
        }
    }

    private suspend fun getKana(character: String): ScreenState.Loaded.Kana {
        val kanaInfo = getKanaInfo(character.first())
        return ScreenState.Loaded.Kana(
            character = character,
            strokes = getStrokes(character),
            words = getWords(character),
            kanaSystem = kanaInfo.classification,
            reading = kanaInfo.reading
        )
    }

    private suspend fun getKanji(character: String): ScreenState.Loaded.Kanji {
        val kanjiData = appDataRepository.getData(character)

        val readings = appDataRepository.getReadings(character)
        val onReadings = readings.filter { it.value == ReadingType.ON }
            .map { it.key }
        val kunReadings = readings.filter { it.value == ReadingType.KUN }
            .map { it.key }


        val classifications = characterClassifier.get(character)
        val strokes = getStrokes(character)
        val radicals = getRadicals(character).sortedWith(
            compareBy<CharacterRadical> { it.startPosition }
                .thenByDescending { it.strokesCount }
        )

        return ScreenState.Loaded.Kanji(
            character = character,
            strokes = strokes,
            words = getWords(character),
            meanings = appDataRepository.getMeanings(character),
            on = onReadings,
            kun = kunReadings,
            grade = classifications.find { it is CharacterClassification.Grade }
                ?.let { it as CharacterClassification.Grade }
                ?.number,
            jlptLevel = classifications.find { it is CharacterClassification.JLPT }
                ?.let { it as CharacterClassification.JLPT }
                ?.level,
            frequency = kanjiData?.frequency,
            radicalsSectionData = KanjiRadicalsSectionData(
                strokes = strokes,
                radicals = radicals.map {
                    KanjiRadicalDetails(
                        value = it.radical,
                        strokeIndicies = it.startPosition until it.startPosition + it.strokesCount,
                        meanings = appDataRepository.getMeanings(it.radical)
                    )
                }
            ),
            displayRadicals = radicals.map { it.radical }.distinct()
        )
    }

    private suspend fun getStrokes(character: String) = parseKanjiStrokes(
        appDataRepository.getStrokes(character)
    ).also { require(it.isNotEmpty()) { NoStrokesErrorMessage } }

    private suspend fun getRadicals(character: String) = appDataRepository
        .getRadicalsInCharacter(character)
        .sortedBy { it.strokesCount }

    private suspend fun getWords(character: String): MutableState<PaginatableJapaneseWordList> {
        val totalWordsCount = appDataRepository.getWordsWithTextCount(character)
        val initialList = appDataRepository.getWordsWithText(
            text = character,
            limit = KanjiInfoScreenContract.InitiallyLoadedWordsAmount
        )
        return PaginatableJapaneseWordList(
            totalCount = totalWordsCount,
            items = initialList
        ).let { mutableStateOf(it) }
    }

}