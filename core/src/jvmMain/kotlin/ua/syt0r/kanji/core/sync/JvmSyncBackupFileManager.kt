package ua.syt0r.kanji.core.sync

import io.ktor.client.request.forms.ChannelProvider
import io.ktor.util.cio.readChannel
import ua.syt0r.kanji.core.backup.PlatformFile
import ua.syt0r.kanji.core.backup.PlatformFileJvm
import ua.syt0r.kanji.core.getUserDataDirectory
import java.io.File

class JvmSyncBackupFileManager : SyncBackupFileManager {

    private val backupFile = File(getUserDataDirectory(), "sync_backup.zip")

    override fun getFile(): PlatformFile = PlatformFileJvm(backupFile)

    override fun getChannelProvider(): ChannelProvider = ChannelProvider(
        size = backupFile.length(),
        block = { backupFile.readChannel() }
    )

    override fun clean() {
        backupFile.delete()
    }

}