package ua.syt0r.kanji

import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.analytics.FirebaseAnalyticsManager
import ua.syt0r.kanji.core.review.AppReviewContract
import ua.syt0r.kanji.core.review.PlayServicesReviewManager
import ua.syt0r.kanji.core.review.ReviewEligibilityUseCase
import ua.syt0r.kanji.presentation.androidMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.GooglePlayReadingPracticeScreenContent
import ua.syt0r.kanji.presentation.screen.main.GooglePlayWritingPracticeScreenContent
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeContract
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.settings.GooglePlaySettingsScreenContent
import ua.syt0r.kanji.presentation.screen.settings.GooglePlaySettingsScreenContract
import ua.syt0r.kanji.presentation.screen.settings.GooglePlaySettingsViewModel

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

    factory<GooglePlaySettingsScreenContract.ViewModel> {
        GooglePlaySettingsViewModel(
            viewModelScope = it.component1(),
            userPreferencesRepository = get(),
            analyticsManager = get(),
            reminderScheduler = get()
        )
    }

    androidMultiplatformViewModel<GooglePlaySettingsScreenContract.ViewModel>()

}