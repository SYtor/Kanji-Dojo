package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.koin.compose.koinInject
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.about.AboutScreen
import ua.syt0r.kanji.presentation.screen.main.screen.backup.BackupScreen
import ua.syt0r.kanji.presentation.screen.main.screen.credits.CreditsScreen
import ua.syt0r.kanji.presentation.screen.main.screen.daily_limit.DailyLimitScreen
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
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreen
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract
import kotlin.reflect.KClass

interface MainNavigationState {
    val currentDestination: State<MainDestination?>
    fun navigateBack()
    fun popUpToHome()
    fun navigate(destination: MainDestination)
}

@Composable
expect fun rememberMainNavigationState(): MainNavigationState

@Composable
expect fun MainNavigation(state: MainNavigationState)

interface MainDestination {

    val analyticsName: String?

    @Composable
    fun Content(state: MainNavigationState)


    @Serializable
    object Home : MainDestination {

        override val analyticsName: String? = null

        @Composable
        override fun Content(state: MainNavigationState) {
            HomeScreen(
                mainNavigationState = rememberUpdatedState(state)
            )
        }

    }

    @Serializable
    object About : MainDestination {

        override val analyticsName: String = "about"

        @Composable
        override fun Content(state: MainNavigationState) {
            AboutScreen(
                mainNavigationState = state
            )
        }

    }

    @Serializable
    object Credits : MainDestination {

        override val analyticsName: String = "credits"

        @Composable
        override fun Content(state: MainNavigationState) {
            CreditsScreen(state)
        }

    }

    @Serializable
    data class DeckPicker(
        val configuration: DeckPickerScreenConfiguration
    ) : MainDestination {

        override val analyticsName: String = "deck_picker"

        @Composable
        override fun Content(state: MainNavigationState) {
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

        override val analyticsName: String = "deck_edit"

        @Composable
        override fun Content(state: MainNavigationState) {
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

        override val analyticsName: String = "deck_details"

        @Composable
        override fun Content(state: MainNavigationState) {
            DeckDetailsScreen(
                configuration = configuration,
                mainNavigationState = state
            )
        }
    }

    @Serializable
    data class LetterPractice(
        val configuration: LetterPracticeScreenConfiguration
    ) : MainDestination {

        override val analyticsName: String = when (configuration.practiceType) {
            ScreenLetterPracticeType.Writing -> "writing_practice"
            ScreenLetterPracticeType.Reading -> "reading_practice"
        }

        @Composable
        override fun Content(state: MainNavigationState) {
            val content = koinInject<LetterPracticeScreenContract.Content>()
            content(
                configuration = configuration,
                mainNavigationState = state,
                viewModel = getMultiplatformViewModel()
            )
        }

    }

    @Serializable
    data class VocabPractice(
        val configuration: VocabPracticeScreenConfiguration
    ) : MainDestination {

        override val analyticsName: String = "vocab_practice"

        @Composable
        override fun Content(state: MainNavigationState) {
            VocabPracticeScreen(
                configuration = configuration,
                mainNavigationState = state
            )
        }

    }

    @Serializable
    data class KanjiInfo(
        val character: String
    ) : MainDestination {

        override val analyticsName: String = "kanji_info"

        @Composable
        override fun Content(state: MainNavigationState) {
            KanjiInfoScreen(
                kanji = character,
                mainNavigationState = state
            )
        }

    }

    @Serializable
    object Backup : MainDestination {

        override val analyticsName: String = "backup"

        @Composable
        override fun Content(state: MainNavigationState) {
            BackupScreen(state)
        }

    }

    @Serializable
    data class Feedback(
        val topic: FeedbackTopic
    ) : MainDestination {

        override val analyticsName: String = "feedback"

        @Composable
        override fun Content(state: MainNavigationState) {
            FeedbackScreen(
                feedbackTopic = topic,
                mainNavigationState = state
            )
        }

    }

    @Serializable
    object Sponsor : MainDestination {

        override val analyticsName: String = "sponsor"

        @Composable
        override fun Content(state: MainNavigationState) {
            val content = koinInject<SponsorScreenContract.Content>()
            content(state)
        }

    }

    @Serializable
    object DailyLimit : MainDestination {

        override val analyticsName: String = "daily_limit"

        @Composable
        override fun Content(state: MainNavigationState) {
            DailyLimitScreen(state)
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
    MainDestination.DailyLimit.configuration(),
    MainDestination.DeckPicker::class.configuration(),
    MainDestination.DeckDetails::class.configuration(),
    MainDestination.DeckEdit::class.configuration(),
    MainDestination.Feedback::class.configuration(),
    MainDestination.KanjiInfo::class.configuration(),
    MainDestination.LetterPractice::class.configuration(),
    MainDestination.VocabPractice::class.configuration(),
)
