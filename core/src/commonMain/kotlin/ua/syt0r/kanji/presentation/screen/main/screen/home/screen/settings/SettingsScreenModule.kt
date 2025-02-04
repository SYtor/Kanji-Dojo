package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items.DefaultHomeTabSettingItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items.ThemeSettingItem

val defaultSettingItemsQualifier = qualifier("default_setting_items")
val settingItemsQualifier = qualifier("setting_items")

val settingsScreenModule = module {

    multiplatformViewModel<SettingsScreenContract.ViewModel> {
        SettingsScreenViewModel(
            coroutineScope = it.component1(),
            defaultSettingItems = get(defaultSettingItemsQualifier),
            customSettingItems = get(settingItemsQualifier)
        )
    }

    factory(defaultSettingItemsQualifier) {
        listOf(
            ThemeSettingItem(themeManager = get()),
            DefaultHomeTabSettingItem(appPreferences = get())
        )
    }

    factory(settingItemsQualifier) { listOf<SettingsScreenContract.ListItem>() }

}