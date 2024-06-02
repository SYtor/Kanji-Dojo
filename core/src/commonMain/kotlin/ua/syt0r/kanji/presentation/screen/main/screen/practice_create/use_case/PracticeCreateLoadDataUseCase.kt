package ua.syt0r.kanji.presentation.screen.main.screen.practice_create.use_case

import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.dakutenHiraganaReadings
import ua.syt0r.kanji.core.japanese.hiraganaReadings
import ua.syt0r.kanji.core.japanese.hiraganaToKatakana
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.practice_create.PracticeCreateScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_create.data.CreatePracticeData

class PracticeCreateLoadDataUseCase(
    private val practiceRepository: LetterPracticeRepository
) : PracticeCreateScreenContract.LoadDataUseCase {

    override suspend fun load(configuration: MainDestination.CreatePractice): CreatePracticeData {
        return when (configuration) {

            is MainDestination.CreatePractice.New -> {
                CreatePracticeData(
                    title = "",
                    characters = emptySet()
                )
            }

            is MainDestination.CreatePractice.EditExisting -> {
                val practice = practiceRepository.getPracticeInfo(configuration.practiceId)
                CreatePracticeData(
                    title = practice.name,
                    characters = practiceRepository.getKanjiForPractice(configuration.practiceId)
                        .toSet()
                )
            }

            is MainDestination.CreatePractice.Import -> {
                CreatePracticeData(
                    title = configuration.title,
                    characters = when (val classification = configuration.classification) {
                        CharacterClassification.Kana.Hiragana -> {
                            (hiraganaReadings.toList() + dakutenHiraganaReadings.toList()).map {
                                it.first.toString()
                            }
                        }

                        CharacterClassification.Kana.Katakana -> {
                            (hiraganaReadings.toList() + dakutenHiraganaReadings.toList()).map {
                                hiraganaToKatakana(it.first).toString()
                            }
                        }

                        else -> classification.characters
                    }.toSet()
                )
            }

        }
    }

}