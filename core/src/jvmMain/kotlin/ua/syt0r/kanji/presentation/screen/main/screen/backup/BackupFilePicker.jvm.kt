package ua.syt0r.kanji.presentation.screen.main.screen.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ua.syt0r.kanji.core.backup.PlatformFileJvm
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.filechooser.FileFilter


private class SwingBackupFilePicker(
    private val onFileCreateCallback: ((FilePickResult) -> Unit)?,
    private val onFileSelectCallback: ((FilePickResult) -> Unit)?
) : BackupFilePicker {

    override fun startCreateFileFlow() {
        val frame = JFrame()
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = SwingBackupFileFilter
        fileChooser.selectedFile = File(getDefaultBackupFileName())
        val resultValue = fileChooser.showSaveDialog(frame)

        val result = if (resultValue == JFileChooser.APPROVE_OPTION) {
            FilePickResult.Picked(PlatformFileJvm(fileChooser.selectedFile))
        } else FilePickResult.Canceled

        onFileCreateCallback!!.invoke(result)
    }

    override fun startSelectFileFlow() {
        val frame = JFrame()
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = SwingBackupFileFilter
        val resultValue = fileChooser.showOpenDialog(frame)

        val result = if (resultValue == JFileChooser.APPROVE_OPTION) {
            FilePickResult.Picked(PlatformFileJvm(fileChooser.selectedFile))
        } else FilePickResult.Canceled

        onFileSelectCallback!!.invoke(result)
    }

}

private object SwingBackupFileFilter : FileFilter() {
    override fun accept(file: File): Boolean {
        return file.isDirectory || file.path.endsWith(".zip")
    }

    override fun getDescription(): String {
        return "Zip archive"
    }
}

@Composable
internal actual fun internalRememberBackupFilePicker(
    onFileCreateCallback: ((FilePickResult) -> Unit)?,
    onFileSelectCallback: ((FilePickResult) -> Unit)?
): BackupFilePicker {
    return remember { SwingBackupFilePicker(onFileCreateCallback, onFileSelectCallback) }
}