package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.koin.java.KoinJavaComponent.getKoin
import ua.syt0r.kanji.presentation.screen.main.screen.about.AboutScreen
import ua.syt0r.kanji.presentation.screen.main.screen.backup.BackupScreen
import ua.syt0r.kanji.presentation.screen.main.screen.credits.CreditsScreen
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.DeckDetailsScreen
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreen
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.DeckPickerScreen
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreen
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic
import ua.syt0r.kanji.presentation.screen.main.screen.home.HomeScreen
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreen
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreen
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeScreen
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract
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
    data class DeckPicker(
        val configuration: DeckPickerScreenConfiguration
    ) : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            DeckPickerScreen(
                configuration = configuration,
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
    data class DeckDetails(
        val configuration: DeckDetailsScreenConfiguration
    ) : MainDestination {
        @Composable
        override fun Draw(state: MainNavigationState) {
            DeckDetailsScreen(
                configuration = configuration,
                mainNavigationState = state
            )
        }
    }

    @Serializable
    sealed interface Practice : MainDestination {

        @Serializable
        data class Writing(
            val deckId: Long,
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
            val deckId: Long,
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
        val wordIds: List<Long>
    ) : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            VocabPracticeScreen(
                wordIds = wordIds,
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

    @Serializable
    object Sponsor : MainDestination {

        @Composable
        override fun Draw(state: MainNavigationState) {
            val content = remember { getKoin().get<SponsorScreenContract.Content>() }
            content(state)
        }

    }

}

sealed interface MainDestinationConfiguration<T : MainDestination> {

    val clazz: KClass<T>
    val subclassRegisterer: (PolymorphicModuleBuilder<MainDestination>) -> Unit

    data class NoParams<T : MainDestination>(
        val instance: T,
        override val clazz: KClass<T>,
        override val subclassRegisterer: (PolymorphicModuleBuilder<MainDestination>) -> Unit
    ) : MainDestinationConfiguration<T>

    data class WithArguments<T : MainDestination>(
        override val clazz: KClass<T>,
        override val subclassRegisterer: (PolymorphicModuleBuilder<MainDestination>) -> Unit
    ) : MainDestinationConfiguration<T>

}

inline fun <reified T : MainDestination> T.configuration(): MainDestinationConfiguration.NoParams<T> {
    return MainDestinationConfiguration.NoParams(
        instance = this,
        clazz = T::class,
        subclassRegisterer = {
            it.subclass(
                subclass = T::class,
                serializer = kotlinx.serialization.serializer()
            )
        }
    )
}

inline fun <reified T : MainDestination> KClass<T>.configuration(): MainDestinationConfiguration.WithArguments<T> {
    return MainDestinationConfiguration.WithArguments(
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
    MainDestination.Home.configuration(),
    MainDestination.Backup.configuration(),
    MainDestination.About.configuration(),
    MainDestination.Credits.configuration(),
    MainDestination.Sponsor.configuration(),
    MainDestination.DeckPicker::class.configuration(),
    MainDestination.DeckDetails::class.configuration(),
    MainDestination.DeckEdit::class.configuration(),
    MainDestination.Feedback::class.configuration(),
    MainDestination.KanjiInfo::class.configuration(),
    MainDestination.Practice.Reading::class.configuration(),
    MainDestination.Practice.Writing::class.configuration(),
    MainDestination.VocabPractice::class.configuration(),
)
