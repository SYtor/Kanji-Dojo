package ua.syt0r.kanji.presentation.common.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

@Composable
fun FancyLoading(
    modifier: Modifier = Modifier
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {

        val loadingTransition = rememberInfiniteTransition(label = "Transition")
        val periodsToRemove by loadingTransition.animateValue(
            initialValue = 3,
            targetValue = 0,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
            label = "Periods Animation"
        )

        Text(
            text = buildAnnotatedString {
                val text = resolveString { loading } + "..."
                append(text)
                addStyle(
                    style = SpanStyle(Color.Transparent),
                    start = text.length - periodsToRemove,
                    end = text.length
                )
            },
            maxLines = 1
        )

        LinearProgressIndicator(
            modifier = Modifier.width(200.dp),
            color = MaterialTheme.colorScheme.primary
        )

    }

}