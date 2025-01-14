package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.dakutenHiraganaReadings
import ua.syt0r.kanji.core.japanese.hiraganaReadings
import ua.syt0r.kanji.core.japanese.hiraganaToKatakana
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration

interface LoadDeckEditLetterDataUseCase {

    suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration.LetterDeck
    ): DeckEditLetterData

}

data class DeckEditLetterData(
    val title: String?,
    val characters: List<String>
)

class DefaultLoadDeckEditLetterDataUseCase(
    private val letterPracticeRepository: LetterPracticeRepository,
    private val appDataRepository: AppDataRepository
) : LoadDeckEditLetterDataUseCase {

    override suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration.LetterDeck
    ): DeckEditLetterData = withContext(Dispatchers.IO) {
        when (configuration) {

            is DeckEditScreenConfiguration.LetterDeck.CreateNew -> {
                DeckEditLetterData(null, emptyList())
            }

            is DeckEditScreenConfiguration.LetterDeck.CreateDerived -> {
                val characters = when (val classification = configuration.classification) {
                    CharacterClassification.Kana.Hiragana -> {
                        (hiraganaReadings.toList() + dakutenHiraganaReadings.toList()).map {
                            it.first.toString()
                        }
                    }

                    CharacterClassification.Kana.Katakana -> {
                        (hiraganaReadings.toList() + dakutenHiraganaReadings.toList()).map {
                            hiraganaToKatakana(it.first).toString()
                        }
                    }

                    else -> {
                        classification as CharacterClassification.DBDefined
                        appDataRepository.getKanjiForClassification(
                            classification = classification.dbValue
                        )
                    }
                }

                DeckEditLetterData(
                    title = configuration.title,
                    characters = characters
                )
            }

            is DeckEditScreenConfiguration.LetterDeck.Edit -> {
                val deck = letterPracticeRepository.getDeck(configuration.letterDeckId)
                val characters =
                    letterPracticeRepository.getDeckCharacters(configuration.letterDeckId)
                DeckEditLetterData(
                    title = deck.name,
                    characters = characters
                )
            }

        }

    }

}