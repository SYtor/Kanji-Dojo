import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

data class Asset(
    val fileName: String,
    val url: String?
)

data class KanjiDojoAssetLocation(
    val directory: String,
    val expectedAssets: List<Asset>
)

open class PrepareKanjiDojoAssetsTask : DefaultTask() {

    companion object {
        const val AppDataDatabaseVersion = 11
        const val AppDataAssetFileName = "kanji-dojo-data-base-v$AppDataDatabaseVersion.sql"
        const val KanaVoice1AndroidFileName = "ja-JP-Neural2-B.opus"
        const val KanaVoice1JvmFileName = "ja-JP-Neural2-B.wav"

        private val commonAssetLocation = KanjiDojoAssetLocation(
            directory = "core/src/commonMain/resources",
            expectedAssets = listOf(
                Asset(
                    fileName = AppDataAssetFileName,
                    url = "https://github.com/syt0r/Kanji-Dojo-Data/releases/download/v11.0/kanji-dojo-data-base-v11.sql"
                )
            )
        )

        private val androidAssetLocation = KanjiDojoAssetLocation(
            directory = "core/src/androidMain/assets",
            expectedAssets = listOf(
                Asset(
                    fileName = KanaVoice1AndroidFileName,
                    url = "https://github.com/syt0r/Kanji-Dojo-Data/releases/download/voice-v1/ja-JP-Neural2-B.opus"
                )
            )
        )

        private val desktopAssetLocation = KanjiDojoAssetLocation(
            directory = "core/src/jvmMain/resources",
            expectedAssets = listOf(
                Asset(
                    fileName = KanaVoice1JvmFileName,
                    url = "https://github.com/syt0r/Kanji-Dojo-Data/releases/download/voice-v1/ja-JP-Neural2-B.wav"
                ),
                Asset(
                    fileName = "icon.png",
                    url = null
                ),
                Asset(
                    fileName = "aboutlibraries.json",
                    url = null
                )
            )
        )
    }

    enum class Platform(val assetLocation: KanjiDojoAssetLocation) {
        Android(androidAssetLocation),
        Desktop(desktopAssetLocation)
    }

    @Input
    lateinit var platform: Platform

    @TaskAction
    fun download() {
        println("Preparing Kanji Dojo Assets for $platform...")
        handleAssets(commonAssetLocation)
        handleAssets(platform.assetLocation)
    }

    private fun handleAssets(assetLocation: KanjiDojoAssetLocation) {
        val assetsDir = File(project.rootDir, assetLocation.directory)
        if (!assetsDir.exists()) assetsDir.mkdirs()

        val expectedFileNames = assetLocation.expectedAssets.map { it.fileName }.toSet()
        val unexpectedFiles = assetsDir.listFiles()!!
            .filter { !expectedFileNames.contains(it.name) }

        if (unexpectedFiles.isNotEmpty()) {
            val unexpectedFileNames = unexpectedFiles.joinToString { it.name }
            println("Found ${unexpectedFiles.size} unknown assets [$unexpectedFileNames], removing...")

            unexpectedFiles.forEach { it.delete() }
        }

        assetLocation.expectedAssets.forEach { (fileName, url) ->
            url ?: return@forEach
            val assetFile = File(assetsDir, fileName)
            if (!assetFile.exists()) {
                println("Asset $fileName not found, downloading")
                downloadFile(assetFile, url)
            } else {
                println("Skipping $fileName downloading, already exist")
            }
        }
    }

    private fun downloadFile(file: File, url: String) {
        ant.invokeMethod("get", mapOf("src" to url, "dest" to file))
    }

}