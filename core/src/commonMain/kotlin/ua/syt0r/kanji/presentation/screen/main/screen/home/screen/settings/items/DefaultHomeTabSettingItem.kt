package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesDefaultHomeTab
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsPreferencePickerDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum

enum class DisplayableDefaultHomeTab(
    val prefType: PreferencesDefaultHomeTab,
    override val titleResolver: StringResolveScope<String>
) : DisplayableEnum {

    GeneralDashboard(PreferencesDefaultHomeTab.GeneralDashboard, { home.generalDashboardTabLabel }),
    Letters(PreferencesDefaultHomeTab.Letters, { home.lettersDashboardTabLabel }),
    Vocab(PreferencesDefaultHomeTab.Vocab, { home.vocabDashboardTabLabel });

    companion object {
        fun from(prefType: PreferencesDefaultHomeTab): DisplayableDefaultHomeTab =
            entries.first { it.prefType == prefType }
    }

}

class DefaultHomeTabSettingItem(
    private val appPreferences: PreferencesContract.AppPreferences
) : SettingsScreenContract.ConfigurableListItem {

    private lateinit var configuration: MutableState<DisplayableDefaultHomeTab>

    override suspend fun prepare(coroutineScope: CoroutineScope) {
        configuration = mutableStateOf(
            DisplayableDefaultHomeTab.from(appPreferences.defaultHomeTab.get())
        )

        snapshotFlow { configuration.value }
            .drop(1)
            .map { it.prefType }
            .onEach { appPreferences.defaultHomeTab.set(it) }
            .launchIn(coroutineScope)
    }

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {

        var showPicker by rememberSaveable { mutableStateOf(false) }

        ListItem(
            modifier = Modifier.clip(MaterialTheme.shapes.medium)
                .fillMaxWidth()
                .clickable { showPicker = true },
            headlineContent = { Text("Default Tab") },
            supportingContent = { Text(resolveString(configuration.value.titleResolver)) }
        )

        if (showPicker) {
            SettingsPreferencePickerDialog(
                onDismissRequest = { showPicker = false },
                title = "Default Tab",
                options = DisplayableDefaultHomeTab.entries,
                defaultSelected = configuration.value,
                onSelected = { configuration.value = it }
            )
        }

    }

}