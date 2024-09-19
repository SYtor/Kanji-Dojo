package ua.syt0r.kanji.core

import java.io.File

fun getUserDataDirectory(): File {
    val osName = System.getProperty("os.name")
    val userDir = System.getProperty("user.home")
    return when {
        osName.contains("linux", true) -> {
            File("$userDir/.local/share/kanji-dojo/")
        }

        osName.contains("windows", true) -> {
            File("$userDir/AppData/Local/Kanji Dojo")
        }

        else -> File("KanjiDojoData/")
    }
}

fun getUserPreferencesFile(): File {
    return File(getUserDataDirectory(), "user.preferences_pb")
}