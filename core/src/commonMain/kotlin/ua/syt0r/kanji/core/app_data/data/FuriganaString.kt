package ua.syt0r.kanji.core.app_data.data

import kotlinx.serialization.Serializable

@Serializable
data class FuriganaString(
    val compounds: List<FuriganaStringCompound>
) {

    operator fun plus(string: String): FuriganaString {
        return FuriganaString(compounds.plus(FuriganaStringCompound(string)))
    }

}

@Serializable
data class FuriganaStringCompound(
    val text: String,
    val annotation: String? = null
)

class FuriganaStringBuilder {

    private val list = mutableListOf<FuriganaStringCompound>()

    fun append(character: String, annotation: String? = null) =
        list.add(FuriganaStringCompound(character, annotation))

    fun append(furiganaString: FuriganaString) {
        list.addAll(furiganaString.compounds)
    }

    fun build() = FuriganaString(list)

}

fun buildFuriganaString(scope: FuriganaStringBuilder.() -> Unit): FuriganaString {
    val builder = FuriganaStringBuilder()
    builder.scope()
    return builder.build()
}

private const val ENCODED_SYMBOL = "◯"
fun FuriganaString.withEncodedText(
    text: String
): FuriganaString {
    return FuriganaString(
        compounds = compounds.map {
            FuriganaStringCompound(
                text = it.text.replace(text, ENCODED_SYMBOL),
                annotation = it.annotation?.replace(text, ENCODED_SYMBOL)
            )
        }
    )
}

fun FuriganaString.withEmptyFurigana(): FuriganaString {
    return FuriganaString(
        compounds.map { it.copy(annotation = it.annotation?.let { "" }) }
    )
}

fun FuriganaString.withoutAnnotations(): String {
    return compounds.joinToString("") { it.text }
}

fun String.toFurigana() = buildFuriganaString { append(this@toFurigana) }
