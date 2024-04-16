package ua.syt0r.kanji.presentation.screen.main

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ua.syt0r.kanji.core.logger.Logger
import kotlin.reflect.KClass

@Composable
actual fun rememberMainNavigationState(): MainNavigationState {
    val navController = rememberNavController()
    return remember { AndroidMainNavigationState(navController, defaultMainDestinations) }
}


@Composable
actual fun MainNavigation(state: MainNavigationState) {
    state as AndroidMainNavigationState

    NavHost(
        navController = state.navHostController,
        startDestination = state.defaultDestination.getRoute(state.json)
    ) {

        state.destinations.forEach { registerDestination(it.clazz, state) }

    }
}

private const val MainDestinationArgumentKey = "arg"

private val <T : MainDestination> KClass<T>.routeTemplate: String
    get() = "$simpleName/{$MainDestinationArgumentKey}"

private val MainDestinationNavArgument = navArgument(MainDestinationArgumentKey) {
    type = NavType.StringType
}

private fun MainDestination.getRoute(json: Json): String {
    val uri = Uri.encode(
        json.encodeToString(PolymorphicSerializer(MainDestination::class), this)
    )
    val route = "${this::class.simpleName}/$uri"
    Logger.d("route[$route]")
    return route
}

private fun NavGraphBuilder.registerDestination(
    clazz: KClass<out MainDestination>,
    state: AndroidMainNavigationState
) {

    composable(
        route = clazz.routeTemplate,
        arguments = listOf(MainDestinationNavArgument),
        content = {
            val destination = it.getDeserializedDestination(state.json) ?: state.defaultDestination
            destination.Draw(state = state)
        }
    )

}

private fun NavBackStackEntry.getDeserializedDestination(json: Json): MainDestination? {
    return arguments?.getString(MainDestinationArgumentKey)
        ?.let { Uri.decode(it) }
        ?.let {
            Logger.d("decodingJson[$it]")
            json.decodeFromString<MainDestination>(it)
        }
}

@Immutable
private class AndroidMainNavigationState(
    val navHostController: NavHostController,
    val destinations: List<MainDestinationConfiguration<*>>
) : MainNavigationState {

    val json = Json {
        serializersModule = SerializersModule {
            polymorphic(MainDestination::class) {
                destinations.forEach { it.subclassRegisterer.invoke(this) }
            }
        }
    }

    val defaultDestination = MainDestination.Home

    override fun navigateBack() {
        navHostController.navigateUp()
    }

    override fun popUpToHome() {
        navHostController.popBackStack(
            route = MainDestination.Home::class.routeTemplate,
            inclusive = false
        )
    }

    override fun navigate(destination: MainDestination) {
        val route = destination.getRoute(json)
        Logger.d("navigatingToRoute[$route]")
        navHostController.navigate(route)
    }

}