package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.runUnit
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.DeleteDeckUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.LoadDeckEditLetterDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.LoadDeckEditVocabDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.SaveDeckUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.SearchResult
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.SearchValidCharactersUseCase

class DeckEditViewModel(
    private val viewModelScope: CoroutineScope,
    private val loadDeckEditLetterDataUseCase: LoadDeckEditLetterDataUseCase,
    private val loadDeckEditVocabDataUseCase: LoadDeckEditVocabDataUseCase,
    private val searchValidCharactersUseCase: SearchValidCharactersUseCase,
    private val saveDeckUseCase: SaveDeckUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
    private val analyticsManager: AnalyticsManager
) : DeckEditScreenContract.ViewModel {

    private lateinit var configuration: DeckEditScreenConfiguration

    private val itemActions = mutableMapOf<Any, MutableState<DeckEditItemAction>>()
    private val deckTitle = mutableStateOf("")
    private val wasDeckEdited = mutableStateOf(false)

    private lateinit var letterEditingState: MutableLetterDeckEditingState
    private lateinit var vocabDeckEditingState: MutableVocabDeckEditingState

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    override fun initialize(configuration: DeckEditScreenConfiguration) {
        if (this::configuration.isInitialized) return

        this.configuration = configuration
        viewModelScope.launch {

            val defaultListItemAction = when (configuration) {
                is DeckEditScreenConfiguration.EditExisting -> DeckEditItemAction.Nothing
                else -> DeckEditItemAction.Add
            }

            when (configuration) {
                is DeckEditScreenConfiguration.LetterDeck -> {
                    val letterData = loadDeckEditLetterDataUseCase(configuration)
                    val searchResult = searchValidCharactersUseCase(letterData.characters)

                    deckTitle.value = letterData.title ?: ""

                    letterEditingState = MutableLetterDeckEditingState(
                        title = deckTitle,
                        confirmExit = wasDeckEdited,
                        searching = mutableStateOf(false),
                        listState = mutableStateOf(emptyList()),
                        lastSearchResult = mutableStateOf(null),
                    )

                    addSearchLetters(
                        searchResult = searchResult,
                        defaultAction = defaultListItemAction
                    )

                    _state.value = letterEditingState

                    reportInvalidImportCharacter(searchResult.unknownCharacters)
                }

                is DeckEditScreenConfiguration.VocabDeck -> {
                    val vocabData = loadDeckEditVocabDataUseCase(configuration)
                    deckTitle.value = vocabData.title ?: ""

                    vocabDeckEditingState = MutableVocabDeckEditingState(
                        title = deckTitle,
                        confirmExit = wasDeckEdited,
                        list = vocabData.words.map {
                            val mutableAction = mutableStateOf(defaultListItemAction)
                            itemActions[it.id] = mutableAction
                            VocabDeckEditListItem(it, defaultListItemAction, mutableAction)
                        }
                    )
                    _state.value = vocabDeckEditingState
                }
            }

        }
    }

    override fun searchCharacters(input: String) = letterEditingState.runUnit {
        searching.value = true
        viewModelScope.launch {
            val searchResult = searchValidCharactersUseCase(input)
            addSearchLetters(searchResult, DeckEditItemAction.Add)
            lastSearchResult.value = searchResult
            wasDeckEdited.value = true
            searching.value = false
        }
    }

    override fun dismissSearchResult() {
        letterEditingState.lastSearchResult.value = null
    }

    private fun addSearchLetters(
        searchResult: SearchResult,
        defaultAction: DeckEditItemAction
    ) {
        val newListItems = searchResult.detectedCharacter
            .filter { !itemActions.containsKey(it) }
            .map {
                val mutableAction = mutableStateOf(defaultAction)
                itemActions[it] = mutableAction
                LetterDeckEditListItem(it, defaultAction, mutableAction)
            }
        letterEditingState.listState.value = letterEditingState.listState.value.plus(newListItems)
    }

    override fun toggleRemoval(item: DeckEditListItem) {
        val mutableAction = when (item) {
            is LetterDeckEditListItem -> itemActions.getValue(item.character)
            is VocabDeckEditListItem -> itemActions.getValue(item.word.id)
        }
        mutableAction.value = when (mutableAction.value) {
            DeckEditItemAction.Remove -> item.initialAction
            DeckEditItemAction.Add,
            DeckEditItemAction.Nothing -> DeckEditItemAction.Remove
        }
        wasDeckEdited.value = true
    }

    override fun saveDeck() {
        val loadedState = _state.value as ScreenState.Loaded
        _state.value = ScreenState.SavingChanges
        viewModelScope.launch {
            saveDeckUseCase(configuration, deckTitle.value, loadedState.getCurrentList())
            _state.value = ScreenState.Completed(false)
        }
    }

    override fun deleteDeck() {
        _state.value = ScreenState.Deleting
        viewModelScope.launch {
            deleteDeckUseCase(configuration)
            _state.value = ScreenState.Completed(true)
        }
    }

    private fun reportInvalidImportCharacter(characters: List<String>) {
        characters.forEach {
            analyticsManager.sendEvent("import_unknown_character") {
                put("character", it)
            }
        }
    }

}