package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.byUnicodePattern
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.presentation.common.ScrollableScreenContainer
import ua.syt0r.kanji.presentation.common.clickable
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
    startSignIn: () -> Unit
) {

    ScrollableScreenContainer(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Logged out",
            modifier = Modifier.weight(1f).fillMaxWidth().wrapContentSize()
        )

        Button(
            onClick = startSignIn,
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
    byUnicodePattern("uuuu/MM/dd HH:mm")
}

@Composable
fun AccountScreenSignedIn(
    email: String,
    subscriptionInfo: SubscriptionInfo,
    issue: ApiRequestIssue?,
    refresh: () -> Unit,
    signOut: () -> Unit,
    signIn: () -> Unit
) {

    ScrollableScreenContainer(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        if (issue != null) {
            IssueListItem(
                issue = issue,
                signIn = signIn,
                refresh = refresh
            )
        }

        Column {
            SectionTitleText("E-mail")
            ListItem(
                headlineContent = { Text(email) }
            )
        }

        Column {

            SectionTitleText("Subscription")

            val headlineText: String
            val supportText: String?

            when (subscriptionInfo) {
                is SubscriptionInfo.Active -> {
                    headlineText = "Active"
                    supportText =
                        "Valid until ${subscriptionInfo.due.format(SubscriptionDateTimeFormat)}"
                }

                is SubscriptionInfo.Expired -> {
                    headlineText = "Expired"
                    supportText =
                        "Valid until ${subscriptionInfo.due.format(SubscriptionDateTimeFormat)}"
                }

                SubscriptionInfo.Inactive -> {
                    headlineText = "Inactive"
                    supportText = null
                }
            }

            ListItem(
                headlineContent = { Text(headlineText) },
                supportingContent = supportText?.let { { Text(it) } },
                trailingContent = {
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
            )

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
fun AccountScreenError(
    issue: ApiRequestIssue,
    startSignIn: () -> Unit
) {

    ScrollableScreenContainer {

        IssueListItem(
            issue = issue,
            signIn = startSignIn,
            refresh = startSignIn
        )

        Spacer(modifier = Modifier.weight(1f))

        InvertedButton(
            onClick = startSignIn,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in")
        }

    }

}

@Composable
private fun InvertedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        content()
    }
}

@Composable
private fun SectionTitleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun IssueListItem(
    issue: ApiRequestIssue,
    signIn: () -> Unit,
    refresh: () -> Unit
) {

    val title: String
    val message: String
    val trailingIcon: ImageVector?
    val action: (() -> Unit)?

    when (issue) {
        ApiRequestIssue.NoConnection -> {
            title = "No Connection"
            message = "Showing cached data"
            trailingIcon = null
            action = null
        }

        ApiRequestIssue.NotAuthenticated -> {
            title = "Session Expired"
            message = "Click to sign in again"
            trailingIcon = Icons.AutoMirrored.Filled.Login
            action = signIn
        }

        ApiRequestIssue.NoSubscription -> {
            title = "Subscription status outdated"
            message = "Click to refresh"
            trailingIcon = Icons.Default.Refresh
            action = refresh
        }

        is ApiRequestIssue.Other -> {
            title = "Error"
            message = issue.throwable.message ?: "Unknown error"
            trailingIcon = null
            action = null
        }
    }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(message) },
        leadingContent = { Icon(Icons.Default.Info, null) },
        trailingContent = trailingIcon?.let { { Icon(it, null) } },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            supportingColor = MaterialTheme.colorScheme.onErrorContainer,
            leadingIconColor = MaterialTheme.colorScheme.onSurface,
            trailingIconColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(action)
    )

}
