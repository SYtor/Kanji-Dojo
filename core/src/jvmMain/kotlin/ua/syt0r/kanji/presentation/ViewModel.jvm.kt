package ua.syt0r.kanji.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin

@Composable
actual inline fun <reified T> platformGetMultiplatformViewModel(): T {
    /***
     * Using custom coroutine scope instead of remember one since it can leave composition when
     * navigating so view model will have canceled scope after returning to the screen
     */
    return rememberSaveable { getKoin().get { parametersOf(CoroutineScope(Dispatchers.Unconfined)) } }
}