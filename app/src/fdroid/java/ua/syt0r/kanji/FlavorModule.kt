package ua.syt0r.kanji

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.screen.main.FdroidSponsorScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract

val flavorModule = module {

    single<SponsorScreenContract.Content> { FdroidSponsorScreenContent }

}