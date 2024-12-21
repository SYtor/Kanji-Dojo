package ua.syt0r.kanji.presentation.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsSwitchRow

class GooglePlayAnalyticsSettingListItem(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val analyticsManager: AnalyticsManager
) : SettingsScreenContract.ConfigurableListItem {

    private lateinit var enabled: MutableState<Boolean>

    override suspend fun prepare(coroutineScope: CoroutineScope) {
        enabled = mutableStateOf(appPreferences.analyticsEnabled.get())
        snapshotFlow { enabled.value }
            .drop(1)
            .onEach { value ->
                appPreferences.analyticsEnabled.set(value)
                analyticsManager.setAnalyticsEnabled(value)
                analyticsManager.sendEvent("analytics_toggled") {
                    put("analytics_enabled", value)
                }
            }
            .launchIn(coroutineScope)
    }

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {
        val strings = resolveString { settings }
        val value = enabled.value
        SettingsSwitchRow(
            title = strings.analyticsTitle,
            message = strings.analyticsMessage,
            isEnabled = value,
            onToggled = { enabled.value = !value }
        )
    }

}
