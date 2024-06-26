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
import ua.syt0r.kanji.presentation.screen.main.GooglePlayReadingPracticeScreenContent
import ua.syt0r.kanji.presentation.screen.main.GooglePlayWritingPracticeScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeContract
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract
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
            practiceRepository = get()
        )
    }

    factory<AppReviewContract.ReviewManager> {
        PlayServicesReviewManager(
            reviewManager = get(),
            eligibilityUseCase = get(),
            analyticsManager = get()
        )
    }

    single<WritingPracticeScreenContract.Content> { GooglePlayWritingPracticeScreenContent }

    single<ReadingPracticeContract.Content> { GooglePlayReadingPracticeScreenContent }

    single<SettingsScreenContract.Content> { GooglePlaySettingsScreenContent }

    multiplatformViewModel<GooglePlaySettingsScreenContract.ViewModel> {
        GooglePlaySettingsViewModel(
            viewModelScope = it.component1(),
            userPreferencesRepository = get(),
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
            httpClient = get()
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

}