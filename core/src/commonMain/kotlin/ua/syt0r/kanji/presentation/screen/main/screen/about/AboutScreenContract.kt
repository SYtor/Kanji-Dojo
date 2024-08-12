package ua.syt0r.kanji.presentation.screen.main.screen.about

interface AboutScreenContract {

    interface ViewModel {
        fun reportUrlClick(url: String)
    }

}

const val KanjiDojoGithubLink = "https://github.com/syt0r/Kanji-Dojo"