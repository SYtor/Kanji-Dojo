package ua.syt0r.kanji.core.sync

import androidx.core.net.toUri
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.util.cio.readChannel
import ua.syt0r.kanji.core.backup.PlatformFile
import ua.syt0r.kanji.core.backup.PlatformFileAndroid
import java.io.File
import java.io.OutputStream

class AndroidSyncBackupFileManager(
    workingDir: File
) : SyncBackupFileManager {

    private val backupFile = File(workingDir, "sync_backup.zip")

    override fun getFile(): PlatformFile = PlatformFileAndroid(backupFile.toUri())

    override fun getChannelProvider(): ChannelProvider = ChannelProvider(
        size = backupFile.length(),
        block = { backupFile.readChannel() }
    )

    override fun outputStream(): OutputStream {
        return backupFile.outputStream()
    }

    override fun clean() {
        backupFile.delete()
    }

}