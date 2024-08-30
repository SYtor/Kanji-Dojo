package ua.syt0r.kanji.core.japanese

import kotlinx.serialization.Serializable


@Serializable
sealed interface CharacterClassification {

    @Serializable
    sealed interface Kana : CharacterClassification {

        val characters: List<String>

        @Serializable
        object Hiragana : Kana {
            override val characters: List<String> = allHiraganaReadings.keys
                .map { it.toString() }
        }

        @Serializable
        object Katakana : Kana {
            override val characters: List<String> = Hiragana.characters
                .map { hiraganaToKatakana(it.first()).toString() }
        }

        companion object {
            val all = listOf(Hiragana, Katakana)
        }

    }

    interface DBDefined {

        val dbValue: String

        companion object {
            fun fromDbValue(value: String): CharacterClassification {
                val prefix = value.first()
                val number = value.substring(1).toInt()
                return when (prefix) {
                    'n' -> JLPT(number)
                    'g' -> Grade(number)
                    'w' -> Wanikani(number)
                    else -> error("unsupported classification [$value]")
                }
            }
        }

    }

    @Serializable
    data class JLPT(
        val level: Int
    ) : CharacterClassification, DBDefined {

        override val dbValue: String = "n$level"

        companion object {

            val all: List<JLPT> = (5 downTo 1).map { JLPT(it) }

        }

    }

    @Serializable
    data class Grade(
        val number: Int
    ) : CharacterClassification, DBDefined {

        override val dbValue: String = "g$number"

        companion object {

            val all: List<Grade>
                get() = (1..6).plus(8..10).map { Grade(it) }

        }

    }

    @Serializable
    data class Wanikani(
        val level: Int
    ) : CharacterClassification, DBDefined {

        override val dbValue: String = "w$level"

        init {
            require(level in 1..60)
        }


        companion object {

            val all: List<Wanikani>
                get() = (1..60).map { Wanikani(it) }

        }

    }

}
