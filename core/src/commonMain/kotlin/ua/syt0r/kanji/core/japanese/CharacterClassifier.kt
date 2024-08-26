package ua.syt0r.kanji.core.japanese

import ua.syt0r.kanji.core.app_data.AppDataRepository

interface CharacterClassifier {
    suspend fun get(character: String): List<CharacterClassification>
}

class DefaultCharacterClassifier(
    private val appDataRepository: AppDataRepository
) : CharacterClassifier {

    override suspend fun get(character: String): List<CharacterClassification> {
        val char = character.first()
        return when {
            char.isHiragana() -> listOf(CharacterClassification.Kana.Hiragana)
            char.isKatakana() -> listOf(CharacterClassification.Kana.Katakana)
            else -> appDataRepository.getClassificationsForKanji(character)
                .map { CharacterClassification.DBDefined.fromDbValue(it) }
        }
    }

}