package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.use_case

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
                title = { deckPicker.vocabJlptTitle },
                description = { deckPicker.vocabJlptDescription },
                items = WordClassification.JLPT.all.map { jlpt ->
                    VocabDeckPickerDeck(
                        title = { deckPicker.jlptItem(jlpt.level) },
                        classification = jlpt,
                        wordsCount = appDataRepository.getWordsWithClassification(jlpt.dbValue).size
                    )
                }
            ),
            DeckPickerCategory(
                title = { deckPicker.vocabOtherTitle },
                description = { deckPicker.vocabOtherDescription },
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
            { deckPicker.vocabDeckTitleTime },
            { deckPicker.vocabDeckTitleWeek },
            { deckPicker.vocabDeckTitleCommonVerbs },
            { deckPicker.vocabDeckTitleColors },
            { deckPicker.vocabDeckTitleRegularFood },
            { deckPicker.vocabDeckTitleJapaneseFood },
            { deckPicker.vocabDeckTitleGrammarTerms },
            { deckPicker.vocabDeckTitleAnimals },
            { deckPicker.vocabDeckTitleBody },
            { deckPicker.vocabDeckTitleCommonPlaces },
            { deckPicker.vocabDeckTitleCities },
            { deckPicker.vocabDeckTitleTransport }
        )

    }

}