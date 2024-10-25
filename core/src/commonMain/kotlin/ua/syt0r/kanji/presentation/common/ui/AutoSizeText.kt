package ua.syt0r.kanji.presentation.common.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.reflect.KProperty

/**
 * Composable function that automatically adjusts the text size to fit within given constraints, considering the ratio of line spacing to text size.
 *
 * Features:
 *  1. Best performance: Utilizes a dichotomous binary search algorithm for swift and optimal text size determination without unnecessary iterations.
 *  2. Alignment support: Supports six possible alignment values via the Alignment interface.
 *  3. Material Design 3 support.
 *  4. Font scaling support: User-initiated font scaling doesn't affect the visual rendering output.
 *  5. Multiline Support with maxLines Parameter.
 *
 * Limitations:
 *  1. MinLine is set to 1 under the hood and cannot be changed.
 *
 * @param text The text to be displayed.
 * @param modifier The modifier for the text composable.
 * @param suggestedFontSizes The suggested font sizes to choose from.
 * @param minTextSize The minimum text size allowed.
 * @param maxTextSize The maximum text size allowed.
 * @param stepGranularityTextSize The step size for adjusting the text size.
 * @param alignment The alignment of the text within its container.
 * @param color The color of the text.
 * @param fontStyle The font style of the text.
 * @param fontWeight The font weight of the text.
 * @param fontFamily The font family of the text.
 * @param letterSpacing The letter spacing of the text.
 * @param textDecoration The text decoration style.
 * @param textAlign The alignment of the text within the lines of the paragraph.
 * @param lineSpacingRatio The ratio of line spacing to text size.
 * @param maxLines The maximum number of lines for the text.
 * @param onTextLayout Callback invoked when the text layout is available.
 * @param style The base style to apply to the text.
 * @author Reda El Madini - For support, contact gladiatorkilo@gmail.com
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    suggestedFontSizes: ImmutableWrapper<List<TextUnit>> = emptyList<TextUnit>().toImmutableWrapper(),
    stepGranularityTextSize: TextUnit = TextUnit.Unspecified,
    alignment: Alignment = Alignment.Center,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineSpacingRatio: Float = 0.1F,
    maxLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    minTextSize: TextUnit = TextUnit.Unspecified,
    maxTextSize: TextUnit = style.fontSize,
) {
    AutoSizeText(
        text = AnnotatedString(text),
        modifier = modifier,
        suggestedFontSizes = suggestedFontSizes,
        minTextSize = minTextSize,
        maxTextSize = maxTextSize,
        stepGranularityTextSize = stepGranularityTextSize,
        alignment = alignment,
        color = color,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineSpacingRatio = lineSpacingRatio,
        maxLines = maxLines,
        onTextLayout = onTextLayout,
        style = style,
    )
}

/**
 * Composable function that automatically adjusts the text size to fit within given constraints using AnnotatedString, considering the ratio of line spacing to text size.
 *
 * Features:
 *  Similar to AutoSizeText(String), with support for AnnotatedString.
 *
 * @see AutoSizeText
 */
@Composable
fun AutoSizeText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    suggestedFontSizes: ImmutableWrapper<List<TextUnit>> = emptyList<TextUnit>().toImmutableWrapper(),
    minTextSize: TextUnit = TextUnit.Unspecified,
    maxTextSize: TextUnit = TextUnit.Unspecified,
    stepGranularityTextSize: TextUnit = TextUnit.Unspecified,
    alignment: Alignment = Alignment.Center,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineSpacingRatio: Float = 0.1F,
    maxLines: Int = 1,
    inlineContent: ImmutableWrapper<Map<String, InlineTextContent>> = mapOf<String, InlineTextContent>().toImmutableWrapper(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val permittedTextUnitTypes = remember { listOf(TextUnitType.Unspecified, TextUnitType.Sp) }
    check(minTextSize.type in permittedTextUnitTypes)
    check(maxTextSize.type in permittedTextUnitTypes)
    check(stepGranularityTextSize.type in permittedTextUnitTypes)

    val density = LocalDensity.current.density
    val textMeasurer = rememberTextMeasurer()
    // Change font scale to 1
    CompositionLocalProvider(LocalDensity provides Density(density = density, fontScale = 1F)) {
        BoxWithConstraints(
            modifier = modifier,
            contentAlignment = alignment,
        ) {
            // (1 / density).sp represents 1px when font scale equals 1
            val step = remember(stepGranularityTextSize) {
                (1 / density).let {
                    if (stepGranularityTextSize.isUnspecified) it.sp
                    else stepGranularityTextSize.value.coerceAtLeast(it).sp
                }
            }

            val max = remember(maxWidth, maxHeight, maxTextSize) {
                min(maxWidth, maxHeight).value.let {
                    if (maxTextSize.isUnspecified) it.sp
                    else maxTextSize.value.coerceAtMost(it).sp
                }
            }

            val min = remember(minTextSize, step, max) {
                if (minTextSize.isUnspecified) step
                else
                    minTextSize.value.coerceIn(
                        minimumValue = step.value,
                        maximumValue = max.value
                    ).sp
            }

            val possibleFontSizes = remember(suggestedFontSizes, min, max, step) {
                suggestedFontSizes.value
                    .filter {
                        it.isSp && it.value in min.value..max.value
                    }
                    .sortedByDescending { it.value }
                    .takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        val firstIndex = ceil(min.value / step.value).toInt()
                        val lastIndex = floor(max.value / step.value).toInt()
                        MutableList(size = (lastIndex - firstIndex) + 1) { index ->
                            step * (lastIndex - index)
                        }
                    }
            }

            var combinedTextStyle = (LocalTextStyle.current + style).copy(
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            )

            if (possibleFontSizes.isNotEmpty()) {
                // Semi Dichotomous binary search
                var low = 0
                var high = possibleFontSizes.lastIndex
                while (low <= high) {
                    val mid = low + (high - low) / 2
                    val fontSize = possibleFontSizes[mid]
                    val shouldShrink = shouldShrink(
                        textMeasurer = textMeasurer,
                        text = text,
                        textStyle = combinedTextStyle.copy(
                            fontSize = fontSize,
                            lineHeight = fontSize * (1 + lineSpacingRatio),
                        ),
                        maxLines = maxLines,
                    )

                    if (shouldShrink) low = mid + 1
                    else high = mid - 1
                }
                val electedFontSize = possibleFontSizes[low.coerceIn(possibleFontSizes.indices)]
                combinedTextStyle = combinedTextStyle.copy(
                    fontSize = electedFontSize,
                    lineHeight = electedFontSize * (1 + lineSpacingRatio),
                )
            }

            Text(
                text = text,
                modifier = Modifier,
                color = color,
                fontSize = TextUnit.Unspecified,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                overflow = TextOverflow.Visible,
                maxLines = maxLines,
                minLines = 1,
                inlineContent = inlineContent.value,
                onTextLayout = onTextLayout,
                style = combinedTextStyle,
                softWrap = true,
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.shouldShrink(
    textMeasurer: TextMeasurer,
    text: AnnotatedString,
    textStyle: TextStyle,
    maxLines: Int,
): Boolean {
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = textStyle,
        overflow = TextOverflow.Clip,
        maxLines = maxLines,
        constraints = constraints,
    )

    return textLayoutResult.hasVisualOverflow
}

@Immutable
data class ImmutableWrapper<T>(val value: T)

/**
 * May hold null value
 */
fun <T> T.toImmutableWrapper() = ImmutableWrapper(this)

operator fun <T> ImmutableWrapper<T>.getValue(thisRef: Any?, property: KProperty<*>) = value