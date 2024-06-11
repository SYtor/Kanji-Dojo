package ua.syt0r.kanji.core.srs.use_case

import ua.syt0r.kanji.core.srs.LetterSrsManager

interface NotifySrsPreferencesChangedUseCase {
    suspend operator fun invoke()
}

class DefaultNotifySrsPreferencesChangedUseCase(
    private val manager: LetterSrsManager
) : NotifySrsPreferencesChangedUseCase {
    override suspend fun invoke() {
        manager.notifyPreferencesChange()
    }

}