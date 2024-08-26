package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.use_case

import androidx.compose.ui.text.buildAnnotatedString
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.WordClassification
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerCategory
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerLetterCategories
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.VocabDeckPickerDeck

interface GetDeckPickerCategoriesUseCase {
    suspend operator fun invoke(configuration: DeckPickerScreenConfiguration): List<DeckPickerCategory>
}

class DefaultGetDeckPickerCategoriesUseCase(
    private val appDataRepository: AppDataRepository
) : GetDeckPickerCategoriesUseCase {

    override suspend fun invoke(
        configuration: DeckPickerScreenConfiguration
    ): List<DeckPickerCategory> {
        return when (configuration) {
            DeckPickerScreenConfiguration.Letters -> getLetterCategories()
            DeckPickerScreenConfiguration.Vocab -> getVocabCategories()
        }
    }

    private fun getLetterCategories(): List<DeckPickerCategory> {
        return DeckPickerLetterCategories
    }

    private suspend fun getVocabCategories(): List<DeckPickerCategory> {
        return listOf(
            DeckPickerCategory(
                title = { "JLPT *Experimental*" },
                description = {
                    buildAnnotatedString {
                        append(
                            """
                            The JLPT (Japanese Language Proficiency Test) vocabulary decks are organized by levels, ranging from N5 to N1, with N5 being the most basic and N1 the most advanced.
                            * Experimental: Decks might be incomplete and contain irrelevant words with similar reading
                            """.trimIndent()
                        )
                    }
                },
                items = WordClassification.JLPT.all.map { jlpt ->
                    VocabDeckPickerDeck(
                        title = { deckPicker.jlptItem(jlpt.level) },
                        classification = jlpt,
                        wordsCount = appDataRepository.getWordsWithClassification(jlpt.dbValue).size
                    )
                }
            ),
            DeckPickerCategory(
                title = { "Other" },
                description = { buildAnnotatedString { append("Some basic decks to get started") } },
                items = WordClassification.Other.all.zip(vocabOtherTitles).map { (clazz, title) ->
                    VocabDeckPickerDeck(
                        title = title,
                        classification = clazz,
                        wordsCount = appDataRepository.getWordsWithClassification(clazz.dbValue).size
                    )
                }
            )
        )
    }

    companion object {
        private val vocabOtherTitles = listOf<StringResolveScope<String>>(
            { vocabDashboard.deckTitleTime },
            { vocabDashboard.deckTitleWeek },
            { vocabDashboard.deckTitleCommonVerbs },
            { vocabDashboard.deckTitleColors },
            { vocabDashboard.deckTitleRegularFood },
            { vocabDashboard.deckTitleJapaneseFood },
            { vocabDashboard.deckTitleGrammarTerms },
            { vocabDashboard.deckTitleAnimals },
            { vocabDashboard.deckTitleBody },
            { vocabDashboard.deckTitleCommonPlaces },
            { vocabDashboard.deckTitleCities },
            { vocabDashboard.deckTitleTransport }
        )

    }

}