package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.SortOption

interface LetterDeckDetailsApplySortUseCase {
    operator fun invoke(
        items: List<LetterDeckDetailsItemData>,
        sortOption: SortOption,
        isDescending: Boolean,
    ): List<LetterDeckDetailsItemData>
}

class DefaultLetterDeckDetailsApplySortUseCase : LetterDeckDetailsApplySortUseCase {

    override fun invoke(
        items: List<LetterDeckDetailsItemData>,
        sortOption: SortOption,
        isDescending: Boolean,
    ): List<LetterDeckDetailsItemData> {
        val comparator = when (sortOption) {
            SortOption.ADD_ORDER -> {

                val reviewDateComparator: Comparator<LetterDeckDetailsItemData> = when (
                    isDescending
                ) {
                    true -> compareByDescending { it.positionInPractice }
                    false -> compareBy { it.positionInPractice }
                }

                reviewDateComparator

            }

            SortOption.NAME -> {

                val nameComparator: Comparator<LetterDeckDetailsItemData> = when (isDescending) {
                    true -> compareByDescending { it.character }
                    false -> compareBy { it.character }
                }

                nameComparator

            }

            SortOption.FREQUENCY -> {

                val frequencyComparator: Comparator<LetterDeckDetailsItemData> =
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
