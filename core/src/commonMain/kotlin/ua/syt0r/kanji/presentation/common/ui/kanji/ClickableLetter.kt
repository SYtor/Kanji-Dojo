package ua.syt0r.kanji.presentation.common.ui.kanji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.copyCentered

@Composable
fun ClickableLetter(
    letter: String,
    onClick: (String) -> Unit
) {

    Text(
        text = letter,
        fontSize = 32.sp,
        modifier = Modifier.clip(MaterialTheme.shapes.small)
            .width(IntrinsicSize.Min)
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick(letter) }
            .padding(8.dp)
            .wrapContentSize(),
        style = MaterialTheme.typography.bodyLarge.copyCentered()
    )

}