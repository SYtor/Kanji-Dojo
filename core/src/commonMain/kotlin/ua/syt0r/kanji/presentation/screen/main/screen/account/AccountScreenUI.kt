package ua.syt0r.kanji.presentation.screen.main.screen.account

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Error
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
import kotlinx.datetime.format
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.presentation.common.CommonDateTimeFormat
import ua.syt0r.kanji.presentation.common.InvertedButton
import ua.syt0r.kanji.presentation.common.ScrollableScreenContainer
import ua.syt0r.kanji.presentation.common.clickable
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.errorColors
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
                title = { Text(resolveString { account.title }) },
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
            text = resolveString { account.loggedOutMessage },
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
            Text(text = resolveString { account.signInButton })
        }

    }

}

@Composable
fun AccountScreenLoading() {
    CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
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
            SectionTitleText(resolveString { account.emailTitle })
            ListItem(
                headlineContent = { Text(email) }
            )
        }

        Column {

            SectionTitleText(resolveString { account.subscriptionTitle })

            val headlineText: String
            val supportText: String?

            when (subscriptionInfo) {
                is SubscriptionInfo.Active -> {
                    headlineText = resolveString { account.subscriptionStatusActive }
                    supportText = resolveString {
                        account.subscriptionValidUntilTemplate.format(
                            subscriptionInfo.due.format(
                                CommonDateTimeFormat
                            )
                        )
                    }
                }

                is SubscriptionInfo.Expired -> {
                    headlineText = resolveString { account.subscriptionStatusExpired }
                    supportText = resolveString {
                        account.subscriptionValidUntilTemplate.format(
                            subscriptionInfo.due.format(
                                CommonDateTimeFormat
                            )
                        )
                    }
                }

                SubscriptionInfo.Inactive -> {
                    headlineText = resolveString { account.subscriptionStatusInactive }
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
            Text(text = resolveString { account.signOutButton })
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
            Text(resolveString { account.signInButton })
        }

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
            title = resolveString { account.issueNoConnectionTitle }
            message = resolveString { account.issueNoConnectionMessage }
            trailingIcon = null
            action = null
        }

        ApiRequestIssue.NotAuthenticated -> {
            title = resolveString { account.issueSessionExpiredTitle }
            message = resolveString { account.issueSessionExpiredMessage }
            trailingIcon = Icons.AutoMirrored.Filled.Login
            action = signIn
        }

        ApiRequestIssue.NoSubscription -> {
            title = resolveString { account.issueSubscriptionOutdatedTitle }
            message = resolveString { account.issueSubscriptionOutdatedMessage }
            trailingIcon = Icons.Default.Refresh
            action = refresh
        }

        is ApiRequestIssue.Other -> {
            title = resolveString { account.issueOtherTitle }
            message = issue.throwable.message ?: resolveString { account.issueOtherMessageFallback }
            trailingIcon = null
            action = null
        }
    }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(message) },
        leadingContent = { Icon(Icons.Default.Error, null) },
        trailingContent = trailingIcon?.let { { Icon(it, null) } },
        colors = ListItemDefaults.errorColors(),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(action)
    )

}
