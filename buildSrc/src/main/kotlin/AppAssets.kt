data class Asset(
    val fileName: String,
    val url: String?
)

data class AssetLocation(
    val directory: String,
    val expectedAssets: List<Asset>
)

object AppAssets {

    const val AppDataDatabaseVersion = 13
    const val AppDataAssetFileName = "kanji-dojo-data-base-v$AppDataDatabaseVersion.sql"

    const val KanaVoice1AndroidFileName = "ja-JP-Neural2-B.opus"
    const val KanaVoice1JvmFileName = "ja-JP-Neural2-B.wav"

    val CommonAssetsLocation = AssetLocation(
        directory = "core/src/commonMain/resources",
        expectedAssets = listOf(
            Asset(
                fileName = AppDataAssetFileName,
                url = "https://github.com/syt0r/Kanji-Dojo-Data/releases/download/v12.0/kanji-dojo-data-base-v12.sql"
            )
        )
    )

    val AndroidAssetsLocation = AssetLocation(
        directory = "core/src/androidMain/assets",
        expectedAssets = listOf(
            Asset(
                fileName = KanaVoice1AndroidFileName,
                url = "https://github.com/syt0r/Kanji-Dojo-Data/releases/download/voice-v1/ja-JP-Neural2-B.opus"
            )
        )
    )

    val DesktopAssetsLocation = AssetLocation(
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
