package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf

data class LimitItem(
    val new: LimitInputData,
    val due: LimitInputData
)

class LimitInputData(
    val input: MutableState<String>
) {
    val validated: State<Int?> = derivedStateOf { input.value.asValidLimitNumber() }
}

private fun String.asValidLimitNumber(): Int? = toIntOrNull()?.takeIf { it >= 0 }