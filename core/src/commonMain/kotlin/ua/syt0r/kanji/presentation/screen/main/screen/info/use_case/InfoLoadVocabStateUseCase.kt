package ua.syt0r.kanji.presentation.screen.main.screen.info.use_case

import kotlinx.coroutines.CoroutineScope
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.paginateable
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.info.VocabInfoData

class InfoLoadVocabStateUseCase(
    private val appDataRepository: AppDataRepository
) : InfoScreenContract.LoadVocabStateUseCase {

    override suspend fun invoke(
        data: InfoScreenData.Vocab,
        coroutineScope: CoroutineScope
    ): ScreenState {

        val targetWord: JapaneseWord = when {
            data.id == null || data.kanaReading == null -> {
                appDataRepository
                    .findWords(null, data.kanjiReading, data.kanaReading)
                    .firstOrNull() ?: return ScreenState.NoData
            }

            else -> {
                appDataRepository.getWord(data.id, data.kanjiReading, data.kanaReading)
            }
        }

        val detailedWord = appDataRepository.getDetailedWord(targetWord.id)

        val reading = targetWord.reading
        val matchingSenses = detailedWord.senseList.filter { vocabSense ->
            vocabSense.readings.any {
                it.kanji == reading.kanjiReading && it.kana == reading.kanaReading
            }
        }

        val sentenceSearchReading = reading.run { kanjiReading ?: kanaReading }
        val sentencePaginateable = paginateable(
            coroutineScope = coroutineScope,
            limit = appDataRepository.getSentencesWithTextCount(sentenceSearchReading),
            load = { offset ->
                appDataRepository.getSentencesWithText(
                    text = sentenceSearchReading,
                    offset = offset,
                    limit = InfoScreenContract.VocabListPageItemsCount
                )
            }
        )

        val vocabInfoData = VocabInfoData(
            word = targetWord,
            matchingSenses = matchingSenses,
            detailedJapaneseWord = detailedWord,
            sentences = sentencePaginateable
        )

        return ScreenState.Loaded.Vocab(vocabInfoData)
    }

}