package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import ua.syt0r.kanji.presentation.screen.main.screen.about.AboutScreen
import ua.syt0r.kanji.presentation.screen.main.screen.backup.BackupScreen
import ua.syt0r.kanji.presentation.screen.main.screen.credits.CreditsScreen
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreen
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreen
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic
import ua.syt0r.kanji.presentation.screen.main.screen.home.HomeScreen
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreen
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsScreen
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.LetterDeckPickerScreen
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreen
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeScreen
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreen
import kotlin.reflect.KClass

interface MainNavigationState {
    fun navigateBack()
    fun popUpToHome()
    fun navigate(destination: MainDestination)
}

@Composable
expect fun rememberMainNavigationState(): MainNavigationState

@Composable
expect fun MainNavigation(state: MainNavigationState)

interface MainDestination {

    @Composable
    fun Draw(state: MainNavigationState)


    @Serializable
    object Home : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            HomeScreen(
                mainNavigationState = rememberUpdatedState(state)
            )
        }

    }

    @Serializable
    object About : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            AboutScreen(
                mainNavigationState = state
            )
        }

    }

    @Serializable
    object Credits : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            CreditsScreen(state)
        }

    }

    @Serializable
    object LetterDeckPicker : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            LetterDeckPickerScreen(
                mainNavigationState = state
            )
        }

    }

    @Serializable
    data class DeckEdit(
        val configuration: DeckEditScreenConfiguration
    ) : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            DeckEditScreen(
                configuration = configuration,
                mainNavigationState = state
            )
        }

    }

    @Serializable
    data class LetterDeckDetails(
        val id: Long
    ) : MainDestination {
        @Composable
        override fun Draw(state: MainNavigationState) {
            LetterDeckDetailsScreen(
                deckId = id,
                mainNavigationState = state
            )
        }
    }

    @Serializable
    sealed interface Practice : MainDestination {

        @Serializable
        data class Writing(
            val practiceId: Long,
            val characterList: List<String>
        ) : Practice {

            @Composable
            override fun Draw(state: MainNavigationState) {
                WritingPracticeScreen(
                    mainNavigationState = state,
                    configuration = this
                )
            }

        }

        @Serializable
        data class Reading(
            val practiceId: Long,
            val characterList: List<String>
        ) : Practice {

            @Composable
            override fun Draw(state: MainNavigationState) {
                ReadingPracticeScreen(
                    navigationState = state,
                    configuration = this
                )
            }

        }

    }

    @Serializable
    data class VocabPractice(
        val expressionIds: List<Long>
    ) : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            VocabPracticeScreen(
                expressionsIds = expressionIds,
                mainNavigationState = state
            )
        }

    }

    @Serializable
    data class KanjiInfo(
        val character: String
    ) : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            KanjiInfoScreen(
                kanji = character,
                mainNavigationState = state
            )
        }

    }

    @Serializable
    object Backup : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            BackupScreen(state)
        }

    }

    @Serializable
    data class Feedback(
        val topic: FeedbackTopic
    ) : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            FeedbackScreen(
                feedbackTopic = topic,
                mainNavigationState = state
            )
        }

    }

}

data class MainDestinationConfiguration<T : MainDestination>(
    val clazz: KClass<T>,
    val subclassRegisterer: (PolymorphicModuleBuilder<MainDestination>) -> Unit
)

inline fun <reified T : MainDestination> KClass<T>.configuration(): MainDestinationConfiguration<T> {
    return MainDestinationConfiguration(
        clazz = this,
        subclassRegisterer = {
            it.subclass(
                subclass = this@configuration,
                serializer = kotlinx.serialization.serializer()
            )
        }
    )
}

val defaultMainDestinations: List<MainDestinationConfiguration<*>> = listOf(
    MainDestination.About::class.configuration(),
    MainDestination.Credits::class.configuration(),
    MainDestination.Backup::class.configuration(),
    MainDestination.DeckEdit::class.configuration(),
    MainDestination.Feedback::class.configuration(),
    MainDestination.Home::class.configuration(),
    MainDestination.LetterDeckPicker::class.configuration(),
    MainDestination.KanjiInfo::class.configuration(),
    MainDestination.Practice.Reading::class.configuration(),
    MainDestination.Practice.Writing::class.configuration(),
    MainDestination.VocabPractice::class.configuration(),
    MainDestination.LetterDeckDetails::class.configuration(),
)
