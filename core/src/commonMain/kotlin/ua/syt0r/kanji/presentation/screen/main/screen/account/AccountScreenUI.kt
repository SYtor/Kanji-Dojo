package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.presentation.common.ScrollableScreenContainer
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AccountScreenContainer(
    state: State<T>,
    onUpClick: () -> Unit,
    content: @Composable (T) -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = {
                    IconButton(onUpClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec(),
            modifier = Modifier.padding(paddingValues)
        ) { screenState ->
            content(screenState)
        }

    }

}

@Composable
fun AccountScreenSignedOut(
    openLoginWebPage: () -> Unit,
) {

    ScrollableScreenContainer(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Logged out",
            modifier = Modifier.weight(1f).fillMaxWidth().wrapContentSize()
        )

        Button(
            onClick = openLoginWebPage,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Sign in")
        }

    }

}

@Composable
fun AccountScreenLoading() {
    CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
}

private val SubscriptionDateTimeFormat = LocalDateTime.Format {
    year()
    chars("-")
    monthNumber()
    chars("-")
    dayOfMonth()
    chars(" ")
    hour()
    chars(":")
    minute()
}

@Composable
fun AccountScreenSignedIn(
    email: String,
    subscriptionInfo: SubscriptionInfo,
    refresh: () -> Unit,
    signOut: () -> Unit
) {

    ScrollableScreenContainer(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Column {
            SectionTitleText("E-mail")
            SectionDataText(email)
        }

        Column {

            SectionTitleText("Subscription")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {


                val message = when (subscriptionInfo) {
                    is SubscriptionInfo.Active -> "Active, valid until ${
                        subscriptionInfo.due.format(SubscriptionDateTimeFormat)
                    }"

                    is SubscriptionInfo.Expired -> "Expired, valid until ${
                        subscriptionInfo.due.format(SubscriptionDateTimeFormat)
                    }"

                    SubscriptionInfo.Inactive -> "Inactive"
                }

                SectionDataText(
                    text = message,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                        .height(IntrinsicSize.Max)
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(onClick = refresh)
                        .wrapContentSize()
                ) {
                    Icon(Icons.Default.Refresh, null)
                }

            }

        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = signOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Sign out")
        }

    }

}

@Composable
private fun SectionTitleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun SectionDataText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}