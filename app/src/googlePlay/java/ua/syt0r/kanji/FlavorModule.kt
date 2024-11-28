package ua.syt0r.kanji

import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.analytics.FirebaseAnalyticsManager
import ua.syt0r.kanji.core.review.AppReviewContract
import ua.syt0r.kanji.core.review.PlayServicesReviewManager
import ua.syt0r.kanji.core.review.ReviewEligibilityUseCase
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.GooglePlayLetterPracticeScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.account.AccountScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.account.GooglePlayAccountScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.account.GooglePlayAccountScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.account.GooglePlayAccountScreenViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract
import ua.syt0r.kanji.presentation.screen.settings.GooglePlaySettingsScreenContent
import ua.syt0r.kanji.presentation.screen.settings.GooglePlaySettingsScreenContract
import ua.syt0r.kanji.presentation.screen.settings.GooglePlaySettingsViewModel
import ua.syt0r.kanji.presentation.screen.sponsor.DefaultGooglePlayPurchaseManager
import ua.syt0r.kanji.presentation.screen.sponsor.GooglePlayPurchaseManager
import ua.syt0r.kanji.presentation.screen.sponsor.GooglePlaySponsorScreenContent
import ua.syt0r.kanji.presentation.screen.sponsor.GooglePlaySponsorScreenContract
import ua.syt0r.kanji.presentation.screen.sponsor.GooglePlaySponsorViewModel
import ua.syt0r.kanji.presentation.screen.sponsor.use_case.DefaultGooglePlaySendSponsorResultsUseCase
import ua.syt0r.kanji.presentation.screen.sponsor.use_case.GooglePlaySendSponsorResultsUseCase

val flavorModule = module {

    single<AnalyticsManager> { FirebaseAnalyticsManager(firebaseAnalytics = Firebase.analytics) }

    single<ReviewManager> { ReviewManagerFactory.create(androidApplication()) }

    factory<AppReviewContract.ReviewEligibilityUseCase> {
        ReviewEligibilityUseCase(
            reviewHistoryRepository = get()
        )
    }

    factory<AppReviewContract.ReviewManager> {
        PlayServicesReviewManager(
            reviewManager = get(),
            eligibilityUseCase = get(),
            analyticsManager = get()
        )
    }

    single<LetterPracticeScreenContract.Content> { GooglePlayLetterPracticeScreenContent }

    single<SettingsScreenContract.Content> { GooglePlaySettingsScreenContent }

    multiplatformViewModel<GooglePlaySettingsScreenContract.ViewModel> {
        GooglePlaySettingsViewModel(
            viewModelScope = it.component1(),
            appPreferences = get(),
            analyticsManager = get(),
            reminderScheduler = get()
        )
    }

    single<SponsorScreenContract.Content> { GooglePlaySponsorScreenContent }

    factory<GooglePlayPurchaseManager> {
        DefaultGooglePlayPurchaseManager(
            context = androidContext(),
            coroutineScope = it.component1()
        )
    }

    factory<GooglePlaySendSponsorResultsUseCase> {
        DefaultGooglePlaySendSponsorResultsUseCase(
            networkApi = get()
        )
    }

    multiplatformViewModel<GooglePlaySponsorScreenContract.ViewModel> {
        GooglePlaySponsorViewModel(
            viewModelScope = it.component1(),
            purchaseManager = get { it },
            sendSponsorResultsUseCase = get(),
            analyticsManager = get()
        )
    }

    single<AccountScreenContract.Content> { GooglePlayAccountScreenContent }

    multiplatformViewModel<GooglePlayAccountScreenContract.ViewModel> {
        GooglePlayAccountScreenViewModel(
            coroutineScope = it.component1(),
            appPreferences = get()
        )
    }

}