package ua.syt0r.kanji.core.srs

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