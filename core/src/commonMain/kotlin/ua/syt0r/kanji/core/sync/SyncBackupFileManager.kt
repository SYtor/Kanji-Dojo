package ua.syt0r.kanji.core.sync

import io.ktor.client.request.forms.ChannelProvider
import ua.syt0r.kanji.core.backup.PlatformFile
import java.io.OutputStream

interface SyncBackupFileManager {
    fun getFile(): PlatformFile
    fun getChannelProvider(): ChannelProvider
    fun outputStream(): OutputStream
    fun clean()
}