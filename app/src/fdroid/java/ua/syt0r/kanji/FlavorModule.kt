package ua.syt0r.kanji

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.FdroidAccountScreenContent
import ua.syt0r.kanji.presentation.screen.main.FdroidAccountScreenContract
import ua.syt0r.kanji.presentation.screen.main.FdroidAccountScreenViewModel
import ua.syt0r.kanji.presentation.screen.main.FdroidSponsorScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.account.AccountScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract

val flavorModule = module {

    single<SponsorScreenContract.Content> { FdroidSponsorScreenContent }
    single<AccountScreenContract.Content> { FdroidAccountScreenContent }

    multiplatformViewModel<FdroidAccountScreenContract.ViewModel> {
        FdroidAccountScreenViewModel(
            coroutineScope = it.component1(),
            accountManager = get()
        )
    }

}