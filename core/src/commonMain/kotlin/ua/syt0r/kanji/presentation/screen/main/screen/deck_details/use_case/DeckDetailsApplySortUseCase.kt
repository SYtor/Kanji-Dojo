package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.LettersSortOption

interface DeckDetailsApplySortUseCase {
    operator fun invoke(
        items: List<DeckDetailsItemData.LetterData>,
        sortOption: LettersSortOption,
        isDescending: Boolean,
    ): List<DeckDetailsItemData.LetterData>
}

class DefaultDeckDetailsApplySortUseCase : DeckDetailsApplySortUseCase {

    override fun invoke(
        items: List<DeckDetailsItemData.LetterData>,
        sortOption: LettersSortOption,
        isDescending: Boolean,
    ): List<DeckDetailsItemData.LetterData> {
        val comparator = when (sortOption) {
            LettersSortOption.ADD_ORDER -> {

                val reviewDateComparator: Comparator<DeckDetailsItemData.LetterData> = when (
                    isDescending
                ) {
                    true -> compareByDescending { it.positionInPractice }
                    false -> compareBy { it.positionInPractice }
                }

                reviewDateComparator

            }

            LettersSortOption.NAME -> {

                val nameComparator: Comparator<DeckDetailsItemData.LetterData> =
                    when (isDescending) {
                        true -> compareByDescending { it.character }
                        false -> compareBy { it.character }
                    }

                nameComparator

            }

            LettersSortOption.FREQUENCY -> {

                val frequencyComparator: Comparator<DeckDetailsItemData.LetterData> =
                    when (isDescending) {
                        true -> compareByDescending { it.frequency }
                        false -> compareBy { it.frequency }
                    }

                frequencyComparator.thenBy { it.character }

            }
        }

        return items.sortedWith(comparator)
    }

}
