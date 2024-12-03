package ua.syt0r.kanji.presentation.common.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.presentation.common.theme.AppTheme


private const val RubyTagName = "ruby"
private const val RtTagName = "rt"

private fun xmlTagRegex(tagName: String) = "<$tagName>(.*?)</$tagName>".toRegex()

@Composable
fun furiganaStringResource(@StringRes stringResId: Int): FuriganaString {
    val rawText = stringResource(stringResId)
    return buildFuriganaString {

        val rubyRegex = xmlTagRegex(RubyTagName)

        val noRubyTagContent = rawText.split(rubyRegex)
        val rubyTagContents: List<String?> = rubyRegex.findAll(rawText)
            .map { it.groupValues[1] }
            .toList()
            .run {
                if (size != noRubyTagContent.size) plus(null)
                else this
            }

        noRubyTagContent.zip(rubyTagContents).map { (textBeforeRuby, rubyContent) ->

            append(textBeforeRuby)
            if (rubyContent == null) return@map

            val rtRegex = xmlTagRegex(RtTagName)

            val noRtTagContent = rubyContent.split(rtRegex)
            val rtTagsContent = rtRegex.findAll(rubyContent)
                .map { it.groupValues[1] }
                .toList()
                .run {
                    if (size != noRtTagContent.size) plus(null)
                    else this
                }

            noRtTagContent.zip(rtTagsContent).forEach { (content, annotation) ->
                append(content, annotation)
            }

        }

    }

}

@Preview(showBackground = true)
@Composable
private fun ClickPreview() {

    AppTheme {

        var counter by remember { mutableStateOf(0) }
        var selected: String? by remember { mutableStateOf(null) }

        Column {
            ClickableFuriganaText(
                furiganaString = buildFuriganaString {
                    append("事", "じ")
                    append("イランコントラ")
                    append("事", "じ")
                    append("件", "けん")
                    append(" - accident of sdfq fqoe fqeiof foewij fewifoefwio")
                },
                onClick = {
                    counter++
                    selected = it
                },
                modifier = Modifier.width(200.dp)
            )
            Text(
                text = "${selected}: $counter",
                modifier = Modifier.background(Color.Green)
            )
        }

    }

}