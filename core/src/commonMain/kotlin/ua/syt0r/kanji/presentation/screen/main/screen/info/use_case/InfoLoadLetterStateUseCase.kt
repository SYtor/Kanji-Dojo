package ua.syt0r.kanji.presentation.screen.main.screen.info.use_case

import kotlinx.coroutines.CoroutineScope
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.Sentence
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.CharacterClassifier
import ua.syt0r.kanji.core.japanese.getKanaInfo
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.presentation.common.Paginateable
import ua.syt0r.kanji.presentation.common.paginateable
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalDetails
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalsSectionData
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.info.LetterInfoData

class InfoLoadLetterStateUseCase(
    private val appDataRepository: AppDataRepository,
    private val characterClassifier: CharacterClassifier,
    private val analyticsManager: AnalyticsManager
) : InfoScreenContract.LoadLetterStateUseCase {

    companion object {
        private const val NoStrokesErrorMessage = "No strokes found"
    }

    override suspend operator fun invoke(
        character: String,
        coroutineScope: CoroutineScope
    ): ScreenState {
        return kotlin.runCatching {
            val char = character.first()
            val data = when {
                char.isKana() -> getKana(character, coroutineScope)
                else -> getKanji(character, coroutineScope)
            }
            ScreenState.Loaded.Letter(data)
        }.getOrElse {
            analyticsManager.sendEvent("kanji_info_loading_error") {
                put("message", it.message ?: "No message")
            }
            ScreenState.NoData
        }
    }

    private suspend fun getKana(
        character: String,
        coroutineScope: CoroutineScope
    ): LetterInfoData.Kana {
        val kanaInfo = getKanaInfo(character.first())
        return LetterInfoData.Kana(
            character = character,
            strokes = getStrokes(character),
            kanaSystem = kanaInfo.classification,
            reading = kanaInfo.reading,
            vocab = getPaginateableVocab(character, coroutineScope),
            sentences = getPaginateableSentences(character, coroutineScope)
        )
    }

    private suspend fun getKanji(
        character: String,
        coroutineScope: CoroutineScope
    ): LetterInfoData.Kanji {
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

        return LetterInfoData.Kanji(
            character = character,
            strokes = strokes,
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
            displayRadicals = radicals.map { it.radical }.distinct(),
            vocab = getPaginateableVocab(character, coroutineScope),
            sentences = getPaginateableSentences(character, coroutineScope)
        )
    }

    private suspend fun getStrokes(character: String) = parseKanjiStrokes(
        appDataRepository.getStrokes(character)
    ).also { require(it.isNotEmpty()) { NoStrokesErrorMessage } }

    private suspend fun getRadicals(character: String) = appDataRepository
        .getRadicalsInCharacter(character)
        .sortedBy { it.strokesCount }

    private suspend fun getPaginateableVocab(
        letter: String,
        coroutineScope: CoroutineScope
    ) = paginateable(
        coroutineScope = coroutineScope,
        limit = appDataRepository.getWordsWithTextCount(letter),
        load = { offset ->
            appDataRepository.getWordsWithText(
                text = letter,
                offset = offset,
                limit = InfoScreenContract.VocabListPageItemsCount
            )
        }
    )

    private suspend fun getPaginateableSentences(
        character: String,
        coroutineScope: CoroutineScope
    ): Paginateable<Sentence> = paginateable(
        coroutineScope,
        limit = appDataRepository.getSentencesWithTextCount(character),
        load = { offset ->
            appDataRepository.getSentencesWithText(
                text = character,
                offset = offset,
                limit = InfoScreenContract.VocabListPageItemsCount
            )
        }
    )

}