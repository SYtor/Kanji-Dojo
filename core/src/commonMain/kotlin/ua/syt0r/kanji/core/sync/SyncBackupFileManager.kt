package ua.syt0r.kanji.core.sync

import io.ktor.client.request.forms.ChannelProvider
import ua.syt0r.kanji.core.backup.PlatformFile

interface SyncBackupFileManager {
    fun getFile(): PlatformFile
    fun getChannelProvider(): ChannelProvider
    fun clean()
}