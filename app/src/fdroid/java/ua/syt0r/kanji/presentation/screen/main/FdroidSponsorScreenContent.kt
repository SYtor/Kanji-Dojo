package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenUI

object FdroidSponsorScreenContent : SponsorScreenContract.Content {

    @Composable
    override fun invoke(state: MainNavigationState) {
        SponsorScreenUI(
            onUpClick = { state.navigateBack() }
        ) {

            val urlHandler = rememberUrlHandler()
            Button(
                onClick = { urlHandler.openInBrowser("https://buymeacoffee.com/syt0r") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDD00),
                    contentColor = Color(0xFF0D0C23)
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Coffee, null)
                Spacer(Modifier.width(12.dp))
                Text("Buy Me A Coffee")
            }

        }
    }

}