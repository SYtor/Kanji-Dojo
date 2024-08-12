package ua.syt0r.kanji.presentation.screen.main.screen.kanji_info

import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Path
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalsSectionData

interface KanjiInfoScreenContract {

    companion object {
        const val InitiallyLoadedWordsAmount: Int = 50
        const val LoadMoreWordsAmount: Int = 50
        const val StartLoadMoreWordsFromItemsToEnd: Int = 20
    }

    interface ViewModel {
        val state: State<ScreenState>
        fun loadCharacterInfo(character: String)
        fun loadMoreWords()
        fun reportCharacter(character: String)
    }

    sealed class ScreenState {

        object Loading : ScreenState()

        object NoData : ScreenState()

        sealed class Loaded : ScreenState() {

            abstract val character: String
            abstract val strokes: List<Path>
            abstract val words: State<PaginatableJapaneseWordList>

            data class Kana(
                override val character: String,
                override val strokes: List<Path>,
                override val words: State<PaginatableJapaneseWordList>,
                val kanaSystem: CharacterClassification.Kana,
                val reading: KanaReading,
            ) : Loaded()

            data class Kanji(
                override val character: String,
                override val strokes: List<Path>,
                override val words: State<PaginatableJapaneseWordList>,
                val on: List<String>,
                val kun: List<String>,
                val meanings: List<String>,
                val grade: Int?,
                val jlptLevel: Int?,
                val frequency: Int?,
                val radicalsSectionData: KanjiRadicalsSectionData,
                val displayRadicals: List<String>
            ) : Loaded()

        }

    }

    interface LoadDataUseCase {
        suspend fun load(character: String): ScreenState
    }

    interface LoadCharacterWordsUseCase {
        suspend fun load(character: String, offset: Int, limit: Int): List<JapaneseWord>
    }

}