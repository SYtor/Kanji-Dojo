package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

interface AccountScreenContract {
    companion object {
        private const val BASE = "https://kanji-dojo.com/account"
        const val DEEP_LINK_AUTH_URL = "$BASE?deepLinkAuth=true"
        fun serverAuthUrl(port: Int): String = "$BASE?callbackPort=$port"
    }

    @Serializable
    data class ScreenData(
        val refreshToken: String,
        val idToken: String
    )

    interface Content {

        @Composable
        operator fun invoke(
            state: MainNavigationState,
            data: ScreenData?
        )

    }

}