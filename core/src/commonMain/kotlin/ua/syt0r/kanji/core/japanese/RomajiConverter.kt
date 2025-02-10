package ua.syt0r.kanji.core.japanese

import dev.esnault.wanakana.core.Wanakana

interface RomajiConverter {
    fun toRomaji(kanaText: String): String
}

class WanakanaRomajiConverter : RomajiConverter {

    override fun toRomaji(kanaText: String): String {
        return Wanakana.toRomaji(kanaText)
    }

}