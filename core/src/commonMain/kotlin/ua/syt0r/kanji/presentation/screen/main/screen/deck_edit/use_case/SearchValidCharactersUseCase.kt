package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.core.japanese.isKanji

interface SearchValidCharactersUseCase {
    suspend operator fun invoke(input: String): SearchResult
    suspend operator fun invoke(character: List<String>): SearchResult
}

class SearchResult(
    val detectedCharacter: List<String>,
    val unknownCharacters: List<String>
)


class DefaultSearchValidCharactersUseCase(
    private val appDataRepository: AppDataRepository
) : SearchValidCharactersUseCase {

    override suspend fun invoke(input: String): SearchResult {
        return processInput(input = input.map { it })
    }

    override suspend fun invoke(character: List<String>): SearchResult {
        return processInput(input = character.map { it.first() })
    }

    private suspend fun processInput(input: Iterable<Char>): SearchResult {
        val parsedCharacters = input.filter { it.isKanji() || it.isKana() }

        val known = mutableListOf<String>()
        val unknown = mutableListOf<String>()

        parsedCharacters.forEach { character ->

            val strokes = appDataRepository.getStrokes(character.toString())

            val isKnown = strokes.isNotEmpty() && character.let {
                when {
                    it.isKana() -> true
                    it.isKanji() -> {
                        appDataRepository.getReadings(character.toString()).isNotEmpty()
                    }

                    else -> false
                }
            }

            if (isKnown) {
                known.add(character.toString())
            } else {
                unknown.add(character.toString())
            }

        }

        return SearchResult(
            detectedCharacter = known,
            unknownCharacters = unknown
        )
    }

}