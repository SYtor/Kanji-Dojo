package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.core.japanese.hiraganaToKatakana
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.CharacterReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeGroupSummary
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeType

interface CreatePracticeGroupsUseCase {

    operator fun invoke(
        items: List<LetterDeckDetailsItemData>,
        visibleItems: List<LetterDeckDetailsItemData>,
        type: PracticeType,
        probeKanaGroups: Boolean,
    ): PracticeGroupsCreationResult

}

data class PracticeGroupsCreationResult(
    val kanaGroups: Boolean,
    val groups: List<DeckDetailsListItem.Group>,
    val selectionStates: Map<DeckDetailsListItem, MutableState<Boolean>>,
)

class DefaultCreatePracticeGroupsUseCase : CreatePracticeGroupsUseCase {

    companion object {
        private const val CharactersInGroup = 6

        private val HiraganaBaseGroups = listOf(
            "あいうえお",
            "かきくけこ",
            "さしすせそ",
            "たちつてと",
            "なにぬねの",
            "はひふへほ",
            "まみむめも",
            "らりるれろ",
            "やゆよ",
            "わをん"
        )

        private val HiraganaFullGroups = HiraganaBaseGroups + listOf(
            "がぎぐげご",
            "ざじずぜぞ",
            "だぢづでど",
            "ばびぶべぼ",
            "ぱぴぷぺぽ"
        )

        private val KatakanaBaseGroups = HiraganaBaseGroups.groupsToKatakana()
        private val KatakanaFullGroups = HiraganaFullGroups.groupsToKatakana()

        private fun List<String>.groupsToKatakana(): List<String> = map { groupString ->
            groupString.toCharArray().joinToString("") { hiraganaToKatakana(it).toString() }
        }

    }


    override fun invoke(
        items: List<LetterDeckDetailsItemData>,
        visibleItems: List<LetterDeckDetailsItemData>,
        type: PracticeType,
        probeKanaGroups: Boolean,
    ): PracticeGroupsCreationResult {

        val itemsMap = items.associateBy { it.character }
        val characters = itemsMap.keys

        val (chunkedGroups: List<List<LetterDeckDetailsItemData>>, kanaGroupsFound: Boolean) = when {
            !probeKanaGroups -> visibleItems.chunked(CharactersInGroup) to false

            HiraganaBaseGroups.areMatchingSets(characters) -> {
                HiraganaBaseGroups.associateGroupsWithItems(itemsMap) to true
            }

            HiraganaFullGroups.areMatchingSets(characters) -> {
                HiraganaFullGroups.associateGroupsWithItems(itemsMap) to true
            }

            KatakanaBaseGroups.areMatchingSets(characters) -> {
                KatakanaBaseGroups.associateGroupsWithItems(itemsMap) to true
            }

            KatakanaFullGroups.areMatchingSets(characters) -> {
                KatakanaFullGroups.associateGroupsWithItems(itemsMap) to true
            }

            else -> visibleItems.chunked(CharactersInGroup) to false
        }

        val selectionStates = mutableMapOf<DeckDetailsListItem, MutableState<Boolean>>()
        val groups = chunkedGroups.mapIndexed { index, groupItems ->
            val selectionState = mutableStateOf(false)
            val group = createGroup(index, groupItems, type, selectionState)
            selectionStates[group] = selectionState
            group
        }

        return PracticeGroupsCreationResult(
            kanaGroups = kanaGroupsFound,
            groups = groups,
            selectionStates = selectionStates
        )
    }

    private fun List<String>.areMatchingSets(characters: Set<String>): Boolean {
        val set = flatMap { it.map { it.toString() } }.toSet()
        return set.size == characters.size && characters.intersect(set).size == set.size
    }

    private fun List<String>.associateGroupsWithItems(
        itemsMap: Map<String, LetterDeckDetailsItemData>,
    ): List<List<LetterDeckDetailsItemData>> = map { groupString ->
        groupString.map { char -> itemsMap.getValue(char.toString()) }
    }

    private fun createGroup(
        index: Int,
        groupItems: List<LetterDeckDetailsItemData>,
        practiceType: PracticeType,
        selectionState: State<Boolean>,
    ): DeckDetailsListItem.Group {

        val itemReviewStates = when (practiceType) {
            PracticeType.Writing -> groupItems.map { it.writingSummary.state }
            PracticeType.Reading -> groupItems.map { it.readingSummary.state }
        }

        val groupReviewState = when {
            itemReviewStates.all { it == CharacterReviewState.Done } -> CharacterReviewState.Done
            itemReviewStates.any { it == CharacterReviewState.Due } -> CharacterReviewState.Due
            else -> CharacterReviewState.New
        }

        val summary = when (practiceType) {
            PracticeType.Writing -> PracticeGroupSummary(
                firstReviewDate = groupItems
                    .mapNotNull { it.writingSummary.firstReviewDate }
                    .minOrNull(),
                lastReviewDate = groupItems
                    .mapNotNull { it.writingSummary.lastReviewDate }
                    .maxOrNull(),
                state = groupReviewState
            )

            PracticeType.Reading -> PracticeGroupSummary(
                firstReviewDate = groupItems
                    .mapNotNull { it.readingSummary.firstReviewDate }
                    .minOrNull(),
                lastReviewDate = groupItems
                    .mapNotNull { it.readingSummary.lastReviewDate }
                    .maxOrNull(),
                state = groupReviewState
            )
        }

        return DeckDetailsListItem.Group(
            index = index + 1,
            items = groupItems,
            summary = summary,
            reviewState = groupReviewState,
            selected = selectionState
        )
    }

}
