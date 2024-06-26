package ua.syt0r.kanji.presentation.screen.sponsor

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale

interface GooglePlaySponsorScreenStrings {

    val initButton: String

    val inputEmailHint: String
    val inputMessageHint: String
    val inputMultipleQuantityNotice: String
    val inputButton: (formattedPrice: String) -> String

    val errorLabel: String
    val errorRetryButton: String

    val completeThankYouMessage: String
    val completeIconCredit: String

    companion object {

        @Composable
        fun getLocalized(): GooglePlaySponsorScreenStrings {
            return when (Locale.current.language) {
                "ja" -> JapaneseGooglePlaySponsorScreenStrings
                else -> EnglishGooglePlaySponsorScreenStrings
            }
        }

    }

}


object EnglishGooglePlaySponsorScreenStrings : GooglePlaySponsorScreenStrings {
    override val initButton: String = "Contribute"
    override val inputEmailHint: String = "(Optional) E-mail"
    override val inputMessageHint: String = "(Optional) Message"
    override val inputMultipleQuantityNotice: String =
        "* Click on + in the next screen to donate more"
    override val inputButton: (String) -> String =
        { formattedPrice -> "Support ($formattedPrice+)" }
    override val errorLabel: String = "Error"
    override val errorRetryButton: String = "Retry"
    override val completeThankYouMessage: String = "Thank you!"
    override val completeIconCredit: String = "* Icon by Freepik"
}

object JapaneseGooglePlaySponsorScreenStrings :
    GooglePlaySponsorScreenStrings by EnglishGooglePlaySponsorScreenStrings